package com.utsa.kpstore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.utsa.kpstore.models.Developer;

public class DeveloperProfile extends AppCompatActivity {

    private TextView developerNameText, developerEmailText;
    private CardView addAppCard, viewAppsCard;
    private Button logoutButton;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference developersReference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        userId = currentUser.getUid();
        developersReference = FirebaseDatabase.getInstance().getReference("developers").child(userId);

        initViews();
        setupClickListeners();
        loadDeveloperData();
    }

    private void initViews() {
        developerNameText = findViewById(R.id.developerNameText);
        developerEmailText = findViewById(R.id.developerEmailText);
        addAppCard = findViewById(R.id.addAppCard);
        viewAppsCard = findViewById(R.id.viewAppsCard);
        logoutButton = findViewById(R.id.logoutButton);
    }

    private void setupClickListeners() {
        addAppCard.setOnClickListener(v -> {
            Intent intent = new Intent(DeveloperProfile.this, AddAppActivity.class);
            startActivity(intent);
        });

        viewAppsCard.setOnClickListener(v -> {
            Intent intent = new Intent(DeveloperProfile.this, DeveloperViewApps.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> logout());
    }

    private void loadDeveloperData() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            developerEmailText.setText(currentUser.getEmail());

            developersReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Developer developer = snapshot.getValue(Developer.class);
                        if (developer != null) {
                            developerNameText.setText(developer.getName());
                            
                            if (!developer.isApproved()) {
                                Toast.makeText(DeveloperProfile.this, 
                                    "Your developer account is pending approval", 
                                    Toast.LENGTH_LONG).show();
                            }
                            
                            if (developer.isBanned()) {
                                Toast.makeText(DeveloperProfile.this, 
                                    "Your developer account has been banned", 
                                    Toast.LENGTH_LONG).show();
                                addAppCard.setEnabled(false);
                                viewAppsCard.setEnabled(false);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(DeveloperProfile.this, 
                        "Failed to load developer data", 
                        Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void logout() {
        getOnBackPressedDispatcher().onBackPressed();
    }
}