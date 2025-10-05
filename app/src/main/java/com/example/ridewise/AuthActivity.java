package com.example.ridewise;
import com.example.car_login.R;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AuthActivity extends AppCompatActivity {

    Button loginBtn, loginTab, signupTab;
    EditText emailInput, passwordInput;
    ImageButton googleBtn, appleBtn, phoneBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        loginBtn = findViewById(R.id.loginBtn);
        loginTab = findViewById(R.id.loginTab);
        signupTab = findViewById(R.id.signupTab);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        googleBtn = findViewById(R.id.googleBtn);
        appleBtn = findViewById(R.id.appleBtn);
        phoneBtn = findViewById(R.id.phoneBtn);

        loginBtn.setOnClickListener(v ->
                Toast.makeText(this, "Login clicked", Toast.LENGTH_SHORT).show()
        );

        googleBtn.setOnClickListener(v ->
                Toast.makeText(this, "Google Login", Toast.LENGTH_SHORT).show()
        );

        appleBtn.setOnClickListener(v ->
                Toast.makeText(this, "Apple Login", Toast.LENGTH_SHORT).show()
        );

        phoneBtn.setOnClickListener(v ->
                Toast.makeText(this, "Phone Login", Toast.LENGTH_SHORT).show()
        );
    }
}
