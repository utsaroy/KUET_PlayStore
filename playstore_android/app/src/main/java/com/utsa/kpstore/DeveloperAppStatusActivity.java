package com.utsa.kpstore;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.utsa.kpstore.models.AppDetails;

public class DeveloperAppStatusActivity extends AppCompatActivity {

    private ImageView backIcon, appIcon;
    private TextView appName, appCategory, appVersion;
    private TextView statusText, statusDescription;
    private CardView adminReviewCard;
    private TextView adminReviewText;
    private Button editAppButton, viewDetailsButton;

    private String appId;
    private DatabaseReference appDetailsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer_app_status);

        appId = getIntent().getStringExtra("app_id");
        if (appId == null) {
            Toast.makeText(this, "App ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupClickListeners();
        loadAppDetails();
    }

    private void initViews() {
        backIcon = findViewById(R.id.backIcon);
        appIcon = findViewById(R.id.appIcon);
        appName = findViewById(R.id.appName);
        appCategory = findViewById(R.id.appCategory);
        appVersion = findViewById(R.id.appVersion);
        statusText = findViewById(R.id.statusText);
        statusDescription = findViewById(R.id.statusDescription);
        adminReviewCard = findViewById(R.id.adminReviewCard);
        adminReviewText = findViewById(R.id.adminReviewText);
        editAppButton = findViewById(R.id.editAppButton);
        viewDetailsButton = findViewById(R.id.viewDetailsButton);
    }

    private void setupClickListeners() {
        backIcon.setOnClickListener(v -> finish());

        editAppButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditAppActivity.class);
            intent.putExtra("app_id", appId);
            startActivity(intent);
        });

        viewDetailsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AppViewActivity.class);
            intent.putExtra("app_id", appId);
            startActivity(intent);
        });
    }

    private void loadAppDetails() {
        appDetailsReference = FirebaseDatabase.getInstance().getReference("appDetails").child(appId);

        appDetailsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    AppDetails app = snapshot.getValue(AppDetails.class);
                    if (app != null) {
                        displayAppDetails(app);
                    }
                } else {
                    Toast.makeText(DeveloperAppStatusActivity.this, "App not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DeveloperAppStatusActivity.this, "Error loading app", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayAppDetails(AppDetails app) {
        appName.setText(app.getAppName());
        appCategory.setText(app.getCategory());
        appVersion.setText("Version " + app.getVersion());

        // Load icon
        if (app.getIconUrl() != null && !app.getIconUrl().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(app.getIconUrl(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                appIcon.setImageBitmap(bitmap);
            } catch (Exception e) {
                appIcon.setImageResource(R.drawable.logo);
            }
        }

        // Update status display
        String status = app.getStatus();
        if (status == null)
            status = "pending";

        switch (status) {
            case "pending":
                statusText.setText("PENDING REVIEW");
                statusText.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFF9800));
                statusDescription.setText(
                        "Your app is currently under review by our team. This usually takes 1-2 business days.");
                adminReviewCard.setVisibility(View.GONE);
                break;
            case "approved":
                statusText.setText("APPROVED");
                statusText.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF4CAF50));
                statusDescription
                        .setText("Congratulations! Your app has been approved and is now visible to all users.");
                adminReviewCard.setVisibility(View.GONE);
                break;
            case "rejected":
                statusText.setText("NEEDS CHANGES");
                statusText.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF44336));
                statusDescription.setText(
                        "Your app requires changes before it can be approved. Please review the admin feedback below.");

                // Show admin review
                String adminReview = app.getAdminReview();
                if (adminReview != null && !adminReview.isEmpty()) {
                    adminReviewCard.setVisibility(View.VISIBLE);
                    adminReviewText.setText(adminReview);
                } else {
                    adminReviewCard.setVisibility(View.VISIBLE);
                    adminReviewText.setText("No specific feedback provided. Please contact support for more details.");
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning from edit
        if (appDetailsReference != null) {
            loadAppDetails();
        }
    }
}
