package com.example.ridewise.models;

public class TripRequest {
    private double pickupLat;
    private double pickupLng;
    private double dropoffLat;
    private double dropoffLng;
    private String pickupAddress;
    private String dropoffAddress;
    private long timestamp;

    public TripRequest(double pickupLat, double pickupLng, double dropoffLat, double dropoffLng) {
        this.pickupLat = pickupLat;
        this.pickupLng = pickupLng;
        this.dropoffLat = dropoffLat;
        this.dropoffLng = dropoffLng;
        this.pickupAddress = "";
        this.dropoffAddress = "";
        this.timestamp = System.currentTimeMillis();
    }

    public TripRequest(double pickupLat, double pickupLng, double dropoffLat, double dropoffLng,
                       String pickupAddress, String dropoffAddress) {
        this.pickupLat = pickupLat;
        this.pickupLng = pickupLng;
        this.dropoffLat = dropoffLat;
        this.dropoffLng = dropoffLng;
        this.pickupAddress = pickupAddress;
        this.dropoffAddress = dropoffAddress;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public double getPickupLat() { return pickupLat; }
    public double getPickupLng() { return pickupLng; }
    public double getDropoffLat() { return dropoffLat; }
    public double getDropoffLng() { return dropoffLng; }
    public String getPickupAddress() { return pickupAddress; }
    public String getDropoffAddress() { return dropoffAddress; }
    public long getTimestamp() { return timestamp; }

    // Setters
    public void setPickupLat(double pickupLat) { this.pickupLat = pickupLat; }
    public void setPickupLng(double pickupLng) { this.pickupLng = pickupLng; }
    public void setDropoffLat(double dropoffLat) { this.dropoffLat = dropoffLat; }
    public void setDropoffLng(double dropoffLng) { this.dropoffLng = dropoffLng; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }
    public void setDropoffAddress(String dropoffAddress) { this.dropoffAddress = dropoffAddress; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}