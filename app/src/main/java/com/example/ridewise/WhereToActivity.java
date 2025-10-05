package com.example.ridewise;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

// This import is now correct because the package name matches
import com.example.car_login.R;

public class WhereToActivity extends AppCompatActivity {

    EditText pickupInput, destinationInput;
    ImageButton backBtn;
    ImageView mapPreview;
    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_where_to);

        pickupInput = findViewById(R.id.pickupInput);
        destinationInput = findViewById(R.id.destinationInput);
        backBtn = findViewById(R.id.backBtn);
        mapPreview = findViewById(R.id.mapPreview);
        bottomNav = findViewById(R.id.bottomNav);

        backBtn.setOnClickListener(v ->
                // This could be changed to finish() to close the activity
                Toast.makeText(this, "Back clicked", Toast.LENGTH_SHORT).show()
        );

        // STEP 2: Replaced the switch statement with if-else if
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_search) {
                Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_trips) {
                Toast.makeText(this, "Trips", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_settings) {
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }
}
