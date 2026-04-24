package com.f1predictor.service;

import com.f1predictor.data.DataRepository;
import com.f1predictor.model.CircuitStats;
import com.f1predictor.model.Driver;
import com.f1predictor.model.RaceResult;
import com.f1predictor.model.Team;
import com.f1predictor.util.StatisticsUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for performing complex analytics on race data.
 */
public class AnalyticsService {
    private final DataRepository repository;

    public AnalyticsService(DataRepository repository) {
        this.repository = repository;
    }

    public List<Driver> getTopDriversByWins(int limit) {
        return repository.getDriverStats().values().stream()
                .sorted(Comparator.comparingInt(Driver::getWins).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Driver> getTopDriversByPoints(int limit) {
        return repository.getDriverStats().values().stream()
                .sorted(Comparator.comparingDouble(Driver::getTotalPoints).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Driver> getTopDriversByConsistency(int limit) {
        return repository.getDriverStats().values().stream()
                .sorted(Comparator.comparingDouble(Driver::getConsistency).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<RaceResult> getDriverTrend(String name, int lastN) {
        return repository.getByDriver(name).stream()
                .sorted(Comparator.comparingInt(RaceResult::getSeason).reversed()
                        .thenComparingInt(RaceResult::getRound).reversed())
                .limit(lastN)
                .collect(Collectors.toList());
    }

    public List<CircuitStats> getCircuitStats(String driverName) {
        List<RaceResult> results = repository.getByDriver(driverName);
        Map<String, List<RaceResult>> byCircuit = results.stream()
                .collect(Collectors.groupingBy(RaceResult::getCircuit));

        return byCircuit.entrySet().stream()
                .map(entry -> {
                    String circuit = entry.getKey();
                    List<RaceResult> circuitResults = entry.getValue();
                    int total = circuitResults.size();
                    int wins = (int) circuitResults.stream().filter(r -> r.getFinishPosition() == 1).count();
                    int podiums = (int) circuitResults.stream().filter(r -> r.getFinishPosition() >= 1 && r.getFinishPosition() <= 3).count();
                    double avgPos = circuitResults.stream().mapToInt(r -> r.getFinishPosition() == 0 ? 20 : r.getFinishPosition()).average().orElse(0);
                    double avgPts = circuitResults.stream().mapToDouble(RaceResult::getPoints).average().orElse(0);

                    return CircuitStats.builder()
                            .circuit(circuit)
                            .driverName(driverName)
                            .races(total)
                            .wins(wins)
                            .podiums(podiums)
                            .avgPosition(avgPos)
                            .avgPoints(avgPts)
                            .build();
                })
                .sorted(Comparator.comparingInt(CircuitStats::getWins).reversed())
                .collect(Collectors.toList());
    }

    public Map<String, Object> compareDrivers(String driver1, String driver2) {
        Driver d1 = repository.getDriver(driver1);
        Driver d2 = repository.getDriver(driver2);

        if (d1 == null || d2 == null) return Collections.emptyMap();

        Map<String, Object> comparison = new LinkedHashMap<>();
        comparison.put("Driver 1", d1.getName());
        comparison.put("Driver 2", d2.getName());
        comparison.put("Wins", d1.getWins() + " vs " + d2.getWins());
        comparison.put("Podiums", d1.getPodiums() + " vs " + d2.getPodiums());
        comparison.put("Avg Points", String.format("%.2f vs %.2f", d1.getAvgPoints(), d2.getAvgPoints()));
        comparison.put("Consistency", String.format("%.2f vs %.2f", d1.getConsistency(), d2.getConsistency()));
        
        return comparison;
    }
}
