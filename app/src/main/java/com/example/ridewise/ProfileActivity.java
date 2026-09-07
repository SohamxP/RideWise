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

import com.example.ridewise.models.RideHistory;
import com.example.ridewise.repository.RideRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private RideRepository rideRepository;

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

        Toolbar toolbar =
                findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar()
                    .setDisplayHomeAsUpEnabled(true);

            getSupportActionBar()
                    .setDisplayShowHomeEnabled(true);
        }

        auth = FirebaseAuth.getInstance();
        rideRepository = new RideRepository();

        initViews();
        loadUserProfile();
        loadProfileStats();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*
         * Refresh stats in case the user compared
         * another trip before returning to Profile.
         */
        loadProfileStats();
    }

    private void initViews() {

        nameText =
                findViewById(R.id.nameText);

        emailText =
                findViewById(R.id.emailText);

        passwordText =
                findViewById(R.id.passwordText);

        totalRidesText =
                findViewById(R.id.totalRidesText);

        totalSavingsText =
                findViewById(R.id.totalSavingsText);

        memberSinceText =
                findViewById(R.id.memberSinceText);

        profileInitials =
                findViewById(R.id.profileInitials);

        deleteAccountBtn =
                findViewById(R.id.deleteAccountBtn);

        logoutBtn =
                findViewById(R.id.logoutBtn);

        editNameBtn =
                findViewById(R.id.editNameBtn);

        editEmailBtn =
                findViewById(R.id.editEmailBtn);

        editPasswordBtn =
                findViewById(R.id.editPasswordBtn);
    }

    private void loadUserProfile() {

        FirebaseUser user =
                auth.getCurrentUser();

        if (user == null) {
            return;
        }

        String name =
                user.getDisplayName();

        String email =
                user.getEmail();

        if (name != null
                && !name.trim().isEmpty()) {

            nameText.setText(name);

            String initials =
                    getInitials(name);

            profileInitials.setText(initials);

            profileInitials.setVisibility(
                    View.VISIBLE
            );

            findViewById(
                    R.id.profileImage
            ).setVisibility(
                    View.GONE
            );

        } else {

            nameText.setText(
                    "No name set"
            );

            profileInitials.setVisibility(
                    View.GONE
            );

            findViewById(
                    R.id.profileImage
            ).setVisibility(
                    View.VISIBLE
            );
        }

        if (email != null) {
            emailText.setText(email);
        }

        loadMemberSince(user);
    }

    private void loadMemberSince(
            FirebaseUser user
    ) {

        if (user.getMetadata() == null) {
            memberSinceText.setText(
                    "Unknown"
            );
            return;
        }

        long creationTimestamp =
                user.getMetadata()
                        .getCreationTimestamp();

        if (creationTimestamp <= 0) {
            memberSinceText.setText(
                    "Unknown"
            );
            return;
        }

        SimpleDateFormat format =
                new SimpleDateFormat(
                        "MMMM yyyy",
                        Locale.US
                );

        String memberSince =
                format.format(
                        new Date(
                                creationTimestamp
                        )
                );

        memberSinceText.setText(
                memberSince
        );
    }

    private void loadProfileStats() {

        totalRidesText.setText("—");
        totalSavingsText.setText("—");

        rideRepository.getRideHistory(
                1000,
                new RideRepository.LoadCallback() {

                    @Override
                    public void onSuccess(
                            List<RideHistory> rides
                    ) {

                        int totalTrips =
                                rides.size();

                        double totalEstimatedSavings =
                                0.0;

                        for (RideHistory ride : rides) {

                            totalEstimatedSavings +=
                                    Math.max(
                                            0.0,
                                            ride.getSavings()
                                    );
                        }

                        totalRidesText.setText(
                                String.valueOf(
                                        totalTrips
                                )
                        );

                        totalSavingsText.setText(
                                String.format(
                                        Locale.US,
                                        "$%.2f",
                                        totalEstimatedSavings
                                )
                        );
                    }

                    @Override
                    public void onError(
                            String error
                    ) {

                        totalRidesText.setText("0");
                        totalSavingsText.setText("$0.00");
                    }
                }
        );
    }

    private String getInitials(
            String name
    ) {

        String trimmed =
                name.trim();

        if (trimmed.isEmpty()) {
            return "";
        }

        String[] parts =
                trimmed.split("\\s+");

        if (parts.length >= 2) {

            return (
                    parts[0].substring(0, 1)
                            + parts[1].substring(0, 1)
            ).toUpperCase(
                    Locale.US
            );
        }

        return trimmed
                .substring(0, 1)
                .toUpperCase(
                        Locale.US
                );
    }

    private void setupClickListeners() {

        /*
         * Hidden in the current layout until
         * proper account deletion is implemented.
         */
        if (deleteAccountBtn != null) {

            deleteAccountBtn.setOnClickListener(
                    v -> Toast.makeText(
                            this,
                            "Account deletion is not available yet.",
                            Toast.LENGTH_SHORT
                    ).show()
            );
        }

        logoutBtn.setOnClickListener(
                v -> logout()
        );

        editNameBtn.setOnClickListener(
                v -> showEditNameDialog()
        );

        editEmailBtn.setOnClickListener(
                v -> Toast.makeText(
                        this,
                        "Email changes are not currently supported.",
                        Toast.LENGTH_LONG
                ).show()
        );

        editPasswordBtn.setOnClickListener(
                v -> sendPasswordReset()
        );
    }

    private void sendPasswordReset() {

        FirebaseUser user =
                auth.getCurrentUser();

        if (user == null
                || user.getEmail() == null) {

            Toast.makeText(
                    this,
                    "No email address is available.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        auth.sendPasswordResetEmail(
                        user.getEmail()
                )
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                ProfileActivity.this,
                                "Password reset email sent.",
                                Toast.LENGTH_LONG
                        ).show();

                    } else {

                        Toast.makeText(
                                ProfileActivity.this,
                                "Could not send password reset email.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void showEditNameDialog() {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        builder.setTitle("Edit name");

        final EditText input =
                new EditText(this);

        input.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_CAP_WORDS
        );

        input.setText(
                nameText.getText()
                        .toString()
        );

        input.setSelectAllOnFocus(true);

        builder.setView(input);

        builder.setPositiveButton(
                "Save",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int which
                    ) {

                        String newName =
                                input.getText()
                                        .toString()
                                        .trim();

                        if (!newName.isEmpty()) {

                            updateName(
                                    newName
                            );
                        }
                    }
                }
        );

        builder.setNegativeButton(
                "Cancel",
                (dialog, which) ->
                        dialog.cancel()
        );

        builder.show();
    }

    private void updateName(
            String newName
    ) {

        FirebaseUser user =
                auth.getCurrentUser();

        if (user == null) {
            return;
        }

        UserProfileChangeRequest profileUpdates =
                new UserProfileChangeRequest.Builder()
                        .setDisplayName(
                                newName
                        )
                        .build();

        user.updateProfile(
                        profileUpdates
                )
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        loadUserProfile();

                        Toast.makeText(
                                ProfileActivity.this,
                                "Name updated",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {

                        Toast.makeText(
                                ProfileActivity.this,
                                "Could not update name",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void logout() {

        auth.signOut();

        Intent intent =
                new Intent(
                        this,
                        AuthActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(
            Menu menu
    ) {

        getMenuInflater().inflate(
                R.menu.main_menu,
                menu
        );

        MenuItem profileItem =
                menu.findItem(
                        R.id.action_profile
                );

        if (profileItem != null) {
            profileItem.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(
            MenuItem item
    ) {

        int id =
                item.getItemId();

        if (id == android.R.id.home) {

            finish();
            return true;

        } else if (
                id == R.id.action_history
        ) {

            startActivity(
                    new Intent(
                            this,
                            SavingsDashboardActivity.class
                    )
            );

            return true;

        } else if (
                id == R.id.action_home
        ) {

            startActivity(
                    new Intent(
                            this,
                            WelcomePageActivity.class
                    )
            );

            return true;

        } else if (
                id == R.id.action_settings
        ) {

            startActivity(
                    new Intent(
                            this,
                            SettingsActivity.class
                    )
            );

            return true;
        }

        return super.onOptionsItemSelected(
                item
        );
    }
}