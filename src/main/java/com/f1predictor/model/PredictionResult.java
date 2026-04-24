package com.f1predictor.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Output of the prediction algorithm.
 */
public class PredictionResult {
    private String driverName;
    private String teamName;
    private double totalScore;
    private int predictedPosition;
    private String explanation;
    private String confidenceLevel;

    // Score components (v2)
    private double recentFormScore;
    private double avgPointsScore;
    private double consistencyScore;
    private double teamStrengthScore;
    private double qualifyingFormScore;
    private double headToHeadScore;

    private Map<String, Double> scoreBreakdown = new HashMap<>();

    private PredictionResult(Builder builder) {
        this.driverName = builder.driverName;
        this.teamName = builder.teamName;
        this.totalScore = builder.totalScore;
        this.predictedPosition = builder.predictedPosition;
        this.explanation = builder.explanation;
        this.confidenceLevel = builder.confidenceLevel;
        this.recentFormScore = builder.recentFormScore;
        this.avgPointsScore = builder.avgPointsScore;
        this.consistencyScore = builder.consistencyScore;
        this.teamStrengthScore = builder.teamStrengthScore;
        this.qualifyingFormScore = builder.qualifyingFormScore;
        this.headToHeadScore = builder.headToHeadScore;

        scoreBreakdown.put("Recent Form (35%)", recentFormScore);
        scoreBreakdown.put("Avg Points (25%)", avgPointsScore);
        scoreBreakdown.put("Consistency (15%)", consistencyScore);
        scoreBreakdown.put("Team Strength (10%)", teamStrengthScore);
        scoreBreakdown.put("Qualifying Form (10%)", qualifyingFormScore);
        scoreBreakdown.put("Head to Head (5%)", headToHeadScore);
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public String getDriverName() { return driverName; }
    public String getTeamName() { return teamName; }
    public double getTotalScore() { return totalScore; }
    public int getPredictedPosition() { return predictedPosition; }
    public String getExplanation() { return explanation; }
    public String getConfidenceLevel() { return confidenceLevel; }
    public Map<String, Double> getScoreBreakdown() { return scoreBreakdown; }

    public static class Builder {
        private String driverName;
        private String teamName;
        private double totalScore;
        private int predictedPosition;
        private String explanation;
        private String confidenceLevel;
        private double recentFormScore;
        private double avgPointsScore;
        private double consistencyScore;
        private double teamStrengthScore;
        private double qualifyingFormScore;
        private double headToHeadScore;

        public Builder driverName(String driverName) { this.driverName = driverName; return this; }
        public Builder teamName(String teamName) { this.teamName = teamName; return this; }
        public Builder totalScore(double totalScore) { this.totalScore = totalScore; return this; }
        public Builder predictedPosition(int predictedPosition) { this.predictedPosition = predictedPosition; return this; }
        public Builder explanation(String explanation) { this.explanation = explanation; return this; }
        public Builder confidenceLevel(String confidenceLevel) { this.confidenceLevel = confidenceLevel; return this; }
        public Builder recentFormScore(double s) { this.recentFormScore = s; return this; }
        public Builder avgPointsScore(double s) { this.avgPointsScore = s; return this; }
        public Builder consistencyScore(double s) { this.consistencyScore = s; return this; }
        public Builder teamStrengthScore(double s) { this.teamStrengthScore = s; return this; }
        public Builder qualifyingFormScore(double s) { this.qualifyingFormScore = s; return this; }
        public Builder headToHeadScore(double s) { this.headToHeadScore = s; return this; }

        public PredictionResult build() {
            return new PredictionResult(this);
        }
    }
}
