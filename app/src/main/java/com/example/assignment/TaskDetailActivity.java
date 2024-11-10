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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

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

        checkAndMarkAsLate();

        // Set up a handler to check if task is late periodically (every 5 minutes)
        Handler handler = new Handler();
        Runnable checkLateTaskRunnable = new Runnable() {
            @Override
            public void run() {
                checkAndMarkAsLate();
                handler.postDelayed(this, 60000); // Check every 1 min
            }
        };
        handler.post(checkLateTaskRunnable);
    }



    // Loads task details from Firebase
    private void loadTaskDetails() {
        if (taskId == null) {
            Toast.makeText(this, "Task ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference taskRef = db.collection("tasks").document(taskId);

        // Retrieve the task data from Firestore
        taskRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String title = documentSnapshot.getString("title");
                        String description = documentSnapshot.getString("description");
                        String status = documentSnapshot.getString("status");
                        Timestamp dueDateTimestamp = documentSnapshot.getTimestamp("duedate");

                        // Set values to the views
                        titleTextView.setText(title);
                        descriptionTextView.setText(description);
                        statusTextView.setText("Status: " + status);

                        // Convert Timestamp to formatted date and time string and display
                        if (dueDateTimestamp != null) {
                            Date dueDate = dueDateTimestamp.toDate();
                            // Format to display both date and time
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
                            String formattedDate = dateFormat.format(dueDate);
                            dueDateTextView.setText("Due date: " + formattedDate);
                        } else {
                            dueDateTextView.setText("Due Date: Not set");
                        }
                    } else {
                        Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to retrieve task details", Toast.LENGTH_SHORT).show();
                });
    }

    // Shows a confirmation dialog before deleting the task
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(TaskDetailActivity.this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes", (dialog, which) -> deleteTask())
                .setNegativeButton("No", null)
                .create()
                .show();
    }

    // Marks the task as complete in Firestore and updates the UI
    private void markTaskAsComplete() {
        if (taskId == null) {
            Toast.makeText(this, "Task ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference taskRef = db.collection("tasks").document(taskId);

        // Update Firestore document status to "Complete"
        taskRef.update("status", "Complete")
                .addOnSuccessListener(aVoid -> {
                    updateStatusUI("Complete");
                    Toast.makeText(this, "Task marked as complete", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to mark task as complete", Toast.LENGTH_SHORT).show();
                });
    }

    // Helper method to update the status UI
    private void updateStatusUI(String status) {
        statusTextView.setText("Status: " + status);
        statusTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
    }

    // Deletes the task from Firestore
    private void deleteTask() {
        if (taskId == null) {
            Toast.makeText(this, "Task ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference taskRef = db.collection("tasks").document(taskId);

        // Delete task document
        taskRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete task", Toast.LENGTH_SHORT).show();
                });
    }
    private void checkAndMarkAsLate() {
        if (taskId == null) {
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference taskRef = db.collection("tasks").document(taskId);

        taskRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String status = documentSnapshot.getString("status");
                Timestamp dueDateTimestamp = documentSnapshot.getTimestamp("duedate");

                if (status != null && !status.equals("Complete") && dueDateTimestamp != null) {
                    Date dueDate = dueDateTimestamp.toDate();
                    Date currentDate = new Date();

                    if (currentDate.after(dueDate)) {
                        // Update the task status to "Late" if past due date
                        taskRef.update("status", "Late")
                                .addOnSuccessListener(aVoid -> {
                                    updateStatusUI("Late");
                                    Toast.makeText(this, "Task is now marked as Late", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to mark task as Late", Toast.LENGTH_SHORT).show();
                                });
                    }
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to retrieve task details", Toast.LENGTH_SHORT).show();
        });
    }
}