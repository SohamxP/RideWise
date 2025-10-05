package com.example.car_login;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileManagementActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_management);

        // Here you would wire up click listeners on "Personal Info", "Saved Addresses", etc.
        // Example:
        findViewById(R.id.cardPersonalInfo).setOnClickListener(v ->
                Toast.makeText(this, "Open Personal Info screen", Toast.LENGTH_SHORT).show());

        findViewById(R.id.cardSavedAddresses).setOnClickListener(v ->
                Toast.makeText(this, "Open Saved Addresses screen", Toast.LENGTH_SHORT).show());

        findViewById(R.id.cardPaymentMethods).setOnClickListener(v ->
                Toast.makeText(this, "Open Payment Methods screen", Toast.LENGTH_SHORT).show());
    }
}
