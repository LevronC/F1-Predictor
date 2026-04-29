package com.f1predictor.service;

import com.f1predictor.data.DataRepository;
import com.f1predictor.model.Driver;
import com.f1predictor.model.PredictionResult;
import com.f1predictor.model.RaceResult;
import com.f1predictor.model.SimulationResult;
import com.f1predictor.model.Team;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SimulationServiceTest {

    @Test
    void testSimulationRunsSpecifiedIterations() {
        Driver maxVerstappen = Driver.builder()
                .name("Max Verstappen")
                .build();
        Team redBull = Team.builder()
                .name("Red Bull")
                .build();

        Map<String, Driver> drivers = new HashMap<>();
        drivers.put(maxVerstappen.getName(), maxVerstappen);

        Map<String, Team> teams = new HashMap<>();
        teams.put(redBull.getName(), redBull);

        DataRepository repository = new StubDataRepository(drivers, teams);
        PredictionService predictionService = createPredictionService(repository);
        SimulationService simulationService = new SimulationService(predictionService, repository);

        List<SimulationResult> results = simulationService.runSimulation(100);

        assertNotNull(results);
        assertEquals(1, results.size());

        SimulationResult result = results.get(0);
        assertEquals("Max Verstappen", result.driverName());
        assertEquals(1.0, result.winProbability());
        assertEquals(1.0, result.podiumProbability());
        assertEquals(1.0, result.top10Probability());
        assertEquals(1.0, result.averagePosition());
        assertEquals(100, result.positionFrequency().get(1));
    }

    private PredictionService createPredictionService(DataRepository repository) {
        PredictionService predictionService = new PredictionService(repository);
        predictionService.setStrategy((driverList, ignoredRepository, ignoredTeams) -> driverList.stream()
                .map(driver -> PredictionResult.builder()
                        .driverName(driver.getName())
                        .teamName("Test Team")
                        .totalScore(100.0)
                        .build())
                .toList());
        return predictionService;
    }

    private static final class StubDataRepository implements DataRepository {
        private final Map<String, Driver> driverStats;
        private final Map<String, Team> teamStats;

        private StubDataRepository(Map<String, Driver> driverStats, Map<String, Team> teamStats) {
            this.driverStats = driverStats;
            this.teamStats = teamStats;
        }

        @Override
        public void addAll(List<RaceResult> results) {
            throw unsupported();
        }

        @Override
        public List<RaceResult> getAll() {
            throw unsupported();
        }

        @Override
        public List<RaceResult> getByDriver(String driverName) {
            throw unsupported();
        }

        @Override
        public List<RaceResult> getBySeason(int season) {
            throw unsupported();
        }

        @Override
        public List<RaceResult> getByTeam(String teamName) {
            throw unsupported();
        }

        @Override
        public List<RaceResult> getByCircuit(String circuitName) {
            throw unsupported();
        }

        @Override
        public Set<String> getAllDrivers() {
            throw unsupported();
        }

        @Override
        public Set<String> getAllTeams() {
            throw unsupported();
        }

        @Override
        public List<RaceResult> getAllResults() {
            throw unsupported();
        }

        @Override
        public void saveAll(List<RaceResult> results) {
            throw unsupported();
        }

        @Override
        public Map<String, Driver> getDriverStats() {
            return driverStats;
        }

        @Override
        public Map<String, Team> getTeamStats() {
            return teamStats;
        }

        @Override
        public Driver getDriver(String name) {
            return driverStats.get(name);
        }

        @Override
        public Team getTeam(String name) {
            return teamStats.get(name);
        }

        private UnsupportedOperationException unsupported() {
            return new UnsupportedOperationException("Not needed for this test");
        }
    }
}
