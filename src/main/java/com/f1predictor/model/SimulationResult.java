package com.f1predictor.model;

import java.util.Map;

public record SimulationResult(
    String driverName,
    double winProbability,
    double podiumProbability,
    double top10Probability,
    double averagePosition,
    Map<Integer, Integer> positionFrequency
) {}
