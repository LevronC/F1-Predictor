package com.f1predictor.data;

import com.f1predictor.model.RaceResult;
import java.io.IOException;
import java.util.List;

/**
 * Interface for loading race results.
 */
public interface DataLoader {
    List<RaceResult> load(String filePath) throws IOException;
}
