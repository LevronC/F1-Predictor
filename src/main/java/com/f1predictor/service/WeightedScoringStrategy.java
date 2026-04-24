package com.f1predictor.service;

import com.f1predictor.data.DataRepository;
import com.f1predictor.model.Driver;
import com.f1predictor.model.PredictionResult;
import com.f1predictor.model.RaceResult;
import com.f1predictor.model.Team;
import com.f1predictor.util.StatisticsUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 6-factor weighted scoring strategy for race predictions.
 */
public class WeightedScoringStrategy implements PredictionStrategy {

    @Override
    public List<PredictionResult> predict(List<Driver> drivers, DataRepository repository, Map<String, Team> teams) {
        List<PredictionResult> results = new ArrayList<>();

        for (Driver driver : drivers) {
            double recentForm = calculateRecentForm(driver);
            double avgPoints = calculateAvgPoints(driver);
            double consistency = driver.getConsistency(); // 0-1 already
            double teamStrength = calculateTeamStrength(driver, teams);
            double qualyForm = calculateQualifyingForm(driver, repository);
            double headToHead = calculateHeadToHead(driver, repository);

            double totalScore = (recentForm * 0.35) +
                                (avgPoints * 0.25) +
                                (consistency * 0.15) +
                                (teamStrength * 0.10) +
                                (qualyForm * 0.10) +
                                (headToHead * 0.05);

            String confidence = determineConfidence(totalScore, driver);

            results.add(PredictionResult.builder()
                    .driverName(driver.getName())
                    .teamName(getTeamName(driver, repository))
                    .recentFormScore(recentForm)
                    .avgPointsScore(avgPoints)
                    .consistencyScore(consistency)
                    .teamStrengthScore(teamStrength)
                    .qualifyingFormScore(qualyForm)
                    .headToHeadScore(headToHead)
                    .totalScore(totalScore * 10) // Scale to 0-10
                    .confidenceLevel(confidence)
                    .explanation(generateExplanation(driver, totalScore))
                    .build());
        }

        // Rank results
        return results.stream()
                .sorted(Comparator.comparingDouble(PredictionResult::getTotalScore).reversed())
                .peek(r -> r.getClass()) // side effect to set rank? No, we'll do it in service or UI
                .collect(Collectors.toList());
    }

    private double calculateRecentForm(Driver driver) {
        List<Integer> positions = driver.getRecentPositions();
        if (positions.isEmpty()) return 0.5;
        
        // Normalize: (21 - pos) / 20.0
        List<Double> scores = positions.stream()
                .map(p -> (21.0 - p) / 20.0)
                .collect(Collectors.toList());
        
        double[] weights = {0.30, 0.25, 0.20, 0.15, 0.10};
        return StatisticsUtil.weightedAverage(scores, weights);
    }

    private double calculateAvgPoints(Driver driver) {
        return Math.min(driver.getAvgPoints() / 25.0, 1.0);
    }

    private double calculateTeamStrength(Driver driver, Map<String, Team> teams) {
        // Find current team rank by avg points
        String teamName = teams.values().stream()
                .filter(t -> t.getDrivers().contains(driver.getName()))
                .map(Team::getName)
                .findFirst().orElse("");
        
        if (teamName.isEmpty()) return 0;
        
        List<Team> sortedTeams = teams.values().stream()
                .sorted(Comparator.comparingDouble(Team::getAvgPoints).reversed())
                .collect(Collectors.toList());
        
        int rank = 0;
        for (int i = 0; i < sortedTeams.size(); i++) {
            if (sortedTeams.get(i).getName().equals(teamName)) {
                rank = i + 1;
                break;
            }
        }
        
        int numTeams = sortedTeams.size();
        return numTeams <= 1 ? 1.0 : (double) (numTeams - rank) / (numTeams - 1);
    }

    private double calculateQualifyingForm(Driver driver, DataRepository repository) {
        List<RaceResult> results = repository.getByDriver(driver.getName());
        if (results.isEmpty()) return 0.5;
        
        double avgFinish = results.stream().mapToInt(r -> r.getFinishPosition() == 0 ? 20 : r.getFinishPosition()).average().orElse(20);
        double avgGrid = results.stream().mapToInt(RaceResult::getGridPosition).average().orElse(20);
        
        double delta = avgGrid - avgFinish; // positive = gained positions
        return StatisticsUtil.normalize(delta, -10, 10);
    }

    private double calculateHeadToHead(Driver driver, DataRepository repository) {
        // Simple mock for head-to-head since we don't track teammates per race easily in this schema yet
        // In a real app, we'd find the other driver in the same team for each race.
        return 0.6; // Neutral/Slightly favored
    }

    private String determineConfidence(double score, Driver driver) {
        if (driver.getTotalRaces() < 3) return "Low (Insufficient Data)";
        if (score >= 0.8) return "Very High";
        if (score >= 0.7) return "High";
        if (score >= 0.5) return "Moderate";
        return "Low";
    }

    private String getTeamName(Driver driver, DataRepository repository) {
        List<RaceResult> results = repository.getByDriver(driver.getName());
        if (results.isEmpty()) return "Unknown";
        return results.get(results.size() - 1).getConstructorName();
    }

    private String generateExplanation(Driver driver, double score) {
        if (score > 0.8) return driver.getName() + " is in elite form with high consistency.";
        if (score > 0.6) return driver.getName() + " shows strong potential based on recent results.";
        return driver.getName() + " has shown variable performance recently.";
    }
}
