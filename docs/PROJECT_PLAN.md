# F1 Predictor - Project Plan

## Problem Statement
Build a portfolio-quality Java application that analyzes historical Formula One race data to produce insights and simple, explainable predictions about driver/team performance.

---

## Architecture Overview

### Layer Stack
```
┌─────────────────────────────────────────────────────────┐
│  PRESENTATION   │  MenuController + ReportGenerator     │
├─────────────────────────────────────────────────────────┤
│  SERVICE        │  AnalyticsService + PredictionService │
├─────────────────────────────────────────────────────────┤
│  DATA           │  DataLoader → DataCleaner → Repository│
├─────────────────────────────────────────────────────────┤
│  MODEL          │  RaceResult, Driver, Team, ...        │
└─────────────────────────────────────────────────────────┘
```

### Key Design Decisions
| Decision | Choice | Rationale |
|---|---|---|
| Storage | In-memory List + cache | Fast for <100K rows, simple |
| Predictions | Explainable weighted scoring | Interview-friendly, not a black box |
| Architecture | Layered + interfaces | Testable, SOLID principles |
| Build | Maven | Industry standard |
| Java Version | 17+ | Records, text blocks, modern APIs |

---

## Improved Prediction Model

The original 4-factor model is upgraded to 6 factors. **Qualifying position** is one of the strongest predictors in F1 (grid position correlates ~0.65 with finishing position) and is trivial to explain. **Head-to-Head** normalizes for car quality.

### Formula (v2)
```
Score = (RecentForm     × 0.35)   ← Last 5 races, decay-weighted
      + (AvgPoints      × 0.25)   ← Season points per race, normalized
      + (Consistency    × 0.15)   ← 1 / (1 + stddev of positions)
      + (TeamStrength   × 0.10)   ← Constructor championship standing
      + (QualifyingForm × 0.10)   ← Avg qualifying vs grid position
      + (HeadToHead     × 0.05)   ← Win rate vs current teammate
```

### Factor Definitions

**RecentForm (35%)**
- Take last 5 race finish positions, apply decay weights [0.30, 0.25, 0.20, 0.15, 0.10]
- DNF → treated as P20 (full penalty)
- Normalize: `score = (21 - position) / 20.0` → range 0–1

**AvgPoints (25%)**
- `avgPoints = totalPoints / racesStarted`
- Normalized: `score = min(avgPoints / 25.0, 1.0)` (25 pts = maximum score)

**Consistency (15%)**
- `score = 1 / (1 + stddev(finishPositions))`
- DNF positions encoded as 20 before stddev calculation
- Range: ~0 (chaotic) to 1 (perfectly consistent)

**TeamStrength (10%)**
- Team's points rank in the constructor standings from the loaded dataset
- `score = (numTeams - constructorRank) / (numTeams - 1)`
- P1 team → 1.0, last place → 0.0

**QualifyingForm (10%)**
- `qualifyingScore = avgFinishPosition - avgGridPosition`
- Positive = gains positions on average (good race pace vs qualifying)
- Normalize: `score = (score + 10) / 20.0` → clamped to 0–1
- Requires `grid` field in dataset; falls back to 0 if not available

**HeadToHead (5%)**
- Win rate against current teammate over shared races
- `score = driverWinsVsTeammate / sharedRaces`
- Falls back to 0.5 (neutral) if <5 shared races

### Confidence Level
Confidence is determined by data sufficiency, not just score magnitude:
```
Very High: score ≥ 8.5 AND recentRaces ≥ 5
High:      score ≥ 7.0 OR  recentRaces ≥ 5
Moderate:  score ≥ 5.5
Low:       score ≥ 4.0
Very Low:  score < 4.0 OR recentRaces < 3
```

---

## Domain Models

### Core Models
```
RaceResult     — one driver's result in one race
Driver         — aggregated stats across all races
Team           — constructor aggregated stats
SeasonStats    — per-season breakdown (nested in Driver/Team)
CircuitStats   — per-circuit performance per driver (NEW)
PredictionResult — scoring output with full breakdown
DataQualityIssue — individual data problem record
DataQualityReport — summary of all issues
SessionState   — runtime state (data loaded, file path, etc.)
```

### CircuitStats (New)
```java
public class CircuitStats {
    private String circuit;
    private String driverName;
    private int races;
    private int wins;
    private int podiums;
    private double avgPosition;
    private double avgPoints;
}
```

Enables: "Verstappen at Monaco: 3 races, 2 wins, avg P1.7"

---

## Technology Stack

| Component | Technology | Why |
|---|---|---|
| Build Tool | Maven | Industry standard |
| CSV Parsing | OpenCSV 5.x | Handles edge cases, widely used |
| Testing | JUnit 5 + Mockito | Modern, feature-rich |
| Logging | SLF4J + Logback | Professional logging standard |
| Java Version | 17+ | Records, streams, modern features |

### pom.xml Dependencies (Key)
```xml
<dependency>groupId: com.opencsv / artifactId: opencsv / version: 5.9</dependency>
<dependency>groupId: org.slf4j / artifactId: slf4j-api / version: 2.0.x</dependency>
<dependency>groupId: ch.qos.logback / artifactId: logback-classic / version: 1.5.x</dependency>
<dependency>groupId: org.junit.jupiter / artifactId: junit-jupiter / version: 5.10.x (test)</dependency>
<dependency>groupId: org.mockito / artifactId: mockito-core / version: 5.x (test)</dependency>
```

---

## Implementation Phases

### Phase 0 — Project Foundation
**Goal**: Working Maven project skeleton that compiles and runs.

**Tasks**:
- [ ] Create Maven project: `mvn archetype:generate -DarchetypeArtifactId=maven-archetype-quickstart`
- [ ] Configure `pom.xml`: Java 17, all dependencies, exec plugin, JaCoCo
- [ ] Create folder structure (see File Structure section)
- [ ] Add `.gitignore` (target/, *.class, .idea/, *.iml)
- [ ] Create `logback.xml` with console + file appenders
- [ ] Create placeholder `Main.java` that prints "F1 Predictor starting..."
- [ ] Verify: `mvn clean install` succeeds

**Done when**: `mvn exec:java` prints startup message, tests run (0 tests, 0 failures)

---

### Phase 1 — Domain Models
**Goal**: All model classes with builders, equals/hashCode, toString.

**Tasks**:
- [ ] `RaceResult.java` — all fields, builder, computed fields (positionsGained, onPodium, hasFastestLap)
- [ ] `Driver.java` — aggregated stats fields, builder
- [ ] `Team.java` — constructor stats, builder
- [ ] `SeasonStats.java` — yearly breakdown, builder
- [ ] `CircuitStats.java` — per-circuit per-driver stats, builder
- [ ] `PredictionResult.java` — 6-component score breakdown, predictedPosition, explanation
- [ ] `DataQualityIssue.java` — rowNumber, fieldName, issueType (enum: MISSING/INVALID/SUSPICIOUS), description
- [ ] `DataQualityReport.java` — totalRows, validRows, issues list, `getQualityScore()`
- [ ] `SessionState.java` — dataLoaded, currentFile, loadTime, resultCount

**Dependencies**: None (pure POJOs)

**Done when**: All models compile, `equals`/`hashCode` verified in unit tests

---

### Phase 2 — Data Pipeline
**Goal**: Load a CSV file, clean the data, store in memory — all with proper error handling.

**Tasks**:

**2a. DataLoader**
- [ ] `DataLoader.java` interface: `List<RaceResult> load(String filePath)`
- [ ] `CSVDataLoader.java` implementation using OpenCSV
- [ ] `parseRow(String[] row)` — field-by-field parsing
- [ ] Error handling: skip malformed rows, log warnings, continue
- [ ] Progress reporting: log every 100 rows processed

**2b. DataCleaner**
- [ ] `clean(List<RaceResult> raw)` — returns validated list + populates `DataQualityReport`
- [ ] `normalizeDriverName(String)` — trim, title-case, handle "Last, First" format
- [ ] `normalizeTeamName(String)` — alias map (Red Bull Racing → Red Bull, etc.)
- [ ] `parsePosition(String)` — numeric, DNF/DNS/DSQ → 0, "+N Lap" → actual position
- [ ] `validatePoints(int position, double points)` — warn on unexpected point totals
- [ ] Duplicate detection: flag exact (season + round + driver) duplicates

**2c. DataRepository**
- [ ] `DataRepository.java` interface — all query methods
- [ ] `InMemoryRepository.java` implementation
- [ ] Storage: `List<RaceResult> raceResults` (source of truth)
- [ ] Caches: `Map<String, Driver> driverCache`, `Map<String, Team> teamCache`
- [ ] Cache invalidation on `addAll()`
- [ ] Query methods: `getByDriver`, `getBySeason`, `getByTeam`, `getByCircuit`

**2d. Sample Data**
- [ ] Download from Kaggle (formula-1-race-data) or Ergast CSV export
- [ ] Place at `data/sample-f1-data.csv` (2021–2023 seasons recommended)
- [ ] Verify CSV headers match schema

**Dependencies**: Phase 1 complete

**Done when**: `DataLoaderTest` passes for valid CSV, malformed CSV, and empty file

---

### Phase 3 — Analytics Engine
**Goal**: Compute all statistics needed for analysis and prediction.

**Tasks**:

**3a. StatisticsUtil**
- [ ] `mean(List<Double>)`
- [ ] `stdDev(List<Double>)`
- [ ] `normalize(double value, double min, double max)` → 0–1
- [ ] `weightedAverage(List<Double> values, double[] weights)`
- [ ] `rank(List<T> items, Comparator<T>)` — returns ranked list

**3b. AnalyticsService**
- [ ] `getTopDriversByWins(int limit)`
- [ ] `getTopDriversByPoints(int limit)`
- [ ] `getTopDriversByConsistency(int limit)`
- [ ] `getDriverStats(String name)` — full Driver object
- [ ] `getDriverTrend(String name, int lastN)` — list of recent RaceResults
- [ ] `getCircuitStats(String name)` — list of CircuitStats for a driver
- [ ] `getTopTeamsBySeason(int season, int limit)`
- [ ] `getTeamStats(String teamName)` — full Team object
- [ ] `compareDrivers(String driver1, String driver2)` — side-by-side stats map
- [ ] Season filtering: overloads that accept `Optional<Integer> season`

**Dependencies**: Phase 2 complete

**Done when**: `AnalyticsServiceTest` verifies all aggregation calculations with known test data

---

### Phase 4 — Prediction System
**Goal**: Generate ranked predictions with full score breakdowns.

**Tasks**:

**4a. PredictionStrategy interface**
- [ ] `interface PredictionStrategy { List<PredictionResult> predict(int topN); }`
- [ ] Allows future swap-in of different algorithms

**4b. WeightedScoringStrategy**
- [ ] Implement the 6-factor formula (v2)
- [ ] `calculateRecentForm(Driver)`
- [ ] `calculateAvgPoints(Driver)`
- [ ] `calculateConsistency(Driver)`
- [ ] `calculateTeamStrength(String teamName, Map<String, Team> teams)`
- [ ] `calculateQualifyingForm(Driver, List<RaceResult>)` — graceful fallback if grid data missing
- [ ] `calculateHeadToHead(Driver, DataRepository)` — fallback to 0.5 if < 5 shared races
- [ ] Score normalization: all components clamped to [0, 1]
- [ ] Confidence level computation (data-sufficiency based, see above)

**4c. PredictionService**
- [ ] `predictTopFinishers(int topN)` → `List<PredictionResult>`
- [ ] `predictDriver(String name)` → `PredictionResult`
- [ ] `explainPrediction(String name)` → formatted multi-line explanation string
- [ ] Strategy injection (default: `WeightedScoringStrategy`)

**Dependencies**: Phase 3 complete

**Done when**: `PredictionServiceTest` verifies score calculations and ranking order

---

### Phase 5 — CLI Interface
**Goal**: A fully interactive, navigable menu system with formatted output.

**Tasks**:

**5a. ReportGenerator**
- [ ] `formatTable(String[] headers, List<String[]> rows)` — padded column table
- [ ] `formatProgressBar(int value, int max, int width)` — `■■■■■░░░░`
- [ ] `formatSeparator(int width)` — `═══════`
- [ ] `formatDriverProfile(Driver)` — full profile block
- [ ] `formatPredictionResult(PredictionResult)` — score breakdown with bars
- [ ] `formatSeasonReport(int season)` — season summary
- [ ] `exportToCSV(List<Driver> drivers, String path)` — CSV export

**5b. MenuController**
- [ ] `run()` — main loop with `SessionState`
- [ ] `displayMainMenu()` — 7 options
- [ ] `handleLoadData()` — file path input, progress, quality report
- [ ] `handleDriverAnalysis()` — 7-option sub-menu
- [ ] `handleTeamAnalysis()` — 5-option sub-menu
- [ ] `handlePredictions()` — 5-option sub-menu (uses v2 model)
- [ ] `handleReports()` — 6-option sub-menu
- [ ] `handleDataInfo()` — dataset summary
- [ ] Input helpers: `getValidatedInt`, `getValidatedDriverName` (fuzzy search), `getValidatedFilePath`
- [ ] Fuzzy driver search: if exact name not found, find closest match and confirm with user
- [ ] Guard: all analysis/prediction menus check `sessionState.isDataLoaded()` first

**Dependencies**: Phases 3 + 4 complete

**Done when**: Full end-to-end walkthrough works: load CSV → driver analysis → predictions → export

---

### Phase 6 — Testing
**Goal**: 80%+ coverage, all critical paths tested.

**Unit Tests**:
```
DataLoaderTest
  - testLoadValidCSV()
  - testHandleMalformedRow_skipsAndContinues()
  - testEmptyFile_returnsEmptyList()
  - testMissingFile_throwsException()

DataCleanerTest
  - testNormalizeDriverName_lastFirstFormat()
  - testNormalizeTeamName_aliasResolution()
  - testParsePosition_numericString()
  - testParsePosition_dnf()
  - testParsePosition_plusNLap()
  - testDuplicateDetection()
  - testMissingRequiredField_flagsIssue()

AnalyticsServiceTest
  - testTopDriversByWins_correctRanking()
  - testConsistencyCalculation_lowVarianceHighScore()
  - testRecentFormCalculation_decayWeights()
  - testCircuitStats_aggregatedCorrectly()
  - testCompareDrivers_returnsMapWithBothDrivers()

PredictionServiceTest
  - testScoreCalculation_allSixComponents()
  - testQualifyingFormFallback_whenGridMissing()
  - testHeadToHead_fallbackForFewSharedRaces()
  - testRanking_highestScoreFirst()
  - testConfidenceLevel_dataSufficiency()

StatisticsUtilTest
  - testMean()
  - testStdDev()
  - testWeightedAverage()
  - testNormalize()
```

**Integration Tests**:
```
EndToEndTest
  - testLoadCleanStoreQuery_fullPipeline()
  - testAnalyticsOnSampleData_nonEmptyResults()
  - testPredictionOnSampleData_topNReturned()
  - testDataQualityReport_accurateMetrics()
```

**Test Data**:
- `test-data-valid.csv` — 20 rows, known correct results
- `test-data-malformed.csv` — rows with DNF, missing fields, bad positions
- `test-data-empty.csv` — headers only

**Coverage**: Run `mvn jacoco:report`, target 80%+ on service + data layers

---

### Phase 7 — Documentation & Polish
**Goal**: Portfolio-ready presentation.

**Tasks**:
- [ ] Update `README.md` — project description, build/run instructions, architecture diagram, sample output screenshots (ASCII)
- [ ] JavaDoc on all public methods in service and data layers
- [ ] Update `QUICK_REFERENCE.md` with v2 prediction formula
- [ ] Add `CONTRIBUTING.md` (optional, shows professionalism)
- [ ] Verify `mvn clean install` runs green
- [ ] Verify `mvn jacoco:report` shows 80%+
- [ ] Record 2-3 resume bullets (see QUICK_REFERENCE.md)

---

## File Structure (Final)

```
f1-predictor/
├── pom.xml
├── README.md
├── data/
│   └── sample-f1-data.csv         ← 2021-2023 F1 results
├── src/
│   ├── main/
│   │   ├── java/com/f1predictor/
│   │   │   ├── Main.java
│   │   │   ├── model/
│   │   │   │   ├── RaceResult.java
│   │   │   │   ├── Driver.java
│   │   │   │   ├── Team.java
│   │   │   │   ├── SeasonStats.java
│   │   │   │   ├── CircuitStats.java      ← NEW
│   │   │   │   ├── PredictionResult.java
│   │   │   │   ├── DataQualityIssue.java
│   │   │   │   └── DataQualityReport.java
│   │   │   ├── data/
│   │   │   │   ├── DataLoader.java        ← interface
│   │   │   │   ├── CSVDataLoader.java     ← implementation
│   │   │   │   ├── DataCleaner.java
│   │   │   │   ├── DataRepository.java    ← interface
│   │   │   │   └── InMemoryRepository.java ← implementation
│   │   │   ├── service/
│   │   │   │   ├── AnalyticsService.java
│   │   │   │   ├── PredictionService.java
│   │   │   │   ├── PredictionStrategy.java ← interface
│   │   │   │   ├── WeightedScoringStrategy.java ← NEW
│   │   │   │   └── ReportGenerator.java
│   │   │   ├── controller/
│   │   │   │   ├── MenuController.java
│   │   │   │   └── SessionState.java      ← NEW
│   │   │   └── util/
│   │   │       └── StatisticsUtil.java
│   │   └── resources/
│   │       └── logback.xml
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
    ├── PROJECT_PLAN.md            ← this file
    ├── ARCHITECTURE.md
    ├── DATA_SCHEMA.md
    ├── CLI_FLOW.md
    └── QUICK_REFERENCE.md
```

---

## Deliverables Checklist

| # | Deliverable | Phase | Status |
|---|---|---|---|
| 1 | Architecture design | Design | ✅ Done |
| 2 | Data schema | Design | ✅ Done |
| 3 | CLI flow design | Design | ✅ Done |
| 4 | Maven project structure | 0 | ⬜ |
| 5 | Domain models (7 classes) | 1 | ⬜ |
| 6 | DataLoader + CSVDataLoader | 2 | ⬜ |
| 7 | DataCleaner | 2 | ⬜ |
| 8 | DataRepository + InMemoryRepository | 2 | ⬜ |
| 9 | Sample dataset (CSV) | 2 | ⬜ |
| 10 | StatisticsUtil | 3 | ⬜ |
| 11 | AnalyticsService (all methods) | 3 | ⬜ |
| 12 | PredictionStrategy interface | 4 | ⬜ |
| 13 | WeightedScoringStrategy (6-factor) | 4 | ⬜ |
| 14 | PredictionService | 4 | ⬜ |
| 15 | ReportGenerator | 5 | ⬜ |
| 16 | MenuController (all menus) | 5 | ⬜ |
| 17 | SessionState | 5 | ⬜ |
| 18 | Unit tests (all classes) | 6 | ⬜ |
| 19 | Integration tests | 6 | ⬜ |
| 20 | 80%+ JaCoCo coverage | 6 | ⬜ |
| 21 | Updated README + JavaDocs | 7 | ⬜ |

---

## Data Source

**Recommended**: Kaggle — "Formula 1 World Championship (1950-2023)"
- URL: search "formula 1 race results kaggle"
- Use `results.csv` + `races.csv` + `drivers.csv` + `constructors.csv`
- Filter to 2021–2023 for manageable demo dataset

**Alternative**: Ergast API CSV exports at `ergast.com/mrd/`

**Column mapping from Kaggle**:
| Kaggle field | Our schema field |
|---|---|
| year | season |
| round | round |
| name (from races.csv) | circuit |
| date (from races.csv) | date |
| driverRef / forename+surname | driver |
| constructorRef | constructor |
| grid | grid |
| positionOrder | position |
| points | points |
| laps | laps |
| statusId → status name | status |
| fastestLapTime | fastestLap |

---

## Success Criteria

- [ ] `mvn clean install` passes with 0 errors
- [ ] `mvn exec:java` launches interactive CLI
- [ ] Can load sample CSV and see data quality report
- [ ] Driver analysis shows ranked tables with correct stats
- [ ] Prediction shows 6-component score breakdown
- [ ] `mvn jacoco:report` shows ≥80% line coverage
- [ ] Code is clean: no warnings, proper logging, no magic numbers
- [ ] Prediction methodology is fully explainable in 2 minutes
