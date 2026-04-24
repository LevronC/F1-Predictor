package com.f1predictor.model;

/**
 * Tracks individual data problems.
 */
public class DataQualityIssue {
    public enum IssueType {
        MISSING, INVALID, SUSPICIOUS
    }

    private int rowNumber;
    private String fieldName;
    private IssueType issueType;
    private String description;
    private String originalValue;

    public DataQualityIssue(int rowNumber, String fieldName, IssueType issueType, String description, String originalValue) {
        this.rowNumber = rowNumber;
        this.fieldName = fieldName;
        this.issueType = issueType;
        this.description = description;
        this.originalValue = originalValue;
    }

    // Getters
    public int getRowNumber() { return rowNumber; }
    public String getFieldName() { return fieldName; }
    public IssueType getIssueType() { return issueType; }
    public String getDescription() { return description; }
    public String getOriginalValue() { return originalValue; }

    @Override
    public String toString() {
        return String.format("Row %d [%s]: %s (%s) - %s", rowNumber, issueType, fieldName, originalValue, description);
    }
}
