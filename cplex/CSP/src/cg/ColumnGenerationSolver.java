package cg;

import ilog.concert.IloException;
import master.RestrictedMasterProblem;
import model.Flight;
import model.Pairing;
import pricing.PricingProblem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColumnGenerationSolver {
    private List<Flight> flights;
    private PricingProblem pricingProblem;
    private RestrictedMasterProblem masterProblem;
    private int maxColsPerIter;

    // Statistics
    private int iterations = 0;
    private long startTime;
    private long endTime;

    public ColumnGenerationSolver(List<Flight> flights, PricingProblem pricingProblem, int maxColsPerIter) throws IloException {
        this.flights = flights;
        this.pricingProblem = pricingProblem;
        this.masterProblem = new RestrictedMasterProblem(flights);
        this.maxColsPerIter = maxColsPerIter;
    }

    public void solve() throws IloException {
        startTime = System.currentTimeMillis();

        System.out.println("Step 4: Column Generation Execution");
        System.out.println("-----------------------------------");

        // 1. Init RMP
        masterProblem.generateInitialSolution();

        boolean improvement = true;
        while (improvement) {
            iterations++;

            // 2. Solve RMP
            masterProblem.solve();
            double objVal = masterProblem.getObjectiveValue();

            // 3. Get Duals
            double[] dualArray = masterProblem.getDuals();
            Map<String, Double> dualMap = new HashMap<>();
            for (int i = 0; i < flights.size(); i++) {
                dualMap.put(flights.get(i).getFlightId(), dualArray[i]);
            }

            // 4. Solve PP
            List<Pairing> newColumns = pricingProblem.solve(dualMap);

            // 5. Add columns (limit to maxColsPerIter, selecting best reduced costs)
            int addedCount = 0;
            double bestRedCost = 0;

            // Calculate reduced costs and collect
            List<Pairing> candidates = new ArrayList<>();
            for (Pairing p : newColumns) {
                double rc = p.getCost();
                for (Flight f : p.getFlights()) {
                    rc -= dualMap.getOrDefault(f.getFlightId(), 0.0);
                }
                if (rc < 0) { // only negative
                    candidates.add(p);
                }
                if (rc < bestRedCost) bestRedCost = rc;
            }

            // Sort by reduced cost ascending (most negative first)
            candidates.sort(Comparator.comparingDouble(p -> {
                double rc = p.getCost();
                for (Flight f : p.getFlights()) {
                    rc -= dualMap.getOrDefault(f.getFlightId(), 0.0);
                }
                return rc;
            }));

            // Add top maxColsPerIter
            for (int i = 0; i < Math.min(candidates.size(), maxColsPerIter); i++) {
                Pairing p = candidates.get(i);
                masterProblem.addColumn(p);
                addedCount++;
            }

            System.out.printf("Iter %d: Obj = %.2f | Cols Added = %d | Best RedCost = %.2f%n",
                    iterations, objVal, addedCount, bestRedCost);

            if (addedCount == 0) {
                improvement = false;
            }
        }

        endTime = System.currentTimeMillis();
    }

    public void printSolution() throws IloException {
        System.out.println("\nSTEP 5: FINAL OUTPUT");
        System.out.println("--------------------");
        System.out.println("Total Cost: " + masterProblem.getObjectiveValue());
        System.out.println("Execution Time: " + (endTime - startTime) + " ms");
        System.out.println("Iterations: " + iterations);
        System.out.println("\nSelected Pairings:");

        List<Pairing> solution = masterProblem.getSolution();
        for (Pairing p : solution) {
            System.out.println(p.toString());
        }

        // Cleanup
        masterProblem.close();
    }
}
