package com.example.assignment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import com.google.firebase.auth.FirebaseAuth;

import com.example.assignment.LoginActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView userNameTextView;
    private TextView userEmailTextView;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        userNameTextView = findViewById(R.id.userName);
        userEmailTextView = findViewById(R.id.userEmail);
        logoutButton = findViewById(R.id.logoutButton);

        // Display user data (replace with actual user data retrieval logic)
        displayUserInfo();

        // Set up the logout button click listener
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmLogout();
            }
        });
    }

    // Display user info (mocked here, replace with actual data if available)
    private void displayUserInfo() {
        // Example user info, replace with data from user session or database
        userNameTextView.setText("User Name: John Doe");
        userEmailTextView.setText("Email: johndoe@example.com");
    }

    // Show a confirmation dialog before logging out
    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    logout();
                })
                .setNegativeButton("No", null)
                .show();
    }

    // Log out and redirect to LoginActivity
    private void logout() {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut();

        // Redirect to login activity
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear the back stack
        startActivity(intent);

        // Finish ProfileActivity so the user can't go back to it
        finish();
    }

}

