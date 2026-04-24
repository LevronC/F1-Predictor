package com.f1predictor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregated stats for a driver.
 */
public class Driver {
    private String name;
    private int totalRaces;
    private int wins;
    private int podiums;
    private int pointsFinishes;
    private double totalPoints;
    private double avgPoints;
    private double avgPosition;
    private double consistency; // 1 / (1 + stddev)
    private int dnfCount;
    private List<Integer> recentPositions = new ArrayList<>();
    private Map<Integer, SeasonStats> seasonStats = new HashMap<>();

    private Driver(Builder builder) {
        this.name = builder.name;
        this.totalRaces = builder.totalRaces;
        this.wins = builder.wins;
        this.podiums = builder.podiums;
        this.pointsFinishes = builder.pointsFinishes;
        this.totalPoints = builder.totalPoints;
        this.avgPoints = builder.avgPoints;
        this.avgPosition = builder.avgPosition;
        this.consistency = builder.consistency;
        this.dnfCount = builder.dnfCount;
        this.recentPositions = builder.recentPositions;
        this.seasonStats = builder.seasonStats;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public String getName() { return name; }
    public int getTotalRaces() { return totalRaces; }
    public int getWins() { return wins; }
    public int getPodiums() { return podiums; }
    public int getPointsFinishes() { return pointsFinishes; }
    public double getTotalPoints() { return totalPoints; }
    public double getAvgPoints() { return avgPoints; }
    public double getAvgPosition() { return avgPosition; }
    public double getConsistency() { return consistency; }
    public int getDnfCount() { return dnfCount; }
    public List<Integer> getRecentPositions() { return recentPositions; }
    public Map<Integer, SeasonStats> getSeasonStats() { return seasonStats; }

    public static class Builder {
        private String name;
        private int totalRaces;
        private int wins;
        private int podiums;
        private int pointsFinishes;
        private double totalPoints;
        private double avgPoints;
        private double avgPosition;
        private double consistency;
        private int dnfCount;
        private List<Integer> recentPositions = new ArrayList<>();
        private Map<Integer, SeasonStats> seasonStats = new HashMap<>();

        public Builder name(String name) { this.name = name; return this; }
        public Builder totalRaces(int totalRaces) { this.totalRaces = totalRaces; return this; }
        public Builder wins(int wins) { this.wins = wins; return this; }
        public Builder podiums(int podiums) { this.podiums = podiums; return this; }
        public Builder pointsFinishes(int pointsFinishes) { this.pointsFinishes = pointsFinishes; return this; }
        public Builder totalPoints(double totalPoints) { this.totalPoints = totalPoints; return this; }
        public Builder avgPoints(double avgPoints) { this.avgPoints = avgPoints; return this; }
        public Builder avgPosition(double avgPosition) { this.avgPosition = avgPosition; return this; }
        public Builder consistency(double consistency) { this.consistency = consistency; return this; }
        public Builder dnfCount(int dnfCount) { this.dnfCount = dnfCount; return this; }
        public Builder recentPositions(List<Integer> recentPositions) { 
            this.recentPositions = recentPositions != null ? recentPositions : new ArrayList<>();
            return this; 
        }
        public Builder seasonStats(Map<Integer, SeasonStats> seasonStats) {
            this.seasonStats = seasonStats != null ? seasonStats : new HashMap<>();
            return this;
        }

        public Driver build() {
            return new Driver(this);
        }
    }
}
