package com.f1predictor.service;

import com.f1predictor.data.DataRepository;
import com.f1predictor.model.Driver;
import com.f1predictor.model.PredictionResult;
import com.f1predictor.model.Team;

import java.util.List;
import java.util.Map;

/**
 * Strategy interface for race prediction algorithms.
 */
public interface PredictionStrategy {
    List<PredictionResult> predict(List<Driver> drivers, DataRepository repository, Map<String, Team> teams);
}
