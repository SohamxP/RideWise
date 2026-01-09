package com.example.ridewise;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.ridewise.models.*;
import com.example.ridewise.prediction.WaitAndSavePredictor;
import com.google.firebase.auth.FirebaseAuth;

public class WaitAndSaveActivity extends AppCompatActivity {
    private Button btnStartTimer;
    private Button btnUber;
    private Button btnLyft;

    private WaitAndSavePredictor predictor;
    private WaitAndSaveAnalysis analysis;
    private FirebaseAuth auth;


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

        predictor = new WaitAndSavePredictor();
        auth = FirebaseAuth.getInstance();

        initViews();
        loadPrediction();
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
        } else if (id == R.id.action_home) {
            startActivity(new Intent(this, WelcomePageActivity.class));
            return true;
        } else if (id == R.id.action_savings) {
            startActivity(new Intent(this, SavingsDashboardActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        btnStartTimer = findViewById(R.id.btnStartTimer);
        btnUber = findViewById(R.id.btnUber);
        btnLyft = findViewById(R.id.btnLyft);
    }

    private void loadPrediction() {
        // Get current estimate (in real app, pass from previous activity)
        RideEstimate currentEstimate = new RideEstimate(
                RideProvider.UBER,
                "Standard",
                18.0,
                24.0,
                5,
                1.5,
                "USD"
        );

        TripRequest tripRequest = new TripRequest(32.7357, -97.1081, 32.7555, -96.7969);

        analysis = predictor.analyzePriceDropProbability(currentEstimate, tripRequest);
    }

    private void setupClickListeners() {
        btnStartTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(WaitAndSaveActivity.this,
                        "Timer started! We'll notify you when prices drop",
                        Toast.LENGTH_LONG).show();
            }
        });

        btnUber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(WaitAndSaveActivity.this, "Opening Uber...", Toast.LENGTH_SHORT).show();
            }
        });

        btnLyft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(WaitAndSaveActivity.this, "Opening Lyft...", Toast.LENGTH_SHORT).show();
            }
        });
    }
}