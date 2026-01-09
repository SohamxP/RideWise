package com.example.ridewise.models;

public class RideHistory {
    private String id;
    private String userId;
    private long date;
    private RideProvider provider;
    private String pickupAddress;
    private String dropoffAddress;
    private double distance;
    private double basePrice;
    private double actualPrice;
    private double savings;
    private String strategyUsed;

    public RideHistory() {
        this.id = "";
        this.userId = "";
        this.date = System.currentTimeMillis();
    }

    public RideHistory(String id, String userId, long date, RideProvider provider,
                       String pickupAddress, String dropoffAddress, double distance,
                       double basePrice, double actualPrice, double savings, String strategyUsed) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.provider = provider;
        this.pickupAddress = pickupAddress;
        this.dropoffAddress = dropoffAddress;
        this.distance = distance;
        this.basePrice = basePrice;
        this.actualPrice = actualPrice;
        this.savings = savings;
        this.strategyUsed = strategyUsed;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }

    public RideProvider getProvider() { return provider; }
    public void setProvider(RideProvider provider) { this.provider = provider; }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public String getDropoffAddress() { return dropoffAddress; }
    public void setDropoffAddress(String dropoffAddress) { this.dropoffAddress = dropoffAddress; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }

    public double getActualPrice() { return actualPrice; }
    public void setActualPrice(double actualPrice) { this.actualPrice = actualPrice; }

    public double getSavings() { return savings; }
    public void setSavings(double savings) { this.savings = savings; }

    public String getStrategyUsed() { return strategyUsed; }
    public void setStrategyUsed(String strategyUsed) { this.strategyUsed = strategyUsed; }
}