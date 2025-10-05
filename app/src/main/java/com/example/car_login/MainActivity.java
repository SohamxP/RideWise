package com.example.car_login;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    Button planRideBtn;
    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);

        planRideBtn = findViewById(R.id.planRideBtn);
        bottomNav = findViewById(R.id.bottomNav);

        planRideBtn.setOnClickListener(v ->
                Toast.makeText(this, "Plan Ride Clicked!", Toast.LENGTH_SHORT).show()
        );


        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId(); // Get the ID once

            if (itemId == R.id.nav_search) {
                Toast.makeText(this, "Search Selected", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_trips) {
                Toast.makeText(this, "Trips Selected", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_settings) {
                Toast.makeText(this, "Settings Selected", Toast.LENGTH_SHORT).show();
                return true;
            }

            return false;
        });

    }
}
