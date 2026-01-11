package com.example.ridewise;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.ridewise.models.*;
import com.example.ridewise.pricing.RidePricingEngine;
import com.example.ridewise.repository.RideRepository;
import com.example.ridewise.utils.DeepLinkHelper;
import com.google.firebase.auth.FirebaseAuth;

public class TripCompareActivity extends AppCompatActivity {

    private TextView tvPickup;
    private TextView tvDropoff;
    private TextView tvDistance;
    private TextView tvTime;
    private TextView tvBanner;
    private TextView tvUberPrice;
    private TextView tvUberEta;
    private TextView tvLyftPrice;
    private TextView tvLyftEta;
    private Button btnUber;
    private Button btnLyft;

    private RidePricingEngine pricingEngine;
    private TripRequest tripRequest;
    private FirebaseAuth auth;
    private RideRepository repository;  // ADD THIS

    private RideEstimate uberEstimate;
    private RideEstimate lyftEstimate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_compare);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        pricingEngine = new RidePricingEngine();
        auth = FirebaseAuth.getInstance();
        repository = new RideRepository();  // ADD THIS

        double pickupLat = getIntent().getDoubleExtra("pickup_lat", 0.0);
        double pickupLng = getIntent().getDoubleExtra("pickup_lng", 0.0);
        double dropoffLat = getIntent().getDoubleExtra("dropoff_lat", 0.0);
        double dropoffLng = getIntent().getDoubleExtra("dropoff_lng", 0.0);
        String pickupAddress = getIntent().getStringExtra("pickup_address");
        String dropoffAddress = getIntent().getStringExtra("dropoff_address");

        tripRequest = new TripRequest(pickupLat, pickupLng, dropoffLat, dropoffLng,
                pickupAddress, dropoffAddress);

        initViews();
        loadEstimates();
        setupClickListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_history) {
            Intent intent = new Intent(this, SavingsDashboardActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_home) {
            startActivity(new Intent(this, WelcomePageActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        auth.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void initViews() {
        tvPickup = findViewById(R.id.tvPickup);
        tvDropoff = findViewById(R.id.tvDropoff);
        tvDistance = findViewById(R.id.tvDistance);
        tvTime = findViewById(R.id.tvTime);
        tvBanner = findViewById(R.id.tvBanner);
        tvUberPrice = findViewById(R.id.tvUberPrice);
        tvUberEta = findViewById(R.id.tvUberEta);
        tvLyftPrice = findViewById(R.id.tvLyftPrice);
        tvLyftEta = findViewById(R.id.tvLyftEta);
        btnUber = findViewById(R.id.btnUber);
        btnLyft = findViewById(R.id.btnLyft);
        //ImageButton backBtn = findViewById(R.id.backBtn);
        //backBtn.setOnClickListener(v -> finish());
        if (tripRequest.getPickupAddress() != null) {
            tvPickup.setText("Pickup  •  " + tripRequest.getPickupAddress());
        }
        if (tripRequest.getDropoffAddress() != null) {
            tvDropoff.setText("Dropoff •  " + tripRequest.getDropoffAddress());
        }
    }

    private void loadEstimates() {
        uberEstimate = pricingEngine.estimateRide(tripRequest, RideProvider.UBER);
        lyftEstimate = pricingEngine.estimateRide(tripRequest, RideProvider.LYFT);

        updateUI();
    }

    private void updateUI() {
        if (uberEstimate == null || lyftEstimate == null) return;

        // Update prices
        tvUberPrice.setText(String.format("$%.2f - $%.2f",
                uberEstimate.getPriceMin(), uberEstimate.getPriceMax()));
        tvUberEta.setText(String.format("ETA: %d mins", uberEstimate.getEtaMinutes()));

        tvLyftPrice.setText(String.format("$%.2f - $%.2f",
                lyftEstimate.getPriceMin(), lyftEstimate.getPriceMax()));
        tvLyftEta.setText(String.format("ETA: %d mins", lyftEstimate.getEtaMinutes()));

        // Calculate distance and time
        double distance = calculateDistance();
        tvDistance.setText(String.format("Distance: %.1f mi", distance));
        tvTime.setText(String.format("Time: %d mins", (int)((distance / 25.0) * 60)));

        // Show banner for cheaper option
        double uberAvg = uberEstimate.getAveragePrice();
        double lyftAvg = lyftEstimate.getAveragePrice();
        double diff = Math.abs(uberAvg - lyftAvg);

        if (diff > 1.0) {
            if (lyftAvg < uberAvg) {
                tvBanner.setText(String.format("Lyft is $%.2f cheaper right now", diff));
                tvBanner.setBackgroundColor(getColor(android.R.color.holo_blue_light));
            } else {
                tvBanner.setText(String.format("Uber is $%.2f cheaper right now", diff));
                tvBanner.setBackgroundColor(getColor(android.R.color.holo_green_light));
            }
        } else {
            tvBanner.setText("Prices are similar - check Wait & Save for better deals");
        }
    }

    private double calculateDistance() {
        double earthRadius = 3958.8; // miles
        double dLat = Math.toRadians(tripRequest.getDropoffLat() - tripRequest.getPickupLat());
        double dLng = Math.toRadians(tripRequest.getDropoffLng() - tripRequest.getPickupLng());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(tripRequest.getPickupLat())) *
                        Math.cos(Math.toRadians(tripRequest.getDropoffLat())) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    private void setupClickListeners() {
        btnUber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeepLinkHelper.openUber(TripCompareActivity.this, tripRequest);

                // SAVE TO FIREBASE
                saveRideToFirebase(RideProvider.UBER, uberEstimate.getAveragePrice());

                Toast.makeText(TripCompareActivity.this, "Opening Uber...", Toast.LENGTH_SHORT).show();
            }
        });

        btnLyft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeepLinkHelper.openLyft(TripCompareActivity.this, tripRequest);

                // SAVE TO FIREBASE
                saveRideToFirebase(RideProvider.LYFT, lyftEstimate.getAveragePrice());

                Toast.makeText(TripCompareActivity.this, "Opening Lyft...", Toast.LENGTH_SHORT).show();
            }
        });
        // Wait & Save button
        Button btnWaitSave = findViewById(R.id.btnWaitSave);
        btnWaitSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TripCompareActivity.this, WaitAndSaveActivity.class);
                // Pass current price data
                intent.putExtra("current_price", uberEstimate.getAveragePrice());
                startActivity(intent);
            }
        });

// Walk Nearby button
        Button btnWalkNearby = findViewById(R.id.btnWalkNearby);
        btnWalkNearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TripCompareActivity.this, WalkNearbyActivity.class);
                // Pass trip request data
                intent.putExtra("pickup_lat", tripRequest.getPickupLat());
                intent.putExtra("pickup_lng", tripRequest.getPickupLng());
                intent.putExtra("dropoff_lat", tripRequest.getDropoffLat());
                intent.putExtra("dropoff_lng", tripRequest.getDropoffLng());
                startActivity(intent);
            }
        });
    }

    // NEW METHOD: Save ride to Firebase
    private void saveRideToFirebase(RideProvider provider, double price) {
        // Calculate savings (difference from more expensive option)
        double uberPrice = uberEstimate.getAveragePrice();
        double lyftPrice = lyftEstimate.getAveragePrice();
        double savings;

        if (provider == RideProvider.UBER && lyftPrice < uberPrice) {
            savings = 0; // Chose more expensive
        } else if (provider == RideProvider.LYFT && uberPrice < lyftPrice) {
            savings = 0; // Chose more expensive
        } else if (provider == RideProvider.UBER && uberPrice < lyftPrice) {
            savings = lyftPrice - uberPrice; // Saved by choosing Uber
        } else if (provider == RideProvider.LYFT && lyftPrice < uberPrice) {
            savings = uberPrice - lyftPrice; // Saved by choosing Lyft
        } else {
            savings = 0.0;
        }

        RideHistory ride = new RideHistory();
        ride.setProvider(provider);
        ride.setPickupAddress(tripRequest.getPickupAddress());
        ride.setDropoffAddress(tripRequest.getDropoffAddress());
        ride.setDistance(calculateDistance());
        ride.setBasePrice(Math.max(uberPrice, lyftPrice)); // Baseline (more expensive)
        ride.setActualPrice(price);
        ride.setSavings(savings);
        ride.setStrategyUsed("book_now");
        ride.setDate(System.currentTimeMillis());

        // Save to Firebase
        repository.saveRideHistory(ride, new RideRepository.SaveCallback() {
            @Override
            public void onSuccess(String rideId) {
                Toast.makeText(TripCompareActivity.this,
                        String.format("Ride saved! You saved $%.2f", savings),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(TripCompareActivity.this,
                        "Save failed: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}