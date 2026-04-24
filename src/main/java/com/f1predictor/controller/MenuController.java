package com.f1predictor.controller;

import com.f1predictor.data.*;
import com.f1predictor.model.*;
import com.f1predictor.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Controller for the CLI menu system.
 */
public class MenuController {
    private static final Logger logger = LoggerFactory.getLogger(MenuController.class);
    private final Scanner scanner = new Scanner(System.in);
    
    private final SessionState sessionState = new SessionState();
    private final DataLoader dataLoader = new CSVDataLoader();
    private final DataCleaner dataCleaner = new DataCleaner();
    private final DataRepository repository = new InMemoryRepository();
    private final AnalyticsService analyticsService = new AnalyticsService(repository);
    private final PredictionService predictionService = new PredictionService(repository);
    private final ReportGenerator reportGenerator = new ReportGenerator();

    public void run() {
        boolean running = true;
        while (running) {
            displayMainMenu();
            int choice = getValidatedInt("Select an option: ", 0, 6);
            switch (choice) {
                case 1 -> handleLoadData();
                case 2 -> handleDriverAnalysis();
                case 3 -> handleTeamAnalysis();
                case 4 -> handlePredictions();
                case 5 -> handleDataInfo();
                case 6 -> {
                    System.out.println("Exporting data quality report...");
                    // Placeholder for export
                }
                case 0 -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
            }
        }
    }

    private void displayMainMenu() {
        System.out.println("\n=== F1 PREDICTOR MAIN MENU ===");
        System.out.println("1. Load Race Data (CSV)");
        System.out.println("2. Driver Analysis");
        System.out.println("3. Team Analysis");
        System.out.println("4. Generate Predictions");
        System.out.println("5. Data Quality Info");
        System.out.println("6. Export Reports");
        System.out.println("0. Exit");
    }

    private void handleLoadData() {
        System.out.print("Enter CSV file path [default: data/sample-f1-data.csv]: ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) path = "data/sample-f1-data.csv";

        try {
            long start = System.currentTimeMillis();
            List<RaceResult> raw = dataLoader.load(path);
            List<RaceResult> cleaned = new ArrayList<>();
            DataQualityReport report = dataCleaner.clean(raw, cleaned);
            
            repository.addAll(cleaned);
            
            sessionState.setDataLoaded(true);
            sessionState.setCurrentFile(path);
            sessionState.setLoadTime(System.currentTimeMillis() - start);
            sessionState.setCurrentData(cleaned);

            System.out.println("\n--- Data Load Complete ---");
            System.out.println("Loaded: " + cleaned.size() + " results");
            System.out.println("Quality Score: " + String.format("%.2f%% (%s)", report.getQualityScore() * 100, report.getQualityLabel()));
            System.out.println("Time: " + sessionState.getLoadTime() + "ms");
        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }

    private void handleDriverAnalysis() {
        if (!checkDataLoaded()) return;

        System.out.println("\n--- DRIVER ANALYSIS ---");
        System.out.println("1. Top Drivers by Wins");
        System.out.println("2. Top Drivers by Points");
        System.out.println("3. Top Drivers by Consistency");
        System.out.println("4. Search Specific Driver");
        System.out.println("0. Back");

        int choice = getValidatedInt("Option: ", 0, 4);
        switch (choice) {
            case 1 -> {
                List<Driver> top = analyticsService.getTopDriversByWins(10);
                String[] headers = {"Rank", "Driver", "Wins", "Avg Pos"};
                List<String[]> rows = new ArrayList<>();
                for (int i = 0; i < top.size(); i++) {
                    Driver d = top.get(i);
                    rows.add(new String[]{String.valueOf(i + 1), d.getName(), String.valueOf(d.getWins()), String.format("%.1f", d.getAvgPosition())});
                }
                reportGenerator.formatTable(headers, rows);
            }
            case 2 -> {
                List<Driver> top = analyticsService.getTopDriversByPoints(10);
                String[] headers = {"Rank", "Driver", "Points", "Avg Pts"};
                List<String[]> rows = new ArrayList<>();
                for (int i = 0; i < top.size(); i++) {
                    Driver d = top.get(i);
                    rows.add(new String[]{String.valueOf(i + 1), d.getName(), String.format("%.1f", d.getTotalPoints()), String.format("%.2f", d.getAvgPoints())});
                }
                reportGenerator.formatTable(headers, rows);
            }
            case 4 -> {
                System.out.print("Enter driver name: ");
                String name = scanner.nextLine();
                Driver d = repository.getDriver(name);
                if (d == null) {
                    System.out.println("Driver not found. Try 'Max Verstappen' or 'Lewis Hamilton'.");
                } else {
                    reportGenerator.formatDriverProfile(d);
                }
            }
        }
    }

    private void handleTeamAnalysis() {
        if (!checkDataLoaded()) return;
        System.out.println("Team analysis coming soon...");
    }

    private void handlePredictions() {
        if (!checkDataLoaded()) return;

        System.out.println("\n--- GENERATE PREDICTIONS ---");
        System.out.println("1. Predict Top 10 Finishers");
        System.out.println("2. Predict Specific Driver");
        System.out.println("0. Back");

        int choice = getValidatedInt("Option: ", 0, 2);
        if (choice == 1) {
            List<PredictionResult> top = predictionService.predictTopFinishers(10);
            System.out.println("\n=== PREDICTED TOP 10 FINISHERS ===");
            String[] headers = {"Rank", "Driver", "Team", "Score", "Confidence"};
            List<String[]> rows = new ArrayList<>();
            for (int i = 0; i < top.size(); i++) {
                PredictionResult r = top.get(i);
                rows.add(new String[]{String.valueOf(i + 1), r.getDriverName(), r.getTeamName(), String.format("%.2f", r.getTotalScore()), r.getConfidenceLevel()});
            }
            reportGenerator.formatTable(headers, rows);
        } else if (choice == 2) {
            System.out.print("Enter driver name: ");
            String name = scanner.nextLine();
            PredictionResult r = predictionService.predictDriver(name);
            if (r == null) System.out.println("Driver not found.");
            else reportGenerator.formatPredictionResult(r);
        }
    }

    private void handleDataInfo() {
        if (!checkDataLoaded()) return;
        System.out.println("\n--- DATA INFO ---");
        System.out.println("File: " + sessionState.getCurrentFile());
        System.out.println("Total Records: " + sessionState.getResultCount());
        System.out.println("Unique Drivers: " + repository.getAllDrivers().size());
        System.out.println("Unique Teams: " + repository.getAllTeams().size());
    }

    private boolean checkDataLoaded() {
        if (!sessionState.isDataLoaded()) {
            System.err.println("Error: Please load data first (Option 1).");
            return false;
        }
        return true;
    }

    private int getValidatedInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int val = Integer.parseInt(scanner.nextLine());
                if (val >= min && val <= max) return val;
                System.out.println("Please enter a value between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }
}
