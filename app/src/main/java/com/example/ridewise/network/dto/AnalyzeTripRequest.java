package com.example.ridewise.network.dto;

import com.google.gson.annotations.SerializedName;

public class AnalyzeTripRequest {

    @SerializedName("pickup_lat")
    private final double pickupLat;

    @SerializedName("pickup_lon")
    private final double pickupLon;

    @SerializedName("dropoff_lat")
    private final double dropoffLat;

    @SerializedName("dropoff_lon")
    private final double dropoffLon;

    public AnalyzeTripRequest(
            double pickupLat,
            double pickupLon,
            double dropoffLat,
            double dropoffLon
    ) {
        this.pickupLat = pickupLat;
        this.pickupLon = pickupLon;
        this.dropoffLat = dropoffLat;
        this.dropoffLon = dropoffLon;
    }
}