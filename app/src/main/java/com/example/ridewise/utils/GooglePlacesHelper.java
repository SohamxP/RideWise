package com.example.ridewise.utils;

import android.content.Context;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import java.util.Arrays;
import java.util.List;

public class GooglePlacesHelper {

    private static final String TAG = "GooglePlacesHelper";
    private PlacesClient placesClient;
    private AutocompleteSessionToken token;

    public GooglePlacesHelper(Context context, String apiKey) {
        if (!Places.isInitialized()) {
            Places.initialize(context, apiKey);
        }
        placesClient = Places.createClient(context);
        token = AutocompleteSessionToken.newInstance();
    }

    public interface LocationCallback {
        void onLocationFound(LatLng location, String formattedAddress);
        void onError(String error);
    }

    /**
     * Get coordinates from address/location name
     */
    public void getCoordinatesFromAddress(String query, LocationCallback callback) {
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setQuery(query)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            List<AutocompletePrediction> predictions = response.getAutocompletePredictions();

            if (predictions.isEmpty()) {
                callback.onError("No locations found");
                return;
            }

            // Get the first prediction
            AutocompletePrediction prediction = predictions.get(0);
            String placeId = prediction.getPlaceId();

            // Fetch place details to get coordinates
            List<Place.Field> placeFields = Arrays.asList(
                    Place.Field.LAT_LNG,
                    Place.Field.ADDRESS
            );

            FetchPlaceRequest fetchRequest = FetchPlaceRequest.builder(placeId, placeFields).build();

            placesClient.fetchPlace(fetchRequest).addOnSuccessListener((placeResponse) -> {
                Place place = placeResponse.getPlace();
                LatLng latLng = place.getLatLng();
                String address = place.getAddress();

                if (latLng != null) {
                    callback.onLocationFound(latLng, address != null ? address : query);
                } else {
                    callback.onError("Could not get coordinates");
                }
            }).addOnFailureListener((exception) -> {
                Log.e(TAG, "Place fetch failed: " + exception.getMessage());
                callback.onError("Failed to get place details");
            });

        }).addOnFailureListener((exception) -> {
            Log.e(TAG, "Autocomplete failed: " + exception.getMessage());
            callback.onError("Location search failed");
        });
    }
}