# F1 Predictor - Data Schema Specification

## CSV Input Schema

### Required Format
```csv
season,round,circuit,date,driver,constructor,grid,position,points,laps,status,fastestLap
2023,1,Bahrain,2023-03-05,Max Verstappen,Red Bull,1,1,25,57,Finished,1:32.544
2023,1,Bahrain,2023-03-05,Sergio Perez,Red Bull,2,2,18,57,Finished,1:32.891
```

### Field Definitions

| Field | Type | Required | Description | Example | Validation |
|-------|------|----------|-------------|---------|------------|
| season | Integer | Yes | Year of the season | 2023 | 1950-2030 |
| round | Integer | Yes | Race number in season | 1 | 1-25 |
| circuit | String | Yes | Race track name | "Bahrain" | Not empty |
| date | Date | No | Race date | "2023-03-05" | ISO 8601 |
| driver | String | Yes | Driver full name | "Max Verstappen" | Not empty |
| constructor | String | Yes | Team/Constructor | "Red Bull" | Not empty |
| grid | Integer | Yes | Starting grid position | 1 | 1-20 or 0 (pit start) |
| position | Mixed | Yes | Finishing position | 1, "DNF", "DSQ" | 1-20 or status |
| points | Double | Yes | Points scored | 25.0 | 0-26 |
| laps | Integer | No | Laps completed | 57 | >= 0 |
| status | String | Yes | Finish status | "Finished", "DNF" | Known status |
| fastestLap | Time | No | Fastest lap time | "1:32.544" | mm:ss.SSS |

### Status Values
- **Finished**: Completed race
- **DNF**: Did Not Finish (mechanical, crash, etc.)
- **DSQ**: Disqualified
- **DNS**: Did Not Start
- **+N Lap**: Finished N laps behind
- **Retired**: General retirement

### Position Handling
- **Numeric (1-20)**: Actual finishing position
- **DNF/DNS/DSQ**: Encoded as position = 0 internally
- **+N Lap**: Actual position with notation

## Domain Models

### 1. RaceResult (Core Entity)
Represents a single driver's performance in one race.

```java
public class RaceResult {
    private int season;
    private int round;
    private String circuit;
    private LocalDate date;
    private String driverName;
    private String constructorName;
    private int gridPosition;
    private int finishPosition;  // 0 = DNF
    private double points;
    private int lapsCompleted;
    private String status;
    private String fastestLap;
    
    // Computed fields
    private int positionsGained;  // grid - finish
    private boolean finished;
    private boolean onPodium;
    private boolean hasFastestLap;
}
```

**Business Rules**:
- `positionsGained = gridPosition - finishPosition` (positive = gained)
- `finished = status.equals("Finished")`
- `onPodium = finishPosition <= 3 && finished`

### 2. Driver (Aggregated Stats)
Statistical summary of a driver across races.

```java
public class Driver {
    private String name;
    private int totalRaces;
    private int wins;              // P1 finishes
    private int podiums;           // P1-P3 finishes
    private int pointsFinishes;    // Top 10 finishes
    private double totalPoints;
    private double avgPoints;      // totalPoints / totalRaces
    private double avgPosition;    // mean finishing position
    private double consistency;    // 1 / stddev of positions
    private int dnfCount;
    private List<Integer> recentPositions;  // Last 5 races
    private Map<Integer, SeasonStats> seasonStats;
}
```

**Key Metrics**:
- **Win Rate**: `wins / totalRaces`
- **Podium Rate**: `podiums / totalRaces`
- **DNF Rate**: `dnfCount / totalRaces`
- **Consistency Score**: `1 / (1 + stddev(positions))`
- **Recent Form**: Weighted average of last N races

### 3. Team (Constructor Stats)
Team performance aggregation.

```java
public class Team {
    private String name;
    private int totalRaces;
    private int wins;
    private int podiums;
    private double totalPoints;
    private double avgPoints;
    private Set<String> drivers;
    private Map<Integer, SeasonStats> seasonStats;
    private int championships;  // If data available
}
```

**Key Metrics**:
- **Dominance Index**: Win rate × average points
- **Consistency**: stddev of race results
- **Driver Count**: Number of drivers fielded

### 4. SeasonStats (Yearly Breakdown)
```java
public class SeasonStats {
    private int season;
    private int races;
    private int wins;
    private int podiums;
    private double points;
    private int championshipPosition;  // If available
}
```

### 5. PredictionResult
Output of prediction algorithm.

```java
public class PredictionResult {
    private String driverName;
    private String teamName;
    private double totalScore;
    private Map<String, Double> scoreBreakdown;
    private int predictedPosition;
    private String explanation;
    
    // Score components
    private double recentFormScore;
    private double avgPointsScore;
    private double consistencyScore;
    private double teamStrengthScore;
}
```

### 6. DataQualityIssue
Tracks data problems for reporting.

```java
public class DataQualityIssue {
    private int rowNumber;
    private String fieldName;
    private String issueType;  // MISSING, INVALID, SUSPICIOUS
    private String description;
    private String originalValue;
}
```

## Data Transformations

### 1. Name Normalization

**Driver Names**:
```
Input                    → Output
"Verstappen, Max"        → "Max Verstappen"
"max verstappen"         → "Max Verstappen"
"M. Verstappen"          → "Max Verstappen"
"  Max  Verstappen  "    → "Max Verstappen"
```

**Team Names**:
```
Input                    → Output
"Red Bull Racing"        → "Red Bull"
"Scuderia Ferrari"       → "Ferrari"
"Mercedes-AMG"           → "Mercedes"
"oracle red bull racing" → "Red Bull"
```

**Mapping Table**:
```java
Map<String, String> teamAliases = Map.of(
    "Red Bull Racing", "Red Bull",
    "Oracle Red Bull Racing", "Red Bull",
    "Scuderia Ferrari", "Ferrari",
    "Ferrari HP", "Ferrari",
    "Mercedes-AMG Petronas", "Mercedes",
    "McLaren F1 Team", "McLaren"
    // ... etc
);
```

### 2. Position Normalization

```java
private int parsePosition(String position) {
    if (position == null || position.isEmpty()) {
        return 0;  // DNF
    }
    
    if (position.matches("\\d+")) {
        return Integer.parseInt(position);
    }
    
    // Handle status strings
    if (position.equalsIgnoreCase("DNF") || 
        position.equalsIgnoreCase("DNS") ||
        position.equalsIgnoreCase("DSQ") ||
        position.equalsIgnoreCase("Retired")) {
        return 0;
    }
    
    // Handle "+N Lap" format
    if (position.matches("\\+\\d+ Lap.*")) {
        // Extract actual position from context
        return extractPositionFromLapFormat(position);
    }
    
    throw new IllegalArgumentException("Invalid position: " + position);
}
```

### 3. Points Validation

```java
private void validatePoints(int position, double points) {
    Map<Integer, Double> expectedPoints = Map.of(
        1, 25.0, 2, 18.0, 3, 15.0, 4, 12.0, 5, 10.0,
        6, 8.0, 7, 6.0, 8, 4.0, 9, 2.0, 10, 1.0
    );
    
    if (position >= 1 && position <= 10) {
        double expected = expectedPoints.get(position);
        if (Math.abs(points - expected) > 1.0) {
            // Could be sprint race or fastest lap bonus
            logger.warn("Unexpected points: P{} with {} points", position, points);
        }
    }
}
```

## Data Aggregation Queries

### 1. Driver Aggregation
```java
Driver aggregateDriver(String driverName, List<RaceResult> results) {
    List<RaceResult> driverResults = results.stream()
        .filter(r -> r.getDriverName().equals(driverName))
        .collect(toList());
    
    return Driver.builder()
        .name(driverName)
        .totalRaces(driverResults.size())
        .wins(countWins(driverResults))
        .podiums(countPodiums(driverResults))
        .totalPoints(sumPoints(driverResults))
        .avgPoints(avgPoints(driverResults))
        .avgPosition(avgPosition(driverResults))
        .consistency(calculateConsistency(driverResults))
        .recentPositions(getRecentPositions(driverResults, 5))
        .build();
}
```

### 2. Team Aggregation
```java
Team aggregateTeam(String teamName, List<RaceResult> results) {
    List<RaceResult> teamResults = results.stream()
        .filter(r -> r.getConstructorName().equals(teamName))
        .collect(toList());
    
    return Team.builder()
        .name(teamName)
        .totalRaces(teamResults.size())
        .wins(countWins(teamResults))
        .totalPoints(sumPoints(teamResults))
        .drivers(getUniqueDrivers(teamResults))
        .seasonStats(aggregateBySeason(teamResults))
        .build();
}
```

### 3. Recent Form Calculation
```java
double calculateRecentForm(List<Integer> recentPositions) {
    if (recentPositions.isEmpty()) return 0.0;
    
    // Weight recent races more heavily
    double[] weights = {0.3, 0.25, 0.2, 0.15, 0.1};
    double weightedSum = 0.0;
    
    for (int i = 0; i < Math.min(recentPositions.size(), 5); i++) {
        int position = recentPositions.get(i);
        if (position == 0) position = 20;  // DNF penalty
        
        // Lower position = better score
        double score = (21 - position) / 20.0;  // Normalize to 0-1
        weightedSum += score * weights[i];
    }
    
    return weightedSum;
}
```

## Sample Data Requirements

### Minimum Viable Dataset
- **Seasons**: At least 2 complete seasons (40+ races)
- **Drivers**: At least 10 unique drivers
- **Teams**: At least 5 unique teams
- **Races per season**: 15-23 races

### Recommended Dataset
- **Seasons**: 3-5 years (60-115 races)
- **Drivers**: 20-30 drivers
- **Teams**: 8-10 teams
- **Complete fields**: All required + most optional

### Data Sources
1. **Ergast API**: http://ergast.com/mrd/ (free F1 data)
2. **Kaggle**: F1 historical datasets
3. **Manual**: Create synthetic data for testing

## Data Quality Metrics

Track and report:
```java
public class DataQualityReport {
    private int totalRows;
    private int validRows;
    private int invalidRows;
    private int missingFields;
    private Map<String, Integer> issuesByType;
    private List<DataQualityIssue> issues;
    
    public double getQualityScore() {
        return (double) validRows / totalRows;
    }
}
```

**Quality Thresholds**:
- **Excellent**: > 95% valid
- **Good**: 85-95% valid
- **Fair**: 70-85% valid
- **Poor**: < 70% valid

## Storage Strategy

### In-Memory Repository
```java
public class InMemoryRepository {
    private List<RaceResult> raceResults;
    private Map<String, Driver> driverCache;
    private Map<String, Team> teamCache;
    private boolean cacheValid;
    
    public void invalidateCache() {
        cacheValid = false;
    }
    
    public Map<String, Driver> getDriverStats() {
        if (!cacheValid) {
            rebuildCache();
        }
        return driverCache;
    }
}
```

**Cache Strategy**:
- Rebuild cache after data load
- Invalidate on new data
- Lazy computation of aggregates

## Export Schema

### CSV Export Format
```csv
rank,driver,team,races,wins,podiums,points,avgPoints,avgPosition,consistency
1,Max Verstappen,Red Bull,22,19,21,575,26.1,1.3,0.95
2,Sergio Perez,Red Bull,22,2,12,285,13.0,3.8,0.78
```

### JSON Export Format
```json
{
  "driver": "Max Verstappen",
  "team": "Red Bull",
  "stats": {
    "totalRaces": 22,
    "wins": 19,
    "podiums": 21,
    "totalPoints": 575,
    "avgPoints": 26.1,
    "avgPosition": 1.3,
    "consistency": 0.95
  },
  "recentForm": [1, 1, 1, 2, 1],
  "predictionScore": 9.2
}
```
