package com.example.ridewise.models;

public class WalkNearbyZone {
    private double lat;
    private double lng;
    private int distanceMeters;
    private String direction;
    private double estimatedSavings;
    private double confidence;

    public WalkNearbyZone(double lat, double lng, int distanceMeters, String direction,
                          double estimatedSavings, double confidence) {
        this.lat = lat;
        this.lng = lng;
        this.distanceMeters = distanceMeters;
        this.direction = direction;
        this.estimatedSavings = estimatedSavings;
        this.confidence = confidence;
    }

    // Getters
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public int getDistanceMeters() { return distanceMeters; }
    public String getDirection() { return direction; }
    public double getEstimatedSavings() { return estimatedSavings; }
    public double getConfidence() { return confidence; }

    public double getDistanceFeet() {
        return distanceMeters * 3.28084;
    }
}