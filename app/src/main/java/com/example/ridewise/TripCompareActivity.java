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

import com.example.ridewise.models.RideHistory;
import com.example.ridewise.models.RideProvider;
import com.example.ridewise.models.TripRequest;
import com.example.ridewise.network.ApiClient;
import com.example.ridewise.network.dto.AnalyzeTripRequest;
import com.example.ridewise.network.dto.AnalyzeTripResponse;
import com.example.ridewise.network.dto.ProviderPrediction;
import com.example.ridewise.network.dto.RouteInfo;
import com.example.ridewise.repository.RideRepository;
import com.example.ridewise.utils.DeepLinkHelper;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


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
    private Button btnWaitSave;
    private Button btnWalkNearby;

    private TripRequest tripRequest;

    private FirebaseAuth auth;
    private RideRepository repository;

    private ProviderPrediction uberPrediction;
    private ProviderPrediction lyftPrediction;

    private RouteInfo routeInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_compare);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar()
                    .setDisplayHomeAsUpEnabled(true);

            getSupportActionBar()
                    .setDisplayShowHomeEnabled(true);
        }

        auth = FirebaseAuth.getInstance();
        repository = new RideRepository();

        double pickupLat =
                getIntent().getDoubleExtra(
                        "pickup_lat",
                        0.0
                );

        double pickupLng =
                getIntent().getDoubleExtra(
                        "pickup_lng",
                        0.0
                );

        double dropoffLat =
                getIntent().getDoubleExtra(
                        "dropoff_lat",
                        0.0
                );

        double dropoffLng =
                getIntent().getDoubleExtra(
                        "dropoff_lng",
                        0.0
                );

        String pickupAddress =
                getIntent().getStringExtra(
                        "pickup_address"
                );

        String dropoffAddress =
                getIntent().getStringExtra(
                        "dropoff_address"
                );

        tripRequest = new TripRequest(
                pickupLat,
                pickupLng,
                dropoffLat,
                dropoffLng,
                pickupAddress,
                dropoffAddress
        );

        initViews();
        setupClickListeners();

        analyzeTrip();
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

        btnWaitSave = findViewById(R.id.btnWaitSave);
        btnWalkNearby = findViewById(R.id.btnWalkNearby);


        if (tripRequest.getPickupAddress() != null) {
            tvPickup.setText(
                    "Pickup  •  "
                            + tripRequest.getPickupAddress()
            );
        }

        if (tripRequest.getDropoffAddress() != null) {
            tvDropoff.setText(
                    "Dropoff •  "
                            + tripRequest.getDropoffAddress()
            );
        }

        setLoadingState();
    }


    private void setLoadingState() {

        tvUberPrice.setText("Analyzing...");
        tvLyftPrice.setText("Analyzing...");

        tvUberEta.setText(
                "ML prediction loading"
        );

        tvLyftEta.setText(
                "ML prediction loading"
        );

        tvDistance.setText(
                "Distance: calculating..."
        );

        tvTime.setText(
                "Time: calculating..."
        );

        tvBanner.setText(
                "Analyzing route and historical ride data..."
        );

        btnUber.setEnabled(false);
        btnLyft.setEnabled(false);
    }


    private void analyzeTrip() {

        AnalyzeTripRequest request =
                new AnalyzeTripRequest(
                        tripRequest.getPickupLat(),
                        tripRequest.getPickupLng(),
                        tripRequest.getDropoffLat(),
                        tripRequest.getDropoffLng()
                );

        ApiClient.getApi()
                .analyzeTrip(request)
                .enqueue(
                        new Callback<AnalyzeTripResponse>() {

                            @Override
                            public void onResponse(
                                    Call<AnalyzeTripResponse> call,
                                    Response<AnalyzeTripResponse> response
                            ) {

                                if (!response.isSuccessful()
                                        || response.body() == null) {

                                    showAnalysisError(
                                            "Analysis failed. Server returned "
                                                    + response.code()
                                    );

                                    return;
                                }

                                handleAnalysisResponse(
                                        response.body()
                                );
                            }


                            @Override
                            public void onFailure(
                                    Call<AnalyzeTripResponse> call,
                                    Throwable throwable
                            ) {

                                showAnalysisError(
                                        throwable.getMessage() != null
                                                ? throwable.getMessage()
                                                : "Could not connect to RideWise backend."
                                );
                            }
                        }
                );
    }


    private void handleAnalysisResponse(
            AnalyzeTripResponse response
    ) {

        routeInfo = response.getRoute();

        List<ProviderPrediction> predictions =
                response.getPredictions();

        if (routeInfo == null
                || predictions == null
                || predictions.isEmpty()) {

            showAnalysisError(
                    "Incomplete trip analysis received."
            );

            return;
        }


        for (ProviderPrediction prediction : predictions) {

            if ("uber".equalsIgnoreCase(
                    prediction.getProvider()
            )) {

                uberPrediction = prediction;

            } else if ("lyft".equalsIgnoreCase(
                    prediction.getProvider()
            )) {

                lyftPrediction = prediction;
            }
        }


        if (uberPrediction == null
                || lyftPrediction == null) {

            showAnalysisError(
                    "Provider predictions were incomplete."
            );

            return;
        }

        updateUI(response);
    }


    private void updateUI(
            AnalyzeTripResponse response
    ) {

        tvDistance.setText(
                String.format(
                        "Distance: %.1f mi",
                        routeInfo.getTripMiles()
                )
        );

        tvTime.setText(
                String.format(
                        "Traffic-aware time: %.0f mins",
                        routeInfo.getTripMinutes()
                )
        );


        tvUberPrice.setText(
                String.format(
                        "$%.2f",
                        uberPrediction.getEstimatedFare()
                )
        );

        tvUberEta.setText(
                String.format(
                        "Estimated range: $%.2f – $%.2f",
                        uberPrediction.getLowerBound(),
                        uberPrediction.getUpperBound()
                )
        );


        tvLyftPrice.setText(
                String.format(
                        "$%.2f",
                        lyftPrediction.getEstimatedFare()
                )
        );

        tvLyftEta.setText(
                String.format(
                        "Estimated range: $%.2f – $%.2f",
                        lyftPrediction.getLowerBound(),
                        lyftPrediction.getUpperBound()
                )
        );


        double uberFare =
                uberPrediction.getEstimatedFare();

        double lyftFare =
                lyftPrediction.getEstimatedFare();

        double difference =
                Math.abs(
                        uberFare - lyftFare
                );


        if (difference < 1.0) {

            tvBanner.setText(
                    "Historical estimates are very similar"
            );

        } else if (uberFare < lyftFare) {

            tvBanner.setText(
                    String.format(
                            "Lower predicted fare: Uber by $%.2f",
                            difference
                    )
            );

        } else {

            tvBanner.setText(
                    String.format(
                            "Lower predicted fare: Lyft by $%.2f",
                            difference
                    )
            );
        }


        btnUber.setEnabled(true);
        btnLyft.setEnabled(true);
    }


    private void showAnalysisError(
            String message
    ) {

        tvBanner.setText(
                "Trip analysis unavailable"
        );

        tvUberPrice.setText("--");
        tvLyftPrice.setText("--");

        tvUberEta.setText(
                "Prediction unavailable"
        );

        tvLyftEta.setText(
                "Prediction unavailable"
        );

        btnUber.setEnabled(false);
        btnLyft.setEnabled(false);

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }


    private void setupClickListeners() {

        btnUber.setOnClickListener(v -> {

            if (uberPrediction == null) {
                return;
            }

            saveRideToFirebase(
                    RideProvider.UBER,
                    uberPrediction.getEstimatedFare()
            );

            DeepLinkHelper.openUber(
                    TripCompareActivity.this,
                    tripRequest
            );

            Toast.makeText(
                    TripCompareActivity.this,
                    "Opening Uber...",
                    Toast.LENGTH_SHORT
            ).show();
        });


        btnLyft.setOnClickListener(v -> {

            if (lyftPrediction == null) {
                return;
            }

            saveRideToFirebase(
                    RideProvider.LYFT,
                    lyftPrediction.getEstimatedFare()
            );

            DeepLinkHelper.openLyft(
                    TripCompareActivity.this,
                    tripRequest
            );

            Toast.makeText(
                    TripCompareActivity.this,
                    "Opening Lyft...",
                    Toast.LENGTH_SHORT
            ).show();
        });


        /*
         * These two existing features still use heuristic/random logic.
         *
         * We temporarily stop presenting them as functional intelligence
         * until we rebuild them using the new backend.
         */

        btnWaitSave.setOnClickListener(v ->

                Toast.makeText(
                        TripCompareActivity.this,
                        "Wait & Save is being upgraded to use ML predictions.",
                        Toast.LENGTH_SHORT
                ).show()
        );


        btnWalkNearby.setOnClickListener(v ->

                Toast.makeText(
                        TripCompareActivity.this,
                        "Walk Nearby is being upgraded with route intelligence.",
                        Toast.LENGTH_SHORT
                ).show()
        );
    }


    private void saveRideToFirebase(
            RideProvider provider,
            double predictedPrice
    ) {

        if (uberPrediction == null
                || lyftPrediction == null
                || routeInfo == null) {

            return;
        }


        double uberPrice =
                uberPrediction.getEstimatedFare();

        double lyftPrice =
                lyftPrediction.getEstimatedFare();


        double comparisonBaseline =
                Math.max(
                        uberPrice,
                        lyftPrice
                );


        double predictedSavings =
                Math.max(
                        0.0,
                        comparisonBaseline
                                - predictedPrice
                );


        RideHistory ride =
                new RideHistory();

        ride.setProvider(provider);

        ride.setPickupAddress(
                tripRequest.getPickupAddress()
        );

        ride.setDropoffAddress(
                tripRequest.getDropoffAddress()
        );

        ride.setDistance(
                routeInfo.getTripMiles()
        );

        ride.setBasePrice(
                comparisonBaseline
        );

        ride.setActualPrice(
                predictedPrice
        );

        ride.setSavings(
                predictedSavings
        );

        ride.setStrategyUsed(
                "historical_ml_prediction"
        );

        ride.setDate(
                System.currentTimeMillis()
        );


        repository.saveRideHistory(
                ride,
                new RideRepository.SaveCallback() {

                    @Override
                    public void onSuccess(
                            String rideId
                    ) {

                        Toast.makeText(
                                TripCompareActivity.this,
                                "Trip analysis saved",
                                Toast.LENGTH_SHORT
                        ).show();
                    }


                    @Override
                    public void onError(
                            String error
                    ) {

                        Toast.makeText(
                                TripCompareActivity.this,
                                "Could not save trip: "
                                        + error,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
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