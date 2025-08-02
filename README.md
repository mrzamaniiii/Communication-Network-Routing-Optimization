# 🔁 Communication Network Routing Optimization

This repository contains Java implementations of routing and resource allocation models for optical and IP-layer networks.  

## 📂 Files Included

| File                            | Description                                                                 |
|---------------------------------|-----------------------------------------------------------------------------|
| `UnSplittableFlowFormulation.java` | Implements unsplittable flow routing model                                 |
| `RouteFormulationWP.java`         | Implements wavelength path formulation with continuity constraints          |
| `10869960_11041528_CND_TASK_1.pdf` | Report and explanation of code/results for                                 |
| `10869960_11041528_CND_TASK_2.pdf` | Report and explanation of code/results for                                 |

---

## 🧠 Summaries

### 📌 Splittable vs Unsplittable Flow Routing

- **Objective**: Compare performance of splittable and unsplittable traffic routing.
- **Metrics Evaluated**: Link capacity usage and routing flexibility.
- **Approach**: Run multiple simulations under varying capacity values \( C = \{10,15,\dots,50\} \).
- **Key Takeaway**: Splittable flow leads to more efficient capacity utilization and avoids bottlenecks.

### 📌 Wavelength Path Assignment

- **Objective**: Minimize total wavelength usage across the network, respecting continuity.
- **Key Elements**:
  - Binary decision variable `r_cn_lambda` for route-wavelength allocation
  - Wavelength continuity constraint across all paths
  - Optimized cost via `op.getOptimalCost()`
- **Scenarios**:
  - Wavelength Path (WP)
  - Virtual Wavelength Path (vWP)
- **Key Takeaway**: Minimizing spectral usage is achievable with proper path and wavelength assignment, depending on capacity C and number of allowed routes K.

---

## 🚀 How to Run

1. Open the `.java` files in your preferred IDE (e.g., IntelliJ, Eclipse).
2. Ensure that any required MILP/LP solver libraries (e.g., CPLEX, Gurobi) are installed if used.
3. Modify input parameters (e.g., capacity, route sets) in `main()` as needed.
4. Compile and run.
