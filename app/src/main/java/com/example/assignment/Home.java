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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import android.util.Log;



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
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Home.this, AddTaskActivity.class));
            }
        });

        // Set up Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.BottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    // Already on Home
                    return true;
                } else if (itemId == R.id.nav_tasks) {
                    startActivity(new Intent(Home.this, TasksActivity.class));
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(Home.this, ProfileActivity.class));
                    return true;
                }
                return false;
            }
        });
    }

    private void checkUserLogin() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User is not logged in, redirect to LoginActivity
            startActivity(new Intent(Home.this, LoginActivity.class));
            finish();
        }
    }

    private void loadActiveTasks() {
        db.collection("tasks")
                .whereEqualTo("status", "pending") // Filter tasks with 'pending' status
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        taskList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Task taskData = document.toObject(Task.class);
                            taskList.add(taskData);
                        }
                        taskAdapter.notifyDataSetChanged();
                        toggleNoTasksMessage();
                    } else {
                        // Log or handle errors if needed
                        Log.e("Home", "Error getting documents: ", task.getException());
                    }
                });
    }


    private void toggleNoTasksMessage() {
        if (taskList.isEmpty()) {
            noTasksTextView.setVisibility(View.VISIBLE);
        } else {
            noTasksTextView.setVisibility(View.GONE);
        }
    }
}
