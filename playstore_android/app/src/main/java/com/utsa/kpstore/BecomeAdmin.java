package com.utsa.kpstore;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.utsa.kpstore.models.Developer;

public class BecomeAdmin extends AppCompatActivity {

    private EditText nameField, emailField, bioField;
    private TextView errorText;
    private Button submitButton;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference developersReference, usersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_become_admin);

        firebaseAuth = FirebaseAuth.getInstance();
        developersReference = FirebaseDatabase.getInstance().getReference("developers");
        usersReference = FirebaseDatabase.getInstance().getReference("users");

        initViews();
        setupClickListeners();
        prefillUserData();
    }

    private void initViews() {
        nameField = findViewById(R.id.nameField);
        emailField = findViewById(R.id.emailField);
        bioField = findViewById(R.id.bioField);
        errorText = findViewById(R.id.errorText);
        submitButton = findViewById(R.id.submitButton);
    }

    private void prefillUserData() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.getDisplayName() != null) {
                nameField.setText(currentUser.getDisplayName());
            }
            if (currentUser.getEmail() != null) {
                emailField.setText(currentUser.getEmail());
            }
        }
    }

    private void setupClickListeners() {
        submitButton.setOnClickListener(v -> submitRequest());
    }

    private void submitRequest() {
        String name = nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String bio = bioField.getText().toString().trim();

        // Validate inputs
        if (name.isEmpty()) {
            showError("Name is required");
            return;
        }

        if (email.isEmpty()) {
            showError("Email is required");
            return;
        }

        if (bio.isEmpty()) {
            showError("Bio/Experience is required");
            return;
        }

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            showError("You must be logged in to submit a request");
            return;
        }

        hideError();
        submitButton.setEnabled(false);
        submitButton.setText("Submitting...");

        String userId = currentUser.getUid();

        // Create Developer object
        Developer developer = new Developer(userId, name, email, bio);

        // Save to developers/userId
        developersReference.child(userId).setValue(developer)
                .addOnSuccessListener(aVoid -> {
                    // Update user's request status
                    usersReference.child(userId).child("developerRequestStatus").setValue("pending")
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(BecomeAdmin.this,
                                        "Request submitted successfully! Please wait for admin approval.",
                                        Toast.LENGTH_LONG).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                showError("Failed to update status: " + e.getMessage());
                                submitButton.setEnabled(true);
                                submitButton.setText("Submit Request");
                            });
                })
                .addOnFailureListener(e -> {
                    showError("Failed to submit request: " + e.getMessage());
                    submitButton.setEnabled(true);
                    submitButton.setText("Submit Request");
                });
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        errorText.setVisibility(View.GONE);
    }
}
