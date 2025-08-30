package com.example.expense_tracker_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SigninActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private Button signinButton;
    private TextView signupLink;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin);

        // Initialize views
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        signinButton = findViewById(R.id.signin);
        signupLink = findViewById(R.id.signup_link);

        // Initialize database helper
        dbHelper = new DBHelper(this);

        // Set click listener for sign-in button
        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // Validate inputs
                if (email.isEmpty()) {
                    emailEditText.setError("Email is required");
                    emailEditText.requestFocus();
                    return;
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailEditText.setError("Enter a valid email address");
                    emailEditText.requestFocus();
                    return;
                }
                if (password.isEmpty()) {
                    passwordEditText.setError("Password is required");
                    passwordEditText.requestFocus();
                    return;
                }

                // Check user credentials
                boolean isValid = dbHelper.checkUserLogin(email, password);
                if (isValid) {
                    Toast.makeText(SigninActivity.this, "Sign-in successful!", Toast.LENGTH_SHORT).show();
                    // Navigate to HomeActivity
                    Intent intent = new Intent(SigninActivity.this, HomeActivity.class);
                    intent.putExtra("user_email", email);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SigninActivity.this, "Invalid email or password.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set click listener for signup link
        signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to SignupActivity
                Intent intent = new Intent(SigninActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }
}