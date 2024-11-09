package com.example.assignment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

public class CompletedTasksActivity extends AppCompatActivity {

    private RecyclerView completedTasksRecyclerView;
    private TextView noCompletedTasksTextView;
    private TaskAdapter completedTaskAdapter;
    private ArrayList<Task> completedTaskList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Button goBackButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_tasks);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        noCompletedTasksTextView = findViewById(R.id.noCompletedTasksTextView);
        completedTasksRecyclerView = findViewById(R.id.completedTasksRecyclerView);
        goBackButton = findViewById(R.id.goBackButton);  // Reference the Go Back Button

        // Initialize list and adapter
        completedTaskList = new ArrayList<>();
        completedTaskAdapter = new TaskAdapter(completedTaskList, this);

        // Set up RecyclerView with adapter
        completedTasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        completedTasksRecyclerView.setAdapter(completedTaskAdapter);

        // Load completed tasks
        loadCompletedTasks();

        // Set the "Go Back" button click listener
        goBackButton.setOnClickListener(v -> {
            // Navigate back to the Home Activity (MainActivity or other)
            Intent intent = new Intent(CompletedTasksActivity.this, Home.class);
            startActivity(intent); // Start the home activity

            // Optional: Finish the current activity to ensure it doesn't remain in the back stack
            finish();
        });
    }

    private void loadCompletedTasks() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e("CompletedTasks", "User is not logged in.");
            return;
        }

        db.collection("tasks")
                .whereEqualTo("status", "Complete") // Only show completed tasks
                .whereEqualTo("userid", currentUser.getUid()) // Filter by current user ID
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        completedTaskList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Task completedTaskData = document.toObject(Task.class);
                            completedTaskData.setId(document.getId()); // Set document ID as task ID
                            completedTaskList.add(completedTaskData);
                        }
                        completedTaskAdapter.notifyDataSetChanged(); // Notify adapter of data change
                        toggleNoCompletedTasksMessage();
                    } else {
                        Log.e("CompletedTasks", "Error getting completed tasks: ", task.getException());
                    }
                });
    }

    private void toggleNoCompletedTasksMessage() {
        if (completedTaskList.isEmpty()) {
            noCompletedTasksTextView.setVisibility(View.VISIBLE);
        } else {
            noCompletedTasksTextView.setVisibility(View.GONE);
        }
    }
}
