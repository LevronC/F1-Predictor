package com.f1predictor.model;

/**
 * Yearly breakdown of stats for a driver or team.
 */
public class SeasonStats {
    private int season;
    private int races;
    private int wins;
    private int podiums;
    private double points;
    private int championshipPosition;

    private SeasonStats(Builder builder) {
        this.season = builder.season;
        this.races = builder.races;
        this.wins = builder.wins;
        this.podiums = builder.podiums;
        this.points = builder.points;
        this.championshipPosition = builder.championshipPosition;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public int getSeason() { return season; }
    public int getRaces() { return races; }
    public int getWins() { return wins; }
    public int getPodiums() { return podiums; }
    public double getPoints() { return points; }
    public int getChampionshipPosition() { return championshipPosition; }

    public static class Builder {
        private int season;
        private int races;
        private int wins;
        private int podiums;
        private double points;
        private int championshipPosition;

        public Builder season(int season) { this.season = season; return this; }
        public Builder races(int races) { this.races = races; return this; }
        public Builder wins(int wins) { this.wins = wins; return this; }
        public Builder podiums(int podiums) { this.podiums = podiums; return this; }
        public Builder points(double points) { this.points = points; return this; }
        public Builder championshipPosition(int championshipPosition) { this.championshipPosition = championshipPosition; return this; }

        public SeasonStats build() {
            return new SeasonStats(this);
        }
    }
}
