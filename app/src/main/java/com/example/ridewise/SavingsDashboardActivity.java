package com.example.ridewise;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ridewise.models.RideHistory;
import com.example.ridewise.repository.RideRepository;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class SavingsDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Spinner filterSpinner;
    private TextView totalSavingsText;
    private TextView emptyStateText;
    private FirebaseAuth auth;
    private RideRepository repository;
    private List<RideHistory> rideHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savings_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        rideHistory = new ArrayList<>();
        repository = new RideRepository();
        auth = FirebaseAuth.getInstance();

        initViews();
        loadRideHistory();
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
            // Already on Savings/History
            return true;
        } else if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (id == R.id.action_home) {
            startActivity(new Intent(this, WelcomePageActivity.class));
            return true;
        } else if (id == R.id.action_savings) {
            // Already on Savings/History
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.rideHistoryRecyclerView);
        filterSpinner = findViewById(R.id.filterSpinner);

        // Add header to show total savings (you can add this to XML)
        totalSavingsText = new TextView(this);
        totalSavingsText.setTextSize(18);
        totalSavingsText.setPadding(16, 16, 16, 16);
        totalSavingsText.setTextColor(getColor(R.color.primary));

        emptyStateText = new TextView(this);
        emptyStateText.setText("No rides yet! Book your first ride to start saving.");
        emptyStateText.setTextSize(16);
        emptyStateText.setTextColor(getColor(R.color.muted));
        emptyStateText.setVisibility(View.GONE);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadRideHistory() {
        repository.getRideHistory(50, new RideRepository.LoadCallback() {
            @Override
            public void onSuccess(List<RideHistory> rides) {
                rideHistory = rides;
                setupRecyclerView();
                calculateTotalSavings(rides);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(SavingsDashboardActivity.this,
                        "Error loading history: " + error,
                        Toast.LENGTH_SHORT).show();
                setupRecyclerView(); // Show empty state
            }
        });
    }

    private void setupRecyclerView() {
        RideHistoryAdapter adapter = new RideHistoryAdapter(rideHistory);
        recyclerView.setAdapter(adapter);

        if (rideHistory.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void calculateTotalSavings(List<RideHistory> rides) {
        double totalSavings = 0;
        int totalRides = rides.size();

        for (RideHistory ride : rides) {
            totalSavings += ride.getSavings();
        }

        String savingsText = String.format("Total Savings: $%.2f\nTotal Rides: %d",
                totalSavings, totalRides);

        Toast.makeText(this, savingsText, Toast.LENGTH_LONG).show();
    }
}