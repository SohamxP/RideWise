package com.example.ridewise;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ridewise_settings";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";

    private SwitchCompat switchNotifications;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        preferences = getSharedPreferences(
                PREFS_NAME,
                MODE_PRIVATE
        );

        initViews();
        loadPreferences();
        setupClickListeners();
    }

    private void initViews() {
        switchNotifications =
                findViewById(R.id.switchNotifications);
    }

    private void loadPreferences() {

        boolean notificationsEnabled =
                preferences.getBoolean(
                        KEY_NOTIFICATIONS,
                        true
                );

        switchNotifications.setChecked(
                notificationsEnabled
        );
    }

    private void setupClickListeners() {

        switchNotifications.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    preferences.edit()
                            .putBoolean(
                                    KEY_NOTIFICATIONS,
                                    isChecked
                            )
                            .apply();

                    Toast.makeText(
                            this,
                            isChecked
                                    ? "Notifications enabled"
                                    : "Notifications disabled",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );
    }

    @Override
    public boolean onOptionsItemSelected(
            MenuItem item
    ) {

        if (item.getItemId()
                == android.R.id.home) {

            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}