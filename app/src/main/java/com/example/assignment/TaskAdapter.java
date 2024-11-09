package com.example.assignment;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import com.google.firebase.Timestamp;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private ArrayList<Task> taskList;
    private Context context;

    public TaskAdapter(ArrayList<Task> taskList, Context context) {
        this.taskList = taskList;
        this.context = context;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        // Set main task information
        holder.titleTextView.setText(task.getTitle());
        holder.descriptionTextView.setText(task.getDescription());
        holder.statusTextView.setText(task.getStatus());

        // Format and set due date for display
        Timestamp dueDateTimestamp = task.getDueDate(); // Assuming dueDate is a Timestamp in Task class
        if (dueDateTimestamp != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDueDate = sdf.format(dueDateTimestamp.toDate());
            holder.taskDueTime.setText(formattedDueDate);
        } else {
            holder.taskDueTime.setText("No due date set");
        }


        // Pass the task data to TaskDetailActivity
        holder.viewDetailsButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, TaskDetailActivity.class);
            intent.putExtra("TASK_ID", task.getId()); // Pass task ID
            intent.putExtra("title", task.getTitle());
            intent.putExtra("description", task.getDescription());
            intent.putExtra("status", task.getStatus());

            // Pass the due date as a Timestamp
            intent.putExtra("duedate", dueDateTimestamp);

            context.startActivity(intent);
        });

        // Handle action buttons
        holder.markCompleteButton.setOnClickListener(v -> {
            markTaskAsComplete(task);
        });

        holder.editButton.setOnClickListener(v -> {
            editTask(task);
        });

        holder.deleteButton.setOnClickListener(v -> {
            deleteTask(task);
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView, descriptionTextView, statusTextView, taskDueTime;
        Button viewDetailsButton, markCompleteButton, editButton, deleteButton;
        LinearLayout expandedDetails;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            taskDueTime = itemView.findViewById(R.id.taskDueTime); // Display the due date
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
            expandedDetails = itemView.findViewById(R.id.expandedDetails);
            markCompleteButton = itemView.findViewById(R.id.markCompleteButton);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    private void markTaskAsComplete(Task task) {
        // Logic to mark task as complete
    }

    private void editTask(Task task) {
        // Logic to edit the task
    }

    private void deleteTask(Task task) {
        // Logic to delete the task
    }
}
