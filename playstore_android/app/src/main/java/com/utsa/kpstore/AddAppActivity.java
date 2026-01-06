package com.utsa.kpstore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.utsa.kpstore.models.AppDetails;
import com.utsa.kpstore.models.ListApp;
import com.utsa.kpstore.models.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class AddAppActivity extends AppCompatActivity {

    private EditText appNameField, versionField, shortDescriptionField, fullDescriptionField, apkUrlField, appSizeField;
    private Spinner categorySpinner;
    private TextView errorText;
    private Button submitButton, cancelButton;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference, usersReference, appsReference, developerAppListReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_app);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("appDetails");
        appsReference = FirebaseDatabase.getInstance().getReference("apps");
        usersReference = FirebaseDatabase.getInstance().getReference("users");
        developerAppListReference = FirebaseDatabase.getInstance().getReference("developerAppList");

        initViews();
        setupCategorySpinner();
        setupClickListeners();
    }

    private void initViews() {
        appNameField = findViewById(R.id.appNameField);
        versionField = findViewById(R.id.versionField);
        shortDescriptionField = findViewById(R.id.shortDescriptionField);
        fullDescriptionField = findViewById(R.id.fullDescriptionField);
        apkUrlField = findViewById(R.id.apkUrlField);
        appSizeField = findViewById(R.id.appSizeField);
        categorySpinner = findViewById(R.id.categorySpinner);
        errorText = findViewById(R.id.errorText);
        submitButton = findViewById(R.id.submitButton);
        cancelButton = findViewById(R.id.cancelButton);
    }

    private void setupCategorySpinner() {
        String[] categories = {
                "Select Category",
                "Education",
                "Entertainment",
                "Games",
                "Productivity",
                "Social",
                "Tools",
                "Utilities",
                "Other"
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private void setupClickListeners() {
        submitButton.setOnClickListener(v -> submitApp());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void submitApp() {
        String appName = appNameField.getText().toString().trim();
        String version = versionField.getText().toString().trim();
        String shortDescription = shortDescriptionField.getText().toString().trim();
        String fullDescription = fullDescriptionField.getText().toString().trim();
        String apkUrl = apkUrlField.getText().toString().trim();
        String appSize = appSizeField.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();

        if (!validateInputs(appName, version, shortDescription, fullDescription, apkUrl, appSize, category)) {
            return;
        }

        submitButton.setEnabled(false);
        submitButton.setText("Publishing...");

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            showError("You must be logged in to add an app");
            submitButton.setEnabled(true);
            submitButton.setText("Publish App");
            return;
        }

        String userId = currentUser.getUid();
        
        // Get user details
        usersReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        publishApp(user, appName, version, shortDescription, fullDescription, apkUrl, appSize, category);
                    } else {
                        showError("Failed to retrieve user information");
                        submitButton.setEnabled(true);
                        submitButton.setText("Publish App");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showError("Database error: " + error.getMessage());
                submitButton.setEnabled(true);
                submitButton.setText("Publish App");
            }
        });
    }

    private void publishApp(User user, String appName, String version, String shortDescription, 
                          String fullDescription, String apkUrl, String appSize, String category) {
        String appId = UUID.randomUUID().toString();
        String currentDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());

        long fileSizeInBytes = (long) Double.parseDouble(appSize);
        
        AppDetails appDetails = new AppDetails();
        appDetails.setAppId(appId);
        appDetails.setAppName(appName);
        appDetails.setVersion(version);
        appDetails.setShortDescription(shortDescription);
        appDetails.setDescription(fullDescription);
        appDetails.setApkUrl(apkUrl);
        appDetails.setCategory(category);
        appDetails.setDeveloperName(user.getName());
        appDetails.setDeveloperEmail(user.getEmail());
        appDetails.setPublishedDate(currentDate);
        appDetails.setLastUpdated(currentDate);
        appDetails.setRating(0.0);
        appDetails.setTotalRatings(0);
        appDetails.setTotalDownloads(0);
        appDetails.setFileSize(fileSizeInBytes);

        ListApp listApp = new ListApp(appName, shortDescription, appId, category);
        appsReference.child(appId).setValue(listApp).addOnSuccessListener(v->{
            databaseReference.child(appId).setValue(appDetails)
                    .addOnSuccessListener(aVoid -> {
                        String developerId = firebaseAuth.getCurrentUser().getUid();
                        developerAppListReference.child(developerId).child(appId).setValue(true)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(AddAppActivity.this, "App published successfully!", Toast.LENGTH_LONG).show();
                                    clearFields();
                                    submitButton.setEnabled(true);
                                    submitButton.setText("Publish App");

                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    showError("Failed to update developer app list: " + e.getMessage());
                                    submitButton.setEnabled(true);
                                    submitButton.setText("Publish App");
                                });
                    })
                    .addOnFailureListener(e -> {
                        showError("Failed to publish app: " + e.getMessage());
                        submitButton.setEnabled(true);
                        submitButton.setText("Publish App");
                    });
        });

    }

    private boolean validateInputs(String appName, String version, String shortDescription, 
                                  String fullDescription, String apkUrl, String appSize, String category) {
        if (appName.isEmpty()) {
            showError("App name is required");
            return false;
        }

        if (version.isEmpty()) {
            showError("Version is required");
            return false;
        }

        if (shortDescription.isEmpty()) {
            showError("Short description is required");
            return false;
        }

        if (fullDescription.isEmpty()) {
            showError("Full description is required");
            return false;
        }

        if (apkUrl.isEmpty()) {
            showError("APK URL is required");
            return false;
        }

        if (!apkUrl.startsWith("http://") && !apkUrl.startsWith("https://")) {
            showError("Please enter a valid URL starting with http:// or https://");
            return false;
        }

        if (appSize.isEmpty()) {
            showError("App size is required");
            return false;
        }

        try {
            double size = Double.parseDouble(appSize);
            if (size <= 0) {
                showError("App size must be greater than 0");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid number for app size");
            return false;
        }

        if (category.equals("Select Category")) {
            showError("Please select a category");
            return false;
        }

        hideError();
        return true;
    }

    private void clearFields() {
        appNameField.setText("");
        versionField.setText("");
        shortDescriptionField.setText("");
        fullDescriptionField.setText("");
        apkUrlField.setText("");
        appSizeField.setText("");
        categorySpinner.setSelection(0);
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        errorText.setVisibility(View.GONE);
    }
}