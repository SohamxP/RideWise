package com.example.ridewise;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.ridewise.models.TripRequest;
import com.example.ridewise.network.ApiClient;
import com.example.ridewise.network.dto.AnalyzeTripRequest;
import com.example.ridewise.network.dto.WaitAndSaveResponse;
import com.example.ridewise.network.dto.WaitOption;
import com.example.ridewise.utils.DeepLinkHelper;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WaitAndSaveActivity extends AppCompatActivity {

    private TextView tvRecommendationLabel;
    private TextView tvRecommendation;
    private TextView tvSavings;
    private TextView tvExplanation;

    private TextView tvCurrentOption;
    private TextView tv30Option;
    private TextView tv60Option;
    private TextView tv90Option;

    private Button btnUber;
    private Button btnLyft;

    private TripRequest tripRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_and_save);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        double pickupLat =
                getIntent().getDoubleExtra("pickup_lat", 0.0);

        double pickupLng =
                getIntent().getDoubleExtra("pickup_lng", 0.0);

        double dropoffLat =
                getIntent().getDoubleExtra("dropoff_lat", 0.0);

        double dropoffLng =
                getIntent().getDoubleExtra("dropoff_lng", 0.0);

        String pickupAddress =
                getIntent().getStringExtra("pickup_address");

        String dropoffAddress =
                getIntent().getStringExtra("dropoff_address");

        tripRequest = new TripRequest(
                pickupLat,
                pickupLng,
                dropoffLat,
                dropoffLng,
                pickupAddress,
                dropoffAddress
        );

        initViews();
        setupButtons();
        loadWaitAnalysis();
    }

    private void initViews() {
        tvRecommendationLabel =
                findViewById(R.id.tvRecommendationLabel);

        tvRecommendation =
                findViewById(R.id.tvRecommendation);

        tvSavings =
                findViewById(R.id.tvSavings);

        tvExplanation =
                findViewById(R.id.tvExplanation);

        tvCurrentOption =
                findViewById(R.id.tvCurrentOption);

        tv30Option =
                findViewById(R.id.tv30Option);

        tv60Option =
                findViewById(R.id.tv60Option);

        tv90Option =
                findViewById(R.id.tv90Option);

        btnUber =
                findViewById(R.id.btnUber);

        btnLyft =
                findViewById(R.id.btnLyft);

        btnUber.setEnabled(false);
        btnLyft.setEnabled(false);
    }

    private void setupButtons() {
        btnUber.setOnClickListener(v -> {
            DeepLinkHelper.openUber(
                    WaitAndSaveActivity.this,
                    tripRequest
            );
        });

        btnLyft.setOnClickListener(v -> {
            DeepLinkHelper.openLyft(
                    WaitAndSaveActivity.this,
                    tripRequest
            );
        });
    }

    private void loadWaitAnalysis() {
        AnalyzeTripRequest request =
                new AnalyzeTripRequest(
                        tripRequest.getPickupLat(),
                        tripRequest.getPickupLng(),
                        tripRequest.getDropoffLat(),
                        tripRequest.getDropoffLng()
                );

        ApiClient.getApi()
                .waitAndSave(request)
                .enqueue(
                        new Callback<WaitAndSaveResponse>() {

                            @Override
                            public void onResponse(
                                    Call<WaitAndSaveResponse> call,
                                    Response<WaitAndSaveResponse> response
                            ) {

                                if (!response.isSuccessful()
                                        || response.body() == null) {

                                    showError(
                                            "Wait & Save analysis failed. Server returned "
                                                    + response.code()
                                    );

                                    return;
                                }

                                updateUI(response.body());
                            }

                            @Override
                            public void onFailure(
                                    Call<WaitAndSaveResponse> call,
                                    Throwable throwable
                            ) {
                                showError(
                                        throwable.getMessage() != null
                                                ? throwable.getMessage()
                                                : "Could not connect to RideWise backend."
                                );
                            }
                        }
                );
    }

    private void updateUI(
            WaitAndSaveResponse response
    ) {
        if ("wait".equalsIgnoreCase(
                response.getRecommendation()
        )) {
            tvRecommendationLabel.setText(
                    "Historical model suggests waiting"
            );

            tvRecommendation.setText(
                    String.format(
                            "WAIT %d MIN",
                            response.getRecommendedWaitMinutes()
                    )
            );

            tvSavings.setText(
                    String.format(
                            "Potential predicted savings: $%.2f",
                            response.getPotentialSavings()
                    )
            );

            tvExplanation.setText(
                    String.format(
                            "Current lowest estimate: $%.2f • Later estimate: $%.2f",
                            response.getCurrentLowestFare(),
                            response.getRecommendedFare()
                    )
            );

        } else {
            tvRecommendationLabel.setText(
                    "Historical model suggests booking now"
            );

            tvRecommendation.setText(
                    "RIDE NOW"
            );

            tvSavings.setText(
                    "No meaningful predicted savings from waiting."
            );

            tvExplanation.setText(
                    String.format(
                            "Current lowest estimate: $%.2f",
                            response.getCurrentLowestFare()
                    )
            );
        }

        List<WaitOption> options =
                response.getOptions();

        if (options != null) {
            for (WaitOption option : options) {

                String text =
                        String.format(
                                "%s • Uber $%.2f • Lyft $%.2f • Lowest: %s $%.2f",
                                formatWindow(option.getWaitMinutes()),
                                option.getUberFare(),
                                option.getLyftFare(),
                                capitalize(option.getLowestProvider()),
                                option.getLowestFare()
                        );

                switch (option.getWaitMinutes()) {
                    case 0:
                        tvCurrentOption.setText(text);
                        break;

                    case 30:
                        tv30Option.setText(text);
                        break;

                    case 60:
                        tv60Option.setText(text);
                        break;

                    case 90:
                        tv90Option.setText(text);
                        break;
                }
            }
        }

        btnUber.setEnabled(true);
        btnLyft.setEnabled(true);
    }

    private String formatWindow(
            int waitMinutes
    ) {
        if (waitMinutes == 0) {
            return "Now";
        }

        return "+" + waitMinutes + " min";
    }

    private String capitalize(
            String value
    ) {
        if (value == null
                || value.isEmpty()) {
            return "";
        }

        return value.substring(0, 1)
                .toUpperCase()
                + value.substring(1);
    }

    private void showError(
            String message
    ) {
        tvRecommendationLabel.setText(
                "Analysis unavailable"
        );

        tvRecommendation.setText(
                "--"
        );

        tvSavings.setText(
                "Could not calculate Wait & Save recommendation."
        );

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    public boolean onCreateOptionsMenu(
            Menu menu
    ) {
        getMenuInflater().inflate(
                R.menu.main_menu,
                menu
        );

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

        } else if (id == R.id.action_history) {
            startActivity(
                    new Intent(
                            this,
                            SavingsDashboardActivity.class
                    )
            );
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