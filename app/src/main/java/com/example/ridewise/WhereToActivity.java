package com.example.ridewise;
import com.example.ridewise.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.ridewise.utils.GooglePlacesHelper;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WhereToActivity extends AppCompatActivity {

    private AutoCompleteTextView pickupInput;
    private AutoCompleteTextView destinationInput;
    private Button compareRidesBtn;

    private GooglePlacesHelper placesHelper;

    private LatLng pickupLatLng;
    private LatLng dropoffLatLng;

    private SharedPreferences prefs;
    private FirebaseAuth auth;
    private static final String PREFS_NAME = "RideWisePrefs";
    private static final String KEY_PICKUP_HISTORY = "pickup_history";
    private static final String KEY_DROPOFF_HISTORY = "dropoff_history";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_where_to);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        String apiKey = getApiKeyFromManifest();
        android.util.Log.d(
                "RideWiseKeyCheck",
                "API key present: "
                        + (!apiKey.isEmpty())
                        + ", length: "
                        + apiKey.length()
        );
        placesHelper = new GooglePlacesHelper(this, apiKey);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        auth = FirebaseAuth.getInstance();

        initViews();
        setupAutocomplete();
        setupClickListeners();
    }

    private String getApiKeyFromManifest() {
        try {
            android.content.pm.ApplicationInfo ai = getPackageManager()
                    .getApplicationInfo(getPackageName(), android.content.pm.PackageManager.GET_META_DATA);
            Object value = ai.metaData.get("com.google.android.geo.API_KEY");
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void initViews() {
        pickupInput = findViewById(R.id.pickupInput);
        destinationInput = findViewById(R.id.destinationInput);
        compareRidesBtn = findViewById(R.id.compareRidesBtn);
        // backBtn logic moved to Toolbar
    }

    private void setupAutocomplete() {
        // Load history from SharedPreferences
        Set<String> pickupHistory = prefs.getStringSet(KEY_PICKUP_HISTORY, new HashSet<>());
        Set<String> dropoffHistory = prefs.getStringSet(KEY_DROPOFF_HISTORY, new HashSet<>());

        // Convert to lists
        List<String> pickupList = new ArrayList<>(pickupHistory);
        List<String> dropoffList = new ArrayList<>(dropoffHistory);

        // Create adapters
        ArrayAdapter<String> pickupAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                pickupList
        );

        ArrayAdapter<String> dropoffAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                dropoffList
        );

        // Set adapters
        pickupInput.setAdapter(pickupAdapter);
        destinationInput.setAdapter(dropoffAdapter);

        // Show dropdown on focus
        pickupInput.setThreshold(0);
        destinationInput.setThreshold(0);

        pickupInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                pickupInput.showDropDown();
            }
        });

        destinationInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                destinationInput.showDropDown();
            }
        });
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
        } else if (id == R.id.action_home) {
            startActivity(new Intent(this, WelcomePageActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupClickListeners() {
        compareRidesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pickup = pickupInput.getText().toString().trim();
                String destination = destinationInput.getText().toString().trim();

                if (TextUtils.isEmpty(pickup)) {
                    pickupInput.setError("Enter pickup location");
                    pickupInput.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(destination)) {
                    destinationInput.setError("Enter destination");
                    destinationInput.requestFocus();
                    return;
                }

                // Save to history
                saveToHistory(pickup, destination);

                fetchLocationsAndNavigate(pickup, destination);
            }
        });
    }

    private void saveToHistory(String pickup, String destination) {
        // Get existing history
        Set<String> pickupHistory = new HashSet<>(prefs.getStringSet(KEY_PICKUP_HISTORY, new HashSet<>()));
        Set<String> dropoffHistory = new HashSet<>(prefs.getStringSet(KEY_DROPOFF_HISTORY, new HashSet<>()));

        // Add new entries
        pickupHistory.add(pickup);
        dropoffHistory.add(destination);

        // Limit to 10 recent entries
        if (pickupHistory.size() > 10) {
            List<String> list = new ArrayList<>(pickupHistory);
            pickupHistory = new HashSet<>(list.subList(list.size() - 10, list.size()));
        }
        if (dropoffHistory.size() > 10) {
            List<String> list = new ArrayList<>(dropoffHistory);
            dropoffHistory = new HashSet<>(list.subList(list.size() - 10, list.size()));
        }

        prefs.edit()
                .putStringSet(KEY_PICKUP_HISTORY, pickupHistory)
                .putStringSet(KEY_DROPOFF_HISTORY, dropoffHistory)
                .apply();
    }

    private void fetchLocationsAndNavigate(String pickup, String destination) {
        compareRidesBtn.setEnabled(false);
        compareRidesBtn.setText("Finding locations...");

        placesHelper.getCoordinatesFromAddress(pickup, new GooglePlacesHelper.LocationCallback() {
            @Override
            public void onLocationFound(LatLng location, String formattedAddress) {
                pickupLatLng = location;

                placesHelper.getCoordinatesFromAddress(destination, new GooglePlacesHelper.LocationCallback() {
                    @Override
                    public void onLocationFound(LatLng location, String formattedAddress) {
                        dropoffLatLng = location;
                        navigateToComparison(pickup, destination);
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(WhereToActivity.this,
                                "Could not find destination: " + error,
                                Toast.LENGTH_SHORT).show();
                        resetButton();
                    }
                });
            }

            @Override
            public void onError(String error) {
                Toast.makeText(WhereToActivity.this,
                        "Could not find pickup location: " + error,
                        Toast.LENGTH_SHORT).show();
                resetButton();
            }
        });
    }

    private void navigateToComparison(String pickup, String destination) {
        Intent intent = new Intent(this, TripCompareActivity.class);
        intent.putExtra("pickup_address", pickup);
        intent.putExtra("dropoff_address", destination);
        intent.putExtra("pickup_lat", pickupLatLng.latitude);
        intent.putExtra("pickup_lng", pickupLatLng.longitude);
        intent.putExtra("dropoff_lat", dropoffLatLng.latitude);
        intent.putExtra("dropoff_lng", dropoffLatLng.longitude);
        startActivity(intent);

        resetButton();
    }

    private void resetButton() {
        compareRidesBtn.setEnabled(true);
        compareRidesBtn.setText("Compare Rides");
    }
}