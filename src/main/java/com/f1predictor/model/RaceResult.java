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
    private int finishPosition; // 0 = DNF/DSQ/DNS
    private double points;
    private int lapsCompleted;
    private String status;
    private String fastestLap;

    // Computed fields
    private int positionsGained;
    private boolean finished;
    private boolean onPodium;
    private boolean hasFastestLap;

    private RaceResult(Builder builder) {
        this.season = builder.season;
        this.round = builder.round;
        this.circuit = builder.circuit;
        this.date = builder.date;
        this.driverName = builder.driverName;
        this.constructorName = builder.constructorName;
        this.gridPosition = builder.gridPosition;
        this.finishPosition = builder.finishPosition;
        this.points = builder.points;
        this.lapsCompleted = builder.lapsCompleted;
        this.status = builder.status;
        this.fastestLap = builder.fastestLap;

        // Calculate computed fields
        this.finished = "Finished".equalsIgnoreCase(status) || (status != null && status.startsWith("+"));
        this.finishPosition = builder.finishPosition;
        this.positionsGained = (finishPosition > 0) ? gridPosition - finishPosition : -10; // Penalty for DNF
        this.onPodium = finishPosition >= 1 && finishPosition <= 3;
        this.hasFastestLap = fastestLap != null && !fastestLap.isEmpty();
    }

    public static Builder builder() {
        return new Builder();
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
    public int getPositionsGained() { return positionsGained; }
    public boolean isFinished() { return finished; }
    public boolean isOnPodium() { return onPodium; }
    public boolean isHasFastestLap() { return hasFastestLap; }

    public static class Builder {
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

        public Builder season(int season) { this.season = season; return this; }
        public Builder round(int round) { this.round = round; return this; }
        public Builder circuit(String circuit) { this.circuit = circuit; return this; }
        public Builder date(LocalDate date) { this.date = date; return this; }
        public Builder driverName(String driverName) { this.driverName = driverName; return this; }
        public Builder constructorName(String constructorName) { this.constructorName = constructorName; return this; }
        public Builder gridPosition(int gridPosition) { this.gridPosition = gridPosition; return this; }
        public Builder finishPosition(int finishPosition) { this.finishPosition = finishPosition; return this; }
        public Builder points(double points) { this.points = points; return this; }
        public Builder lapsCompleted(int lapsCompleted) { this.lapsCompleted = lapsCompleted; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder fastestLap(String fastestLap) { this.fastestLap = fastestLap; return this; }

        public RaceResult build() {
            return new RaceResult(this);
        }
    }

    @Override
    public String toString() {
        return String.format("RaceResult{season=%d, round=%d, driver='%s', finish=%d}", 
            season, round, driverName, finishPosition);
    }
}
