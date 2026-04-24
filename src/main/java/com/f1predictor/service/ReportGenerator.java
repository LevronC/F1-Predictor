package com.f1predictor.service;

import com.f1predictor.model.Driver;
import com.f1predictor.model.PredictionResult;
import com.f1predictor.model.Team;

import java.util.List;
import java.util.Map;

/**
 * Generates formatted reports for the CLI.
 */
public class ReportGenerator {

    public void formatTable(String[] headers, List<String[]> rows) {
        if (headers == null || rows == null) return;

        // Calculate column widths
        int[] widths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            widths[i] = headers[i].length();
        }

        for (String[] row : rows) {
            for (int i = 0; i < Math.min(row.length, widths.length); i++) {
                if (row[i] != null) {
                    widths[i] = Math.max(widths[i], row[i].length());
                }
            }
        }

        // Print header
        formatSeparator(widths);
        for (int i = 0; i < headers.length; i++) {
            System.out.printf("| %-" + widths[i] + "s ", headers[i]);
        }
        System.out.println("|");
        formatSeparator(widths);

        // Print rows
        for (String[] row : rows) {
            for (int i = 0; i < headers.length; i++) {
                String val = i < row.length ? (row[i] != null ? row[i] : "") : "";
                System.out.printf("| %-" + widths[i] + "s ", val);
            }
            System.out.println("|");
        }
        formatSeparator(widths);
    }

    public void formatSeparator(int[] widths) {
        for (int w : widths) {
            System.out.print("+");
            for (int i = 0; i < w + 2; i++) System.out.print("-");
        }
        System.out.println("+");
    }

    public void formatProgressBar(double value, double max, int width) {
        int filled = (int) ((value / max) * width);
        System.out.print("[");
        for (int i = 0; i < width; i++) {
            if (i < filled) System.out.print("■");
            else System.out.print("░");
        }
        System.out.print("] " + String.format("%.1f/%.1f", value, max));
    }

    public void formatDriverProfile(Driver driver) {
        System.out.println("\n=== DRIVER PROFILE: " + driver.getName() + " ===");
        System.out.println("Total Races: " + driver.getTotalRaces());
        System.out.println("Wins:        " + driver.getWins());
        System.out.println("Podiums:     " + driver.getPodiums());
        System.out.println("Avg Points:  " + String.format("%.2f", driver.getAvgPoints()));
        System.out.println("Consistency: " + String.format("%.2f", driver.getConsistency()));
        System.out.println("DNF Count:   " + driver.getDnfCount());
        System.out.print("Recent Form: ");
        for (int p : driver.getRecentPositions()) System.out.print(p + " ");
        System.out.println();
    }

    public void formatPredictionResult(PredictionResult result) {
        System.out.println("\n--- Prediction for " + result.getDriverName() + " (" + result.getTeamName() + ") ---");
        System.out.println("Total Score: " + String.format("%.2f/10.0", result.getTotalScore()));
        System.out.println("Confidence:  " + result.getConfidenceLevel());
        System.out.println("Explanation: " + result.getExplanation());
        System.out.println("Score Breakdown:");
        for (Map.Entry<String, Double> entry : result.getScoreBreakdown().entrySet()) {
            System.out.printf("  %-25s ", entry.getKey());
            formatProgressBar(entry.getValue(), 1.0, 20);
            System.out.println();
        }
    }
}
