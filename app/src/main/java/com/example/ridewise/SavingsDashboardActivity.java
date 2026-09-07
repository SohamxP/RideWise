package com.example.ridewise;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ridewise.models.RideHistory;
import com.example.ridewise.repository.RideRepository;

import java.util.ArrayList;
import java.util.List;

public class SavingsDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private LinearLayout historyContent;
    private LinearLayout emptyStateContainer;

    private TextView totalTripsText;
    private TextView totalSavingsText;

    private Button btnCompareRide;

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

        initViews();
        setupClickListeners();
        loadRideHistory();
    }

    private void initViews() {

        recyclerView =
                findViewById(R.id.rideHistoryRecyclerView);

        historyContent =
                findViewById(R.id.historyContent);

        emptyStateContainer =
                findViewById(R.id.emptyStateContainer);

        totalTripsText =
                findViewById(R.id.totalTripsText);

        totalSavingsText =
                findViewById(R.id.totalSavingsText);

        btnCompareRide =
                findViewById(R.id.btnCompareRide);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );
    }

    private void setupClickListeners() {

        btnCompareRide.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            SavingsDashboardActivity.this,
                            WhereToActivity.class
                    );

            startActivity(intent);
        });
    }

    private void loadRideHistory() {

        repository.getRideHistory(
                50,
                new RideRepository.LoadCallback() {

                    @Override
                    public void onSuccess(
                            List<RideHistory> rides
                    ) {

                        rideHistory = rides;

                        setupRecyclerView();
                        updateSummary();
                        updateVisibility();
                    }

                    @Override
                    public void onError(
                            String error
                    ) {

                        rideHistory = new ArrayList<>();

                        setupRecyclerView();
                        updateVisibility();

                        Toast.makeText(
                                SavingsDashboardActivity.this,
                                "Could not load trip history",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void setupRecyclerView() {

        RideHistoryAdapter adapter =
                new RideHistoryAdapter(
                        rideHistory
                );

        recyclerView.setAdapter(adapter);
    }

    private void updateSummary() {

        double totalEstimatedSavings = 0.0;

        for (RideHistory ride : rideHistory) {

            totalEstimatedSavings +=
                    ride.getSavings();
        }

        totalTripsText.setText(
                String.valueOf(
                        rideHistory.size()
                )
        );

        totalSavingsText.setText(
                String.format(
                        "$%.2f",
                        totalEstimatedSavings
                )
        );
    }

    private void updateVisibility() {

        if (rideHistory == null
                || rideHistory.isEmpty()) {

            historyContent.setVisibility(
                    View.GONE
            );

            emptyStateContainer.setVisibility(
                    View.VISIBLE
            );

        } else {

            historyContent.setVisibility(
                    View.VISIBLE
            );

            emptyStateContainer.setVisibility(
                    View.GONE
            );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(
            Menu menu
    ) {

        getMenuInflater().inflate(
                R.menu.main_menu,
                menu
        );

        MenuItem historyItem =
                menu.findItem(
                        R.id.action_history
                );

        if (historyItem != null) {
            historyItem.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(
            MenuItem item
    ) {

        int id = item.getItemId();

        if (id == android.R.id.home) {

            finish();
            return true;

        } else if (id == R.id.action_profile) {

            startActivity(
                    new Intent(
                            this,
                            ProfileActivity.class
                    )
            );

            return true;

        } else if (id == R.id.action_home) {

            startActivity(
                    new Intent(
                            this,
                            WelcomePageActivity.class
                    )
            );

            return true;

        } else if (id == R.id.action_settings) {

            startActivity(
                    new Intent(
                            this,
                            SettingsActivity.class
                    )
            );

            return true;
        }

        return super.onOptionsItemSelected(
                item
        );
    }
}