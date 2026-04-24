package com.f1predictor.data;

import com.f1predictor.model.Driver;
import com.f1predictor.model.RaceResult;
import com.f1predictor.model.SeasonStats;
import com.f1predictor.model.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory implementation of DataRepository.
 */
public class InMemoryRepository implements DataRepository {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryRepository.class);

    private List<RaceResult> raceResults = new ArrayList<>();
    private Map<String, Driver> driverCache = new HashMap<>();
    private Map<String, Team> teamCache = new HashMap<>();
    private boolean cacheValid = false;

    @Override
    public void addAll(List<RaceResult> results) {
        this.raceResults.addAll(results);
        cacheValid = false;
        logger.info("Added {} results to repository. Cache invalidated.", results.size());
    }

    @Override
    public List<RaceResult> getAll() {
        return Collections.unmodifiableList(raceResults);
    }

    @Override
    public List<RaceResult> getByDriver(String driverName) {
        return raceResults.stream()
                .filter(r -> r.getDriverName().equalsIgnoreCase(driverName))
                .collect(Collectors.toList());
    }

    @Override
    public List<RaceResult> getBySeason(int season) {
        return raceResults.stream()
                .filter(r -> r.getSeason() == season)
                .collect(Collectors.toList());
    }

    @Override
    public List<RaceResult> getByTeam(String teamName) {
        return raceResults.stream()
                .filter(r -> r.getConstructorName().equalsIgnoreCase(teamName))
                .collect(Collectors.toList());
    }

    @Override
    public List<RaceResult> getByCircuit(String circuitName) {
        return raceResults.stream()
                .filter(r -> r.getCircuit().equalsIgnoreCase(circuitName))
                .collect(Collectors.toList());
    }

    @Override
    public Set<String> getAllDrivers() {
        return raceResults.stream().map(RaceResult::getDriverName).collect(Collectors.toSet());
    }

    @Override
    public Set<String> getAllTeams() {
        return raceResults.stream().map(RaceResult::getConstructorName).collect(Collectors.toSet());
    }

    @Override
    public Map<String, Driver> getDriverStats() {
        if (!cacheValid) rebuildCache();
        return Collections.unmodifiableMap(driverCache);
    }

    @Override
    public Map<String, Team> getTeamStats() {
        if (!cacheValid) rebuildCache();
        return Collections.unmodifiableMap(teamCache);
    }

    @Override
    public Driver getDriver(String name) {
        if (!cacheValid) rebuildCache();
        return driverCache.get(name);
    }

    @Override
    public Team getTeam(String name) {
        if (!cacheValid) rebuildCache();
        return teamCache.get(name);
    }

    private void rebuildCache() {
        logger.info("Rebuilding driver and team caches...");
        driverCache.clear();
        teamCache.clear();

        // 1. Group results by driver
        Map<String, List<RaceResult>> byDriver = raceResults.stream()
                .collect(Collectors.groupingBy(RaceResult::getDriverName));

        for (Map.Entry<String, List<RaceResult>> entry : byDriver.entrySet()) {
            driverCache.put(entry.getKey(), aggregateDriver(entry.getKey(), entry.getValue()));
        }

        // 2. Group results by team
        Map<String, List<RaceResult>> byTeam = raceResults.stream()
                .collect(Collectors.groupingBy(RaceResult::getConstructorName));

        for (Map.Entry<String, List<RaceResult>> entry : byTeam.entrySet()) {
            teamCache.put(entry.getKey(), aggregateTeam(entry.getKey(), entry.getValue()));
        }

        cacheValid = true;
        logger.info("Cache rebuild complete. Drivers: {}, Teams: {}", driverCache.size(), teamCache.size());
    }

    private Driver aggregateDriver(String name, List<RaceResult> results) {
        int total = results.size();
        int wins = 0;
        int podiums = 0;
        int pointsFinishes = 0;
        double totalPoints = 0;
        int dnfCount = 0;
        List<Integer> positions = new ArrayList<>();

        for (RaceResult r : results) {
            totalPoints += r.getPoints();
            if (r.getFinishPosition() > 0) {
                positions.add(r.getFinishPosition());
                if (r.getFinishPosition() == 1) wins++;
                if (r.getFinishPosition() <= 3) podiums++;
                if (r.getFinishPosition() <= 10) pointsFinishes++;
            } else {
                dnfCount++;
                positions.add(20); // Penalty for DNF in stats
            }
        }

        double avgPos = positions.stream().mapToInt(Integer::intValue).average().orElse(0);
        
        // Simple consistency: 1 / (1 + stddev)
        double variance = 0;
        if (!positions.isEmpty()) {
            for (int p : positions) {
                variance += Math.pow(p - avgPos, 2);
            }
            variance /= positions.size();
        }
        double stddev = Math.sqrt(variance);
        double consistency = 1.0 / (1.0 + stddev);

        // Sort by date/season/round to get recent positions
        List<Integer> recent = results.stream()
                .sorted(Comparator.comparingInt(RaceResult::getSeason).reversed()
                        .thenComparingInt(RaceResult::getRound).reversed())
                .limit(5)
                .map(r -> r.getFinishPosition() == 0 ? 20 : r.getFinishPosition())
                .collect(Collectors.toList());

        return Driver.builder()
                .name(name)
                .totalRaces(total)
                .wins(wins)
                .podiums(podiums)
                .pointsFinishes(pointsFinishes)
                .totalPoints(totalPoints)
                .avgPoints(total == 0 ? 0 : totalPoints / total)
                .avgPosition(avgPos)
                .consistency(consistency)
                .dnfCount(dnfCount)
                .recentPositions(recent)
                .build();
    }

    private Team aggregateTeam(String name, List<RaceResult> results) {
        int total = results.size();
        int wins = 0;
        int podiums = 0;
        double totalPoints = 0;
        Set<String> drivers = new HashSet<>();

        for (RaceResult r : results) {
            totalPoints += r.getPoints();
            drivers.add(r.getDriverName());
            if (r.getFinishPosition() == 1) wins++;
            if (r.getFinishPosition() >= 1 && r.getFinishPosition() <= 3) podiums++;
        }

        return Team.builder()
                .name(name)
                .totalRaces(total)
                .wins(wins)
                .podiums(podiums)
                .totalPoints(totalPoints)
                .avgPoints(total == 0 ? 0 : totalPoints / total)
                .drivers(drivers)
                .build();
    }
}
