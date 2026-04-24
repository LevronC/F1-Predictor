package com.f1predictor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.f1predictor.controller.MenuController;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("F1 Predictor Application starting...");
        
        try {
            MenuController controller = new MenuController();
            controller.run();
        } catch (Exception e) {
            logger.error("Fatal application error", e);
            System.err.println("A fatal error occurred: " + e.getMessage());
        }
        
        logger.info("F1 Predictor Application shutting down...");
    }
}
