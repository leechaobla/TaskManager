package com.example.assignment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class TaskDetailActivity extends AppCompatActivity {

    private TextView titleTextView, descriptionTextView, statusTextView, dueDateTextView;
    private Button markDoneButton, deleteButton;
    private ImageButton backButton;
    private String taskId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        // Initialize Firebase Firestore instance
        db = FirebaseFirestore.getInstance();

        // Initialize views
        titleTextView = findViewById(R.id.taskTitle);
        descriptionTextView = findViewById(R.id.taskDescription);
        statusTextView = findViewById(R.id.taskStatus);
        dueDateTextView = findViewById(R.id.taskDueDate);
        backButton = findViewById(R.id.backButton);
        markDoneButton = findViewById(R.id.markCompleteButton);
        deleteButton = findViewById(R.id.deleteButton);

        // Retrieve task ID from Intent
        Intent intent = getIntent();
        taskId = intent.getStringExtra("TASK_ID");

        // Load task details from Firebase
        loadTaskDetails();

        // Back button action
        backButton.setOnClickListener(v -> finish());

        // Mark as Done button action
        markDoneButton.setOnClickListener(v -> markTaskAsComplete());

        // Delete button action with confirmation dialog
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());

        // Periodically check and mark as late
        startLateCheckHandler();
    }

    // Loads task details from Firebase
    private void loadTaskDetails() {
        if (taskId == null) {
            showToast("Task ID not found");
            finish();
            return;
        }

        DocumentReference taskRef = db.collection("tasks").document(taskId);

        taskRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        titleTextView.setText(documentSnapshot.getString("title"));
                        descriptionTextView.setText(documentSnapshot.getString("description"));
                        updateStatusUI(documentSnapshot.getString("status"));

                        Timestamp dueDateTimestamp = documentSnapshot.getTimestamp("duedate");
                        if (dueDateTimestamp != null) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
                            dueDateTextView.setText("Due date: " + dateFormat.format(dueDateTimestamp.toDate()));
                        } else {
                            dueDateTextView.setText("Due Date: Not set");
                        }
                    } else {
                        showToast("Task not found");
                        finish();
                    }
                })
                .addOnFailureListener(e -> showToast("Failed to retrieve task details"));
    }

    // Confirmation dialog for task deletion
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(TaskDetailActivity.this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes", (dialog, which) -> deleteTask())
                .setNegativeButton("No", null)
                .create()
                .show();
    }

    // Mark task as complete in Firestore
    private void markTaskAsComplete() {
        if (taskId == null) {
            showToast("Task ID not found");
            return;
        }

        DocumentReference taskRef = db.collection("tasks").document(taskId);

        taskRef.update("status", "Complete")
                .addOnSuccessListener(aVoid -> {
                    updateStatusUI("Complete");
                    showToast("Task marked as complete");
                })
                .addOnFailureListener(e -> showToast("Failed to mark task as complete"));
    }

    // Helper method to update the status UI
    private void updateStatusUI(String status) {
        statusTextView.setText("Status: " + status);
        int color = status.equals("Complete") ? android.R.color.holo_green_dark
                : status.equals("Late") ? android.R.color.holo_red_dark
                : status.equals("pending")? android.R.color.holo_orange_light
                : android.R.color.black;
        statusTextView.setTextColor(getResources().getColor(color));
    }

    // Deletes the task from Firestore
    private void deleteTask() {
        if (taskId == null) {
            showToast("Task ID not found");
            return;
        }

        DocumentReference taskRef = db.collection("tasks").document(taskId);

        taskRef.delete()
                .addOnSuccessListener(aVoid -> {
                    showToast("Task deleted");


                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("task_deleted", true);
                    setResult(RESULT_OK, resultIntent);

                    // Close this activity (TaskDetailsActivity)
                    finish();
                })
                .addOnFailureListener(e -> showToast("Failed to delete task"));
    }

    // Check if the task is late and update status if necessary
    private void checkAndMarkAsLate() {
        if (taskId == null) {
            return;
        }

        DocumentReference taskRef = db.collection("tasks").document(taskId);

        taskRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String status = documentSnapshot.getString("status");
                Timestamp dueDateTimestamp = documentSnapshot.getTimestamp("duedate");

                if (!"Complete".equals(status) && dueDateTimestamp != null) {
                    if (new Date().after(dueDateTimestamp.toDate())) {
                        taskRef.update("status", "Late")
                                .addOnSuccessListener(aVoid -> {
                                    updateStatusUI("Late");
                                    showToast("Task is now marked as Late");
                                })
                                .addOnFailureListener(e -> showToast("Failed to mark task as Late"));
                    }
                }
            }
        }).addOnFailureListener(e -> showToast("Failed to retrieve task details"));
    }

    // Periodic handler to check if the task is late
    private void startLateCheckHandler() {
        Handler handler = new Handler();
        Runnable checkLateTaskRunnable = new Runnable() {
            @Override
            public void run() {
                checkAndMarkAsLate();
                handler.postDelayed(this, 60000); // Check every 5 minutes
            }
        };
        handler.post(checkLateTaskRunnable);
    }

    // Show Toast helper method
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
