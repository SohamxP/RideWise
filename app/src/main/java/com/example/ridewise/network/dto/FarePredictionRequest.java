package com.example.ridewise.network.dto;

import com.google.gson.annotations.SerializedName;

public class FarePredictionRequest {

    @SerializedName("trip_miles")
    private final double tripMiles;

    @SerializedName("trip_minutes")
    private final double tripMinutes;

    @SerializedName("pickup_hour")
    private final int pickupHour;

    @SerializedName("day_of_week")
    private final int dayOfWeek;

    @SerializedName("pickup_lat")
    private final double pickupLat;

    @SerializedName("pickup_lon")
    private final double pickupLon;

    @SerializedName("dropoff_lat")
    private final double dropoffLat;

    @SerializedName("dropoff_lon")
    private final double dropoffLon;

    public FarePredictionRequest(
            double tripMiles,
            double tripMinutes,
            int pickupHour,
            int dayOfWeek,
            double pickupLat,
            double pickupLon,
            double dropoffLat,
            double dropoffLon
    ) {
        this.tripMiles = tripMiles;
        this.tripMinutes = tripMinutes;
        this.pickupHour = pickupHour;
        this.dayOfWeek = dayOfWeek;
        this.pickupLat = pickupLat;
        this.pickupLon = pickupLon;
        this.dropoffLat = dropoffLat;
        this.dropoffLon = dropoffLon;
    }
}