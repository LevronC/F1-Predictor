package com.f1predictor.service;

import com.f1predictor.data.DataRepository;
import com.f1predictor.data.InMemoryRepository;
import com.f1predictor.model.Driver;
import com.f1predictor.model.RaceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class AnalyticsServiceTest {
    private DataRepository repository;
    private AnalyticsService service;

    @BeforeEach
    public void setup() {
        repository = new InMemoryRepository();
        service = new AnalyticsService(repository);
        
        repository.addAll(List.of(
            RaceResult.builder().driverName("Driver A").constructorName("Team 1").finishPosition(1).points(25).season(2023).round(1).status("Finished").build(),
            RaceResult.builder().driverName("Driver A").constructorName("Team 1").finishPosition(1).points(25).season(2023).round(2).status("Finished").build(),
            RaceResult.builder().driverName("Driver B").constructorName("Team 1").finishPosition(2).points(18).season(2023).round(1).status("Finished").build()
        ));
    }

    @Test
    public void testTopDriversByWins() {
        List<Driver> top = service.getTopDriversByWins(1);
        assertEquals(1, top.size());
        assertEquals("Driver A", top.get(0).getName());
        assertEquals(2, top.get(0).getWins());
    }

    @Test
    public void testTopDriversByPoints() {
        List<Driver> top = service.getTopDriversByPoints(2);
        assertEquals(2, top.size());
        assertEquals("Driver A", top.get(0).getName());
        assertEquals(50.0, top.get(0).getTotalPoints());
    }
}
