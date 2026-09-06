package com.example.ridewise.utils;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
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

    private final PlacesClient placesClient;

    public GooglePlacesHelper(
            Context context,
            String apiKey
    ) {

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Google Places API key is missing."
            );
        }

        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(
                    context.getApplicationContext(),
                    apiKey
            );
        }

        placesClient =
                Places.createClient(
                        context.getApplicationContext()
                );
    }

    public interface LocationCallback {

        void onLocationFound(
                LatLng location,
                String formattedAddress
        );

        void onError(
                String error
        );
    }

    public void getCoordinatesFromAddress(
            String query,
            LocationCallback callback
    ) {

        if (query == null
                || query.trim().isEmpty()) {

            callback.onError(
                    "Location query is empty"
            );

            return;
        }

        AutocompleteSessionToken token =
                AutocompleteSessionToken
                        .newInstance();

        FindAutocompletePredictionsRequest request =
                FindAutocompletePredictionsRequest
                        .builder()
                        .setSessionToken(token)
                        .setQuery(
                                query.trim()
                        )
                        .setCountries("US")
                        .build();

        placesClient
                .findAutocompletePredictions(request)
                .addOnSuccessListener(
                        response -> {

                            List<AutocompletePrediction> predictions =
                                    response.getAutocompletePredictions();

                            if (predictions == null
                                    || predictions.isEmpty()) {

                                callback.onError(
                                        "No matching locations found"
                                );

                                return;
                            }

                            AutocompletePrediction prediction =
                                    predictions.get(0);

                            fetchPlaceDetails(
                                    prediction.getPlaceId(),
                                    token,
                                    query,
                                    callback
                            );
                        }
                )
                .addOnFailureListener(
                        exception -> {

                            Log.e(
                                    TAG,
                                    "Autocomplete failed",
                                    exception
                            );

                            callback.onError(
                                    buildErrorMessage(
                                            "Location search failed",
                                            exception
                                    )
                            );
                        }
                );
    }

    private void fetchPlaceDetails(
            String placeId,
            AutocompleteSessionToken token,
            String originalQuery,
            LocationCallback callback
    ) {

        List<Place.Field> fields =
                Arrays.asList(
                        Place.Field.LOCATION,
                        Place.Field.FORMATTED_ADDRESS,
                        Place.Field.DISPLAY_NAME
                );

        FetchPlaceRequest request =
                FetchPlaceRequest
                        .builder(
                                placeId,
                                fields
                        )
                        .setSessionToken(token)
                        .build();

        placesClient
                .fetchPlace(request)
                .addOnSuccessListener(
                        response -> {

                            Place place =
                                    response.getPlace();

                            LatLng location =
                                    place.getLocation();

                            if (location == null) {

                                callback.onError(
                                        "Place has no coordinates"
                                );

                                return;
                            }

                            CharSequence formattedAddress =
                                    place.getFormattedAddress();

                            CharSequence displayName =
                                    place.getDisplayName();

                            String address;

                            if (formattedAddress != null
                                    && formattedAddress.length() > 0) {

                                address =
                                        formattedAddress.toString();

                            } else if (displayName != null
                                    && displayName.length() > 0) {

                                address =
                                        displayName.toString();

                            } else {

                                address =
                                        originalQuery;
                            }

                            callback.onLocationFound(
                                    location,
                                    address
                            );
                        }
                )
                .addOnFailureListener(
                        exception -> {

                            Log.e(
                                    TAG,
                                    "Place details failed",
                                    exception
                            );

                            callback.onError(
                                    buildErrorMessage(
                                            "Place details failed",
                                            exception
                                    )
                            );
                        }
                );
    }

    private String buildErrorMessage(
            String prefix,
            Exception exception
    ) {

        if (exception instanceof ApiException) {

            ApiException apiException =
                    (ApiException) exception;

            return prefix
                    + " (code "
                    + apiException.getStatusCode()
                    + "): "
                    + apiException.getMessage();
        }

        return prefix
                + ": "
                + (
                    exception.getMessage() != null
                            ? exception.getMessage()
                            : "Unknown error"
                );
    }
}