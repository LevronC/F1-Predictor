package com.f1predictor.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Statistical utility functions.
 */
public class StatisticsUtil {

    public static double mean(List<Double> values) {
        if (values == null || values.isEmpty()) return 0;
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    public static double stdDev(List<Double> values) {
        if (values == null || values.size() <= 1) return 0;
        double avg = mean(values);
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - avg, 2))
                .sum() / values.size();
        return Math.sqrt(variance);
    }

    public static double normalize(double value, double min, double max) {
        if (max == min) return 0;
        double normalized = (value - min) / (max - min);
        return Math.max(0, Math.min(1, normalized));
    }

    public static double weightedAverage(List<Double> values, double[] weights) {
        if (values == null || values.isEmpty() || weights == null || weights.length == 0) return 0;
        double sum = 0;
        double weightSum = 0;
        int count = Math.min(values.size(), weights.length);
        for (int i = 0; i < count; i++) {
            sum += values.get(i) * weights[i];
            weightSum += weights[i];
        }
        return weightSum == 0 ? 0 : sum / weightSum;
    }

    public static <T> List<T> rank(List<T> items, Comparator<T> comparator) {
        if (items == null) return Collections.emptyList();
        return items.stream().sorted(comparator).collect(Collectors.toList());
    }
}
