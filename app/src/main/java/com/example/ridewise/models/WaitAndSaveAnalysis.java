package com.example.ridewise.models;

import java.util.List;

public class WaitAndSaveAnalysis {
    private double currentPrice;
    private double predictedPrice;
    private int estimatedWaitMinutes;
    private double confidence;
    private List<String> factors;
    private String recommendation;

    public WaitAndSaveAnalysis(double currentPrice, double predictedPrice, int estimatedWaitMinutes,
                               double confidence, List<String> factors, String recommendation) {
        this.currentPrice = currentPrice;
        this.predictedPrice = predictedPrice;
        this.estimatedWaitMinutes = estimatedWaitMinutes;
        this.confidence = confidence;
        this.factors = factors;
        this.recommendation = recommendation;
    }

    // Getters
    public double getCurrentPrice() { return currentPrice; }
    public double getPredictedPrice() { return predictedPrice; }
    public int getEstimatedWaitMinutes() { return estimatedWaitMinutes; }
    public double getConfidence() { return confidence; }
    public List<String> getFactors() { return factors; }
    public String getRecommendation() { return recommendation; }

    public double getSavingsAmount() {
        return currentPrice - predictedPrice;
    }

    public double getSavingsPercent() {
        return ((currentPrice - predictedPrice) / currentPrice) * 100.0;
    }
}