package com.example.ridewise;

import android.content.Intent;
import android.net.Uri;
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
import com.example.ridewise.network.dto.WalkNearbyOption;
import com.example.ridewise.network.dto.WalkNearbyResponse;
import com.example.ridewise.utils.DeepLinkHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalkNearbyActivity extends AppCompatActivity {

    private TextView tvRecommendationLabel;
    private TextView tvRecommendation;
    private TextView tvWalkInfo;
    private TextView tvSavings;

    private TextView tvCurrentFare;
    private TextView tvAlternateFare;
    private TextView tvProvider;
    private TextView tvRouteInfo;

    private Button btnNavigate;
    private Button btnUber;
    private Button btnLyft;

    private TripRequest originalTrip;
    private TripRequest alternateTrip;

    private WalkNearbyOption bestOption;

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

        originalTrip = new TripRequest(
                pickupLat,
                pickupLng,
                dropoffLat,
                dropoffLng,
                pickupAddress,
                dropoffAddress
        );

        initViews();
        setupButtons();
        analyzeNearbyPickups();
    }

    private void initViews() {

        tvRecommendationLabel =
                findViewById(R.id.tvRecommendationLabel);

        tvRecommendation =
                findViewById(R.id.tvRecommendation);

        tvWalkInfo =
                findViewById(R.id.tvWalkInfo);

        tvSavings =
                findViewById(R.id.tvSavings);

        tvCurrentFare =
                findViewById(R.id.tvCurrentFare);

        tvAlternateFare =
                findViewById(R.id.tvAlternateFare);

        tvProvider =
                findViewById(R.id.tvProvider);

        tvRouteInfo =
                findViewById(R.id.tvRouteInfo);

        btnNavigate =
                findViewById(R.id.btnNavigate);

        btnUber =
                findViewById(R.id.btnUber);

        btnLyft =
                findViewById(R.id.btnLyft);

        btnNavigate.setEnabled(false);
        btnUber.setEnabled(false);
        btnLyft.setEnabled(false);
    }

    private void analyzeNearbyPickups() {

        AnalyzeTripRequest request =
                new AnalyzeTripRequest(
                        originalTrip.getPickupLat(),
                        originalTrip.getPickupLng(),
                        originalTrip.getDropoffLat(),
                        originalTrip.getDropoffLng()
                );

        ApiClient.getApi()
                .walkNearby(request)
                .enqueue(
                        new Callback<WalkNearbyResponse>() {

                            @Override
                            public void onResponse(
                                    Call<WalkNearbyResponse> call,
                                    Response<WalkNearbyResponse> response
                            ) {

                                if (!response.isSuccessful()
                                        || response.body() == null) {

                                    showError(
                                            "Walk Nearby analysis failed. Server returned "
                                                    + response.code()
                                    );

                                    return;
                                }

                                updateUI(response.body());
                            }

                            @Override
                            public void onFailure(
                                    Call<WalkNearbyResponse> call,
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
            WalkNearbyResponse response
    ) {

        bestOption =
                response.getBestOption();

        tvCurrentFare.setText(
                String.format(
                        "Current pickup lowest estimate: $%.2f",
                        response.getCurrentLowestFare()
                )
        );

        if ("walk".equalsIgnoreCase(
                response.getRecommendation()
        ) && bestOption != null) {

            tvRecommendationLabel.setText(
                    "Alternate pickup may reduce your fare"
            );

            tvRecommendation.setText(
                    "WALK NEARBY"
            );

            tvWalkInfo.setText(
                    String.format(
                            "Walk %.1f min • %d m %s",
                            bestOption.getWalkingMinutes(),
                            bestOption.getWalkingDistanceMeters(),
                            bestOption.getDirection()
                    )
            );

            tvSavings.setText(
                    String.format(
                            "Potential predicted savings: $%.2f",
                            response.getPotentialSavings()
                    )
            );

            tvAlternateFare.setText(
                    String.format(
                            "Alternate pickup lowest estimate: $%.2f",
                            response.getRecommendedFare()
                    )
            );

            tvProvider.setText(
                    String.format(
                            "Best historical provider: %s",
                            capitalize(
                                    bestOption.getLowestProvider()
                            )
                    )
            );

            tvRouteInfo.setText(
                    String.format(
                            "From alternate pickup: %.1f mi • %.0f min driving",
                            bestOption.getDrivingMiles(),
                            bestOption.getDrivingMinutes()
                    )
            );

            alternateTrip =
                    new TripRequest(
                            bestOption.getPickupLat(),
                            bestOption.getPickupLon(),
                            originalTrip.getDropoffLat(),
                            originalTrip.getDropoffLng(),
                            "Recommended alternate pickup",
                            originalTrip.getDropoffAddress()
                    );

            btnNavigate.setEnabled(true);
            btnUber.setEnabled(true);
            btnLyft.setEnabled(true);

        } else {

            tvRecommendationLabel.setText(
                    "No worthwhile nearby pickup found"
            );

            tvRecommendation.setText(
                    "STAY HERE"
            );

            tvWalkInfo.setText(
                    "Walking did not produce meaningful predicted savings."
            );

            tvSavings.setText(
                    "No significant savings detected."
            );

            if (bestOption != null) {

                tvAlternateFare.setText(
                        String.format(
                                "Best nearby estimate: $%.2f",
                                bestOption.getLowestFare()
                        )
                );

                tvProvider.setText(
                        String.format(
                                "Best nearby provider: %s",
                                capitalize(
                                        bestOption.getLowestProvider()
                                )
                        )
                );

                tvRouteInfo.setText(
                        String.format(
                                "Nearest evaluated option: %.1f min walk",
                                bestOption.getWalkingMinutes()
                        )
                );

            } else {

                tvAlternateFare.setText(
                        "No valid nearby pickup found."
                );

                tvProvider.setText(
                        ""
                );

                tvRouteInfo.setText(
                        ""
                );
            }

            btnNavigate.setEnabled(false);

            alternateTrip = originalTrip;

            btnUber.setEnabled(true);
            btnLyft.setEnabled(true);
        }
    }

    private void setupButtons() {

        btnNavigate.setOnClickListener(v -> {

            if (bestOption == null) {
                return;
            }

            String uri =
                    "google.navigation:q="
                            + bestOption.getPickupLat()
                            + ","
                            + bestOption.getPickupLon()
                            + "&mode=w";

            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(uri)
                    );

            intent.setPackage(
                    "com.google.android.apps.maps"
            );

            try {
                startActivity(intent);

            } catch (Exception exception) {

                String webUri =
                        "https://www.google.com/maps/dir/?api=1"
                                + "&destination="
                                + bestOption.getPickupLat()
                                + ","
                                + bestOption.getPickupLon()
                                + "&travelmode=walking";

                startActivity(
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(webUri)
                        )
                );
            }
        });

        btnUber.setOnClickListener(v -> {

            TripRequest trip =
                    alternateTrip != null
                            ? alternateTrip
                            : originalTrip;

            DeepLinkHelper.openUber(
                    WalkNearbyActivity.this,
                    trip
            );
        });

        btnLyft.setOnClickListener(v -> {

            TripRequest trip =
                    alternateTrip != null
                            ? alternateTrip
                            : originalTrip;

            DeepLinkHelper.openLyft(
                    WalkNearbyActivity.this,
                    trip
            );
        });
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

        tvWalkInfo.setText(
                "Could not analyze nearby pickup points."
        );

        tvSavings.setText(
                ""
        );

        btnNavigate.setEnabled(false);
        btnUber.setEnabled(false);
        btnLyft.setEnabled(false);

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