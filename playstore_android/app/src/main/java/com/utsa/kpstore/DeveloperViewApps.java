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
    private DatabaseReference appsReference;
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
            Intent intent = new Intent(DeveloperViewApps.this, AppViewActivity.class);
            intent.putExtra("app_id", app.getId());
            intent.putExtra("app_name", app.getName());
            intent.putExtra("app_title", app.getTitle());
            startActivity(intent);
        });
    }

    private void loadDeveloperApps() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        String userId = currentUser.getUid();

        appsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                developerAppsList.clear();
                
                if (snapshot.exists()) {
                    for (DataSnapshot appSnapshot : snapshot.getChildren()) {
                        ListApp app = appSnapshot.getValue(ListApp.class);
                        if (app != null) {
                            developerAppsList.add(app);
                        }
                    }
                }

                if (developerAppsList.isEmpty()) {
                    appsRecyclerView.setVisibility(View.GONE);
                    emptyStateLayout.setVisibility(View.VISIBLE);
                } else {
                    appsRecyclerView.setVisibility(View.VISIBLE);
                    emptyStateLayout.setVisibility(View.GONE);
                    appAdapter.setAppList(developerAppsList);
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