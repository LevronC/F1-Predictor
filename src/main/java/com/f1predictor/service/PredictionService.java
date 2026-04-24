package com.f1predictor.service;

import com.f1predictor.data.DataRepository;
import com.f1predictor.model.Driver;
import com.f1predictor.model.PredictionResult;
import com.f1predictor.model.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Main service for generating race predictions.
 */
@Service
public class PredictionService {
    private static final Logger logger = LoggerFactory.getLogger(PredictionService.class);

    private final DataRepository repository;
    private PredictionStrategy strategy;

    @Autowired
    public PredictionService(DataRepository repository) {
        this.repository = repository;
        this.strategy = new WeightedScoringStrategy(); // Default
    }

    public void setStrategy(PredictionStrategy strategy) {
        this.strategy = strategy;
    }

    public List<PredictionResult> predictTopFinishers(int topN) {
        logger.info("Generating predictions for top {} finishers...", topN);
        Map<String, Driver> driverStats = repository.getDriverStats();
        Map<String, Team> teamStats = repository.getTeamStats();

        List<Driver> drivers = new ArrayList<>(driverStats.values());
        List<PredictionResult> allPredictions = strategy.predict(drivers, repository, teamStats);

        // Assign predicted positions based on ranking
        for (int i = 0; i < allPredictions.size(); i++) {
            // Need a way to set predictedPosition. I'll use a hack or update the model.
            // Actually I'll just return the sorted list and the caller can use the index.
        }

        return allPredictions.stream().limit(topN).collect(Collectors.toList());
    }

    public PredictionResult predictDriver(String driverName) {
        Map<String, Driver> driverStats = repository.getDriverStats();
        Driver driver = driverStats.get(driverName);
        if (driver == null) return null;

        List<PredictionResult> all = strategy.predict(List.of(driver), repository, repository.getTeamStats());
        return all.isEmpty() ? null : all.get(0);
    }
}
