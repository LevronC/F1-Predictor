package com.f1predictor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Summary of all data quality issues.
 */
public class DataQualityReport {
    private int totalRows;
    private int validRows;
    private int invalidRows;
    private int missingFields;
    private Map<String, Integer> issuesByType = new HashMap<>();
    private List<DataQualityIssue> issues = new ArrayList<>();

    public void addIssue(DataQualityIssue issue) {
        issues.add(issue);
        String typeStr = issue.getIssueType().name();
        issuesByType.put(typeStr, issuesByType.getOrDefault(typeStr, 0) + 1);
        if (issue.getIssueType() == DataQualityIssue.IssueType.MISSING) {
            missingFields++;
        }
    }

    public void setRows(int total, int valid, int invalid) {
        this.totalRows = total;
        this.validRows = valid;
        this.invalidRows = invalid;
    }

    public double getQualityScore() {
        return totalRows == 0 ? 0 : (double) validRows / totalRows;
    }

    public String getQualityLabel() {
        double score = getQualityScore();
        if (score > 0.95) return "Excellent";
        if (score > 0.85) return "Good";
        if (score > 0.70) return "Fair";
        return "Poor";
    }

    // Getters
    public int getTotalRows() { return totalRows; }
    public int getValidRows() { return validRows; }
    public int getInvalidRows() { return invalidRows; }
    public int getMissingFields() { return missingFields; }
    public Map<String, Integer> getIssuesByType() { return issuesByType; }
    public List<DataQualityIssue> getIssues() { return issues; }
}
