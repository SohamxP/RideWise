package com.example.car_login;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WaitAndSaveActivity extends AppCompatActivity {

    private Button btnStartTimer, btnChangeWindow, btnUber, btnLyft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_and_save);

        btnStartTimer = findViewById(R.id.btnStartTimer);
        btnChangeWindow = findViewById(R.id.btnChangeWindow);
        btnUber = findViewById(R.id.btnUber);
        btnLyft = findViewById(R.id.btnLyft);

        btnStartTimer.setOnClickListener(v ->
                Toast.makeText(this, "10-minute wait timer started!", Toast.LENGTH_SHORT).show());

        btnChangeWindow.setOnClickListener(v ->
                Toast.makeText(this, "Change waiting window clicked", Toast.LENGTH_SHORT).show());

        btnUber.setOnClickListener(v ->
                Toast.makeText(this, "Opening Uber...", Toast.LENGTH_SHORT).show());

        btnLyft.setOnClickListener(v ->
                Toast.makeText(this, "Opening Lyft...", Toast.LENGTH_SHORT).show());
    }
}
