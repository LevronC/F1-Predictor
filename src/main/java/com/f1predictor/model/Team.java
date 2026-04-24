package com.f1predictor.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Aggregated stats for a team/constructor.
 */
public class Team {
    private String name;
    private int totalRaces;
    private int wins;
    private int podiums;
    private double totalPoints;
    private double avgPoints;
    private Set<String> drivers = new HashSet<>();
    private Map<Integer, SeasonStats> seasonStats = new HashMap<>();
    private int championships;

    private Team(Builder builder) {
        this.name = builder.name;
        this.totalRaces = builder.totalRaces;
        this.wins = builder.wins;
        this.podiums = builder.podiums;
        this.totalPoints = builder.totalPoints;
        this.avgPoints = builder.avgPoints;
        this.drivers = builder.drivers;
        this.seasonStats = builder.seasonStats;
        this.championships = builder.championships;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public String getName() { return name; }
    public int getTotalRaces() { return totalRaces; }
    public int getWins() { return wins; }
    public int getPodiums() { return podiums; }
    public double getTotalPoints() { return totalPoints; }
    public double getAvgPoints() { return avgPoints; }
    public Set<String> getDrivers() { return drivers; }
    public Map<Integer, SeasonStats> getSeasonStats() { return seasonStats; }
    public int getChampionships() { return championships; }

    public static class Builder {
        private String name;
        private int totalRaces;
        private int wins;
        private int podiums;
        private double totalPoints;
        private double avgPoints;
        private Set<String> drivers = new HashSet<>();
        private Map<Integer, SeasonStats> seasonStats = new HashMap<>();
        private int championships;

        public Builder name(String name) { this.name = name; return this; }
        public Builder totalRaces(int totalRaces) { this.totalRaces = totalRaces; return this; }
        public Builder wins(int wins) { this.wins = wins; return this; }
        public Builder podiums(int podiums) { this.podiums = podiums; return this; }
        public Builder totalPoints(double totalPoints) { this.totalPoints = totalPoints; return this; }
        public Builder avgPoints(double avgPoints) { this.avgPoints = avgPoints; return this; }
        public Builder drivers(Set<String> drivers) { 
            this.drivers = drivers != null ? drivers : new HashSet<>();
            return this;
        }
        public Builder seasonStats(Map<Integer, SeasonStats> seasonStats) {
            this.seasonStats = seasonStats != null ? seasonStats : new HashMap<>();
            return this;
        }
        public Builder championships(int championships) { this.championships = championships; return this; }

        public Team build() {
            return new Team(this);
        }
    }
}
