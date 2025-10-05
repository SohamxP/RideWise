package com.example.car_login;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

// Import the generated binding class for your layout
import com.example.car_login.databinding.ActivityRateComparisonBinding;

public class RateComparisonActivity extends AppCompatActivity {

    // Declare the binding object for your layout
    private ActivityRateComparisonBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout using View Binding. This replaces setContentView(R.layout...).
        binding = ActivityRateComparisonBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Call a method to set up all your click listeners
        setupClickListeners();
    }

    private void setupClickListeners() {
        // --- This is how you connect the screens ---

        // 1. Find the Button using the binding object.
        // The Button ID will be generated from your XML (e.g., you need to add an ID to the button).
        // Let's assume you add android:id="@+id/btnBookLyft" to the Lyft button in your XML.

        binding.btnBookLyft.setOnClickListener(v -> {
            // 2. Create an Intent to describe the new screen to open.
            // It specifies the current context (this) and the class of the Activity to launch.
            Intent intent = new Intent(RateComparisonActivity.this, BookingConfirmationActivity.class);

            // 3. (Optional) Pass data to the new screen.
            // For example, you can pass the price and provider name.
            intent.putExtra("PROVIDER_NAME", "Lyft");
            intent.putExtra("PRICE", "22.45");

            // 4. Start the new Activity.
            startActivity(intent);
        });

        // You would do the same for the Uber button
        binding.btnBookUber.setOnClickListener(v -> {
            Intent intent = new Intent(RateComparisonActivity.this, BookingConfirmationActivity.class);
            intent.putExtra("PROVIDER_NAME", "Uber");
            intent.putExtra("PRICE", "24.99");
            startActivity(intent);
        });

        // Handle the back button click
        binding.btnBack.setOnClickListener(v -> {
            // finish() closes the current activity and goes back to the previous one.
            finish();
        });

        // Handle navigation to other primary screens like the Profile
        binding.btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(RateComparisonActivity.this, ProfileManagementActivity.class);
            startActivity(intent);
        });
    }
}
