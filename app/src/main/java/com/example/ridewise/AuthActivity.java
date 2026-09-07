package com.example.ridewise;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    private EditText emailInput;
    private EditText passwordInput;

    private Button loginBtn;
    private TextView signupLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        auth = FirebaseAuth.getInstance();

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser != null) {
            navigateToWelcome();
            return;
        }

        initViews();
        setupClickListeners();
    }

    private void initViews() {

        emailInput =
                findViewById(R.id.emailInput);

        passwordInput =
                findViewById(R.id.passwordInput);

        loginBtn =
                findViewById(R.id.loginBtn);

        signupLink =
                findViewById(R.id.signupLink);
    }

    private void setupClickListeners() {

        loginBtn.setOnClickListener(v -> {

            String email =
                    emailInput
                            .getText()
                            .toString()
                            .trim();

            String password =
                    passwordInput
                            .getText()
                            .toString()
                            .trim();

            if (validateInput(
                    email,
                    password
            )) {

                loginUser(
                        email,
                        password
                );
            }
        });

        signupLink.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            AuthActivity.this,
                            SignupActivity.class
                    );

            startActivity(intent);
        });
    }

    private boolean validateInput(
            String email,
            String password
    ) {

        if (email.isEmpty()) {

            emailInput.setError(
                    "Email required"
            );

            emailInput.requestFocus();

            return false;
        }

        if (password.isEmpty()) {

            passwordInput.setError(
                    "Password required"
            );

            passwordInput.requestFocus();

            return false;
        }

        if (password.length() < 6) {

            passwordInput.setError(
                    "Password must be at least 6 characters"
            );

            passwordInput.requestFocus();

            return false;
        }

        return true;
    }

    private void loginUser(
            String email,
            String password
    ) {

        loginBtn.setEnabled(false);
        loginBtn.setText("Logging in...");

        auth.signInWithEmailAndPassword(
                        email,
                        password
                )
                .addOnSuccessListener(
                        authResult ->
                                navigateToWelcome()
                )
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            AuthActivity.this,
                            "Login failed. Check your email and password.",
                            Toast.LENGTH_SHORT
                    ).show();

                    loginBtn.setEnabled(true);
                    loginBtn.setText("Log in");
                });
    }

    private void navigateToWelcome() {

        Intent intent =
                new Intent(
                        this,
                        WelcomePageActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}