package com.example.car_login;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SavingsDashboardActivity extends AppCompatActivity {

    // The fix is here: added a space
    private Spinner filterSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savings_dashboard); // This links to your XML layout

        // Find views by their ID from the XML
        filterSpinner = findViewById(R.id.filterSpinner);

        // Set up a listener for the spinner to react to user selections
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item as a string
                String selected = parent.getItemAtPosition(position).toString();

                // Show a temporary message (Toast) with the selected filter
                Toast.makeText(SavingsDashboardActivity.this,
                        "Filter: " + selected, Toast.LENGTH_SHORT).show();

                // Here you would add logic to update the ride history list based on the filter
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // This is called when the selection disappears, usually no action is needed
            }
        });
    }
}
