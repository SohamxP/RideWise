package com.example.ridewise;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private EditText nameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private Button signupBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        signupBtn = findViewById(R.id.signupBtn);


        TextView loginLink = findViewById(R.id.loginLink);
        loginLink.setOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameInput.getText().toString().trim();
                String email = emailInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();
                String confirmPassword = confirmPasswordInput.getText().toString().trim();

                if (validateInput(name, email, password, confirmPassword)) {
                    signupUser(name, email, password);
                }
            }
        });
    }

    private boolean validateInput(String name, String email, String password, String confirmPassword) {
        if (name.isEmpty()) {
            nameInput.setError("Name required");
            return false;
        }
        if (email.isEmpty()) {
            emailInput.setError("Email required");
            return false;
        }
        if (password.isEmpty() || password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords don't match");
            return false;
        }
        return true;
    }

    private void signupUser(String name, String email, String password) {
        signupBtn.setEnabled(false);
        signupBtn.setText("Creating account...");

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // Save name to Firebase Auth profile
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();

                    if (auth.getCurrentUser() != null) {
                        auth.getCurrentUser().updateProfile(profileUpdates)
                                .addOnSuccessListener(aVoid -> {
                                    // Also save to Firestore
                                    saveUserToFirestore(name, email);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Profile update failed", Toast.LENGTH_SHORT).show();
                                    navigateToWelcome();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Signup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    signupBtn.setEnabled(true);
                    signupBtn.setText("Sign Up");
                });
    }

    private void saveUserToFirestore(String name, String email) {
        String userId = auth.getCurrentUser().getUid();

        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Welcome, " + name + "!", Toast.LENGTH_SHORT).show();
                    navigateToWelcome();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Profile save failed", Toast.LENGTH_SHORT).show();
                    navigateToWelcome();
                });
    }

    private void navigateToWelcome() {
        Intent intent = new Intent(this, WelcomePageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}