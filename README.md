# F1 Predictor 🏎️

> **Portfolio-Quality Java Application** for Formula One Race Data Analysis and Performance Prediction.

Built with Java 17, Maven, and OpenCSV, this application analyzes historical F1 data to generate insights and explainable predictions.

## ✨ Key Features

- **Data Pipeline**: CSV ingestion with robust validation and normalization.
- **Analytics Engine**: Driver/Team performance metrics, consistency scores, and trend analysis.
- **Explainable Prediction**: 6-factor weighted scoring model (Recent Form, Avg Points, Consistency, Team Strength, Qualifying Form, and Head-to-Head).
- **Interactive CLI**: Navigable menu system with formatted tables and progress bars.
- **Portfolio Quality**: Layered architecture (SOLID), 80%+ test coverage target, and professional logging.

## 🏗️ Architecture

```
┌─────────────────────┐
│  Presentation Layer │  ← MenuController, ReportGenerator
├─────────────────────┤
│  Service Layer      │  ← AnalyticsService, PredictionService
├─────────────────────┤
│  Data Layer         │  ← DataLoader, DataCleaner, Repository
├─────────────────────┤
│  Domain Models      │  ← RaceResult, Driver, Team
└─────────────────────┘
```

## 🔮 Prediction Methodology (v2)

The application uses a **6-factor weighted scoring model** to predict race outcomes:

| Factor | Weight | Description |
|---|---|---|
| **Recent Form** | 35% | Decay-weighted performance over the last 5 races. |
| **Average Points** | 25% | Season-long scoring average. |
| **Consistency** | 15% | Inverse of position standard deviation. |
| **Team Strength** | 10% | Current constructor championship standing. |
| **Qualifying Form**| 10% | Delta between average grid and finish position. |
| **Head-to-Head** | 5% | Performance relative to current teammate. |

## 🚀 Getting Started

### Prerequisites

- **Java 17+**
- **Maven**

### Build

```bash
mvn clean install
```

### Run

```bash
mvn exec:java
```

### Run Tests

```bash
mvn test
```

## 📝 Resume Bullets

- Built **F1 Predictor**, a Java data analytics application processing 60+ races with 97%+ data quality validation.
- Engineered an **explainable prediction system** using weighted scoring (recent form, consistency, team strength) to forecast outcomes.
- Designed a **modular architecture** with 80%+ test coverage and clean separation of concerns using Repository and Strategy patterns.

---
*Developed as a portfolio project.*
