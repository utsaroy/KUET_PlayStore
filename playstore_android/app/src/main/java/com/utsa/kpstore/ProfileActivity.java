package com.utsa.kpstore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.utsa.kpstore.models.User;

public class ProfileActivity extends AppCompatActivity {

    private TextView nameText, emailText;
    private Button developerButton;
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


        nameText = findViewById(R.id.nameText);
        emailText = findViewById(R.id.emailText);
        developerButton = findViewById(R.id.developerButton);
        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> userLogout());

        developerButton.setOnClickListener(v -> handleDeveloperButtonClick());

        loadUserData();
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
                        System.out.println("______________________");
                        System.out.println(user);
                        if (user != null) {
                            currentUserData = user;
                            nameText.setText(user.getName());
                            updateDeveloperButton(user);
                        } else {
                            nameText.setText("User");
                        }
                    } else {
                            nameText.setText("User");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    private void updateDeveloperButton(User user) {
        if (user.isDeveloper()) {
            developerButton.setText("Go to Developer Profile");
            developerButton.setVisibility(View.VISIBLE);
        } else {
            // Check request status
            String requestStatus = user.getDeveloperRequestStatus();
            if (requestStatus == null) requestStatus = "none";
            
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
    }

    private void handleDeveloperButtonClick() {
        if (currentUserData != null && currentUserData.isDeveloper()) {
            Intent intent = new Intent(ProfileActivity.this, DeveloperProfile.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(ProfileActivity.this, BecomeAdmin.class);
            startActivity(intent);
        }
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