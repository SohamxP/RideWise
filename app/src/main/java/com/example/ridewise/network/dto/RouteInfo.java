package com.example.ridewise.network.dto;

import com.google.gson.annotations.SerializedName;

public class RouteInfo {

    @SerializedName("trip_miles")
    private double tripMiles;

    @SerializedName("trip_minutes")
    private double tripMinutes;

    public double getTripMiles() {
        return tripMiles;
    }

    public double getTripMinutes() {
        return tripMinutes;
    }
}