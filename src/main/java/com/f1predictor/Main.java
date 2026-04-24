package com.f1predictor;

import com.f1predictor.data.CSVDataLoader;
import com.f1predictor.data.DataCleaner;
import com.f1predictor.data.DataRepository;
import com.f1predictor.model.RaceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public CommandLineRunner initData(CSVDataLoader loader, DataRepository repository) {
        return args -> {
            logger.info("Initializing F1 Predictor Engine...");
            try {
                List<RaceResult> rawData = loader.load("data/sample-f1-data.csv");
                DataCleaner.CleaningResult result = DataCleaner.clean(rawData);
                repository.saveAll(result.cleanedData());
                logger.info("Successfully loaded {} records. Quality: {}", result.cleanedData().size(), result.report().getQualityLabel());
            } catch (Exception e) {
                logger.error("Failed to initialize data: {}", e.getMessage());
            }
        };
    }
}
