package com.utsa.kpstore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.utsa.kpstore.models.ListApp;

import java.util.ArrayList;
import java.util.List;

public class DeveloperViewApps extends AppCompatActivity {

    private RecyclerView appsRecyclerView;
    private LinearLayout emptyStateLayout;
    private Button addFirstAppButton;
    private AppAdapter appAdapter;
    private DatabaseReference appsReference, developerAppListReference;
    private FirebaseAuth firebaseAuth;
    private List<ListApp> developerAppsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer_view_apps);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            navigateToLogin();
            return;
        }

        appsReference = FirebaseDatabase.getInstance().getReference("apps");
        developerAppListReference = FirebaseDatabase.getInstance().getReference("developerAppList");

        initViews();
        setupClickListeners();
        loadDeveloperApps();
    }

    private void initViews() {
        appsRecyclerView = findViewById(R.id.appsRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        addFirstAppButton = findViewById(R.id.addFirstAppButton);

        developerAppsList = new ArrayList<>();
        appAdapter = new AppAdapter(developerAppsList);
        appsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        appsRecyclerView.setAdapter(appAdapter);
    }

    private void setupClickListeners() {
        addFirstAppButton.setOnClickListener(v -> navigateToAddApp());

        appAdapter.setOnAppClickListener(app -> {
            Intent intent = new Intent(DeveloperViewApps.this, DeveloperAppStatusActivity.class);
            intent.putExtra("app_id", app.getId());
            startActivity(intent);
        });

        appAdapter.setOnAppLongClickListener(app -> {
            showAppOptionsDialog(app);
        });
    }

    private void showAppOptionsDialog(ListApp app) {
        java.util.List<String> optionsList = new java.util.ArrayList<>();
        optionsList.add("Edit");
        optionsList.add("Delete");

        // If app is rejected, add option to view feedback
        if ("rejected".equals(app.getStatus())) {
            optionsList.add("View Admin Feedback");
        }

        String[] options = optionsList.toArray(new String[0]);

        new android.app.AlertDialog.Builder(this)
                .setTitle(app.getName() + (app.getStatus() != null ? " (" + app.getStatus().toUpperCase() + ")" : ""))
                .setItems(options, (dialog, which) -> {
                    String selected = options[which];
                    if ("Edit".equals(selected)) {
                        Intent intent = new Intent(DeveloperViewApps.this, EditAppActivity.class);
                        intent.putExtra("app_id", app.getId());
                        startActivity(intent);
                    } else if ("Delete".equals(selected)) {
                        confirmDeleteApp(app);
                    } else if ("View Admin Feedback".equals(selected)) {
                        showAdminFeedback(app);
                    }
                })
                .show();
    }

    private void showAdminFeedback(ListApp app) {
        DatabaseReference appDetailsRef = FirebaseDatabase.getInstance().getReference("appDetails").child(app.getId());
        appDetailsRef.child("adminReview")
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(
                            @androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        String review = snapshot.getValue(String.class);
                        if (review != null && !review.isEmpty()) {
                            new android.app.AlertDialog.Builder(DeveloperViewApps.this)
                                    .setTitle("Admin Feedback")
                                    .setMessage(review)
                                    .setPositiveButton("Edit App", (dialog, which) -> {
                                        Intent intent = new Intent(DeveloperViewApps.this, EditAppActivity.class);
                                        intent.putExtra("app_id", app.getId());
                                        startActivity(intent);
                                    })
                                    .setNegativeButton("Close", null)
                                    .show();
                        } else {
                            Toast.makeText(DeveloperViewApps.this, "No feedback available", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(
                            @androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                        Toast.makeText(DeveloperViewApps.this, "Failed to load feedback", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmDeleteApp(ListApp app) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete App")
                .setMessage("Are you sure you want to delete \"" + app.getName() + "\"? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteApp(app))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteApp(ListApp app) {
        String appId = app.getId();
        String userId = firebaseAuth.getCurrentUser().getUid();

        DatabaseReference appDetailsRef = FirebaseDatabase.getInstance().getReference("appDetails").child(appId);
        DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference("reviews").child(appId);

        // Delete from all locations
        appsReference.child(appId).removeValue();
        appDetailsRef.removeValue();
        developerAppListReference.child(userId).child(appId).removeValue();
        reviewsRef.removeValue();

        Toast.makeText(this, "App deleted successfully", Toast.LENGTH_SHORT).show();
    }

    private void loadDeveloperApps() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        String userId = currentUser.getUid();

        // Load app IDs from developerAppList
        developerAppListReference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                developerAppsList.clear();

                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    // Iterate through app IDs
                    for (DataSnapshot appIdSnapshot : snapshot.getChildren()) {
                        String appId = appIdSnapshot.getKey();
                        // Load app details from apps collection
                        loadAppDetails(appId);
                    }
                } else {
                    // No apps found
                    appsRecyclerView.setVisibility(View.GONE);
                    emptyStateLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DeveloperViewApps.this,
                        "Failed to load apps: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAppDetails(String appId) {
        appsReference.child(appId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ListApp app = snapshot.getValue(ListApp.class);
                    if (app != null && !developerAppsList.contains(app)) {
                        developerAppsList.add(app);

                        // Update UI
                        if (developerAppsList.isEmpty()) {
                            appsRecyclerView.setVisibility(View.GONE);
                            emptyStateLayout.setVisibility(View.VISIBLE);
                        } else {
                            appsRecyclerView.setVisibility(View.VISIBLE);
                            emptyStateLayout.setVisibility(View.GONE);
                            appAdapter.setAppList(developerAppsList);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DeveloperViewApps.this,
                        "Error loading app details: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToAddApp() {
        Intent intent = new Intent(DeveloperViewApps.this, AddAppActivity.class);
        startActivity(intent);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(DeveloperViewApps.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}