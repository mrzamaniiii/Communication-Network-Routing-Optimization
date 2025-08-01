import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.jom.OptimizationProblem;
import com.net2plan.interfaces.networkDesign.Demand;
import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.Route;
import com.net2plan.libraries.GraphUtils;
import com.net2plan.utils.Constants.RoutingType;
import com.net2plan.utils.DoubleUtils;
import com.net2plan.utils.Triple;

public class RouteFormulationVWP implements IAlgorithm
{
    /** The method called by Net2Plan to run the algorithm (when the user presses the "Execute" button)
     * @param netPlan The input network design. The developed algorithm should modify it: it is the way the new design is returned
     * @param algorithmParameters Pair name-value for the current value of the input parameters
     * @param net2planParameters Pair name-value for some general parameters of Net2Plan
     * @return
     */
    @Override
    public String executeAlgorithm(NetPlan netPlan, Map<String, String> algorithmParameters, Map<String, String> net2planParameters)
    {
        
        final int k = Integer.parseInt(algorithmParameters.get("k")); 
        final int W = Integer.parseInt(algorithmParameters.get("linkCapacity")); 
        final boolean isNonBifurcated = Boolean.parseBoolean(algorithmParameters.get("isNonBifurcated"));

        
        netPlan.setRoutingTypeAllDemands(RoutingType.SOURCE_ROUTING, netPlan.getNetworkLayerDefault());

        
        netPlan.removeAllRoutes();

        
        for (Demand d : netPlan.getDemands())
        {
            List<List<Link>> kShortestPaths = GraphUtils.getKLooplessShortestPaths(netPlan.getNodes(), netPlan.getLinks(), 
                                                                                   d.getIngressNode(), d.getEgressNode(), 
                                                                                   null, k, -1, -1, -1, -1, -1, -1);
            if (kShortestPaths.isEmpty()) throw new Net2PlanException("There are no admissible routes for a demand");
            for (List<Link> sp : kShortestPaths)
                netPlan.addRoute(d, 0, 0, sp, null);
        }

        
        OptimizationProblem op = new OptimizationProblem();

        
        op.addDecisionVariable("r_cn_lambda", isNonBifurcated, new int[]{1, netPlan.getNumberOfRoutes(), W}, 0, 1); 
        
        op.setInputParameter("l_p", netPlan.getVectorRouteNumberOfLinks(), "row");
        op.setObjectiveFunction("minimize", "sum (l_p .* sum(r_cn_lambda, 3))");

       
        for (Demand d : netPlan.getDemands())
        {
            op.setInputParameter("R_lk", NetPlan.getIndexes(d.getRoutes()), "row");
            op.setInputParameter("V_c", d.getOfferedTraffic());
            op.addConstraint("sum(sum(r_cn_lambda(R_lk, :), 3)) == V_c"); 
        }

        
        for (Link e : netPlan.getLinks())
        {
            op.setInputParameter("n_e", NetPlan.getIndexes(e.getTraversingRoutes()), "row");
            op.setInputParameter("linkcapacity", e.getCapacity());
            op.addConstraint("sum(r_cn_lambda(n_e, :)) <= linkcapacity"); 
        
        op.solve("glpk");

        
        if (!op.solutionIsOptimal())
            throw new Net2PlanException("An optimal solution was not found");

        
        final double[][][] r_cn_lambda = op.getPrimalSolution("r_cn_lambda").to3DArray();

       
        for (Route r : netPlan.getRoutes())
        {
            double totalTraffic = DoubleStream.of(r_cn_lambda[r.getIndex()]).sum();
            r.setCarriedTraffic(totalTraffic, totalTraffic);
        }
        netPlan.removeAllRoutesUnused(0.001);

        
        return "Ok! Total number of wavelengths used in the links: " + op.getOptimalCost();
    }

    
    @Override
    public String getDescription()
    {
        return "Routing optimization with wavelength continuity constraints.";
    }

    /** Returns the list of input parameters of the algorithm. For each parameter, return a Triple with its name, default value, and description */
    @Override
    public List<Triple<String, String, String>> getParameters()
    {
        final List<Triple<String, String, String>> param = new LinkedList<Triple<String, String, String>>();
        param.add(Triple.of("k", "10", "Maximum number of loopless admissible paths per demand"));
        param.add(Triple.of("isNonBifurcated", "false", "True if the traffic is constrained to be non-bifurcated"));
        param.add(Triple.of("linkCapacity", "20", "Number of wavelengths available per link"));
        return param;
    }
}
