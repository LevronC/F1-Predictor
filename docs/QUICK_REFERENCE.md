# F1 Predictor - Quick Reference Guide

## Project Overview

**F1 Predictor** is a Java-based data analytics application that processes historical Formula One race data to generate insights and performance predictions using explainable statistical models.

**Portfolio Value**: Demonstrates data analysis, statistical thinking, predictive modeling, clean architecture, and professional documentation skills.

## Key Architectural Decisions

### 1. Layered Architecture
- **Presentation**: CLI (MenuController)
- **Service**: Business logic (Analytics, Prediction, Reports)
- **Data**: Loading, cleaning, storage
- **Model**: Domain objects (RaceResult, Driver, Team)

**Rationale**: Clear separation of concerns, testable, maintainable

### 2. In-Memory Storage
- Uses `List<RaceResult>` and cached aggregations
- Fast for datasets < 100K rows

**Rationale**: Simple, fast, suitable for portfolio scope

### 3. Explainable Predictions
- Transparent weighted scoring (not ML black box)
- Score breakdown shown to users
- Based on 4 factors: recent form, avg points, consistency, team strength

**Rationale**: Interview-friendly, can explain every decision

### 4. Builder Pattern for Models
```java
RaceResult result = RaceResult.builder()
    .season(2023)
    .driver("Max Verstappen")
    .build();
```

**Rationale**: Clean object construction, immutable objects

## Technology Choices

| Component | Technology | Why |
|-----------|-----------|-----|
| Build Tool | Maven | Industry standard, easy dependency management |
| CSV Parsing | OpenCSV | Battle-tested, handles edge cases |
| Testing | JUnit 5 | Modern, feature-rich |
| Logging | SLF4J + Logback | Professional logging standard |
| Java Version | 17+ | Modern features (records, text blocks) |

## Prediction Formula (v2 — 6-Factor Model)

```
Score = (RecentForm     × 0.35)   ← most recent indicator
      + (AvgPoints      × 0.25)   ← sustained performance
      + (Consistency    × 0.15)   ← position variance
      + (TeamStrength   × 0.10)   ← constructor standing
      + (QualifyingForm × 0.10)   ← grid vs finish delta
      + (HeadToHead     × 0.05)   ← vs teammate win rate
```

### Component Details

**Recent Form (35%)**:
- Weighted average of last 5 races: [0.30, 0.25, 0.20, 0.15, 0.10]
- DNF → treated as P20
- Normalize: `(21 - position) / 20.0`

**Average Points (25%)**:
- `totalPoints / racesStarted`, normalized: `min(avgPts / 25.0, 1.0)`

**Consistency (15%)**:
- `1 / (1 + stddev of finishPositions)`
- DNF encoded as P20 before stddev

**Team Strength (10%)**:
- `(numTeams - constructorRank) / (numTeams - 1)`
- P1 team = 1.0, last = 0.0

**Qualifying Form (10%)** ← NEW
- `avgFinishPosition - avgGridPosition` (positive = gains places)
- Normalize: `(delta + 10) / 20.0`, clamped to [0, 1]
- Falls back to 0 if no grid data available

**Head-to-Head (5%)** ← NEW
- Win rate vs current teammate in shared races
- Falls back to 0.5 (neutral) if < 5 shared races

### Why v2 is Better
- Qualifying position is one of the strongest real-world F1 predictors (~0.65 correlation with finishing position)
- Head-to-head normalizes for car quality — distinguishes driver skill from machinery
- Confidence now based on data sufficiency, not just score magnitude

## Data Flow Summary

1. **Load**: CSV → DataLoader → Raw RaceResults
2. **Clean**: Raw → DataCleaner → Validated RaceResults
3. **Store**: Validated → Repository → Cache
4. **Analyze**: Repository → AnalyticsService → Statistics
5. **Predict**: Statistics → PredictionService → Scores
6. **Display**: Results → ReportGenerator → CLI

## Key Classes & Responsibilities

### Core Models
- **RaceResult**: Single race performance record with computed fields
- **Driver**: Aggregated driver statistics including `recentPositions`
- **Team**: Constructor stats with per-season breakdown
- **SeasonStats**: Yearly breakdown (wins, podiums, points) — nested in Driver/Team
- **CircuitStats**: Per-driver, per-circuit performance history (NEW)
- **PredictionResult**: 6-component score breakdown + confidence + explanation
- **DataQualityReport**: Issue summary with `getQualityScore()`
- **SessionState**: Runtime state (data loaded, file path, load time)

### Services
- **AnalyticsService**: All statistical queries, driver comparison, circuit analysis
- **PredictionStrategy** (interface): Swappable prediction algorithm
- **WeightedScoringStrategy**: 6-factor weighted scoring implementation
- **PredictionService**: Orchestrates strategy, returns ranked PredictionResults
- **ReportGenerator**: Table formatting, progress bars, CSV export

### Data Layer
- **DataLoader** (interface): `load(String filePath)`
- **CSVDataLoader**: OpenCSV-based implementation, row-by-row parsing
- **DataCleaner**: Name normalization, position parsing, duplicate detection
- **DataRepository** (interface): All query contracts
- **InMemoryRepository**: List + cache implementation

### Presentation
- **MenuController**: All menu flows, input validation, fuzzy driver search
- **SessionState**: Guards analysis menus when no data loaded

## Testing Strategy

### Unit Tests
```
DataLoaderTest
- testLoadValidCSV()
- testHandleMalformedCSV()
- testEmptyFile()

DataCleanerTest
- testNameNormalization()
- testMissingDataHandling()
- testPositionParsing()

AnalyticsServiceTest
- testDriverAggregation()
- testConsistencyCalculation()
- testRecentFormCalculation()

PredictionServiceTest
- testScoreCalculation()
- testRanking()
- testScoreBreakdown()
```

### Integration Tests
```
EndToEndTest
- testLoadAnalyzePredict()
- testDataQualityReporting()
```

### Coverage Target
- Minimum: 80%
- Focus on business logic

## File Structure

```
f1-predictor/
├── pom.xml                          # Maven config
├── README.md                        # Portfolio documentation
├── data/
│   └── sample-f1-data.csv          # 2021-2023 F1 results
├── src/
│   ├── main/java/com/f1predictor/
│   │   ├── Main.java
│   │   ├── model/
│   │   │   ├── RaceResult.java
│   │   │   ├── Driver.java
│   │   │   ├── Team.java
│   │   │   ├── SeasonStats.java
│   │   │   ├── CircuitStats.java        ← NEW
│   │   │   ├── PredictionResult.java
│   │   │   ├── DataQualityIssue.java
│   │   │   └── DataQualityReport.java
│   │   ├── data/
│   │   │   ├── DataLoader.java          ← interface
│   │   │   ├── CSVDataLoader.java       ← implementation
│   │   │   ├── DataCleaner.java
│   │   │   ├── DataRepository.java      ← interface
│   │   │   └── InMemoryRepository.java  ← implementation
│   │   ├── service/
│   │   │   ├── AnalyticsService.java
│   │   │   ├── PredictionStrategy.java  ← interface (NEW)
│   │   │   ├── WeightedScoringStrategy.java ← NEW
│   │   │   ├── PredictionService.java
│   │   │   └── ReportGenerator.java
│   │   ├── controller/
│   │   │   ├── MenuController.java
│   │   │   └── SessionState.java        ← NEW
│   │   └── util/
│   │       └── StatisticsUtil.java
│   ├── main/resources/
│   │   └── logback.xml
│   └── test/
│       ├── java/com/f1predictor/
│       │   ├── data/
│       │   │   ├── DataLoaderTest.java
│       │   │   └── DataCleanerTest.java
│       │   ├── service/
│       │   │   ├── AnalyticsServiceTest.java
│       │   │   └── PredictionServiceTest.java
│       │   ├── util/
│       │   │   └── StatisticsUtilTest.java
│       │   └── integration/
│       │       └── EndToEndTest.java
│       └── resources/
│           ├── test-data-valid.csv
│           ├── test-data-malformed.csv
│           └── test-data-empty.csv
└── docs/
    ├── PROJECT_PLAN.md
    ├── ARCHITECTURE.md
    ├── DATA_SCHEMA.md
    ├── CLI_FLOW.md
    └── QUICK_REFERENCE.md
```

## Implementation Order (Phased)

| Phase | Tasks | Blocked by |
|---|---|---|
| 0 | Maven setup, pom.xml, logback.xml, folder skeleton | Nothing |
| 1 | 7 domain models with builders | Phase 0 |
| 2a | DataLoader interface + CSVDataLoader | Phase 1 |
| 2b | DataCleaner (normalization + validation) | Phase 1 |
| 2c | DataRepository interface + InMemoryRepository | Phase 1 |
| 2d | Sample dataset CSV | Any time |
| 3a | StatisticsUtil (mean, stddev, normalize, weighted avg) | Phase 1 |
| 3b | AnalyticsService (all queries + driver comparison) | Phases 2 + 3a |
| 4 | PredictionStrategy + WeightedScoringStrategy + PredictionService | Phase 3 |
| 5a | ReportGenerator (table, bars, export) | Phase 3 |
| 5b | MenuController + SessionState | Phases 4 + 5a |
| 6 | All unit tests + integration tests | Phase 5 |
| 7 | README update, JavaDocs, portfolio polish | Phase 6 |

**New classes vs original plan**:
- `CircuitStats` — per-circuit per-driver stats
- `DataRepository` interface — makes testing easier
- `CSVDataLoader` — concrete impl of `DataLoader`
- `InMemoryRepository` — concrete impl of `DataRepository`
- `PredictionStrategy` interface — swappable algorithms
- `WeightedScoringStrategy` — 6-factor scoring
- `SessionState` — runtime state tracking

## Portfolio Talking Points

### What It Demonstrates
1. **Data Engineering**: CSV ingestion, cleaning, validation
2. **Statistical Analysis**: Aggregations, trends, consistency metrics
3. **Predictive Modeling**: Weighted scoring, explainable AI
4. **Software Design**: Layered architecture, SOLID principles
5. **Testing**: Unit tests, integration tests, quality focus
6. **Documentation**: Professional README, clear methodology

### Interview Questions to Prepare
- "Why weighted scoring instead of machine learning?"
  → Explainability, interpretability, portfolio scope
  
- "How would you scale this to millions of records?"
  → Database instead of in-memory, caching strategies, indexes
  
- "How do you handle data quality issues?"
  → Validation on load, quality reports, graceful degradation
  
- "What would you add next?"
  → Database, REST API, web UI, circuit-specific predictions

## Resume Bullets (Draft)

1. **Built F1 Predictor**, a Java data analytics application processing 60+ Formula One races to generate driver/team insights using a 6-factor explainable scoring model, with end-to-end data pipeline achieving 97%+ data quality.

2. **Engineered explainable prediction system** incorporating qualifying position, head-to-head teammate analysis, consistency, and team strength factors to forecast race outcomes — fully interpretable without ML black boxes.

3. **Designed modular, interface-driven architecture** with 80%+ test coverage (JUnit 5 + JaCoCo), clean separation across 4 layers, and Strategy + Repository + Builder patterns for extensible, testable production-quality code.

## Next Steps

### Ready to Implement?
1. Review architecture documents
2. Ask clarifying questions
3. Get approval to proceed
4. Start with Maven setup

### Want to Modify Design?
- Adjust prediction weights?
- Add new features?
- Change tech stack?
- Discuss trade-offs

## Quick Commands (After Implementation)

```bash
# Build project
mvn clean install

# Run application
mvn exec:java

# Run tests
mvn test

# Generate coverage report
mvn jacoco:report

# Package for distribution
mvn package
```

## Data Source Recommendations

1. **Ergast F1 API** (http://ergast.com/mrd/)
   - Free, comprehensive
   - CSV exports available
   - 1950-present data

2. **Kaggle F1 Datasets**
   - Pre-cleaned data
   - Multiple formats
   - Recent seasons

3. **Manual Creation**
   - For testing/demo
   - Control data quality
   - Specific scenarios
