package com.f1predictor.data;

import com.f1predictor.model.DataQualityIssue;
import com.f1predictor.model.DataQualityReport;
import com.f1predictor.model.RaceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Validates and cleans race results.
 */
public class DataCleaner {
    private static final Logger logger = LoggerFactory.getLogger(DataCleaner.class);

    private static final Map<String, String> TEAM_ALIASES = Map.of(
            "Red Bull Racing", "Red Bull",
            "Oracle Red Bull Racing", "Red Bull",
            "Scuderia Ferrari", "Ferrari",
            "Mercedes-AMG Petronas", "Mercedes",
            "McLaren F1 Team", "McLaren",
            "Aston Martin Aramco", "Aston Martin",
            "BWT Alpine F1 Team", "Alpine",
            "MoneyGram Haas F1 Team", "Haas",
            "Williams Racing", "Williams",
            "Alfa Romeo F1 Team Stake", "Alfa Romeo"
    );

    public static CleaningResult clean(List<RaceResult> rawResults) {
        List<RaceResult> cleanedResults = new ArrayList<>();
        DataQualityReport report = new DataQualityReport();
        int valid = 0;
        int invalid = 0;

        Set<String> seenKeys = new HashSet<>();

        for (int i = 0; i < rawResults.size(); i++) {
            RaceResult raw = rawResults.get(i);
            int rowNum = i + 2; 

            boolean isRowValid = true;

            String cleanDriver = normalizeDriverName(raw.getDriverName());
            String cleanTeam = normalizeTeamName(raw.getConstructorName());

            String key = String.format("%d-%d-%s", raw.getSeason(), raw.getRound(), cleanDriver);
            if (seenKeys.contains(key)) {
                report.addIssue(new DataQualityIssue(rowNum, "DUPLICATE", 
                        DataQualityIssue.IssueType.INVALID, "Duplicate entry for driver in race", key));
                isRowValid = false;
            }
            seenKeys.add(key);

            if (raw.getPoints() < 0 || raw.getPoints() > 30) {
                report.addIssue(new DataQualityIssue(rowNum, "points", 
                        DataQualityIssue.IssueType.SUSPICIOUS, "Unusual points value", String.valueOf(raw.getPoints())));
            }

            if (raw.getFinishPosition() < 0 || raw.getFinishPosition() > 40) {
                report.addIssue(new DataQualityIssue(rowNum, "position", 
                        DataQualityIssue.IssueType.INVALID, "Invalid position", String.valueOf(raw.getFinishPosition())));
                isRowValid = false;
            }

            if (isRowValid) {
                RaceResult clean = RaceResult.builder()
                        .season(raw.getSeason())
                        .round(raw.getRound())
                        .circuit(raw.getCircuit())
                        .date(raw.getDate())
                        .driverName(cleanDriver)
                        .constructorName(cleanTeam)
                        .gridPosition(raw.getGridPosition())
                        .finishPosition(raw.getFinishPosition())
                        .points(raw.getPoints())
                        .lapsCompleted(raw.getLapsCompleted())
                        .status(raw.getStatus())
                        .fastestLap(raw.getFastestLap())
                        .build();
                cleanedResults.add(clean);
                valid++;
            } else {
                invalid++;
            }
        }

        report.setRows(rawResults.size(), valid, invalid);
        return new CleaningResult(report, cleanedResults);
    }

    public record CleaningResult(DataQualityReport report, List<RaceResult> cleanedData) {}

    public static String normalizeDriverName(String name) {
        if (name == null) return "";
        String trimmed = name.trim();
        if (trimmed.contains(",")) {
            String[] parts = trimmed.split(",");
            if (parts.length == 2) {
                return (parts[1].trim() + " " + parts[0].trim()).replaceAll("\\s+", " ");
            }
        }
        return trimmed.replaceAll("\\s+", " ");
    }

    public static String normalizeTeamName(String name) {
        if (name == null) return "";
        String trimmed = name.trim();
        for (Map.Entry<String, String> entry : TEAM_ALIASES.entrySet()) {
            if (trimmed.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        return trimmed;
    }
}
