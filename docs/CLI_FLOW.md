# F1 Predictor - CLI Flow Design

## Overview
The CLI provides an interactive, menu-driven interface for users to load data, perform analysis, and generate predictions without any coding.

## Main Menu Structure

```
╔════════════════════════════════════════════╗
║         F1 PREDICTOR v1.0                 ║
║    Formula One Performance Analytics      ║
╚════════════════════════════════════════════╝

Main Menu:
-----------
1. Load Data
2. Driver Analysis
3. Team Analysis
4. Predict Performance
5. Generate Reports
6. Data Info
7. Exit

Enter your choice (1-7): _
```

## Menu Flow Diagram

```
                    ┌─────────────┐
                    │  Main Menu  │
                    └──────┬──────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
        ▼                  ▼                  ▼
┌───────────────┐  ┌───────────────┐  ┌───────────────┐
│  Load Data    │  │    Analysis   │  │  Predictions  │
└───────┬───────┘  └───────┬───────┘  └───────┬───────┘
        │                  │                  │
        ▼                  ▼                  ▼
   ┌─────────┐      ┌─────────────┐    ┌──────────────┐
   │ CSV File│      │Driver/Team  │    │ Top Finishers│
   └─────────┘      │  Sub-Menus  │    └──────────────┘
                    └─────────────┘
```

## Detailed Menu Flows

### 1. Load Data Flow

```
[Main Menu] → [1] Load Data

╔════════════════════════════════════════════╗
║           LOAD DATA                        ║
╚════════════════════════════════════════════╝

Options:
--------
1. Load from file
2. Load sample data
3. Back to main menu

Enter choice: 1

Enter CSV file path (or drag & drop file): 
> /path/to/f1-data.csv

[Processing...]
✓ Reading CSV file...
✓ Parsing 1,234 rows...
✓ Cleaning data...
✓ Validating entries...

Data Quality Report:
--------------------
Total rows:        1,234
Valid rows:        1,198 (97.1%)
Invalid rows:      36 (2.9%)
Unique drivers:    28
Unique teams:      10
Seasons covered:   2021-2023
Total races:       66

Load successful!

Press Enter to continue...
```

**Error Handling**:
```
✗ File not found: /path/to/missing.csv
  Please check the path and try again.

✗ Invalid CSV format on row 45
  Skipping and continuing...

✗ No valid data found in file
  Please check file format.
```

### 2. Driver Analysis Flow

```
[Main Menu] → [2] Driver Analysis

╔════════════════════════════════════════════╗
║         DRIVER ANALYSIS                    ║
╚════════════════════════════════════════════╝

1. Top Drivers by Wins
2. Top Drivers by Points
3. Top Drivers by Podiums
4. Driver Consistency Rankings
5. Individual Driver Stats
6. Driver Performance Trend
7. Back to main menu

Enter choice: 1

How many drivers to display? (1-20): 10

╔════════════════════════════════════════════════════════════╗
║              TOP 10 DRIVERS BY WINS                        ║
╚════════════════════════════════════════════════════════════╝

Rank  Driver              Team           Wins  Races  Win %
----  ------              ----           ----  -----  -----
  1   Max Verstappen      Red Bull        19     22   86.4%
  2   Lewis Hamilton      Mercedes         7     22   31.8%
  3   Charles Leclerc     Ferrari          4     22   18.2%
  4   Sergio Perez        Red Bull         2     22    9.1%
  5   George Russell      Mercedes         1     22    4.5%
  6   Carlos Sainz        Ferrari          1     22    4.5%
...

Options:
--------
1. View detailed stats for a driver
2. Export to CSV
3. Back to analysis menu

Enter choice: _
```

### 3. Individual Driver Stats Flow

```
[Driver Analysis] → [5] Individual Driver Stats

Enter driver name (or part of name): verstappen

Found: Max Verstappen

╔════════════════════════════════════════════════════════════╗
║        DRIVER PROFILE: Max Verstappen                      ║
╚════════════════════════════════════════════════════════════╝

Current Team:      Red Bull
Total Races:       22
Career Points:     575

Performance Summary:
--------------------
Wins:              19 (86.4%)
Podiums:           21 (95.5%)
Points Finishes:   22 (100%)
DNFs:              0 (0%)

Statistics:
-----------
Average Points:    26.1 per race
Average Position:  1.3
Consistency Score: 0.95 (Excellent)
Best Finish:       1st (19 times)
Worst Finish:      7th

Recent Form (Last 5 Races):
---------------------------
Race 22: P1 (25 pts) ■■■■■■■■■■
Race 21: P1 (25 pts) ■■■■■■■■■■
Race 20: P1 (25 pts) ■■■■■■■■■■
Race 19: P2 (18 pts) ■■■■■■■■
Race 18: P1 (25 pts) ■■■■■■■■■■

Season Breakdown:
-----------------
2023: 19 wins, 575 pts (22 races)
2022: ... (if available)

Options:
--------
1. View race-by-race details
2. Compare with another driver
3. Back to analysis menu

Enter choice: _
```

### 4. Team Analysis Flow

```
[Main Menu] → [3] Team Analysis

╔════════════════════════════════════════════╗
║          TEAM ANALYSIS                     ║
╚════════════════════════════════════════════╝

1. Top Teams by Points
2. Top Teams by Wins
3. Team Consistency Rankings
4. Individual Team Stats
5. Team Comparison
6. Back to main menu

Enter choice: 1

╔════════════════════════════════════════════════════════════╗
║            TOP TEAMS BY POINTS (2023)                      ║
╚════════════════════════════════════════════════════════════╝

Rank  Team         Drivers              Points  Wins  Podiums
----  ----         -------              ------  ----  -------
  1   Red Bull     Verstappen, Perez      860    21      33
  2   Mercedes     Hamilton, Russell      409     8      18
  3   Ferrari      Leclerc, Sainz        398     5      17
  4   McLaren      Norris, Piastri       214     0       5
  5   Alpine       Ocon, Gasly           176     0       2
...

Press Enter to continue...
```

### 5. Predict Performance Flow

```
[Main Menu] → [4] Predict Performance

╔════════════════════════════════════════════╗
║       PERFORMANCE PREDICTION               ║
╚════════════════════════════════════════════╝

1. Predict Top 3 Finishers
2. Predict Top 10 Finishers
3. Predict Individual Driver
4. Explain Prediction Method
5. Back to main menu

Enter choice: 1

╔════════════════════════════════════════════════════════════╗
║         PREDICTED TOP 3 FINISHERS                          ║
║         Based on Recent Performance & Statistics           ║
╚════════════════════════════════════════════════════════════╝

Prediction Score Breakdown:
---------------------------
• Recent Form (40%): Last 5 race results weighted
• Avg Points (30%): Season points per race
• Consistency (20%): Variance in finishing positions
• Team Strength (10%): Constructor championship standing

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  1ST PLACE PREDICTION: Max Verstappen (Red Bull)
  ──────────────────────────────────────────────────
  Total Score: 9.2 / 10.0
  
  Score Breakdown:
  • Recent Form:     3.9 / 4.0  ■■■■■■■■■■ (5 wins in last 5)
  • Avg Points:      3.0 / 3.0  ■■■■■■■■■■ (26.1 pts/race)
  • Consistency:     2.0 / 2.0  ■■■■■■■■■■ (0.95 score)
  • Team Strength:   1.0 / 1.0  ■■■■■■■■■■ (P1 in constructors)
  
  Confidence: Very High ⭐⭐⭐⭐⭐

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  2ND PLACE PREDICTION: Sergio Perez (Red Bull)
  ──────────────────────────────────────────────────
  Total Score: 7.4 / 10.0
  
  Score Breakdown:
  • Recent Form:     2.8 / 4.0  ■■■■■■■░░░ (Mixed: P2,P4,P3,P2,P5)
  • Avg Points:      1.9 / 3.0  ■■■■■■░░░░ (13.0 pts/race)
  • Consistency:     1.6 / 2.0  ■■■■■■■■░░ (0.78 score)
  • Team Strength:   1.0 / 1.0  ■■■■■■■■■■ (P1 in constructors)
  
  Confidence: High ⭐⭐⭐⭐

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  3RD PLACE PREDICTION: Charles Leclerc (Ferrari)
  ──────────────────────────────────────────────────
  Total Score: 6.9 / 10.0
  
  Score Breakdown:
  • Recent Form:     2.6 / 4.0  ■■■■■■░░░░ (P3,P2,P4,P3,P6)
  • Avg Points:      1.8 / 3.0  ■■■■■■░░░░ (12.2 pts/race)
  • Consistency:     1.5 / 2.0  ■■■■■■■░░░ (0.72 score)
  • Team Strength:   0.7 / 1.0  ■■■■■■■░░░ (P3 in constructors)
  
  Confidence: Moderate ⭐⭐⭐

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

DISCLAIMER: Predictions are statistical estimates based on
historical performance. Actual race results depend on many
factors including weather, car setup, strategy, and incidents.

Options:
--------
1. View next 5 predictions
2. Get detailed explanation for a driver
3. Export predictions to CSV
4. Back to main menu

Enter choice: _
```

### 6. Explain Prediction Method Flow

```
[Predict Performance] → [4] Explain Prediction Method

╔════════════════════════════════════════════════════════════╗
║          PREDICTION METHODOLOGY                            ║
╚════════════════════════════════════════════════════════════╝

Our prediction system uses a transparent, explainable scoring
model based on four key factors:

1. RECENT FORM (40% weight)
   ─────────────────────────
   What: Weighted average of last 5 race finishing positions
   Why:  Recent performance is the best indicator of current form
   How:  More recent races weighted more heavily
         - Race N:   30%
         - Race N-1: 25%
         - Race N-2: 20%
         - Race N-3: 15%
         - Race N-4: 10%
   
   Scoring: Position converted to 0-1 scale (P1=1.0, P20=0.05)
   DNFs:    Treated as P20 for consistency

2. AVERAGE POINTS (30% weight)
   ────────────────────────────
   What: Season points per race average
   Why:  Consistent point scoring shows sustained performance
   How:  Total points / races completed
   
   Scoring: Normalized to 0-1 scale (25+ pts = 1.0)

3. CONSISTENCY (20% weight)
   ──────────────────────────
   What: Low variance in finishing positions
   Why:  Reliable performers are more predictable
   How:  1 / (1 + standard deviation of positions)
   
   Scoring: 0 = highly inconsistent, 1 = perfectly consistent

4. TEAM STRENGTH (10% weight)
   ────────────────────────────
   What: Constructor championship standing
   Why:  Better cars enable better performance
   How:  Based on team's total points ranking
   
   Scoring: P1 team = 1.0, scales down by position

FINAL SCORE CALCULATION:
------------------------
Score = (RecentForm × 0.4) + (AvgPoints × 0.3) + 
        (Consistency × 0.2) + (TeamStrength × 0.1)

Range: 0.0 (lowest) to 10.0 (perfect score)

CONFIDENCE LEVELS:
------------------
⭐⭐⭐⭐⭐ Very High (9.0+)  - Strong favorite
⭐⭐⭐⭐   High (7.5-8.9)   - Likely contender
⭐⭐⭐     Moderate (6.0-7.4) - Possible finisher
⭐⭐       Low (4.0-5.9)    - Outside chance
⭐         Very Low (<4.0)  - Unlikely

LIMITATIONS:
------------
• Does not account for weather conditions
• Cannot predict crashes or mechanical failures
• Assumes normal race circumstances
• No circuit-specific adjustments
• Based solely on historical data

Press Enter to continue...
```

### 7. Generate Reports Flow

```
[Main Menu] → [5] Generate Reports

╔════════════════════════════════════════════╗
║         GENERATE REPORTS                   ║
╚════════════════════════════════════════════╝

1. Full Season Summary Report
2. Driver Performance Report
3. Team Performance Report
4. Prediction Report
5. Export All Data to CSV
6. Back to main menu

Enter choice: 1

Generating Season Summary Report...

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
      F1 PREDICTOR - SEASON REPORT
      2023 Formula One World Championship
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

SEASON OVERVIEW
───────────────
Total Races:       22
Drivers:           20
Teams:             10
Total Points:      2,586

TOP 3 DRIVERS
─────────────
1. Max Verstappen  - 575 pts (19 wins)
2. Sergio Perez    - 285 pts (2 wins)
3. Lewis Hamilton  - 234 pts (7 wins)

TOP 3 TEAMS
───────────
1. Red Bull        - 860 pts (21 wins)
2. Mercedes        - 409 pts (8 wins)
3. Ferrari         - 398 pts (5 wins)

STATISTICS
──────────
Most Wins:         Max Verstappen (19)
Most Podiums:      Max Verstappen (21)
Most Consistent:   Max Verstappen (0.95)
Least DNFs:        Max Verstappen (0)

RACE DISTRIBUTION
─────────────────
Red Bull wins:     95% dominance
Mercedes wins:     36% of remaining
Ferrari wins:      23% of remaining

Report saved to: f1-report-2023.txt

Options:
--------
1. View detailed breakdown
2. Export to PDF (if available)
3. Back to reports menu

Enter choice: _
```

### 8. Data Info Flow

```
[Main Menu] → [6] Data Info

╔════════════════════════════════════════════════════════════╗
║              CURRENT DATASET INFO                          ║
╚════════════════════════════════════════════════════════════╝

Data Source:       /Users/you/f1-data/results.csv
Load Date:         2024-04-08 02:03:39
Data Quality:      97.1% (Excellent)

DATASET SUMMARY
───────────────
Total Race Results:    1,198
Invalid Entries:       36 (2.9%)

Seasons:               2021, 2022, 2023
Total Races:           66 races
Unique Drivers:        28 drivers
Unique Teams:          10 teams

COVERAGE BY SEASON
──────────────────
2021: 22 races, 381 results
2022: 22 races, 402 results  
2023: 22 races, 415 results

DATA QUALITY ISSUES
───────────────────
Missing lap times:     12 entries
Invalid positions:     8 entries
Duplicate entries:     6 entries
Missing points:        10 entries

CACHE STATUS
────────────
Analytics Cache:       Valid
Prediction Cache:      Valid
Last Updated:          2024-04-08 02:03:39

Press Enter to continue...
```

## Input Validation

### Common Patterns

```java
// Menu choice validation
int choice = getValidatedInt(scanner, 1, 7);

// Driver name validation
String driver = getValidatedDriverName(scanner);

// File path validation
Path csvPath = getValidatedFilePath(scanner);

// Yes/No confirmation
boolean confirm = getConfirmation(scanner, "Export data?");
```

### Error Messages

```
✗ Invalid choice. Please enter a number between 1 and 7.

✗ Driver not found: "verstapn"
  Did you mean: Max Verstappen?

✗ No data loaded. Please load data first (Option 1).

✗ Insufficient data for prediction. Need at least 5 races.
```

## User Experience Features

### 1. Progress Indicators
```
Loading data [■■■■■■■■■░] 90% (1,080/1,200 rows)
```

### 2. Color Coding (if terminal supports)
- **Green**: Success messages
- **Yellow**: Warnings
- **Red**: Errors
- **Blue**: Headers

### 3. Clear Navigation
```
Current: Main Menu > Driver Analysis > Individual Stats
Press 'b' for back, 'h' for home, 'q' to quit
```

### 4. Help Text
```
? - Show help
h - Return to main menu
b - Go back
q - Quit application
```

### 5. Confirmation for Destructive Actions
```
Are you sure you want to exit? Unsaved data will be lost.
(y/n): _
```

## Session State

### What to Track
```java
class SessionState {
    private boolean dataLoaded;
    private String currentDataFile;
    private LocalDateTime loadTime;
    private int totalResults;
    private String currentMenu;
}
```

### State Checks
```java
if (!sessionState.isDataLoaded()) {
    System.out.println("⚠ No data loaded. Please load data first.");
    return;
}
```

## CLI Implementation Checklist

- [ ] Main menu loop
- [ ] Input validation for all menus
- [ ] Error message display
- [ ] Success confirmation messages
- [ ] Progress indicators for long operations
- [ ] Breadcrumb navigation
- [ ] Help system
- [ ] Graceful exit handling
- [ ] Session state management
- [ ] Clear screen between menus (optional)
- [ ] Formatted table output
- [ ] Export functionality
- [ ] Data validation before operations
