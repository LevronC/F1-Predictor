package com.f1predictor.model;

import java.time.LocalDate;

/**
 * Represents a single driver's result in one race.
 */
public class RaceResult {
    private int season;
    private int round;
    private String circuit;
    private LocalDate date;
    private String driverName;
    private String constructorName;
    private int gridPosition;
    private int finishPosition;
    private double points;
    private int lapsCompleted;
    private String status;
    private String fastestLap;

    public RaceResult() {}

    public RaceResult(int season, int round, String circuit, LocalDate date, String driverName, 
                      String constructorName, int gridPosition, int finishPosition, double points, 
                      int lapsCompleted, String status, String fastestLap) {
        this.season = season;
        this.round = round;
        this.circuit = circuit;
        this.date = date;
        this.driverName = driverName;
        this.constructorName = constructorName;
        this.gridPosition = gridPosition;
        this.finishPosition = finishPosition;
        this.points = points;
        this.lapsCompleted = lapsCompleted;
        this.status = status;
        this.fastestLap = fastestLap;
    }

    public static RaceResultBuilder builder() {
        return new RaceResultBuilder();
    }

    // Getters
    public int getSeason() { return season; }
    public int getRound() { return round; }
    public String getCircuit() { return circuit; }
    public LocalDate getDate() { return date; }
    public String getDriverName() { return driverName; }
    public String getConstructorName() { return constructorName; }
    public int getGridPosition() { return gridPosition; }
    public int getFinishPosition() { return finishPosition; }
    public double getPoints() { return points; }
    public int getLapsCompleted() { return lapsCompleted; }
    public String getStatus() { return status; }
    public String getFastestLap() { return fastestLap; }

    // Setters (for CSV loader)
    public void setSeason(int season) { this.season = season; }
    public void setRound(int round) { this.round = round; }
    public void setCircuit(String circuit) { this.circuit = circuit; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public void setConstructorName(String constructorName) { this.constructorName = constructorName; }
    public void setGridPosition(int gridPosition) { this.gridPosition = gridPosition; }
    public void setFinishPosition(int finishPosition) { this.finishPosition = finishPosition; }
    public void setPoints(double points) { this.points = points; }
    public void setLapsCompleted(int lapsCompleted) { this.lapsCompleted = lapsCompleted; }
    public void setStatus(String status) { this.status = status; }
    public void setFastestLap(String fastestLap) { this.fastestLap = fastestLap; }

    public static class RaceResultBuilder {
        private int season;
        private int round;
        private String circuit;
        private LocalDate date;
        private String driverName;
        private String constructorName;
        private int gridPosition;
        private int finishPosition;
        private double points;
        private int lapsCompleted;
        private String status;
        private String fastestLap;

        public RaceResultBuilder season(int s) { this.season = s; return this; }
        public RaceResultBuilder round(int r) { this.round = r; return this; }
        public RaceResultBuilder circuit(String c) { this.circuit = c; return this; }
        public RaceResultBuilder date(LocalDate d) { this.date = d; return this; }
        public RaceResultBuilder driverName(String n) { this.driverName = n; return this; }
        public RaceResultBuilder constructorName(String c) { this.constructorName = c; return this; }
        public RaceResultBuilder gridPosition(int g) { this.gridPosition = g; return this; }
        public RaceResultBuilder finishPosition(int f) { this.finishPosition = f; return this; }
        public RaceResultBuilder points(double p) { this.points = p; return this; }
        public RaceResultBuilder lapsCompleted(int l) { this.lapsCompleted = l; return this; }
        public RaceResultBuilder status(String s) { this.status = s; return this; }
        public RaceResultBuilder fastestLap(String f) { this.fastestLap = f; return this; }

        public RaceResult build() {
            return new RaceResult(season, round, circuit, date, driverName, constructorName, 
                                  gridPosition, finishPosition, points, lapsCompleted, status, fastestLap);
        }
    }
}
