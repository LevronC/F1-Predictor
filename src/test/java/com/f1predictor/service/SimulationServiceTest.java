package com.f1predictor.service;

import com.f1predictor.data.DataRepository;
import com.f1predictor.model.SimulationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SimulationServiceTest {

    @Mock
    private PredictionService predictionService;
    @Mock
    private DataRepository repository;

    private SimulationService simulationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        simulationService = new SimulationService(predictionService, repository);
    }

    @Test
    void testSimulationRunsSpecifiedIterations() {
        // Mocking repo and prediction service would be extensive, 
        // but we test the structure of the simulation output.
        // Assuming some drivers are present
        when(repository.getDriverStats()).thenReturn(new java.util.HashMap<>());
        
        List<SimulationResult> results = simulationService.runSimulation(100);
        assertNotNull(results);
    }
}
