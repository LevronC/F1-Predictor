# F1 Predictor - System Architecture

## Overview
F1 Predictor follows a layered architecture with clear separation of concerns, making it maintainable, testable, and portfolio-ready.

## Architecture Layers

```
┌─────────────────────────────────────────────────────────┐
│                  PRESENTATION LAYER                      │
│                                                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │           MenuController (CLI)                   │  │
│  │  - Main menu                                      │  │
│  │  - User input handling                           │  │
│  │  - Output display                                │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                    SERVICE LAYER                         │
│                                                          │
│  ┌────────────────┐  ┌────────────────┐  ┌──────────┐  │
│  │ Analytics      │  │  Prediction    │  │  Report  │  │
│  │ Service        │  │  Service       │  │Generator │  │
│  │                │  │                │  │          │  │
│  │ - Driver stats │  │ - Score calc   │  │ - Format │  │
│  │ - Team stats   │  │ - Ranking      │  │ - Display│  │
│  │ - Trends       │  │ - Explanation  │  │ - Export │  │
│  └────────────────┘  └────────────────┘  └──────────┘  │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                     DATA LAYER                           │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  DataLoader  │→│  DataCleaner │→│ DataRepository│  │
│  │              │  │              │  │              │  │
│  │ - Read CSV   │  │ - Normalize  │  │ - Store data │  │
│  │ - Parse rows │  │ - Validate   │  │ - Query data │  │
│  │ - Error hdl  │  │ - Filter     │  │ - Aggregate  │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                     MODEL LAYER                          │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ RaceResult   │  │   Driver     │  │    Team      │  │
│  │              │  │              │  │              │  │
│  │ - season     │  │ - name       │  │ - name       │  │
│  │ - driver     │  │ - totalPts   │  │ - totalWins  │  │
│  │ - team       │  │ - avgPos     │  │ - avgPoints  │  │
│  │ - position   │  │ - wins       │  │ - drivers    │  │
│  │ - points     │  │ - podiums    │  │ - seasons    │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
```

## Component Details

### 1. Main Application
**Responsibility**: Bootstrap and dependency injection

```java
public class Main {
    public static void main(String[] args) {
        // Initialize dependencies
        DataLoader loader = new CSVDataLoader();
        DataCleaner cleaner = new DataCleaner();
        DataRepository repository = new InMemoryRepository();
        
        // Wire services
        AnalyticsService analytics = new AnalyticsService(repository);
        PredictionService prediction = new PredictionService(analytics);
        ReportGenerator reporter = new ReportGenerator();
        
        // Start CLI
        MenuController controller = new MenuController(
            loader, cleaner, repository, analytics, prediction, reporter
        );
        controller.run();
    }
}
```

### 2. MenuController (Presentation)
**Responsibility**: User interaction and navigation

**Methods**:
- `void run()` - Main loop
- `void displayMainMenu()` - Show options
- `void handleLoadData()` - Load CSV files
- `void handleDriverAnalysis()` - Driver stats menu
- `void handleTeamAnalysis()` - Team stats menu
- `void handlePredictions()` - Prediction menu
- `void handleReports()` - Report generation

### 3. AnalyticsService
**Responsibility**: Statistical analysis and insights

**Methods**:
- `List<Driver> getTopDriversByWins(int limit)`
- `List<Driver> getTopDriversByPoints(int limit)`
- `List<Team> getTopTeamsBySeason(int season)`
- `Map<String, Double> getDriverConsistency(String driverName)`
- `List<RaceResult> getDriverTrend(String driverName, int races)`
- `double getAverageFinishingPosition(String driverName)`
- `int getPodiumCount(String driverName)`

### 4. PredictionService
**Responsibility**: Performance prediction using explainable model

**Scoring Algorithm**:
```
Score = w1×RecentForm + w2×AvgPoints + w3×Consistency + w4×TeamStrength

Where:
- w1=0.4, w2=0.3, w3=0.2, w4=0.1
- RecentForm = weighted average of last 5 races
- AvgPoints = season points / races
- Consistency = 1 / (1 + stddev of positions)
- TeamStrength = team's championship ranking score
```

**Methods**:
- `List<PredictionResult> predictTopFinishers(int topN)`
- `double calculateDriverScore(String driverName)`
- `String explainPrediction(String driverName)`
- `Map<String, Double> getScoreBreakdown(String driverName)`

### 5. DataLoader
**Responsibility**: CSV file reading and parsing

**Methods**:
- `List<RaceResult> loadFromCSV(String filePath)`
- `RaceResult parseRow(String[] row)`
- `void validateRow(String[] row)`

**Error Handling**:
- Malformed CSV → skip row + log warning
- Missing file → throw FileNotFoundException
- Invalid data → mark as dirty + report

### 6. DataCleaner
**Responsibility**: Data normalization and quality

**Methods**:
- `List<RaceResult> clean(List<RaceResult> raw)`
- `String normalizeDriverName(String name)`
- `String normalizeTeamName(String name)`
- `RaceResult handleMissingData(RaceResult result)`
- `List<DataQualityIssue> reportIssues()`

**Cleaning Rules**:
- Trim whitespace
- Standardize name formats (e.g., "Verstappen, Max" → "Max Verstappen")
- Handle DNF/DNS as position = 0
- Flag suspicious data (negative points, impossible positions)

### 7. DataRepository
**Responsibility**: In-memory data storage and querying

**Methods**:
- `void addRaceResult(RaceResult result)`
- `void addAll(List<RaceResult> results)`
- `List<RaceResult> getAllResults()`
- `List<RaceResult> getResultsByDriver(String driverName)`
- `List<RaceResult> getResultsBySeason(int season)`
- `List<RaceResult> getResultsByTeam(String teamName)`
- `Map<String, Driver> aggregateDriverStats()`
- `Map<String, Team> aggregateTeamStats()`

## Data Flow

### 1. Data Loading Flow
```
User → MenuController → DataLoader → CSVParser
  ↓
RawData → DataCleaner → Validation
  ↓
CleanData → DataRepository → Storage
```

### 2. Analysis Flow
```
User → MenuController → AnalyticsService
  ↓
Query → DataRepository → Fetch Results
  ↓
Calculate → Statistics/Metrics
  ↓
Format → ReportGenerator → Display
```

### 3. Prediction Flow
```
User → MenuController → PredictionService
  ↓
Fetch → AnalyticsService → Recent Stats
  ↓
Calculate → Score Components → Weight & Sum
  ↓
Rank → Top Performers
  ↓
Explain → Score Breakdown → Display
```

## Design Patterns

### 1. **Builder Pattern** (Domain Models)
```java
RaceResult result = RaceResult.builder()
    .season(2023)
    .driver("Max Verstappen")
    .team("Red Bull")
    .position(1)
    .points(25.0)
    .build();
```

### 2. **Strategy Pattern** (Prediction Algorithms)
- Interface: `PredictionStrategy`
- Implementations: `WeightedScoreStrategy`, `RecentFormStrategy`
- Allows swapping prediction methods

### 3. **Repository Pattern** (Data Access)
- Abstract data storage details
- Easy to swap in-memory → database later

### 4. **Service Layer Pattern** (Business Logic)
- Clear separation from presentation and data layers
- Reusable business logic

## Error Handling Strategy

### 1. Data Layer
- **FileNotFoundException** → User-friendly message
- **ParseException** → Log + skip row + continue
- **ValidationException** → Report data quality issues

### 2. Service Layer
- **InsufficientDataException** → "Need at least N races"
- **DriverNotFoundException** → "Driver not found in dataset"

### 3. Presentation Layer
- **Invalid input** → Re-prompt user
- **Empty results** → Helpful message + suggestions

## Logging Strategy

```java
Logger logger = LoggerFactory.getLogger(ClassName.class);

// Info: major operations
logger.info("Loaded {} race results from {}", count, filePath);

// Warn: recoverable issues
logger.warn("Skipping malformed row {}: {}", rowNum, error);

// Error: serious problems
logger.error("Failed to load data file: {}", filePath, exception);
```

## Testing Strategy

### Unit Tests
- **DataLoader**: CSV parsing, error handling
- **DataCleaner**: Normalization, validation
- **AnalyticsService**: Metric calculations
- **PredictionService**: Score calculations

### Integration Tests
- Load → Clean → Store → Query
- Analyze → Predict → Report

### Test Data
- `test-data.csv` with known results
- Edge cases: DNF, missing data, ties

## Performance Considerations

1. **In-Memory Storage**: Fast for datasets < 100K rows
2. **Lazy Loading**: Load data only when needed
3. **Caching**: Cache aggregated stats to avoid recalculation
4. **Streaming**: Use Java Streams for filtering/mapping

## Future Extensibility

### Easy to Add
1. **New Data Sources**: Implement `DataLoader` interface
2. **New Predictions**: Implement `PredictionStrategy`
3. **Database**: Replace `InMemoryRepository` with `JDBCRepository`
4. **REST API**: Add Spring Boot controller layer
5. **Charts**: Add `ChartGenerator` service

### Design Principles Applied
- **SOLID principles**: Single responsibility, open/closed
- **DRY**: No code duplication
- **KISS**: Simple, readable solutions
- **Separation of Concerns**: Clear layer boundaries
- **Testability**: Dependencies injected, logic isolated
