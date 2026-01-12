package com.utsa.kpstore.playstore_desktop.controllers;

import com.utsa.kpstore.playstore_desktop.models.App;
import com.utsa.kpstore.playstore_desktop.models.Rating;
import com.utsa.kpstore.playstore_desktop.services.DatabaseHelper;
import com.utsa.kpstore.playstore_desktop.services.FileUploadService;
import com.utsa.kpstore.playstore_desktop.services.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

public class AppDetailViewController {

    @FXML
    private ImageView appIcon;

    @FXML
    private Text appTitle;

    @FXML
    private Text appDeveloper;

    @FXML
    private Label appCategory;

    @FXML
    private Text appVersion;

    @FXML
    private Text appSize;

    @FXML
    private Text averageRatingText;

    @FXML
    private Text averageRatingTextLarge;

    @FXML
    private Text ratingCountText;

    @FXML
    private Text ratingCountTextSmall;

    @FXML
    private Text downloadsText;

    @FXML
    private Text updatedDateText;

    @FXML
    private Text appDescription;

    @FXML
    private Button downloadButton;

    @FXML
    private Button rateButton;

    @FXML
    private VBox reviewsContainer;

    @FXML
    private HBox userRatingContainer;

    @FXML
    private ProgressBar rating5Bar;

    @FXML
    private ProgressBar rating4Bar;

    @FXML
    private ProgressBar rating3Bar;

    @FXML
    private ProgressBar rating2Bar;

    @FXML
    private ProgressBar rating1Bar;

    @FXML
    private Text rating5Count;

    @FXML
    private Text rating4Count;

    @FXML
    private Text rating3Count;

    @FXML
    private Text rating2Count;

    @FXML
    private Text rating1Count;

    private int currentAppId;
    private App currentApp;
    private MainLayoutController mainLayoutController;
    private int userRating = 0;

    // Admin View Mode Fields
    @FXML
    private VBox adminControlsBox;
    private boolean isAdminViewMode = false;
    private Runnable onAdminBack;

    public void setAdminViewMode(boolean enable, Runnable onBack) {
        this.isAdminViewMode = enable;
        this.onAdminBack = onBack;
        updateUiMode();
    }

    private void updateUiMode() {
        if (adminControlsBox != null) {
            adminControlsBox.setVisible(isAdminViewMode);
            adminControlsBox.setManaged(isAdminViewMode);
        }
    }

    @FXML
    private void handleAdminBack() {
        if (onAdminBack != null) {
            onAdminBack.run();
        }
    }

    public void setMainLayoutController(MainLayoutController controller) {
        this.mainLayoutController = controller;
    }

    public void setAppId(int appId) {
        this.currentAppId = appId;
        loadAppDetails(appId);
    }

    public void setAppData(String appId) {
        try {
            currentAppId = Integer.parseInt(appId);
            loadAppDetails(currentAppId);
        } catch (NumberFormatException e) {
            showError("Invalid app ID");
        }
    }

    private void loadAppDetails(int appId) {
        currentApp = DatabaseHelper.getAppById(appId);

        if (currentApp == null) {
            showError("App not found");
            return;
        }

        // Update UI with app details
        if (appTitle != null)
            appTitle.setText(currentApp.getName());
        if (appDeveloper != null)
            appDeveloper.setText(currentApp.getDeveloperName());
        if (appCategory != null)
            appCategory.setText(currentApp.getCategoryName());
        if (appVersion != null)
            appVersion.setText("v" + currentApp.getVersion());
        if (appSize != null)
            appSize.setText(currentApp.getFormattedSize());
        if (appDescription != null)
            appDescription.setText(currentApp.getDescription());

        // Update app icon
        if (appIcon != null) {
            String iconUrl = currentApp.getIconUrl();
            if (iconUrl != null && !iconUrl.isEmpty() && !iconUrl.equals("default_icon.png")) {
                try {
                    Path iconPath = FileUploadService.getFullPath(iconUrl);
                    if (Files.exists(iconPath)) {
                        appIcon.setImage(new Image(iconPath.toUri().toString()));
                    }
                } catch (Exception e) {
                    System.err.println("Failed to load app icon: " + e.getMessage());
                }
            }
        }

        // Load ratings
        loadRatings();

        // Update download count
        if (downloadsText != null) {
            int downloads = currentApp.getDownloads();
            if (downloads >= 1000000) {
                downloadsText.setText(String.format("%.1fM", downloads / 1000000.0));
            } else if (downloads >= 1000) {
                downloadsText.setText(String.format("%.1fK", downloads / 1000.0));
            } else {
                downloadsText.setText(String.valueOf(downloads));
            }
        }

        // Check if user already rated this app
        checkUserRating();
    }

    private void loadRatings() {
        double avgRating = DatabaseHelper.getAverageRating(currentAppId);
        int totalRatings = DatabaseHelper.getRatingCount(currentAppId);

        if (averageRatingText != null) {
            averageRatingText.setText(String.format("%.1f", avgRating));
        }

        if (averageRatingTextLarge != null) {
            averageRatingTextLarge.setText(String.format("%.1f", avgRating));
        }

        if (ratingCountText != null) {
            ratingCountText.setText(totalRatings + " REVIEWS");
        }

        if (ratingCountTextSmall != null) {
            ratingCountTextSmall.setText(totalRatings + " reviews");
        }

        // Load rating distribution
        List<Rating> allRatings = DatabaseHelper.getRatingsByApp(currentAppId);
        int[] ratingCounts = new int[6]; // index 0 unused, 1-5 for star ratings

        for (Rating rating : allRatings) {
            ratingCounts[rating.getRating()]++;
        }

        // Update rating bars
        if (totalRatings > 0) {
            if (rating5Bar != null)
                rating5Bar.setProgress((double) ratingCounts[5] / totalRatings);
            if (rating4Bar != null)
                rating4Bar.setProgress((double) ratingCounts[4] / totalRatings);
            if (rating3Bar != null)
                rating3Bar.setProgress((double) ratingCounts[3] / totalRatings);
            if (rating2Bar != null)
                rating2Bar.setProgress((double) ratingCounts[2] / totalRatings);
            if (rating1Bar != null)
                rating1Bar.setProgress((double) ratingCounts[1] / totalRatings);

            if (rating5Count != null)
                rating5Count.setText(String.valueOf(ratingCounts[5]));
            if (rating4Count != null)
                rating4Count.setText(String.valueOf(ratingCounts[4]));
            if (rating3Count != null)
                rating3Count.setText(String.valueOf(ratingCounts[3]));
            if (rating2Count != null)
                rating2Count.setText(String.valueOf(ratingCounts[2]));
            if (rating1Count != null)
                rating1Count.setText(String.valueOf(ratingCounts[1]));
        }

        // Load recent reviews
        loadReviews(allRatings);
    }

    private void loadReviews(List<Rating> ratings) {
        if (reviewsContainer == null)
            return;

        reviewsContainer.getChildren().clear();

        // Show latest 5 reviews
        int count = Math.min(5, ratings.size());
        for (int i = 0; i < count; i++) {
            Rating rating = ratings.get(i);
            VBox reviewBox = createReviewBox(rating);
            reviewsContainer.getChildren().add(reviewBox);
        }
    }

    private VBox createReviewBox(Rating rating) {
        VBox reviewBox = new VBox(12);
        reviewBox.setStyle("-fx-padding: 15; -fx-background-color: -fx-background; -fx-background-radius: 8;");

        HBox header = new HBox(15);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // User name
        Text userName = new Text(rating.getUserName());
        userName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Rating stars
        String stars = "⭐".repeat(rating.getRating());
        Text ratingStars = new Text(stars);
        ratingStars.setStyle("-fx-font-size: 14px;");

        header.getChildren().addAll(userName, ratingStars);

        // Date
        Text dateText = new Text(rating.getCreatedAt().toString().substring(0, 10));
        dateText.setStyle("-fx-font-size: 12px; -fx-fill: gray;");

        reviewBox.getChildren().addAll(header, dateText);

        // Review Text
        if (rating.getReviewText() != null && !rating.getReviewText().isEmpty()) {
            Text reviewContent = new Text(rating.getReviewText());
            reviewContent.setWrappingWidth(500); // Adjust based on layout
            reviewContent.setStyle("-fx-font-size: 14px; -fx-fill: -fx-text-base;");
            reviewBox.getChildren().add(reviewContent);
        }

        return reviewBox;
    }

    private void checkUserRating() {
        int userId = UserSession.getInstance().getUserId();
        if (userId == -1)
            return;

        Rating existingRating = DatabaseHelper.getRatingByAppAndUser(currentAppId, userId);
        if (existingRating != null) {
            userRating = existingRating.getRating();
            if (rateButton != null) {
                rateButton.setText("Update Rating (" + userRating + "★)");
            }
        }
    }

    @FXML
    private void handleDownload() {
        if (currentApp == null)
            return;

        int userId = UserSession.getInstance().getUserId();
        if (userId == -1) {
            showError("Please login to download apps");
            return;
        }

        // Choose download location
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Download Location");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File downloadDir = directoryChooser.showDialog(downloadButton.getScene().getWindow());

        if (downloadDir == null) {
            return; // User cancelled
        }

        // Disable download button during download
        downloadButton.setDisable(true);
        downloadButton.setText("Downloading...");

        // Perform download in background thread
        new Thread(() -> {
            try {
                // Get the package file path from database
                String packagePath = currentApp.getApkFilePath();

                if (packagePath == null || packagePath.isEmpty()) {
                    Platform.runLater(() -> showError("Package file not found"));
                    return;
                }

                // Copy file from uploads directory to download location
                Path sourcePath = FileUploadService.getFullPath(packagePath);

                if (!Files.exists(sourcePath)) {
                    Platform.runLater(() -> showError("Package file does not exist"));
                    return;
                }

                // Create destination file name
                String fileName = currentApp.getName().replaceAll("[^a-zA-Z0-9.-]", "_");
                String extension = packagePath.substring(packagePath.lastIndexOf('.'));
                Path destPath = Paths.get(downloadDir.getAbsolutePath(), fileName + extension);

                // Copy file
                Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);

                // Increment download counter
                DatabaseHelper.incrementDownloads(currentAppId);

                // Record user download
                DatabaseHelper.recordUserDownload(userId, currentAppId);

                // Update UI
                Platform.runLater(() -> {
                    showSuccess("App downloaded successfully to:\n" + destPath.toString());
                    downloadButton.setText("Download");
                    downloadButton.setDisable(false);

                    // Update download count
                    currentApp.setDownloads(currentApp.getDownloads() + 1);
                    if (downloadsText != null) {
                        int downloads = currentApp.getDownloads();
                        if (downloads >= 1000000) {
                            downloadsText.setText(String.format("%.1fM", downloads / 1000000.0));
                        } else if (downloads >= 1000) {
                            downloadsText.setText(String.format("%.1fK", downloads / 1000.0));
                        } else {
                            downloadsText.setText(String.valueOf(downloads));
                        }
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Download failed: " + e.getMessage());
                    downloadButton.setText("Download");
                    downloadButton.setDisable(false);
                });
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleRateApp() {
        int userId = UserSession.getInstance().getUserId();
        if (userId == -1) {
            showError("Please login to rate apps");
            return;
        }

        // Create rating dialog
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Rate This App");
        dialog.setHeaderText("How would you rate " + currentApp.getName() + "?");

        // Create rating buttons
        HBox ratingBox = new HBox(10);
        ratingBox.setAlignment(javafx.geometry.Pos.CENTER);

        ToggleGroup ratingGroup = new ToggleGroup();

        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            RadioButton starButton = new RadioButton(i + " ★");
            starButton.setToggleGroup(ratingGroup);
            starButton.setUserData(rating);

            if (userRating == i) {
                starButton.setSelected(true);
            }

            ratingBox.getChildren().add(starButton);
        }

        VBox content = new VBox(15);

        TextArea reviewArea = new TextArea();
        reviewArea.setPromptText("Write your review here (optional)...");
        reviewArea.setWrapText(true);
        reviewArea.setPrefHeight(100);

        Rating existing = DatabaseHelper.getRatingByAppAndUser(currentAppId, userId);
        if (existing != null && existing.getReviewText() != null) {
            reviewArea.setText(existing.getReviewText());
        }

        content.getChildren().addAll(
                new Label("Select your rating:"),
                ratingBox,
                new Label("Your Review:"),
                reviewArea);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                Toggle selected = ratingGroup.getSelectedToggle();
                if (selected != null) {
                    return (Integer) selected.getUserData();
                }
            }
            return null;
        });

        Optional<Integer> result = dialog.showAndWait();

        result.ifPresent(rating -> {
            // Submit rating
            String reviewText = reviewArea.getText().trim();
            Rating newRating = DatabaseHelper.createOrUpdateRating(currentAppId, userId, rating, reviewText);

            if (newRating != null) {
                userRating = rating;
                showSuccess("Thank you for rating this app!");

                // Reload ratings
                loadRatings();

                // Update button text
                if (rateButton != null) {
                    rateButton.setText("Update Rating (" + userRating + "★)");
                }
            } else {
                showError("Failed to submit rating");
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
