package model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Pricing {
    private List<Flight> flights;
    private double cost;

    public Pricing() {
        this.flights = new ArrayList<>();
        this.cost = 0.0;
    }

    public Pricing(List<Flight> flights, double cost) {
        this.flights = flights;
        this.cost = cost;
    }

    public void addFlight(Flight flight) {
        this.flights.add(flight);
    }

    public List<Flight> getFlights() {
        return flights;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public Flight getLastFlight() {
        if (flights.isEmpty()) {
            return null;
        }
        return flights.get(flights.size() - 1);
    }

    @Override
    public String toString() {
        return flights.stream().map(Flight::getFlightId).collect(Collectors.joining("-")) + " ($" + cost + ")";
    }
}
