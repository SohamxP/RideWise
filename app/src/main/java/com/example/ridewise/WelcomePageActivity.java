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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomePageActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private TextView welcomeText;
    private Button startRideBtn;
    private Button viewHistoryBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        auth = FirebaseAuth.getInstance();

        initViews();
        setupClickListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem homeItem = menu.findItem(R.id.action_home);
        if (homeItem != null) {
            homeItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_history) {
            startActivity(new Intent(this, SavingsDashboardActivity.class));
            return true;
        } else if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_savings) {
            startActivity(new Intent(this, SavingsDashboardActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        welcomeText = findViewById(R.id.welcomeText);
        startRideBtn = findViewById(R.id.startRideBtn);
        viewHistoryBtn = findViewById(R.id.viewHistoryBtn);

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                welcomeText.setText("Welcome, " + displayName + "!");
            } else {
                String email = user.getEmail();
                String name = email != null ? email.split("@")[0] : "User";
                welcomeText.setText("Welcome, " + name + "!");
            }
        }
    }

    private void setupClickListeners() {

        startRideBtn.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomePageActivity.this, WhereToActivity.class);
            startActivity(intent);
        });

        viewHistoryBtn.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomePageActivity.this, SavingsDashboardActivity.class);
            startActivity(intent);
        });
    }

    private void logout() {
        auth.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}