package com.example.assignment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Date;
import java.util.Locale;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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

        // Retrieve task data and document ID from the Intent
        Intent intent = getIntent();
        taskId = intent.getStringExtra("TASK_ID");
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        String status = intent.getStringExtra("status");
        Timestamp dueDateTimestamp = intent.getParcelableExtra("duedate");

        // Set task data to TextViews
        titleTextView.setText(title);
        descriptionTextView.setText(description);
        statusTextView.setText("Status: " + status);

        // Convert Timestamp to formatted date string and display
        if (dueDateTimestamp != null) {
            Date dueDate = dueDateTimestamp.toDate();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(dueDate);
            dueDateTextView.setText("Due date: " + formattedDate);
        } else {
            dueDateTextView.setText("Due Date: Not set");
        }

        // Back button action
        backButton.setOnClickListener(v -> finish());

        // Mark as Done button action
        markDoneButton.setOnClickListener(v -> markTaskAsComplete());

        // Delete button action with confirmation dialog
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    // Shows a confirmation dialog before deleting the task
    private void showDeleteConfirmationDialog() {
        // Create the confirmation dialog
        new AlertDialog.Builder(TaskDetailActivity.this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Proceed with deleting the task
                        deleteTask();
                    }
                })
                .setNegativeButton("No", null) // No action if "No" is clicked
                .create()
                .show();
    }

    // Marks the task as complete in Firestore and updates the UI accordingly
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
                    // Update UI to reflect the completed status
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
                    finish();  // Close the TaskDetailActivity after deletion
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete task", Toast.LENGTH_SHORT).show();
                });
    }
}
