package com.utsa.kpstore;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.utsa.kpstore.models.AppDetails;

public class AdminAppReviewActivity extends AppCompatActivity {

    private ImageView backIcon, appIcon;
    private TextView appName, appCategory, developerName;
    private TextView statusBadge, appDescription, appVersion, appSize;
    private EditText reviewInput;
    private CardView reviewInputCard;
    private Button approveButton, rejectButton, viewDetailsButton;

    private String appId;
    private AppDetails currentApp;
    private DatabaseReference appDetailsReference;
    private DatabaseReference appsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_app_review);

        appId = getIntent().getStringExtra("app_id");
        if (appId == null) {
            Toast.makeText(this, "App ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        appDetailsReference = FirebaseDatabase.getInstance().getReference("appDetails").child(appId);
        appsReference = FirebaseDatabase.getInstance().getReference("apps").child(appId);

        initViews();
        setupClickListeners();
        loadAppDetails();
    }

    private void initViews() {
        backIcon = findViewById(R.id.backIcon);
        appIcon = findViewById(R.id.appIcon);
        appName = findViewById(R.id.appName);
        appCategory = findViewById(R.id.appCategory);
        developerName = findViewById(R.id.developerName);
        statusBadge = findViewById(R.id.statusBadge);
        appDescription = findViewById(R.id.appDescription);
        appVersion = findViewById(R.id.appVersion);
        appSize = findViewById(R.id.appSize);
        reviewInput = findViewById(R.id.reviewInput);
        reviewInputCard = findViewById(R.id.reviewInputCard);
        approveButton = findViewById(R.id.approveButton);
        rejectButton = findViewById(R.id.rejectButton);
        viewDetailsButton = findViewById(R.id.viewDetailsButton);
    }

    private void setupClickListeners() {
        backIcon.setOnClickListener(v -> finish());

        approveButton.setOnClickListener(v -> approveApp());
        rejectButton.setOnClickListener(v -> rejectApp());

        viewDetailsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AppViewActivity.class);
            intent.putExtra("app_id", appId);
            intent.putExtra("admin_mode", true);
            startActivity(intent);
        });
    }

    private void loadAppDetails() {
        appDetailsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentApp = snapshot.getValue(AppDetails.class);
                    if (currentApp != null) {
                        displayAppDetails();
                    }
                } else {
                    Toast.makeText(AdminAppReviewActivity.this, "App not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminAppReviewActivity.this, "Error loading app", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayAppDetails() {
        appName.setText(currentApp.getAppName());
        appCategory.setText(currentApp.getCategory());
        developerName.setText("by " + currentApp.getDeveloperName());
        appDescription.setText(currentApp.getDescription());
        appVersion.setText(currentApp.getVersion());
        appSize.setText(currentApp.getFormattedFileSize());

        // Load icon
        if (currentApp.getIconUrl() != null && !currentApp.getIconUrl().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(currentApp.getIconUrl(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                appIcon.setImageBitmap(bitmap);
            } catch (Exception e) {
                appIcon.setImageResource(R.drawable.logo);
            }
        }

        // Update status badge
        String status = currentApp.getStatus();
        if (status == null)
            status = "pending";

        switch (status) {
            case "pending":
                statusBadge.setText("PENDING");
                statusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFF9800));
                approveButton.setEnabled(true);
                rejectButton.setEnabled(true);
                break;
            case "approved":
                statusBadge.setText("APPROVED");
                statusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF4CAF50));
                approveButton.setEnabled(false);
                approveButton.setText("Already Approved");
                rejectButton.setText("Revoke");
                break;
            case "rejected":
                statusBadge.setText("REJECTED");
                statusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF44336));
                approveButton.setEnabled(true);
                rejectButton.setEnabled(false);
                rejectButton.setText("Already Rejected");
                // Show previous review
                if (currentApp.getAdminReview() != null) {
                    reviewInput.setText(currentApp.getAdminReview());
                }
                break;
        }
    }

    private void approveApp() {
        new AlertDialog.Builder(this)
                .setTitle("Approve App")
                .setMessage("Are you sure you want to approve \"" + currentApp.getAppName() + "\"?")
                .setPositiveButton("Approve", (dialog, which) -> {
                    appsReference.child("status").setValue("approved");
                    appDetailsReference.child("status").setValue("approved");
                    appDetailsReference.child("adminReview").setValue(null);

                    Toast.makeText(this, "App approved!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void rejectApp() {
        String review = reviewInput.getText().toString().trim();

        if (review.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Feedback Required")
                    .setMessage("Please provide feedback for the developer explaining why the app is being rejected.")
                    .setPositiveButton("OK", null)
                    .show();
            reviewInput.requestFocus();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Reject App")
                .setMessage("Are you sure you want to reject \"" + currentApp.getAppName()
                        + "\" with the provided feedback?")
                .setPositiveButton("Reject", (dialog, which) -> {
                    appsReference.child("status").setValue("rejected");
                    appDetailsReference.child("status").setValue("rejected");
                    appDetailsReference.child("adminReview").setValue(review);

                    Toast.makeText(this, "App rejected with feedback", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
