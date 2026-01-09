package com.example.ridewise.models;

public class RideEstimate {
    private RideProvider provider;
    private String rideType;
    private double priceMin;
    private double priceMax;
    private int etaMinutes;
    private double surgeMultiplier;
    private String currency;

    public RideEstimate(RideProvider provider, String rideType, double priceMin,
                        double priceMax, int etaMinutes, double surgeMultiplier, String currency) {
        this.provider = provider;
        this.rideType = rideType;
        this.priceMin = priceMin;
        this.priceMax = priceMax;
        this.etaMinutes = etaMinutes;
        this.surgeMultiplier = surgeMultiplier;
        this.currency = currency;
    }

    public RideEstimate(RideProvider provider, String rideType, double priceMin,
                        double priceMax, int etaMinutes) {
        this(provider, rideType, priceMin, priceMax, etaMinutes, 1.0, "USD");
    }

    // Getters
    public RideProvider getProvider() { return provider; }
    public String getRideType() { return rideType; }
    public double getPriceMin() { return priceMin; }
    public double getPriceMax() { return priceMax; }
    public int getEtaMinutes() { return etaMinutes; }
    public double getSurgeMultiplier() { return surgeMultiplier; }
    public String getCurrency() { return currency; }

    public double getAveragePrice() {
        return (priceMin + priceMax) / 2.0;
    }

    // Setters
    public void setProvider(RideProvider provider) { this.provider = provider; }
    public void setRideType(String rideType) { this.rideType = rideType; }
    public void setPriceMin(double priceMin) { this.priceMin = priceMin; }
    public void setPriceMax(double priceMax) { this.priceMax = priceMax; }
    public void setEtaMinutes(int etaMinutes) { this.etaMinutes = etaMinutes; }
    public void setSurgeMultiplier(double surgeMultiplier) { this.surgeMultiplier = surgeMultiplier; }
    public void setCurrency(String currency) { this.currency = currency; }
}