package com.example.assignment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp; // Import Firestore Timestamp
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddTaskActivity extends AppCompatActivity {

    private EditText taskTitle; // EditText for task title
    private EditText taskDescription; // EditText for task description
    private TextView selectedDateTime; // TextView to display selected date and time
    private Button timePickerButton; // Button to trigger date and time selection
    private Button saveTaskButton; // Button to save the task
    private ImageButton backButton; // ImageButton for back navigation

    private FirebaseFirestore db; // Firestore instance
    private Timestamp dueDateTimestamp = null; // Firestore Timestamp for due date

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        taskTitle = findViewById(R.id.taskTitle);
        taskDescription = findViewById(R.id.taskDescription);
        selectedDateTime = findViewById(R.id.selectedDateTime);
        timePickerButton = findViewById(R.id.timePickerButton);
        saveTaskButton = findViewById(R.id.saveTaskButton);
        backButton = findViewById(R.id.backButton);

        // Set click listener for the back button
        backButton.setOnClickListener(v -> {
            // Create an intent to navigate to HomeActivity
            Intent intent = new Intent(AddTaskActivity.this, Home.class);
            startActivity(intent); // Start HomeActivity
            finish(); // Optionally finish AddTaskActivity
        });

        // Set click listener for the time picker button
        timePickerButton.setOnClickListener(v -> showDateTimePicker());

        // Set click listener for the save task button
        saveTaskButton.setOnClickListener(v -> saveTask());
    }

    private void showDateTimePicker() {
        // Get current date and time
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Create DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Once the date is selected, show the time picker
                    TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                            (timeView, selectedHour, selectedMinute) -> {
                                // Display selected date and time
                                String dateTime = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear + " " +
                                        String.format("%02d:%02d", selectedHour, selectedMinute);
                                selectedDateTime.setText(dateTime);

                                // Store the selected date and time as a Firestore Timestamp
                                Calendar selectedCalendar = Calendar.getInstance();
                                selectedCalendar.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute, 0);
                                dueDateTimestamp = new Timestamp(selectedCalendar.getTime());
                            }, hour, minute, true);
                    timePickerDialog.show();
                }, year, month, day);
        datePickerDialog.show();
    }

    private void saveTask() {
        // Get user input
        String title = taskTitle.getText().toString().trim();
        String description = taskDescription.getText().toString().trim();

        // Validate input
        if (title.isEmpty()) {
            taskTitle.setError("Title is required");
            taskTitle.requestFocus();
            return;
        }
        if (description.isEmpty()) {
            taskDescription.setError("Description is required");
            taskDescription.requestFocus();
            return;
        }
        if (dueDateTimestamp == null) {
            Toast.makeText(this, "Please select a due date and time for the task.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the user is authenticated
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Log.e("AddTaskActivity", "User is not authenticated");
            Toast.makeText(this, "Please log in to save a task.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the current user's ID
        String userId = auth.getCurrentUser().getUid();
        Log.d("AddTaskActivity", "User ID: " + userId);

        // Create a new task object to save
        Map<String, Object> task = new HashMap<>();
        task.put("title", title);
        task.put("description", description);
        task.put("duedate", dueDateTimestamp); // Store the Firestore Timestamp as the due date
        task.put("status", "pending"); // Set default status to pending
        task.put("userid", userId); // Add user ID
        Log.d("AddTaskActivity", "Task data: " + task);

        // Save to Firestore
        db.collection("tasks")
                .add(task)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddTaskActivity.this, "Task added", Toast.LENGTH_SHORT).show();
                    Log.d("AddTaskActivity", "Task added successfully with ID: " + documentReference.getId());
                    taskTitle.setText(""); // Clear input fields after saving the task
                    taskDescription.setText("");
                    selectedDateTime.setText("");
                    finish(); // Close activity and return to previous screen
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddTaskActivity.this, "Error adding task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("AddTaskActivity", "Error adding task", e);
                });
    }
}
