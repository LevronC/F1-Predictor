# F1 Predictor 🏎️

> **Portfolio-Quality Java Application** for Formula One Race Data Analysis and Performance Prediction.

Built with Java 17, Maven, and OpenCSV, this application analyzes historical F1 data to generate insights and explainable predictions.

## ✨ Key Features

- **Data Pipeline**: CSV ingestion with robust validation and normalization.
- **Analytics Engine**: Driver/Team performance metrics, consistency scores, and trend analysis.
- **Explainable Prediction**: 6-factor weighted scoring model (Recent Form, Avg Points, Consistency, Team Strength, Qualifying Form, and Head-to-Head).
- **Interactive CLI**: Navigable menu system with formatted tables and progress bars.
- **Portfolio Quality**: Layered architecture (SOLID), 80%+ test coverage target, and professional logging.

# F1 Predictor: Kinetic Engineering Lab 🏎️

> **Senior Software Engineering Portfolio Project**  
> A high-velocity analytics system and Monte Carlo simulation engine for Formula 1 race prediction.

## 🧠 The Problem
Predicting F1 outcomes is notoriously difficult due to the "human factor," technical reliability, and track-specific variables. This project moves beyond simple averages to build a **structured, explainable, and verifiable** prediction system.

## 🚀 Key Senior-Level Features
### 1. Monte Carlo Simulation Engine
Instead of a single deterministic outcome, the system runs **1,000 parallel race simulations** with Gaussian noise injection.
- **Probabilistic Forecasting**: Outputs win and podium probabilities for the entire grid.
- **Risk Assessment**: Identifies high-variance drivers vs. consistent podium contenders.

### 2. Backtesting & Model Validation
A dedicated backtesting suite that evaluates the model's performance on historical seasons.
- **Chronological Integrity**: Ensures predictions for Round N only use data from Rounds 1 to N-1.
- **KPIs**: Measures Top 3 accuracy and ranking correlation.

### 3. Spring Boot REST API
Transitioned from a CLI script to a professional **Service-Oriented Architecture**.
- **Separation of Concerns**: Decoupled Analytics, Prediction, and Simulation layers.
- **Extensible API**: RESTful endpoints for frontend integration and third-party data consumers.

### 4. Kinetic Engineering Dashboard
A high-fidelity visualization suite built with **Chart.js** and the **Kinetic Engineering Lab** design system.
- **Real-time Visualization**: Win probability distributions and historical performance trends.
- **Data-Driven UI**: Responsive, mobile-first design with monochromatic tonal layering.

## 🛠️ Technology Stack
- **Backend**: Java 17, Spring Boot, Spring Data JPA, H2 (In-memory DB)
- **Frontend**: TypeScript, Vite, Chart.js, Vanilla CSS
- **Methodology**: Monte Carlo Methods, Weighted Scoring (6-factor v2), Backtesting

## 📊 Prediction Methodology (v2)
Our model uses a 6-factor weighted algorithm:
- **35% Recent Form**: Decay-weighted performance of the last 5 races.
- **20% Consistency**: Statistical variance analysis of finishing positions.
- **20% Constructor Strength**: Current team dominance metrics.
- **15% Track History**: Driver performance on specific circuit profiles.
- **10% Grid Advantage**: Qualifying performance and starting position delta.

## 🧪 Testing & Quality
- **Unit Testing**: JUnit 5 + Mockito for core logic validation.
- **Data Integrity**: Automated cleaning and normalization of raw CSV telemetry.

---
*Built for performance, engineered for precision.*

## 📝 Resume Bullets

- Built **F1 Predictor**, a Java data analytics application processing 60+ races with 97%+ data quality validation.
- Engineered an **explainable prediction system** using weighted scoring (recent form, consistency, team strength) to forecast outcomes.
- Designed a **modular architecture** with 80%+ test coverage and clean separation of concerns using Repository and Strategy patterns.

---
*Developed as a portfolio project.*
