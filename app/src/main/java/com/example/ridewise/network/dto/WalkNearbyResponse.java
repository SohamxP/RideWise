package com.example.ridewise.network.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WalkNearbyResponse {

    private String recommendation;

    @SerializedName("current_lowest_fare")
    private double currentLowestFare;

    @SerializedName("recommended_fare")
    private double recommendedFare;

    @SerializedName("potential_savings")
    private double potentialSavings;

    @SerializedName("best_option")
    private WalkNearbyOption bestOption;

    private List<WalkNearbyOption> options;

    public String getRecommendation() {
        return recommendation;
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

    public WalkNearbyOption getBestOption() {
        return bestOption;
    }

    public List<WalkNearbyOption> getOptions() {
        return options;
    }
}