package com.example.ridewise.network.dto;

import com.google.gson.annotations.SerializedName;

public class WalkNearbyOption {

    @SerializedName("pickup_lat")
    private double pickupLat;

    @SerializedName("pickup_lon")
    private double pickupLon;

    private String direction;

    @SerializedName("walking_distance_meters")
    private int walkingDistanceMeters;

    @SerializedName("walking_minutes")
    private double walkingMinutes;

    @SerializedName("driving_miles")
    private double drivingMiles;

    @SerializedName("driving_minutes")
    private double drivingMinutes;

    @SerializedName("uber_fare")
    private double uberFare;

    @SerializedName("lyft_fare")
    private double lyftFare;

    @SerializedName("lowest_fare")
    private double lowestFare;

    @SerializedName("lowest_provider")
    private String lowestProvider;

    @SerializedName("predicted_savings")
    private double predictedSavings;

    public double getPickupLat() {
        return pickupLat;
    }

    public double getPickupLon() {
        return pickupLon;
    }

    public String getDirection() {
        return direction;
    }

    public int getWalkingDistanceMeters() {
        return walkingDistanceMeters;
    }

    public double getWalkingMinutes() {
        return walkingMinutes;
    }

    public double getDrivingMiles() {
        return drivingMiles;
    }

    public double getDrivingMinutes() {
        return drivingMinutes;
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

    public double getPredictedSavings() {
        return predictedSavings;
    }
}