package com.f1predictor.controller;

import com.f1predictor.model.RaceResult;
import java.util.ArrayList;
import java.util.List;

/**
 * Runtime state of the application.
 */
public class SessionState {
    private boolean dataLoaded;
    private String currentFile;
    private long loadTime;
    private int resultCount;
    private List<RaceResult> currentData = new ArrayList<>();

    public boolean isDataLoaded() {
        return dataLoaded;
    }

    public void setDataLoaded(boolean dataLoaded) {
        this.dataLoaded = dataLoaded;
    }

    public String getCurrentFile() {
        return currentFile;
    }

    public void setCurrentFile(String currentFile) {
        this.currentFile = currentFile;
    }

    public long getLoadTime() {
        return loadTime;
    }

    public void setLoadTime(long loadTime) {
        this.loadTime = loadTime;
    }

    public int getResultCount() {
        return resultCount;
    }

    public void setResultCount(int resultCount) {
        this.resultCount = resultCount;
    }

    public List<RaceResult> getCurrentData() {
        return currentData;
    }

    public void setCurrentData(List<RaceResult> currentData) {
        this.currentData = currentData;
        this.resultCount = currentData != null ? currentData.size() : 0;
    }
}
