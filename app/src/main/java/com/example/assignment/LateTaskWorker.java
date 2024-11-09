package com.example.assignment;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.List;

public class LateTaskWorker extends Worker {

    private FirebaseFirestore db;

    public LateTaskWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            checkAndMarkLateTasks();
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.retry(); // Retry the task in case of failure
        }
    }

    private void checkAndMarkLateTasks() {
        long currentTime = System.currentTimeMillis();

        // Fetch tasks with "pending" status from Firestore
        db.collection("tasks")
                .whereEqualTo("status", "pending") // Filter tasks with 'pending' status
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot document : documents) {
                        if (document instanceof QueryDocumentSnapshot) {
                            QueryDocumentSnapshot queryDoc = (QueryDocumentSnapshot) document;
                            Long dueDate = queryDoc.getLong("timestamp"); // Assuming 'timestamp' field is the due date as a long
                            if (dueDate != null && dueDate < currentTime) {
                                // Update status to "late" if the task is overdue
                                queryDoc.getReference().update("status", "late")
                                        .addOnSuccessListener(aVoid -> {
                                            // Handle success
                                        })
                                        .addOnFailureListener(e -> {
                                            // Handle failure
                                        });
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error fetching documents
                    e.printStackTrace();
                });
    }
}

