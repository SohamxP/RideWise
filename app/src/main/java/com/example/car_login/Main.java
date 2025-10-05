package com.example.car_login;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.car_login.databinding.ActivityTripCompareBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

/**
 * Minimal side-by-side price compare screen.
 * This version is refactored to use modern View Binding.
 */
public class Main extends AppCompatActivity {

    // A single, final variable for the View Binding object.
    // This replaces all the individual TextView and Button declarations.
    private ActivityTripCompareBinding binding;

    private final DecimalFormat money = new DecimalFormat("$#0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout using View Binding and set the content view.
        // This replaces setContentView(R.layout...) and all findViewById calls.
        binding = ActivityTripCompareBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadTripData();
    }

    /**
     * Reads data from assets, parses it, and updates the UI using the binding object.
     */
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void loadTripData() {
        try {
            JSONObject root = new JSONObject(readAsset("trip_prices.json"));
            JSONArray trips = root.getJSONArray("trips");
            JSONObject trip = trips.getJSONObject(0); // show first trip for demo

            String pickup = trip.getString("pickup");
            String dropoff = trip.getString("dropoff");
            double distanceMi = trip.getDouble("distance_mi");
            int timeMin = trip.getInt("time_min");
            String ride = trip.optString("ride", "standard");

            JSONObject prices = trip.getJSONObject("prices");
            JSONObject uber = prices.getJSONObject("uber");
            JSONObject lyft = prices.getJSONObject("lyft");

            // Compute midpoint prices for banner comparison
            double uMid = (uber.getDouble("price_low") + uber.getDouble("price_high")) / 2.0;
            double lMid = (lyft.getDouble("price_low") + lyft.getDouble("price_high")) / 2.0;
            String bannerText;
            if (uMid < lMid) {
                bannerText = "Uber is " + money.format(lMid - uMid) + " cheaper right now";
            } else if (lMid < uMid) {
                bannerText = "Lyft is " + money.format(uMid - lMid) + " cheaper right now";
            } else {
                bannerText = "Similar prices right now";
            }

            // Update UI elements using the binding object
            // Header
            binding.tvPickup.setText("Pickup  •  " + pickup);
            binding.tvDropoff.setText("Dropoff •  " + dropoff);
            binding.tvDistance.setText(String.format("Distance %.1f mi", distanceMi));
            binding.tvTime.setText("Time " + timeMin + " mins");
            binding.tvRide.setText("Ride " + ride);
            binding.tvBanner.setText(bannerText);
            binding.tvDisclaimer.setText(root.optString("disclaimer",
                    "Demo estimates. Final fare shown in the provider app."));

            // Cards — display ranges
            binding.tvUberPrice.setText(money.format(uber.getDouble("price_low")) + " – " + money.format(uber.getDouble("price_high")));
            binding.tvUberEta.setText("ETA: " + uber.getInt("eta_min") + " mins");
            binding.tvLyftPrice.setText(money.format(lyft.getDouble("price_low")) + " – " + money.format(lyft.getDouble("price_high")));
            binding.tvLyftEta.setText("ETA: " + lyft.getInt("eta_min") + " mins");

            // Optional coordinate fields
            double plat = trip.optDouble("pickup_lat", Double.NaN);
            double plng = trip.optDouble("pickup_lng", Double.NaN);
            double dlat = trip.optDouble("drop_lat", Double.NaN);
            double dlng = trip.optDouble("drop_lng", Double.NaN);
            boolean hasCoords = !(Double.isNaN(plat) || Double.isNaN(plng) || Double.isNaN(dlat) || Double.isNaN(dlng));

            // Set click listeners for the deep links
            binding.btnUber.setOnClickListener(v -> {
                if (hasCoords) openUberWithCoords(plat, plng, dlat, dlng);
                else openUberWithAddresses(pickup, dropoff);
            });
            binding.btnLyft.setOnClickListener(v -> {
                if (hasCoords) openLyftWithCoords(plat, plng, dlat, dlng);
                else openLyftWithAddresses(pickup, dropoff);
            });

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            binding.tvBanner.setText("Failed to load trip data: " + e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // Disable buttons if data fails to load
            binding.btnUber.setEnabled(false);
            binding.btnLyft.setEnabled(false);
        }
    }

    private String readAsset(String name) throws IOException {
        try (InputStream is = getAssets().open(name)) {
            byte[] buf = new byte[is.available()];
            is.read(buf);
            return new String(buf, StandardCharsets.UTF_8);
        }
    }

    // --- Deep links using coordinates (preferred) ---
    private void openUberWithCoords(double plat, double plng, double dlat, double dlng) {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .authority("m.uber.com")
                .path("ul/")
                .appendQueryParameter("action", "setPickup")
                .appendQueryParameter("pickup[latitude]", String.valueOf(plat))
                .appendQueryParameter("pickup[longitude]", String.valueOf(plng))
                .appendQueryParameter("dropoff[latitude]", String.valueOf(dlat))
                .appendQueryParameter("dropoff[longitude]", String.valueOf(dlng))
                .build();
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    private void openLyftWithCoords(double plat, double plng, double dlat, double dlng) {
        // Try the native app first
        Uri app = new Uri.Builder()
                .scheme("lyft")
                .authority("ridetype")
                .appendQueryParameter("id", "lyft")
                .appendQueryParameter("pickup[latitude]", String.valueOf(plat))
                .appendQueryParameter("pickup[longitude]", String.valueOf(plng))
                .appendQueryParameter("destination[latitude]", String.valueOf(dlat))
                .appendQueryParameter("destination[longitude]", String.valueOf(dlng))
                .build();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, app));
        } catch (Exception e) {
            // Fallback to web
            Uri web = new Uri.Builder()
                    .scheme("https")
                    .authority("lyft.com")
                    .path("ride")
                    .appendQueryParameter("id", "lyft")
                    .appendQueryParameter("pickup[latitude]", String.valueOf(plat))
                    .appendQueryParameter("pickup[longitude]", String.valueOf(plng))
                    .appendQueryParameter("destination[latitude]", String.valueOf(dlat))
                    .appendQueryParameter("destination[longitude]", String.valueOf(dlng))
                    .build();
            startActivity(new Intent(Intent.ACTION_VIEW, web));
        }
    }

    // --- Deep links using formatted addresses (fallback) ---
    private void openUberWithAddresses(String pickup, String dropoff) {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .authority("m.uber.com")
                .path("ul/")
                .appendQueryParameter("action", "setPickup")
                .appendQueryParameter("pickup[formatted_address]", pickup)
                .appendQueryParameter("dropoff[formatted_address]", dropoff)
                .build();
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    private void openLyftWithAddresses(String pickup, String dropoff) {
        Uri web = new Uri.Builder()
                .scheme("https")
                .authority("lyft.com")
                .path("ride")
                .appendQueryParameter("id", "lyft")
                .appendQueryParameter("pickup[address]", pickup)
                .appendQueryParameter("destination[address]", dropoff)
                .build();
        startActivity(new Intent(Intent.ACTION_VIEW, web));
    }
}
