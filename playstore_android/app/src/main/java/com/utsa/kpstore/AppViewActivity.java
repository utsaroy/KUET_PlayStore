package com.utsa.kpstore;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.utsa.kpstore.models.AppDetails;

import java.util.HashMap;
import java.util.Map;

public class AppViewActivity extends AppCompatActivity {

    private TextView appName, developerName, category, rating, totalDownloads, fileSize;
    private TextView shortDescription, description, version, lastUpdated, developerEmail;
    private Button installButton;

    private AppDetails appDetails;
    private ImageView backIcon, favoriteIcon;
    private ProgressBar progressBar;
    
    private DatabaseReference databaseReference;
    private DatabaseReference favoritesReference;
    private DatabaseReference downloadsReference;
    private String appId;
    private FirebaseAuth mAuth;
    private String userId;
    private boolean isFavorite = false;
    private ValueEventListener favoriteListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_view);

        // Initialize views
        initViews();
        backIcon = findViewById(R.id.back_icon);
        favoriteIcon = findViewById(R.id.imageView3);
        progressBar = findViewById(R.id.progressBar);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        
        if (currentUser == null) {
            Toast.makeText(this, "Please login to continue", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        userId = currentUser.getUid();

        // Get app_id from Intent
        appId = getIntent().getStringExtra("app_id");
        
        if (appId == null || appId.isEmpty()) {
            Toast.makeText(this, "Error: App ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase references
        databaseReference = FirebaseDatabase.getInstance().getReference("appDetails").child(appId);
        favoritesReference = FirebaseDatabase.getInstance().getReference("favourites").child(userId).child(appId);
        downloadsReference = FirebaseDatabase.getInstance().getReference("downloads").child(userId).child(appId);

        // Load app data from Firebase
        loadAppDetailsFromDatabase();
        
        // Load favorite status
        loadFavoriteStatus();

        // Set click listeners
        setupClickListeners();
        backIcon.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });
        
        favoriteIcon.setOnClickListener(v -> {
            toggleFavorite();
        });
    }

    private void initViews() {
        appName = findViewById(R.id.appName);
        developerName = findViewById(R.id.developerName);
        category = findViewById(R.id.category);
        rating = findViewById(R.id.rating);
        totalDownloads = findViewById(R.id.totalDownloads);
        fileSize = findViewById(R.id.fileSize);
        shortDescription = findViewById(R.id.shortDescription);
        description = findViewById(R.id.description);
        version = findViewById(R.id.version);
        lastUpdated = findViewById(R.id.lastUpdated);
        developerEmail = findViewById(R.id.developerEmail);
        installButton = findViewById(R.id.installButton);
    }

    private void loadAppDetailsFromDatabase() {
        // Show loading state
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Hide loading state
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                
                if (snapshot.exists()) {
                    appDetails = snapshot.getValue(AppDetails.class);
                    if (appDetails != null) {
                        displayAppDetails();
                    } else {
                        Toast.makeText(AppViewActivity.this, 
                            "Error: Could not load app details", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(AppViewActivity.this, 
                        "App not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Hide loading state
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                
                Toast.makeText(AppViewActivity.this, 
                    "Error loading app: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayAppDetails() {
        if (appDetails == null) return;

        // Set basic info
        appName.setText(appDetails.getAppName());
        developerName.setText(appDetails.getDeveloperName());
        category.setText(appDetails.getCategory());

        // Set rating and stats
        rating.setText(String.format("%.1f", appDetails.getRating()));
        totalDownloads.setText(appDetails.getFormattedDownloads());
        fileSize.setText(appDetails.getFormattedFileSize());

        // Set descriptions
        shortDescription.setText(appDetails.getShortDescription());
        description.setText(appDetails.getDescription());

        // Set app info
        version.setText(appDetails.getVersion());
        lastUpdated.setText(appDetails.getLastUpdated());
        developerEmail.setText(appDetails.getDeveloperEmail());

    }

    private void loadFavoriteStatus() {
        favoriteListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isFavorite = snapshot.exists();
                updateFavoriteIcon();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AppViewActivity.this, 
                    "Error loading favorite status", Toast.LENGTH_SHORT).show();
            }
        };
        
        favoritesReference.addValueEventListener(favoriteListener);
    }
    
    private void toggleFavorite() {
        if (isFavorite) {
            favoritesReference.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to remove from favorites", Toast.LENGTH_SHORT).show();
                });
        } else {
            Map<String, Object> favoriteData = new HashMap<>();
            favoriteData.put("appId", appId);
            favoriteData.put("timestamp", System.currentTimeMillis());
            
            favoritesReference.setValue(favoriteData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add to favorites", Toast.LENGTH_SHORT).show();
                });
        }
    }
    
    private void updateFavoriteIcon() {
        favoriteIcon.setVisibility(View.VISIBLE);
        if (isFavorite) {
            favoriteIcon.setImageResource(R.drawable.icon_fav_filled);
            favoriteIcon.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_light));
        } else {
            favoriteIcon.setImageResource(R.drawable.icon_fav);
            favoriteIcon.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray));
        }
    }

    private void setupClickListeners() {
        installButton.setOnClickListener(v -> {
            if (appDetails != null && appDetails.getApkUrl() != null && !appDetails.getApkUrl().isEmpty()) {
                trackDownload();
                
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(appDetails.getApkUrl()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to open link", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Download link not available", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void trackDownload() {
        Map<String, Object> downloadData = new HashMap<>();
        downloadData.put("appId", appId);
        downloadData.put("appName", appDetails.getAppName());
        downloadData.put("timestamp", System.currentTimeMillis());
        
        downloadsReference.setValue(downloadData)
            .addOnSuccessListener(aVoid -> {
                // Increment download count in appDetails
                incrementDownloadCount();
                Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
            });
    }
    
    private void incrementDownloadCount() {
        databaseReference.child("totalDownloads").get().addOnSuccessListener(snapshot -> {
            long currentDownloads = 0;
            if (snapshot.exists() && snapshot.getValue() != null) {
                currentDownloads = snapshot.getValue(Long.class);
            }
            
            // Increment the download count
            long newDownloadCount = currentDownloads + 1;
            databaseReference.child("totalDownloads").setValue(newDownloadCount)
                .addOnSuccessListener(aVoid -> {
                    // Update local display
                    if (appDetails != null) {
                        appDetails.setTotalDownloads((int) newDownloadCount);
                        totalDownloads.setText(appDetails.getFormattedDownloads());
                    }
                })
                .addOnFailureListener(e -> {
                });
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (favoritesReference != null && favoriteListener != null) {
            favoritesReference.removeEventListener(favoriteListener);
        }
    }
}