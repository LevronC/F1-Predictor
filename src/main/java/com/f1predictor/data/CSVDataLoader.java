package com.f1predictor.data;

import com.f1predictor.model.RaceResult;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of DataLoader for CSV files using OpenCSV.
 */
public class CSVDataLoader implements DataLoader {
    private static final Logger logger = LoggerFactory.getLogger(CSVDataLoader.class);

    @Override
    public List<RaceResult> load(String filePath) throws IOException {
        List<RaceResult> results = new ArrayList<>();
        int rowCount = 0;

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] header = reader.readNext(); // Skip header
            if (header == null) return results;

            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                rowCount++;
                try {
                    RaceResult result = parseRow(nextLine);
                    if (result != null) {
                        results.add(result);
                    }
                } catch (Exception e) {
                    logger.warn("Skipping malformed row {}: {}", rowCount + 1, e.getMessage());
                }

                if (rowCount % 100 == 0) {
                    logger.info("Processed {} rows...", rowCount);
                }
            }
        } catch (CsvValidationException e) {
            throw new IOException("CSV validation error: " + e.getMessage(), e);
        }

        logger.info("Finished loading {} race results.", results.size());
        return results;
    }

    private RaceResult parseRow(String[] row) {
        // Expected order: season,round,circuit,date,driver,constructor,grid,position,points,laps,status,fastestLap
        if (row.length < 9) {
            throw new IllegalArgumentException("Insufficient columns in row");
        }

        try {
            int season = Integer.parseInt(row[0].trim());
            int round = Integer.parseInt(row[1].trim());
            String circuit = row[2].trim();
            LocalDate date = null;
            try {
                date = LocalDate.parse(row[3].trim());
            } catch (DateTimeParseException e) {
                // date is optional, but let's log it if it's there and bad
                if (!row[3].trim().isEmpty()) {
                    logger.debug("Bad date format: {}", row[3]);
                }
            }
            String driver = row[4].trim();
            String constructor = row[5].trim();
            int grid = Integer.parseInt(row[6].trim());
            
            // Position can be numeric or DNF/DNS/DSQ
            String posStr = row[7].trim();
            int position = parsePosition(posStr);
            
            double points = Double.parseDouble(row[8].trim());
            
            int laps = row.length > 9 && !row[9].trim().isEmpty() ? Integer.parseInt(row[9].trim()) : 0;
            String status = row.length > 10 ? row[10].trim() : "Finished";
            String fastestLap = row.length > 11 ? row[11].trim() : "";

            return RaceResult.builder()
                    .season(season)
                    .round(round)
                    .circuit(circuit)
                    .date(date)
                    .driverName(driver)
                    .constructorName(constructor)
                    .gridPosition(grid)
                    .finishPosition(position)
                    .points(points)
                    .lapsCompleted(laps)
                    .status(status)
                    .fastestLap(fastestLap)
                    .build();

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Number format error: " + e.getMessage());
        }
    }

    private int parsePosition(String posStr) {
        if (posStr == null || posStr.isEmpty()) return 0;
        if (posStr.matches("\\d+")) {
            return Integer.parseInt(posStr);
        }
        // If it starts with +, it's probably like "+1 Lap", which means they finished.
        // We'll treat it as a finish for now, but in a real app we'd want to know the rank.
        // For simplicity, if it's not a number and doesn't look like a finish rank, it's a DNF (0)
        return 0; 
    }
}
