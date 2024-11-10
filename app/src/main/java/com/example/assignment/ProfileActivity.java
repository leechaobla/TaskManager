package com.example.assignment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

public class ProfileActivity extends AppCompatActivity {

    private TextView userNameTextView;
    private TextView userEmailTextView;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize the views
        userNameTextView = findViewById(R.id.userName);
        userEmailTextView = findViewById(R.id.userEmail);
        logoutButton = findViewById(R.id.logoutButton);

        // Display user data from Firebase
        displayUserInfo();

        // Set up the logout button click listener
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmLogout();
            }
        });
    }

    // Fetch and display user info (name and email)
    private void displayUserInfo() {
        // Get the current user from Firebase Authentication
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Get the user's UID and email from Firebase Authentication
            String userUID = user.getUid();
            String userEmail = user.getEmail();

            // Display the user's email in the TextView
            userEmailTextView.setText("Email: " + (userEmail != null ? userEmail : "No email available"));

            // Fetch the user's username from Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("users").document(userUID);

            userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        // Fetch the 'username' field from Firestore
                        String usernameFromFirestore = documentSnapshot.getString("username");

                        // Update the TextView with the fetched username
                        userNameTextView.setText("User Name: " + (usernameFromFirestore != null ? usernameFromFirestore : "No name available"));
                    } else {
                        // If the document doesn't exist, show a default message
                        userNameTextView.setText("User Name: No name available");
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    // If fetching data fails, show an error message
                    userNameTextView.setText("User Name: Error fetching data");
                }
            });

        } else {
            // If no user is logged in, show a default message
            userNameTextView.setText("User Name: Not logged in");
            userEmailTextView.setText("Email: Not logged in");
        }
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

    // Perform the logout action
    private void logout() {
        // Sign out the user from Firebase Authentication
        FirebaseAuth.getInstance().signOut();

        // Redirect to LoginActivity
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear the back stack
        startActivity(intent);

        // Finish the ProfileActivity so the user can't navigate back to it
        finish();
    }
}
