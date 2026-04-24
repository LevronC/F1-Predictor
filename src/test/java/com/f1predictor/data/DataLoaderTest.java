package com.f1predictor.data;

import com.f1predictor.model.RaceResult;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class DataLoaderTest {
    @Test
    public void testLoadValidCSV() throws IOException {
        DataLoader loader = new CSVDataLoader();
        List<RaceResult> results = loader.load("src/test/resources/test-data-valid.csv");
        
        assertEquals(3, results.size());
        assertEquals("Max Verstappen", results.get(0).getDriverName());
        assertEquals(25.0, results.get(0).getPoints());
        assertEquals("Finished", results.get(0).getStatus());
    }

    @Test
    public void testLoadMissingFile() {
        DataLoader loader = new CSVDataLoader();
        assertThrows(IOException.class, () -> loader.load("non-existent.csv"));
    }
}
