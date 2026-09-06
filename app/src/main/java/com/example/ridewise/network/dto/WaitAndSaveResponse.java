package com.example.ridewise.network.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WaitAndSaveResponse {

    private String recommendation;

    @SerializedName("recommended_wait_minutes")
    private int recommendedWaitMinutes;

    @SerializedName("current_lowest_fare")
    private double currentLowestFare;

    @SerializedName("recommended_fare")
    private double recommendedFare;

    @SerializedName("potential_savings")
    private double potentialSavings;

    private List<WaitOption> options;

    public String getRecommendation() {
        return recommendation;
    }

    public int getRecommendedWaitMinutes() {
        return recommendedWaitMinutes;
    }

    public double getCurrentLowestFare() {
        return currentLowestFare;
    }

    public double getRecommendedFare() {
        return recommendedFare;
    }

    public double getPotentialSavings() {
        return potentialSavings;
    }

    public List<WaitOption> getOptions() {
        return options;
    }
}