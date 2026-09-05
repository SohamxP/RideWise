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

    @SerializedName("pickup_zone_id")
    private final int pickupZoneId;

    @SerializedName("dropoff_zone_id")
    private final int dropoffZoneId;

    public FarePredictionRequest(double tripMiles, double tripMinutes, int pickupHour,
                                 int dayOfWeek, int pickupZoneId, int dropoffZoneId) {
        this.tripMiles = tripMiles;
        this.tripMinutes = tripMinutes;
        this.pickupHour = pickupHour;
        this.dayOfWeek = dayOfWeek;
        this.pickupZoneId = pickupZoneId;
        this.dropoffZoneId = dropoffZoneId;
    }
}
