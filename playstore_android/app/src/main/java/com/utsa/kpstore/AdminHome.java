package com.utsa.kpstore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.utsa.kpstore.models.Developer;

import java.util.ArrayList;
import java.util.List;

public class AdminHome extends AppCompatActivity {

    private RecyclerView requestsRecyclerView;
    private TextView emptyStateText;
    private ProgressBar loadingProgress;
    private DeveloperRequestAdapter requestAdapter;
    private DatabaseReference developersReference;
    private List<Developer> developersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        developersReference = FirebaseDatabase.getInstance().getReference("developers");

        initViews();
        setupRecyclerView();
        loadDevelopers();
    }

    private void initViews() {
        requestsRecyclerView = findViewById(R.id.requestsRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);
        loadingProgress = findViewById(R.id.loadingProgress);

        Button logoutButton = findViewById(R.id.logoutButton);
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> logout());
        }
    }

    private void setupRecyclerView() {
        developersList = new ArrayList<>();
        requestAdapter = new DeveloperRequestAdapter(developersList, this);
        requestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        requestsRecyclerView.setAdapter(requestAdapter);
    }

    private void loadDevelopers() {
        loadingProgress.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);

        developersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                developersList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot devSnapshot : snapshot.getChildren()) {
                        Developer developer = devSnapshot.getValue(Developer.class);
                        if (developer != null) {
                            developersList.add(developer);
                        }
                    }
                }

                loadingProgress.setVisibility(View.GONE);

                if (developersList.isEmpty()) {
                    emptyStateText.setVisibility(View.VISIBLE);
                    requestsRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyStateText.setVisibility(View.GONE);
                    requestsRecyclerView.setVisibility(View.VISIBLE);
                    requestAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingProgress.setVisibility(View.GONE);
                Toast.makeText(AdminHome.this,
                        "Failed to load developers: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void approveDeveloper(Developer developer) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        
        // Update developer status
        developersReference.child(developer.getDeveloperId()).child("approved").setValue(true)
                .addOnSuccessListener(aVoid -> {
                    // Update user to developer
                    usersRef.child(developer.getDeveloperId()).child("developer").setValue(true);
                    usersRef.child(developer.getDeveloperId()).child("developerRequestStatus").setValue("approved");
                    
                    Toast.makeText(this, "Developer approved successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to approve: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void banDeveloper(Developer developer) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        
        // Update developer status
        developersReference.child(developer.getDeveloperId()).child("banned").setValue(true)
                .addOnSuccessListener(aVoid -> {
                    // Update approved to false
                    developersReference.child(developer.getDeveloperId()).child("approved").setValue(false);
                    
                    // Update user status
                    usersRef.child(developer.getDeveloperId()).child("developer").setValue(false);
                    usersRef.child(developer.getDeveloperId()).child("developerRequestStatus").setValue("rejected");
                    
                    Toast.makeText(this, "Developer banned", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to ban: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void unbanDeveloper(Developer developer) {
        // Unban developer
        developersReference.child(developer.getDeveloperId()).child("banned").setValue(false)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Developer unbanned", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to unban: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void logout() {
        Intent intent = new Intent(AdminHome.this, AdminLogin.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}