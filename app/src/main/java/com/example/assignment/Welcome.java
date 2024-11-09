package com.example.assignment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;

public class Welcome extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is logged in, skip the Welcome screen and go to Home
            startActivity(new Intent(Welcome.this, Home.class));
            finish(); // Close the Welcome screen so user can't go back
            return; // Ensure the rest of the code doesn't execute
        }

        // User is not logged in, show the Welcome screen with options to Sign In or Sign Up
        Button signInButton = findViewById(R.id.signInButton);
        Button signUpButton = findViewById(R.id.signUpButton);

        signInButton.setOnClickListener(v -> {
            Intent intent = new Intent(Welcome.this, LoginActivity.class);
            startActivity(intent);
        });

        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(Welcome.this, SignUpActivity.class);
            startActivity(intent);
        });
    }
}
