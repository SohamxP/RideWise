package com.example.car_login;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.car_login.databinding.ActivityBookingConfirmationBinding;

public class BookingConfirmationActivity extends AppCompatActivity {

    private ActivityBookingConfirmationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingConfirmationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        String providerName = intent.getStringExtra("PROVIDER_NAME");
        String price = intent.getStringExtra("PRICE");

        populateUi(providerName, price);
        setupClickListeners();
    }

    private void populateUi(String provider, String price) {
        // Example static data for the trip details
        binding.txtPickup.setText("123 Main St, Anytown");
        binding.txtDropoff.setText("456 Oak Ave, Sometown");
        binding.txtDistance.setText("5.2 mi");
        binding.txtTime.setText("18 mins");
        binding.txtRideType.setText("Standard");
        binding.imgMapPreview.setImageResource(R.drawable.sample_map);

        // Dynamically update the UI based on the chosen provider
        if ("Lyft".equalsIgnoreCase(provider)) {
            binding.txtLyftPrice.setText(price);
            binding.txtUberPrice.setText("---");
            binding.btnBookUber.setVisibility(View.GONE);
            binding.btnBookLyft.setVisibility(View.VISIBLE);
        } else if ("Uber".equalsIgnoreCase(provider)) {
            binding.txtUberPrice.setText(price);
            binding.txtLyftPrice.setText("---");
            binding.btnBookLyft.setVisibility(View.GONE);
            binding.btnBookUber.setVisibility(View.VISIBLE);
        } else {
            // Fallback if no provider was passed correctly
            binding.txtLyftPrice.setText(price != null ? price : "N/A");
            binding.txtUberPrice.setText(price != null ? price : "N/A");
            Toast.makeText(this, "Provider information not found", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sets up the OnClickListeners for the interactive buttons.
     */
    private void setupClickListeners() {
        // Back navigation
        binding.btnBack.setOnClickListener(v -> finish());

        // Booking button logic with Play Store fallback
        binding.btnBookUber.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("uber://?action=setPickup&pickup=my_location"));
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                // If the app is not found, open its page in the Play Store.
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.ubercab")));
                } catch (ActivityNotFoundException playStoreException) {
                    // If the Play Store is not available, open in a web browser.
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.ubercab")));
                }
            }
        });

        binding.btnBookLyft.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("lyft://ridetype?id=lyft&pickup[latitude]=37.77&pickup[longitude]=-122.41"));
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                // If the app is not found, open its page in the Play Store.
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=me.lyft.android")));
                } catch (ActivityNotFoundException playStoreException) {
                    // If the Play Store is not available, open in a web browser.
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=me.lyft.android")));
                }
            }
        });
    }
}
