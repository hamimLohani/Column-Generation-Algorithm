package model;

import java.time.LocalTime;
import util.TimeUtils;

public class Flight {
    private String flightId;
    private String from;
    private String to;
    private LocalTime depTime;
    private LocalTime arrTime;
    private double durationHours;
    private String aircraft;
    private String base;
    private double flightCost;
    private boolean isNight;

    public Flight(String flightId, String from, String to, String depTimeStr, String arrTimeStr, 
                  double durationHours, String aircraft, String base, double flightCost, int night) {
        this.flightId = flightId;
        this.from = from;
        this.to = to;
        this.depTime = TimeUtils.parseTime(depTimeStr);
        this.arrTime = TimeUtils.parseTime(arrTimeStr);
        this.durationHours = durationHours;
        this.aircraft = aircraft;
        this.base = base;
        this.flightCost = flightCost;
        this.isNight = (night == 1);
    }

    public String getFlightId() {
        return flightId;
    }
    public String getFrom() {
        return from;
    }
    public String getTo() {
        return to;
    }
    public LocalTime getDepTime() {
        return depTime;
    }
    public LocalTime getArrTime() {
        return arrTime;
    }
    public double getDurationHours() {
        return durationHours;
    }
    public String getAircraft() {
        return aircraft;
    }
    public String getBase() {
        return base;
    }
    public double getFlightCost() {
        return flightCost;
    }
    public boolean isNight() {
        return isNight;
    }

    @Override
    public String toString() {
        return String.format("%s [%s->%s] %s-%s", flightId, from, to, depTime, arrTime);
    }
}
