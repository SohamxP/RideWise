package com.example.ridewise.network.dto;

import com.google.gson.annotations.SerializedName;

public class WaitOption {

    @SerializedName("wait_minutes")
    private int waitMinutes;

    @SerializedName("uber_fare")
    private double uberFare;

    @SerializedName("lyft_fare")
    private double lyftFare;

    @SerializedName("lowest_fare")
    private double lowestFare;

    @SerializedName("lowest_provider")
    private String lowestProvider;

    public int getWaitMinutes() {
        return waitMinutes;
    }

    public double getUberFare() {
        return uberFare;
    }

    public double getLyftFare() {
        return lyftFare;
    }

    public double getLowestFare() {
        return lowestFare;
    }

    public String getLowestProvider() {
        return lowestProvider;
    }
}