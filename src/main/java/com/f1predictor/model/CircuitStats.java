package com.f1predictor.model;

/**
 * Per-circuit stats for a driver.
 */
public class CircuitStats {
    private String circuit;
    private String driverName;
    private int races;
    private int wins;
    private int podiums;
    private double avgPosition;
    private double avgPoints;

    private CircuitStats(Builder builder) {
        this.circuit = builder.circuit;
        this.driverName = builder.driverName;
        this.races = builder.races;
        this.wins = builder.wins;
        this.podiums = builder.podiums;
        this.avgPosition = builder.avgPosition;
        this.avgPoints = builder.avgPoints;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public String getCircuit() { return circuit; }
    public String getDriverName() { return driverName; }
    public int getRaces() { return races; }
    public int getWins() { return wins; }
    public int getPodiums() { return podiums; }
    public double getAvgPosition() { return avgPosition; }
    public double getAvgPoints() { return avgPoints; }

    public static class Builder {
        private String circuit;
        private String driverName;
        private int races;
        private int wins;
        private int podiums;
        private double avgPosition;
        private double avgPoints;

        public Builder circuit(String circuit) { this.circuit = circuit; return this; }
        public Builder driverName(String driverName) { this.driverName = driverName; return this; }
        public Builder races(int races) { this.races = races; return this; }
        public Builder wins(int wins) { this.wins = wins; return this; }
        public Builder podiums(int podiums) { this.podiums = podiums; return this; }
        public Builder avgPosition(double avgPosition) { this.avgPosition = avgPosition; return this; }
        public Builder avgPoints(double avgPoints) { this.avgPoints = avgPoints; return this; }

        public CircuitStats build() {
            return new CircuitStats(this);
        }
    }
}
