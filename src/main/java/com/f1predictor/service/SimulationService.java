package com.f1predictor.service;

import com.f1predictor.data.DataRepository;
import com.f1predictor.model.Driver;
import com.f1predictor.model.PredictionResult;
import com.f1predictor.model.SimulationResult;
import com.f1predictor.model.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for running Monte Carlo simulations of race outcomes.
 */
@Service
public class SimulationService {
    private static final Logger logger = LoggerFactory.getLogger(SimulationService.class);
    private final PredictionService predictionService;
    private final DataRepository repository;
    private final Random random = new Random();

    @Autowired
    public SimulationService(PredictionService predictionService, DataRepository repository) {
        this.predictionService = predictionService;
        this.repository = repository;
    }

    public List<SimulationResult> runSimulation(int iterations) {
        logger.info("Running Monte Carlo simulation with {} iterations...", iterations);
        
        Map<String, Driver> driverStats = repository.getDriverStats();
        Map<String, Team> teamStats = repository.getTeamStats();
        List<Driver> drivers = new ArrayList<>(driverStats.values());

        Map<String, List<Integer>> results = new HashMap<>();
        drivers.forEach(d -> results.put(d.getName(), new ArrayList<>()));

        for (int i = 0; i < iterations; i++) {
            List<PredictionResult> simResult = runSingleIteration(drivers, teamStats);
            for (int rank = 0; rank < simResult.size(); rank++) {
                results.get(simResult.get(rank).getDriverName()).add(rank + 1);
            }
        }

        return results.entrySet().stream()
                .map(entry -> calculateStats(entry.getKey(), entry.getValue(), iterations))
                .sorted((a, b) -> Double.compare(b.winProbability(), a.winProbability()))
                .collect(Collectors.toList());
    }

    private List<PredictionResult> runSingleIteration(List<Driver> drivers, Map<String, Team> teams) {
        List<PredictionResult> basePredictions = predictionService.predictTopFinishers(drivers.size());
        
        return basePredictions.stream()
                .map(p -> {
                    double noise = random.nextGaussian() * 1.5; 
                    PredictionResult res = PredictionResult.builder()
                            .driverName(p.getDriverName())
                            .totalScore(p.getTotalScore() + noise)
                            .build();
                    return res;
                })
                .sorted(Comparator.comparingDouble(PredictionResult::getTotalScore).reversed())
                .collect(Collectors.toList());
    }

    private SimulationResult calculateStats(String name, List<Integer> positions, int iterations) {
        long wins = positions.stream().filter(p -> p == 1).count();
        long podiums = positions.stream().filter(p -> p <= 3).count();
        long top10 = positions.stream().filter(p -> p <= 10).count();
        double avgPos = positions.stream().mapToInt(Integer::intValue).average().orElse(20);

        Map<Integer, Integer> freq = new HashMap<>();
        positions.forEach(p -> freq.merge(p, 1, Integer::sum));

        return new SimulationResult(
                name,
                (double) wins / iterations,
                (double) podiums / iterations,
                (double) top10 / iterations,
                avgPos,
                freq
        );
    }
}
