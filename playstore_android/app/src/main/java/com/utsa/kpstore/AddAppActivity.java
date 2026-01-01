package com.utsa.kpstore;

import android.content.Intent;
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
import com.utsa.kpstore.models.ListApp;

import java.util.Random;

public class AddAppActivity extends AppCompatActivity {

    private EditText appNameField;
    private TextView errorText;
    private Button submitButton, logoutButton;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_app);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("apps");

        initViews();

        setupClickListeners();
    }

    private void initViews() {
        appNameField = findViewById(R.id.appNameField);
        errorText = findViewById(R.id.errorText);
        submitButton = findViewById(R.id.submitButton);
        logoutButton = findViewById(R.id.logoutButton);
    }

    private void setupClickListeners() {
        submitButton.setOnClickListener(v -> submitApp());
        logoutButton.setOnClickListener(v -> logout());
    }

    private void submitApp() {
        String appName = appNameField.getText().toString().trim();

        if (!validateInputs(appName, "title")) {
            return;
        }

        submitButton.setEnabled(false);

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            showError("You must be logged in to add an app");
            submitButton.setEnabled(true);
            submitButton.setText("Add App");
            return;
        }

        int appId = generateRandomId();

        ListApp listApp = new ListApp(appName, "", appId);

        databaseReference.child(String.valueOf(appId)).setValue(listApp)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddAppActivity.this, "App added", Toast.LENGTH_SHORT).show();
                    appNameField.setText("");
                    submitButton.setEnabled(true);
                    submitButton.setText("Add App");
                })
                .addOnFailureListener(e -> {
                    showError("Failed to add app: " + e.getMessage());
                    submitButton.setEnabled(true);
                    submitButton.setText("Add App");
                });
    }

    private boolean validateInputs(String appName, String appTitle) {
        if (appName.isEmpty()) {
            showError("App name is required");
            return false;
        }

        if (appTitle.isEmpty()) {
            showError("App title is required");
            return false;
        }

        hideError();
        return true;
    }

    private int generateRandomId() {
        Random random = new Random();
        return 1000 + random.nextInt(9000);
    }

    private void logout() {
        firebaseAuth.signOut();
        Intent intent = new Intent(AddAppActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        errorText.setVisibility(View.GONE);
    }
}