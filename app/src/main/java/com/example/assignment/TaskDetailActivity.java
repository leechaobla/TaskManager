package com.example.assignment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

public class TaskDetailActivity extends AppCompatActivity {

    private TextView taskDetailTitleTextView;
    private TextView taskDetailDescriptionTextView;
    private TextView taskDetailStatusTextView;
    private Button markCompleteButton;
    private FirebaseFirestore db;
    private String taskId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get taskId from intent
        taskId = getIntent().getStringExtra("taskId");

        // Initialize UI elements
        taskDetailTitleTextView = findViewById(R.id.taskDetailTitleTextView);
        taskDetailDescriptionTextView = findViewById(R.id.taskDetailDescriptionTextView);
        taskDetailStatusTextView = findViewById(R.id.taskDetailStatusTextView);
        markCompleteButton = findViewById(R.id.markCompleteButton);

        // Load task details
        loadTaskDetails();

        // Set up button click listener to mark the task as complete
        markCompleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markTaskAsComplete();
            }
        });
    }

    private void loadTaskDetails() {
        // Fetch task data from Firestore using taskId
        db.collection("tasks").document(taskId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Task task = documentSnapshot.toObject(Task.class);
                if (task != null) {
                    taskDetailTitleTextView.setText(task.getTitle());
                    taskDetailDescriptionTextView.setText(task.getDescription());
                    taskDetailStatusTextView.setText("Status: " + task.getStatus());

                    // Disable button if task is already completed
                    if ("completed".equalsIgnoreCase(task.getStatus())) {
                        markCompleteButton.setEnabled(false);
                    }
                }
            }
        }).addOnFailureListener(e -> {
            // Handle any errors
            taskDetailTitleTextView.setText("Error loading task");
        });
    }

    private void markTaskAsComplete() {
        // Update task status in Firestore to "completed"
        db.collection("tasks").document(taskId).update("status", "completed")
                .addOnSuccessListener(aVoid -> {
                    taskDetailStatusTextView.setText("Status: Completed");
                    markCompleteButton.setEnabled(false); // Disable button after marking as complete
                })
                .addOnFailureListener(e -> {
                    // Handle failure to update
                    taskDetailStatusTextView.setText("Failed to mark as complete");
                });
    }
}
