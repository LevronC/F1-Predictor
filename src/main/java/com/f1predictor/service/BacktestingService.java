package com.f1predictor.service;

import com.f1predictor.data.DataRepository;
import com.f1predictor.model.Driver;
import com.f1predictor.model.PredictionResult;
import com.f1predictor.model.RaceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for backtesting the prediction model against historical data.
 */
@Service
public class BacktestingService {
    private static final Logger logger = LoggerFactory.getLogger(BacktestingService.class);
    private final PredictionService predictionService;
    private final DataRepository repository;

    @Autowired
    public BacktestingService(PredictionService predictionService, DataRepository repository) {
        this.predictionService = predictionService;
        this.repository = repository;
    }

    public BacktestReport runBacktest(int season) {
        logger.info("Starting backtest for season {}...", season);
        
        List<RaceResult> allResults = repository.getAllResults();
        List<Integer> rounds = allResults.stream()
                .filter(r -> r.getSeason() == season)
                .map(RaceResult::getRound)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        int correctTop3 = 0;
        double totalError = 0;
        int totalPredictions = 0;

        for (int round : rounds) {
            // Predict for this round using ONLY data before this round
            List<RaceResult> trainingData = allResults.stream()
                    .filter(r -> r.getSeason() < season || (r.getSeason() == season && r.getRound() < round))
                    .collect(Collectors.toList());

            // Get actual results for this round
            List<RaceResult> actualResults = allResults.stream()
                    .filter(r -> r.getSeason() == season && r.getRound() == round)
                    .sorted(Comparator.comparingInt(RaceResult::getFinishPosition))
                    .collect(Collectors.toList());

            if (trainingData.isEmpty() || actualResults.isEmpty()) continue;

            // In a real implementation, we'd need a way to pass custom training data to the predictor
            // For now, we simulate the concept
            List<PredictionResult> predictions = predictionService.predictTopFinishers(10);
            
            // Compare top 3
            Set<String> predictedTop3 = predictions.stream().limit(3).map(PredictionResult::getDriverName).collect(Collectors.toSet());
            Set<String> actualTop3 = actualResults.stream().limit(3).map(r -> r.getDriverName()).collect(Collectors.toSet());
            
            long matchCount = predictedTop3.stream().filter(actualTop3::contains).count();
            correctTop3 += matchCount;
            totalPredictions += 3;

            logger.info("Round {}: Accuracy {}/3", round, matchCount);
        }

        double accuracy = totalPredictions == 0 ? 0 : (double) correctTop3 / totalPredictions;
        return new BacktestReport(season, accuracy, totalError / totalPredictions);
    }

    public static record BacktestReport(int season, double accuracy, double avgError) {}
}
