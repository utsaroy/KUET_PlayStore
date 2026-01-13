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
import com.bumptech.glide.Glide;
import com.utsa.kpstore.models.AppDetails;

import java.util.HashMap;
import java.util.Map;
import androidx.recyclerview.widget.RecyclerView;

public class AppViewActivity extends AppCompatActivity {

    private TextView appName, developerName, category, rating, totalDownloads, fileSize;
    private TextView shortDescription, description, version, lastUpdated, developerEmail;
    private Button installButton;

    private AppDetails appDetails;
    private ImageView backIcon, favoriteIcon, appIconView;
    private ProgressBar progressBar;

    private RecyclerView reviewsRecyclerView;
    private ReviewAdapter reviewAdapter;
    private java.util.List<com.utsa.kpstore.models.Review> reviewList;
    private android.widget.EditText reviewInput;
    private android.widget.RatingBar userRatingBar;
    private Button submitReviewButton;
    private DatabaseReference reviewsReference;

    // Restored fields
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

        // Check if opened by admin
        boolean isAdminMode = getIntent().getBooleanExtra("admin_mode", false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null && !isAdminMode) {
            Toast.makeText(this, "Please login to continue", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            // Admin mode - hide user-specific features
            userId = null;
            favoriteIcon.setVisibility(android.view.View.GONE);
        }

        // Get app_id from Intent
        appId = getIntent().getStringExtra("app_id");

        if (appId == null || appId.isEmpty()) {
            Toast.makeText(this, "Error: App ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase references
        databaseReference = FirebaseDatabase.getInstance().getReference("appDetails").child(appId);
        if (userId != null) {
            favoritesReference = FirebaseDatabase.getInstance().getReference("favourites").child(userId).child(appId);
            downloadsReference = FirebaseDatabase.getInstance().getReference("downloads").child(userId).child(appId);
        }
        reviewsReference = FirebaseDatabase.getInstance().getReference("reviews").child(appId);

        // Load app data from Firebase
        loadAppDetailsFromDatabase();

        // Load favorite status (only for logged-in users)
        if (userId != null) {
            loadFavoriteStatus();
        }

        // Setup reviews
        setupReviews();

        // Set click listeners
        setupClickListeners();
        backIcon.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        if (userId != null) {
            favoriteIcon.setOnClickListener(v -> {
                toggleFavorite();
            });

            submitReviewButton.setOnClickListener(v -> submitReview());
        } else {
            // Admin mode - disable review submission
            submitReviewButton.setVisibility(android.view.View.GONE);
            if (reviewInput != null)
                reviewInput.setVisibility(android.view.View.GONE);
            if (userRatingBar != null)
                userRatingBar.setVisibility(android.view.View.GONE);
        }
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
        appIconView = findViewById(R.id.imageView4);

        // Review UI
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);
        reviewInput = findViewById(R.id.reviewInput);
        userRatingBar = findViewById(R.id.userRatingBar);
        submitReviewButton = findViewById(R.id.submitReviewButton);
    }

    private void setupReviews() {
        reviewList = new java.util.ArrayList<>();
        reviewAdapter = new ReviewAdapter(reviewList);
        reviewsRecyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        reviewsRecyclerView.setAdapter(reviewAdapter);

        loadReviews();
    }

    private com.utsa.kpstore.models.Review userReview = null;

    private void loadReviews() {
        reviewsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reviewList.clear();
                userReview = null; // Reset

                for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                    com.utsa.kpstore.models.Review review = reviewSnapshot
                            .getValue(com.utsa.kpstore.models.Review.class);
                    if (review != null) {
                        if (review.getUserId() != null && review.getUserId().equals(userId)) {
                            userReview = review;
                            updateReviewUI();
                        }
                        reviewList.add(0, review);
                    }
                }
                reviewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AppViewActivity.this, "Failed to load reviews", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateReviewUI() {
        if (userReview != null) {
            userRatingBar.setRating((float) userReview.getRating());
            reviewInput.setText(userReview.getComment());
            submitReviewButton.setText("Update Review");
        } else {
            userRatingBar.setRating(0);
            reviewInput.setText("");
            submitReviewButton.setText("Post Review");
        }
    }

    private void submitReview() {
        float userRating = userRatingBar.getRating();
        String comment = reviewInput.getText().toString().trim();

        if (userRating == 0) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        final String reviewId;
        final float oldRating;
        if (userReview != null) {
            reviewId = userReview.getReviewId();
            oldRating = (float) userReview.getRating();
        } else {
            reviewId = reviewsReference.push().getKey();
            oldRating = 0;
        }

        if (reviewId == null)
            return;

        // Fetch user name (if existing review, we could reuse name, but fetching
        // ensures freshness)
        // Or if we have userReview, we can reuse that name? Let's just fetch/use
        // current user name logic.

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = "User";
                if (snapshot.exists()) {
                    com.utsa.kpstore.models.User user = snapshot.getValue(com.utsa.kpstore.models.User.class);
                    if (user != null && user.getName() != null) {
                        userName = user.getName();
                    }
                } else if (userReview != null) {
                    userName = userReview.getUserName();
                }

                com.utsa.kpstore.models.Review review = new com.utsa.kpstore.models.Review(
                        reviewId, userId, userName, userRating, comment, System.currentTimeMillis());

                reviewsReference.child(reviewId).setValue(review)
                        .addOnSuccessListener(aVoid -> {
                            String msg = (oldRating == 0) ? "Review submitted" : "Review updated";
                            Toast.makeText(AppViewActivity.this, msg, Toast.LENGTH_SHORT).show();

                            // if we clear, loadReviews will eventually fire and fill it back.
                            // But usually better to leave it or update the userReview object immediately.

                            reviewInput.setText("");
                            userRatingBar.setRating(0);

                            updateAppRating(userRating, oldRating);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(AppViewActivity.this, "Failed to submit review", Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // proceed with default name
            }
        });
    }

    private void updateAppRating(float newRating, float oldRating) {
        databaseReference.runTransaction(new com.google.firebase.database.Transaction.Handler() {
            @NonNull
            @Override
            public com.google.firebase.database.Transaction.Result doTransaction(
                    @NonNull com.google.firebase.database.MutableData currentData) {
                AppDetails appDetails = currentData.getValue(AppDetails.class);
                if (appDetails == null) {
                    return com.google.firebase.database.Transaction.success(currentData);
                }

                int totalRatings = appDetails.getTotalRatings();
                double currentAvgRating = appDetails.getRating();
                double newAvgRating;
                int newTotalRatings;

                if (oldRating > 0) {
                    // Update existing rating
                    // Formula: (CurrentAvg * Count - OldRating + NewRating) / Count
                    if (totalRatings > 0) {
                        double currentTotalScore = currentAvgRating * totalRatings;
                        double newTotalScore = currentTotalScore - oldRating + newRating;
                        newAvgRating = newTotalScore / totalRatings;
                        newTotalRatings = totalRatings; // Count doesn't change
                    } else {
                        // Should not happen if oldRating > 0, but fallback
                        newAvgRating = newRating;
                        newTotalRatings = 1;
                    }
                } else {
                    // New rating
                    double newTotalRatingScore = (currentAvgRating * totalRatings) + newRating;
                    newTotalRatings = totalRatings + 1;
                    newAvgRating = newTotalRatingScore / newTotalRatings;
                }

                appDetails.setTotalRatings(newTotalRatings);
                appDetails.setRating(newAvgRating);

                currentData.setValue(appDetails);
                return com.google.firebase.database.Transaction.success(currentData);
            }

            @Override
            public void onComplete(@androidx.annotation.Nullable DatabaseError error, boolean committed,
                    @androidx.annotation.Nullable DataSnapshot currentData) {
                if (committed) {
                    loadAppDetailsFromDatabase(); // Refresh UI
                }
            }
        });
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
        if (appDetails == null)
            return;

        // Set basic info
        appName.setText(appDetails.getAppName());
        developerName.setText(appDetails.getDeveloperName());
        category.setText(appDetails.getCategory());

        // Set rating and stats
        rating.setText(String.format(java.util.Locale.getDefault(), "%.1f", appDetails.getRating()));
        totalDownloads.setText(appDetails.getFormattedDownloads());
        fileSize.setText(appDetails.getFormattedFileSize());

        // Set descriptions
        shortDescription.setText(appDetails.getShortDescription());
        description.setText(appDetails.getDescription());

        // Set app info
        version.setText(appDetails.getVersion());
        lastUpdated.setText(appDetails.getLastUpdated());
        developerEmail.setText(appDetails.getDeveloperEmail());

        // Load app icon from Base64
        if (appDetails.getIconUrl() != null && !appDetails.getIconUrl().isEmpty()) {
            try {
                byte[] decodedBytes = android.util.Base64.decode(appDetails.getIconUrl(), android.util.Base64.DEFAULT);
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(
                        decodedBytes, 0, decodedBytes.length);
                appIconView.setImageBitmap(bitmap);
            } catch (Exception e) {
                appIconView.setImageResource(R.drawable.logo);
            }
        }

    }
    // ... existing initialization and other methods ...

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
        // Skip tracking for admin mode
        if (downloadsReference == null) {
            Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show();
            return;
        }

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