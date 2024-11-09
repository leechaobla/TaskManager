package com.example.assignment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.util.Log;
import java.util.ArrayList;

public class Home extends AppCompatActivity {

    private RecyclerView tasksRecyclerView;
    private TextView noTasksTextView;
    private Button addTaskButton;
    private TaskAdapter taskAdapter;
    private ArrayList<Task> taskList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if the user is logged in, and redirect to login if not
        checkUserLogin();

        // Initialize views
        noTasksTextView = findViewById(R.id.noTasksTextView);
        tasksRecyclerView = findViewById(R.id.tasksRecyclerView);
        addTaskButton = findViewById(R.id.addTaskButton);

        // Initialize Firestore and list
        db = FirebaseFirestore.getInstance();
        taskList = new ArrayList<>();

        // Set up RecyclerView with adapter
        taskAdapter = new TaskAdapter(taskList, this);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tasksRecyclerView.setAdapter(taskAdapter);

        // Load tasks from Firestore
        loadActiveTasks();

        // Set up button to add tasks
        addTaskButton.setOnClickListener(v -> {
            startActivity(new Intent(Home.this, AddTaskActivity.class));
        });

        // Set up Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Already on Home
                return true;
            } else if (itemId == R.id.nav_tasks) {
                startActivity(new Intent(Home.this, CompletedTasksActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(Home.this, com.example.assignment.ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadActiveTasks(); // Reload tasks when the activity resumes
    }

    private void checkUserLogin() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // User is not logged in, redirect to LoginActivity
            Log.d("FirebaseAuth", "User is not logged in, redirecting to LoginActivity.");
            startActivity(new Intent(Home.this, LoginActivity.class));
            finish(); // Finish the Home Activity
        } else {
            // User is logged in
            Log.d("FirebaseAuth", "User is logged in: " + currentUser.getEmail());
        }
    }

    private void loadActiveTasks() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            checkUserLogin();
            return;
        }

        db.collection("tasks")
                .whereEqualTo("status", "pending") // Only show pending tasks
                .whereEqualTo("userid", currentUser.getUid()) // Filter by current user ID
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        taskList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Task taskData = document.toObject(Task.class);
                            taskData.setId(document.getId()); // Set document ID as task ID
                            taskList.add(taskData);
                        }
                        taskAdapter.notifyDataSetChanged(); // Notify adapter of data change
                        toggleNoTasksMessage();

                        // Enqueue the LateTaskWorker to check overdue tasks
                        enqueueLateTaskWorker();
                    } else {
                        Log.e("Home", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void enqueueLateTaskWorker() {
        // Create a one-time work request for the LateTaskWorker
        OneTimeWorkRequest lateTaskWorkRequest = new OneTimeWorkRequest.Builder(LateTaskWorker.class)
                .build();

        // Enqueue the work to check and update late tasks
        WorkManager.getInstance(this).enqueue(lateTaskWorkRequest);
    }

    private void toggleNoTasksMessage() {
        if (taskList.isEmpty()) {
            noTasksTextView.setVisibility(View.VISIBLE);
        } else {
            noTasksTextView.setVisibility(View.GONE);
        }
    }
}
