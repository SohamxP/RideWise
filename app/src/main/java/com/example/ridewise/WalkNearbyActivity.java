package com.example.ridewise;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.ridewise.calculator.WalkNearbyCalculator;
import com.example.ridewise.models.*;
import com.example.ridewise.utils.DeepLinkHelper;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

public class WalkNearbyActivity extends AppCompatActivity {

    private Button btnNavigate;
    private Button btnUber;
    private Button btnLyft;

    private WalkNearbyCalculator calculator;
    private List<WalkNearbyZone> zones;
    private FirebaseAuth auth;
    private TripRequest tripRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk_nearby);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        double pickupLat = getIntent().getDoubleExtra("pickup_lat", 0.0);
        double pickupLng = getIntent().getDoubleExtra("pickup_lng", 0.0);
        double dropoffLat = getIntent().getDoubleExtra("dropoff_lat", 0.0);
        double dropoffLng = getIntent().getDoubleExtra("dropoff_lng", 0.0);
        tripRequest = new TripRequest(pickupLat, pickupLng, dropoffLat, dropoffLng);

        calculator = new WalkNearbyCalculator();
        auth = FirebaseAuth.getInstance();

        initViews();
        loadNearbyZones();
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
            startActivity(new Intent(this, SavingsDashboardActivity.class));
            return true;
        } else if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_home) {
            startActivity(new Intent(this, WelcomePageActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        btnNavigate = findViewById(R.id.btnNavigate);
        btnUber = findViewById(R.id.btnUber);
        btnLyft = findViewById(R.id.btnLyft);
        ImageView imgMap = findViewById(R.id.imgMap);
    }

    private void loadNearbyZones() {
        zones = calculator.findCheaperZones(tripRequest);
    }

    private void setupClickListeners() {
        btnNavigate.setOnClickListener(v -> {
            if (zones != null && !zones.isEmpty()) {
                WalkNearbyZone zone = zones.get(0);
                String uri = "google.navigation:q=" + zone.getLat() + "," + zone.getLng() + "&mode=w";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            }
        });

        btnUber.setOnClickListener(v -> {
            if (tripRequest != null) {
                DeepLinkHelper.openUber(this, tripRequest);
            }
        });

        btnLyft.setOnClickListener(v -> {
            if (tripRequest != null) {
                DeepLinkHelper.openLyft(this, tripRequest);
            }
        });
    }
}