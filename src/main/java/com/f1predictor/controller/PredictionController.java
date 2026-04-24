package com.f1predictor.controller;

import com.f1predictor.model.PredictionResult;
import com.f1predictor.model.SimulationResult;
import com.f1predictor.service.AnalyticsService;
import com.f1predictor.service.BacktestingService;
import com.f1predictor.service.PredictionService;
import com.f1predictor.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // For local dev with Vite
public class PredictionController {

    private final PredictionService predictionService;
    private final SimulationService simulationService;
    private final AnalyticsService analyticsService;
    private final BacktestingService backtestingService;

    @Autowired
    public PredictionController(PredictionService predictionService, 
                                SimulationService simulationService,
                                AnalyticsService analyticsService,
                                BacktestingService backtestingService) {
        this.predictionService = predictionService;
        this.simulationService = simulationService;
        this.analyticsService = analyticsService;
        this.backtestingService = backtestingService;
    }

    @GetMapping("/predictions")
    public List<PredictionResult> getPredictions(@RequestParam(defaultValue = "10") int limit) {
        return predictionService.predictTopFinishers(limit);
    }

    @GetMapping("/simulate")
    public List<SimulationResult> simulateRace(@RequestParam(defaultValue = "1000") int iterations) {
        return simulationService.runSimulation(iterations);
    }

    @GetMapping("/backtest/{season}")
    public BacktestingService.BacktestReport runBacktest(@PathVariable int season) {
        return backtestingService.runBacktest(season);
    }

    @GetMapping("/analytics/head-to-head")
    public HeadToHeadResult compare(@RequestParam String d1, @RequestParam String d2) {
        double r1 = analyticsService.calculateRollingAveragePosition(d1, 5);
        double r2 = analyticsService.calculateRollingAveragePosition(d2, 5);
        return new HeadToHeadResult(d1, d2, r1, r2, r1 < r2 ? d1 : d2);
    }

    public static record HeadToHeadResult(String driver1, String driver2, double rollingAvg1, double rollingAvg2, String predictedWinner) {}
}
