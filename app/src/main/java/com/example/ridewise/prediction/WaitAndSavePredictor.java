package com.example.ridewise.prediction;

import com.example.ridewise.models.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class WaitAndSavePredictor {

    private Random random;

    public WaitAndSavePredictor() {
        this.random = new Random();
    }

    public WaitAndSaveAnalysis analyzePriceDropProbability(
            RideEstimate currentEstimate, TripRequest tripRequest) {

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        List<String> factors = new ArrayList<>();
        double confidence = 0.5;
        double predictedDropPercent = 0.0;

        // Analyze surge patterns
        if (currentEstimate.getSurgeMultiplier() > 1.5) {
            factors.add(String.format("High surge detected (%.1fx)", currentEstimate.getSurgeMultiplier()));
            confidence += 0.2;
            predictedDropPercent += 15.0;
        }

        // Peak hour analysis
        boolean isPeakHour = (hour >= 7 && hour <= 9) || (hour >= 17 && hour <= 19);
        if (isPeakHour) {
            factors.add("Peak hour traffic");
            predictedDropPercent += 10.0;
        } else {
            confidence += 0.1;
        }

        // Weekend patterns
        if (dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY) {
            factors.add("Weekday - predictable patterns");
            confidence += 0.1;
        }

        // Time-based prediction
        int waitMinutes;
        if (currentEstimate.getSurgeMultiplier() > 2.0) {
            waitMinutes = 5 + random.nextInt(6); // 5-10 min
        } else if (currentEstimate.getSurgeMultiplier() > 1.5) {
            waitMinutes = 7 + random.nextInt(6); // 7-12 min
        } else {
            waitMinutes = 10 + random.nextInt(6); // 10-15 min
        }

        double currentPrice = currentEstimate.getAveragePrice();
        double predictedPrice = currentPrice * (1 - predictedDropPercent / 100);

        String recommendation;
        if (predictedDropPercent > 15) {
            recommendation = "Strong recommendation to wait";
        } else if (predictedDropPercent > 8) {
            recommendation = "Moderate savings likely";
        } else {
            recommendation = "Small savings possible";
        }

        factors.add("Next refresh in 5 min");

        confidence = Math.min(confidence, 0.95);

        return new WaitAndSaveAnalysis(
                currentPrice,
                predictedPrice,
                waitMinutes,
                confidence,
                factors,
                recommendation
        );
    }
}