package com.example.ridewise;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private TextView nameText;
    private TextView emailText;
    private TextView passwordText;
    private TextView totalRidesText;
    private TextView totalSavingsText;
    private TextView memberSinceText;
    private TextView profileInitials;
    private Button deleteAccountBtn;
    private Button logoutBtn;
    private ImageButton editNameBtn;
    private ImageButton editEmailBtn;
    private ImageButton editPasswordBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        auth = FirebaseAuth.getInstance();

        initViews();
        loadUserProfile();
        setupClickListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_history) {
            Intent intent = new Intent(this, SavingsDashboardActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_profile) {
            // Already on Profile
            return true;
        } else if (id == R.id.action_home) {
            startActivity(new Intent(this, WelcomePageActivity.class));
            return true;
        } else if (id == R.id.action_savings) {
            startActivity(new Intent(this, SavingsDashboardActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        nameText = findViewById(R.id.nameText);
        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
        totalRidesText = findViewById(R.id.totalRidesText);
        totalSavingsText = findViewById(R.id.totalSavingsText);
        memberSinceText = findViewById(R.id.memberSinceText);
        profileInitials = findViewById(R.id.profileInitials);
        deleteAccountBtn = findViewById(R.id.deleteAccountBtn);
        logoutBtn = findViewById(R.id.logoutBtn); // Added logic
        editNameBtn = findViewById(R.id.editNameBtn);
        editEmailBtn = findViewById(R.id.editEmailBtn);
        editPasswordBtn = findViewById(R.id.editPasswordBtn);
    }

    private void loadUserProfile() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();

            if (name != null && !name.isEmpty()) {
                nameText.setText(name);
                String initials = getInitials(name);
                profileInitials.setText(initials);
                profileInitials.setVisibility(View.VISIBLE);
                findViewById(R.id.profileImage).setVisibility(View.GONE);
            } else {
                nameText.setText("No Name Set");
                profileInitials.setVisibility(View.GONE);
                findViewById(R.id.profileImage).setVisibility(View.VISIBLE);
            }

            if (email != null) {
                emailText.setText(email);
            }
        }
    }

    private String getInitials(String name) {
        String[] parts = name.split(" ");
        if (parts.length > 1) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        } else if (name.length() > 0) {
            return name.substring(0, 1).toUpperCase();
        }
        return "";
    }

    private void setupClickListeners() {
        deleteAccountBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Delete Account functionality to be implemented", Toast.LENGTH_SHORT).show();
        });

        logoutBtn.setOnClickListener(v -> logout());

        editNameBtn.setOnClickListener(v -> showEditNameDialog());

        editEmailBtn.setOnClickListener(v -> {
            Toast.makeText(this, "To change email, please contact support or re-register.", Toast.LENGTH_LONG).show();
        });

        editPasswordBtn.setOnClickListener(v -> {
             FirebaseUser user = auth.getCurrentUser();
             if (user != null && user.getEmail() != null) {
                 auth.sendPasswordResetEmail(user.getEmail())
                     .addOnCompleteListener(task -> {
                         if (task.isSuccessful()) {
                             Toast.makeText(ProfileActivity.this, "Password reset email sent!", Toast.LENGTH_LONG).show();
                         } else {
                             Toast.makeText(ProfileActivity.this, "Failed to send reset email.", Toast.LENGTH_SHORT).show();
                         }
                     });
             }
        });
    }

    private void showEditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Name");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setText(nameText.getText().toString());
        builder.setView(input);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString().trim();
                if (!newName.isEmpty()) {
                    updateName(newName);
                }
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateName(String newName) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            loadUserProfile(); // Refresh UI
                            Toast.makeText(ProfileActivity.this, "Name updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ProfileActivity.this, "Failed to update name", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
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