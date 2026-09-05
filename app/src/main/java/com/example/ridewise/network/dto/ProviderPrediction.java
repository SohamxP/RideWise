package com.example.ridewise.network.dto;

import com.google.gson.annotations.SerializedName;

public class ProviderPrediction {
    private String provider;

    @SerializedName("estimated_fare")
    private double estimatedFare;

    @SerializedName("lower_bound")
    private double lowerBound;

    @SerializedName("upper_bound")
    private double upperBound;

    @SerializedName("model_mae")
    private double modelMae;

    public String getProvider() { return provider; }
    public double getEstimatedFare() { return estimatedFare; }
    public double getLowerBound() { return lowerBound; }
    public double getUpperBound() { return upperBound; }
    public double getModelMae() { return modelMae; }
}
