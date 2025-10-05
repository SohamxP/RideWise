package com.example.car_login;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WalkNearbyActivity extends AppCompatActivity {

    private Button btnNavigate, btnUber, btnLyft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk_nearby);

        btnNavigate = findViewById(R.id.btnNavigate);
        btnUber = findViewById(R.id.btnUber);
        btnLyft = findViewById(R.id.btnLyft);

        btnNavigate.setOnClickListener(v ->
                Toast.makeText(this, "Launching navigation to cheaper zone...", Toast.LENGTH_SHORT).show());

        btnUber.setOnClickListener(v ->
                Toast.makeText(this, "Opening Uber...", Toast.LENGTH_SHORT).show());

        btnLyft.setOnClickListener(v ->
                Toast.makeText(this, "Opening Lyft...", Toast.LENGTH_SHORT).show());
    }
}
