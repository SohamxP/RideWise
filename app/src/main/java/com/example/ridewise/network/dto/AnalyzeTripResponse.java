package com.example.ridewise.network.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AnalyzeTripResponse {

    private String market;
    private String currency;

    @SerializedName("data_basis")
    private String dataBasis;

    private RouteInfo route;

    private List<ProviderPrediction> predictions;

    public String getMarket() {
        return market;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDataBasis() {
        return dataBasis;
    }

    public RouteInfo getRoute() {
        return route;
    }

    public List<ProviderPrediction> getPredictions() {
        return predictions;
    }
}