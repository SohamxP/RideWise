package com.example.ridewise.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.example.ridewise.models.TripRequest;

public class DeepLinkHelper {

    public static void openUber(Context context, TripRequest trip) {
        String uberUriString = "uber://?action=setPickup" +
                "&pickup[latitude]=" + trip.getPickupLat() +
                "&pickup[longitude]=" + trip.getPickupLng() +
                "&dropoff[latitude]=" + trip.getDropoffLat() +
                "&dropoff[longitude]=" + trip.getDropoffLng();

        Uri uberUri = Uri.parse(uberUriString);
        Intent intent = new Intent(Intent.ACTION_VIEW, uberUri);

        // Fallback to web if app not installed
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            String webUriString = "https://m.uber.com/ul/" +
                    "?action=setPickup" +
                    "&pickup[latitude]=" + trip.getPickupLat() +
                    "&pickup[longitude]=" + trip.getPickupLng() +
                    "&dropoff[latitude]=" + trip.getDropoffLat() +
                    "&dropoff[longitude]=" + trip.getDropoffLng();

            intent.setData(Uri.parse(webUriString));
        }

        context.startActivity(intent);
    }

    public static void openLyft(Context context, TripRequest trip) {
        String lyftUriString = "lyft://ridetype?id=lyft" +
                "&pickup[latitude]=" + trip.getPickupLat() +
                "&pickup[longitude]=" + trip.getPickupLng() +
                "&destination[latitude]=" + trip.getDropoffLat() +
                "&destination[longitude]=" + trip.getDropoffLng();

        Uri lyftUri = Uri.parse(lyftUriString);
        Intent intent = new Intent(Intent.ACTION_VIEW, lyftUri);

        // Fallback to web if app not installed
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            String webUriString = "https://lyft.com/ride" +
                    "?pickup[latitude]=" + trip.getPickupLat() +
                    "&pickup[longitude]=" + trip.getPickupLng() +
                    "&destination[latitude]=" + trip.getDropoffLat() +
                    "&destination[longitude]=" + trip.getDropoffLng();

            intent.setData(Uri.parse(webUriString));
        }

        context.startActivity(intent);
    }
}