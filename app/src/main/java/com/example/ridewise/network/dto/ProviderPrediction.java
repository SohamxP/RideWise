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

    @SerializedName("median_error")
    private double medianError;

    @SerializedName("error_80_percentile")
    private double error80Percentile;

    public String getProvider() {
        return provider;
    }

    public double getEstimatedFare() {
        return estimatedFare;
    }

    public double getLowerBound() {
        return lowerBound;
    }

    public double getUpperBound() {
        return upperBound;
    }

    public double getModelMae() {
        return modelMae;
    }

    public double getMedianError() {
        return medianError;
    }

    public double getError80Percentile() {
        return error80Percentile;
    }
}