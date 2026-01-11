package pricing;

import model.Flight;
import model.Pairing;
import util.TimeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.time.LocalTime;
import java.util.Map;

public class PricingProblem {
    private List<Flight> allFlights;
    private String base;

    // constraints
    private double maxDutyHours;
    private double maxFlyingHours;
    private long minTurnaroundMin;
    private boolean allowOvernight;

    // cost parameters
    private double fixedCost;
    private double hourlyCost;
    private double nightPenalty;
    private double overtimePenaltyPerHour;

    public PricingProblem(List<Flight> allFlights, String base, double maxDutyHours, double maxFlyingHours,
                          long minTurnaroundMin, boolean allowOvernight, double fixedCost, double hourlyCost, double nightPenalty, double overtimePenaltyPerHour) {
        this.allFlights = new ArrayList<>(allFlights);
        // sort flights by departure time
        this.allFlights.sort(Comparator.comparing(Flight::getDepTime));

        this.base = base;
        this.maxDutyHours = maxDutyHours;
        this.maxFlyingHours = maxFlyingHours;
        this.minTurnaroundMin = minTurnaroundMin;
        this.allowOvernight = allowOvernight;

        this.fixedCost = fixedCost;
        this.hourlyCost = hourlyCost;
        this.nightPenalty = nightPenalty;
        this.overtimePenaltyPerHour = overtimePenaltyPerHour;
    }

    /*
     * solves the RCSP(Resource Constrained Shortest Path) to find pairings with negative reduced cost.
     * 
     * dual values from RMP(Restricted Master Problem), indexed corresponding to allFlights list (or map)
     *
     * list of generated pairings
     */
    public List<Pairing> solve(Map<String, Double> dualMap) {
        List<Pairing> newColumns = new ArrayList<>();

        // simple DFS approach to find valid pairings
        // start from any flight departing from BASE
        for (Flight f : allFlights) {
            if (f.getFrom().equals(base)) {
                List<Flight> path = new ArrayList<>();
                path.add(f);
                dfs(f, path, f.getDurationHours(), dualMap, newColumns);
            }
        }

        return newColumns;
    }

    // we need duals by Flight ID probably, or index. Map<flight_ID, duals> dualMap
    // overloading to match what I wrote above (which might mismatch RMP if RMP uses index)
    // RMP uses List<Flight>, so I can map index to ID. Let's assume RMP passes a Map.

    private void dfs(Flight current, List<Flight> currentPath, double currentFlyingTime,
            Map<String, Double> duals, List<Pairing> solutions) {

        // check if we can close the pairing to Base
        if (current.getTo().equals(base)) {
            // crheck full duty validity & Cost
            double dutyTime = calculateDutyTime(currentPath);
            if (dutyTime <= maxDutyHours) {
                Pairing p = createPairing(currentPath);
                double redCost = calculateReducedCost(p, duals);
                if (redCost < -0.0001) { // negative reduced cost
                    solutions.add(p);
                }
            }
        }

        // try to extend
        for (Flight next : allFlights) {
            if (isValidConnection(current, next)) {
                // check flying time
                if (currentFlyingTime + next.getDurationHours() <= maxFlyingHours) {
                    // check duty time
                    // assuming single duty day for simplicity unless overnight allowed.

                    // if overnight not allowed, next flight must be same day (dep > arr)
                    // TimeUtils handles times. If next.dep < current.arr, it implies next day (or impossible same day).

                    currentPath.add(next);
                    dfs(next, currentPath, currentFlyingTime + next.getDurationHours(), duals, solutions);
                    currentPath.remove(currentPath.size() - 1);
                }
            }
        }
    }

    private boolean isValidConnection(Flight f1, Flight f2) {
        // location connection
        if (!f1.getTo().equals(f2.getFrom()))
            return false;

        // time connection
        long turn = TimeUtils.minutesBetween(f1.getArrTime(), f2.getDepTime());
        if (turn < minTurnaroundMin)
            return false;

        // If times loop around (e.g. 23:00 -> 01:00), turn might be calculated as
        // negative or large?
        // simple TimeUtils.minutesBetween implies same day if we just use LocalTime.
        // If f2.dep < f1.arr, it's next day.
        if (f2.getDepTime().isBefore(f1.getArrTime())) {
            if (!allowOvernight)
                return false;
        }
        return true;
    }

    private double calculateDutyTime(List<Flight> path) {
        if (path.isEmpty())
            return 0;
        LocalTime start = path.get(0).getDepTime();
        LocalTime end = path.get(path.size() - 1).getArrTime();

        long mins = TimeUtils.minutesBetween(start, end);
        // Handle overnight (if end < start, add 24h)
        if (end.isBefore(start)) {
            mins += 24 * 60;
        }
        return mins / 60.0;
    }

    private Pairing createPairing(List<Flight> path) {
        double cost = 0;
        double flyingTime = 0;
        boolean hasNight = false;

        for (Flight f : path) {
            cost += f.getFlightCost();
            flyingTime += f.getDurationHours();
            if (f.isNight())
                hasNight = true;
        }

        cost += fixedCost; // Fixed duty cost
        cost += (flyingTime * hourlyCost);
        if (hasNight)
            cost += nightPenalty;

        // Overtime
        double duty = calculateDutyTime(path);
        // Assuming overtime threshold is standard (e.g. > 8 hours or something? prompt
        // says "Overtime penalty per hour")
        // But prompt input asks "Overtime penalty per hour" without specifying
        // threshold?
        // Typically max duty is separate from overtime threshold. Let's assume overtime
        // threshold = maxFlyingTime?
        // Or maybe standard 8h? Let's use a standard 8h or just ignore if not
        // specified.
        // Prompt just says "Overtime penalty (if duty exceeds threshold)". Let's assume
        // threshold = 8h.
        if (duty > 8.0) {
            cost += (duty - 8.0) * overtimePenaltyPerHour;
        }

        return new Pairing(path, cost);
    }

    private double calculateReducedCost(Pairing p, java.util.Map<String, Double> duals) {
        double dualSum = 0;
        for (Flight f : p.getFlights()) {
            dualSum += duals.getOrDefault(f.getFlightId(), 0.0);
        }
        return p.getCost() - dualSum;
    }

    // Helper to facilitate Mapping
}
