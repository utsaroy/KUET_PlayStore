package com.utsa.kpstore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.utsa.kpstore.models.User;

public class ProfileActivity extends AppCompatActivity {

    private android.widget.TextView nameText, emailText;
    private Button developerButton, logoutButton;
    private LinearLayout developerSection;
    private CardView addAppCard, viewAppsCard;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private User currentUserData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            goToLogin();
            return;
        }

        String userId = currentUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

        initViews();
        setupClickListeners();
        loadUserData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }

    private void initViews() {
        nameText = findViewById(R.id.nameText);
        emailText = findViewById(R.id.emailText);
        developerButton = findViewById(R.id.developerButton);
        logoutButton = findViewById(R.id.logoutButton);
        developerSection = findViewById(R.id.developerSection);
        addAppCard = findViewById(R.id.addAppCard);
        viewAppsCard = findViewById(R.id.viewAppsCard);
    }

    private void setupClickListeners() {
        logoutButton.setOnClickListener(v -> userLogout());
        developerButton.setOnClickListener(v -> handleDeveloperButtonClick());

        addAppCard.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AddAppActivity.class);
            startActivity(intent);
        });

        viewAppsCard.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, DeveloperViewApps.class);
            startActivity(intent);
        });
    }

    private void loadUserData() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            emailText.setText(currentUser.getEmail());
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            currentUserData = user;
                            nameText.setText(user.getName());
                            updateUIForUserType(user);
                        } else {
                            nameText.setText("User");
                        }
                    } else {
                        nameText.setText("User");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateUIForUserType(User user) {
        if (user.isDeveloper()) {
            // Show developer section, hide developer button
            developerSection.setVisibility(View.VISIBLE);
            developerButton.setVisibility(View.GONE);
        } else {
            // Hide developer section, show developer button
            developerSection.setVisibility(View.GONE);
            updateDeveloperButton(user);
        }
    }

    private void updateDeveloperButton(User user) {
        String requestStatus = user.getDeveloperRequestStatus();
        if (requestStatus == null)
            requestStatus = "none";

        switch (requestStatus) {
            case "pending":
                developerButton.setText("Request Pending");
                developerButton.setEnabled(false);
                developerButton.setVisibility(View.VISIBLE);
                break;
            case "rejected":
                developerButton.setText("Request Again");
                developerButton.setVisibility(View.VISIBLE);
                break;
            default:
                developerButton.setText("Be a Developer");
                developerButton.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void handleDeveloperButtonClick() {
        Intent intent = new Intent(ProfileActivity.this, BecomeAdmin.class);
        startActivity(intent);
    }

    private void userLogout() {
        firebaseAuth.signOut();
        goToLogin();
    }

    private void goToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}