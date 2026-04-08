# F1 Predictor

> **Portfolio-Quality Java Application** for Formula One Race Data Analysis and Performance Prediction

## 🏎️ Project Status

**Phase**: Design Complete ✅ → Implementation Ready

## 📚 Documentation

All design documents are in the `docs/` folder:

- **[PROJECT_PLAN.md](docs/PROJECT_PLAN.md)** - Project overview
- **[ARCHITECTURE.md](docs/ARCHITECTURE.md)** - System architecture  
- **[DATA_SCHEMA.md](docs/DATA_SCHEMA.md)** - Data models & CSV format
- **[CLI_FLOW.md](docs/CLI_FLOW.md)** - UI design
- **[QUICK_REFERENCE.md](docs/QUICK_REFERENCE.md)** - Implementation guide

## 🎯 What This Project Does

- Analyzes historical Formula One race data
- Generates driver/team performance insights
- Predicts race outcomes using explainable statistics
- Demonstrates professional software engineering

## ✨ Key Features (Planned)

1. CSV data ingestion with error handling
2. Data cleaning & quality reporting
3. Driver/team analytics engine
4. Explainable prediction system
5. Interactive CLI interface
6. 80%+ test coverage with JUnit 5

## 🔮 Prediction Formula

```
Score = RecentForm(40%) + AvgPoints(30%) + Consistency(20%) + TeamStrength(10%)
```

**Why?** Transparent, explainable, interview-friendly!

## 🛠️ Tech Stack

- **Java 17+** - Modern language features
- **Maven** - Dependency management
- **OpenCSV** - CSV parsing
- **JUnit 5** - Testing framework
- **SLF4J + Logback** - Logging

## 🏗️ Architecture

```
┌─────────────────────┐
│  CLI Interface      │  ← MenuController
├─────────────────────┤
│  Service Layer      │  ← Analytics, Prediction
├─────────────────────┤
│  Data Layer         │  ← Loader, Cleaner
├─────────────────────┤
│  Domain Models      │  ← RaceResult, Driver
└─────────────────────┘
```

See [ARCHITECTURE.md](docs/ARCHITECTURE.md) for details.

## 🎓 Portfolio Value

Demonstrates:
- Data pipeline engineering
- Statistical analysis
- Predictive modeling
- Clean architecture
- Testing practices

**Perfect for**: Data analysis & software engineering roles

## 📝 Resume Bullets (Draft)

1. Built F1 Predictor, a Java data analytics application processing 60+ races with 97%+ data quality validation

2. Engineered explainable prediction system using weighted scoring (recent form, consistency, team strength)

3. Designed modular architecture with 80%+ test coverage and clean separation of concerns

## 🚀 Next Steps

1. ✅ Design complete
2. ⬜ Maven setup
3. ⬜ Domain models
4. ⬜ Data pipeline
5. ⬜ Analytics engine
6. ⬜ Prediction system
7. ⬜ CLI interface
8. ⬜ Tests

---

**Ready to start implementation!** 🏎️💨
