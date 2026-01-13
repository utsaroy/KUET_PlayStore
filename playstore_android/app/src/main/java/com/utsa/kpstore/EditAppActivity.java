package com.utsa.kpstore;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.utsa.kpstore.models.AppDetails;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditAppActivity extends AppCompatActivity {

    private EditText appNameField, versionField, shortDescriptionField, fullDescriptionField, apkUrlField, appSizeField;
    private Spinner categorySpinner;
    private TextView errorText;
    private Button updateButton, cancelButton, selectIconButton;
    private ImageView iconPreview;
    private ProgressBar loadingProgress;

    private DatabaseReference appDetailsReference, appsReference;
    private String appId;
    private AppDetails currentAppDetails;

    private Uri selectedIconUri = null;
    private String iconBase64 = null;
    private boolean iconChanged = false;

    private final String[] categories = {
            "Select Category", "Education", "Entertainment", "Games",
            "Productivity", "Social", "Tools", "Utilities", "Other"
    };

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedIconUri = uri;
                    iconPreview.setImageURI(uri);
                    iconBase64 = convertImageToBase64(uri);
                    iconChanged = true;
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_app);

        appId = getIntent().getStringExtra("app_id");
        if (appId == null) {
            Toast.makeText(this, "App ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        appDetailsReference = FirebaseDatabase.getInstance().getReference("appDetails").child(appId);
        appsReference = FirebaseDatabase.getInstance().getReference("apps").child(appId);

        initViews();
        setupCategorySpinner();
        setupClickListeners();
        loadAppDetails();
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
        updateButton = findViewById(R.id.updateButton);
        cancelButton = findViewById(R.id.cancelButton);
        iconPreview = findViewById(R.id.iconPreview);
        selectIconButton = findViewById(R.id.selectIconButton);
        loadingProgress = findViewById(R.id.loadingProgress);
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private void setupClickListeners() {
        updateButton.setOnClickListener(v -> updateApp());
        cancelButton.setOnClickListener(v -> finish());
        selectIconButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
    }

    private void loadAppDetails() {
        loadingProgress.setVisibility(View.VISIBLE);

        appDetailsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loadingProgress.setVisibility(View.GONE);
                if (snapshot.exists()) {
                    currentAppDetails = snapshot.getValue(AppDetails.class);
                    if (currentAppDetails != null) {
                        populateFields();
                    }
                } else {
                    Toast.makeText(EditAppActivity.this, "App not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingProgress.setVisibility(View.GONE);
                Toast.makeText(EditAppActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateFields() {
        appNameField.setText(currentAppDetails.getAppName());
        versionField.setText(currentAppDetails.getVersion());
        shortDescriptionField.setText(currentAppDetails.getShortDescription());
        fullDescriptionField.setText(currentAppDetails.getDescription());
        apkUrlField.setText(currentAppDetails.getApkUrl());
        appSizeField.setText(String.valueOf(currentAppDetails.getFileSize()));

        // Set category spinner
        String category = currentAppDetails.getCategory();
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(category)) {
                categorySpinner.setSelection(i);
                break;
            }
        }

        // Load icon
        String existingIcon = currentAppDetails.getIconUrl();
        if (existingIcon != null && !existingIcon.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(existingIcon, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                iconPreview.setImageBitmap(bitmap);
                iconBase64 = existingIcon;
            } catch (Exception e) {
                iconPreview.setImageResource(R.drawable.logo);
            }
        }
    }

    private void updateApp() {
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

        updateButton.setEnabled(false);
        updateButton.setText("Updating...");

        String currentDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        long fileSizeInBytes = (long) Double.parseDouble(appSize);

        // Update AppDetails
        currentAppDetails.setAppName(appName);
        currentAppDetails.setVersion(version);
        currentAppDetails.setShortDescription(shortDescription);
        currentAppDetails.setDescription(fullDescription);
        currentAppDetails.setApkUrl(apkUrl);
        currentAppDetails.setCategory(category);
        currentAppDetails.setFileSize(fileSizeInBytes);
        currentAppDetails.setLastUpdated(currentDate);
        if (iconChanged && iconBase64 != null) {
            currentAppDetails.setIconUrl(iconBase64);
        }
        currentAppDetails.setStatus("pending");

        // Save to database
        appDetailsReference.setValue(currentAppDetails)
                .addOnSuccessListener(aVoid -> {
                    // Update apps list entry too
                    appsReference.child("name").setValue(appName);
                    appsReference.child("title").setValue(shortDescription);
                    appsReference.child("category").setValue(category);
                    appsReference.child("status").setValue("pending");
                    if (iconChanged && iconBase64 != null) {
                        appsReference.child("iconUrl").setValue(iconBase64);
                    }

                    Toast.makeText(EditAppActivity.this, "App updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    showError("Failed to update: " + e.getMessage());
                    updateButton.setEnabled(true);
                    updateButton.setText("Update App");
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
            showError("Please enter a valid URL");
            return false;
        }
        if (appSize.isEmpty()) {
            showError("App size is required");
            return false;
        }
        try {
            if (Double.parseDouble(appSize) <= 0) {
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

    private String convertImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null)
                inputStream.close();

            int maxSize = 200;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float scale = Math.min((float) maxSize / width, (float) maxSize / height);
            if (scale < 1) {
                width = Math.round(width * scale);
                height = Math.round(height * scale);
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.encodeToString(imageBytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        errorText.setVisibility(View.GONE);
    }
}
