package com.example.ridewise.network;

import com.example.ridewise.network.dto.AnalyzeTripRequest;
import com.example.ridewise.network.dto.AnalyzeTripResponse;
import com.example.ridewise.network.dto.FarePredictionRequest;
import com.example.ridewise.network.dto.FarePredictionResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RideWiseApi {

    @POST("v1/predict-fares")
    Call<FarePredictionResponse> predictFares(
            @Body FarePredictionRequest request
    );

    @POST("v1/analyze-trip")
    Call<AnalyzeTripResponse> analyzeTrip(
            @Body AnalyzeTripRequest request
    );
}