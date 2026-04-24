package com.f1predictor.data;

import com.f1predictor.model.Driver;
import com.f1predictor.model.RaceResult;
import com.f1predictor.model.Team;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for data storage and querying.
 */
public interface DataRepository {
    void addAll(List<RaceResult> results);
    List<RaceResult> getAll();
    List<RaceResult> getByDriver(String driverName);
    List<RaceResult> getBySeason(int season);
    List<RaceResult> getByTeam(String teamName);
    List<RaceResult> getByCircuit(String circuitName);
    
    Set<String> getAllDrivers();
    Set<String> getAllTeams();
    
    List<RaceResult> getAllResults();
    void saveAll(List<RaceResult> results);
    Map<String, Driver> getDriverStats();
    Map<String, Team> getTeamStats();
    
    Driver getDriver(String name);
    Team getTeam(String name);
}
