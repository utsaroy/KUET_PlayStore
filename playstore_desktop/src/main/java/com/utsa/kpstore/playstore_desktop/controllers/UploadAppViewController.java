package com.utsa.kpstore.playstore_desktop.controllers;

import com.utsa.kpstore.playstore_desktop.models.App;
import com.utsa.kpstore.playstore_desktop.models.Category;
import com.utsa.kpstore.playstore_desktop.services.DatabaseHelper;
import com.utsa.kpstore.playstore_desktop.services.FileUploadService;
import com.utsa.kpstore.playstore_desktop.services.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;

public class UploadAppViewController {

    @FXML
    private Text pageTitle;

    @FXML
    private TextField appNameField;

    @FXML
    private TextArea fullDescArea;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private TextField versionField;

    @FXML
    private TextField iconPathField;

    @FXML
    private TextField packagePathField;

    @FXML
    private Button submitBtn;

    private File iconFile;
    private File packageFile;
    private App currentApp; // If null, creating new app. If set, editing.

    @FXML
    private void initialize() {
        FileUploadService.initializeUploadDirectories();
        loadCategories();
    }

    public void setAppToEdit(App app) {
        this.currentApp = app;

        if (app != null) {
            // Pre-fill fields
            appNameField.setText(app.getName());
            fullDescArea.setText(app.getDescription());
            versionField.setText(app.getVersion());
            categoryComboBox.setValue(app.getCategoryName());

            // For files, we show they are already uploaded
            iconPathField.setText(app.getIconUrl());
            packagePathField.setText(app.getApkFilePath());

            // Update UI text
            submitBtn.setText("Update App");
        }
    }

    private void loadCategories() {
        List<Category> categories = DatabaseHelper.getAllCategories();
        categoryComboBox.getItems().clear();
        for (Category category : categories) {
            categoryComboBox.getItems().add(category.getName());
        }
    }

    @FXML
    private void handleBrowseIcon() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select App Icon");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        File file = fileChooser.showOpenDialog(iconPathField.getScene().getWindow());
        if (file != null) {
            iconFile = file;
            iconPathField.setText(file.getName());
        }
    }

    @FXML
    private void handleBrowsePackage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select App Package");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.zip", "*.jar", "*.exe"),
                new FileChooser.ExtensionFilter("ZIP Files", "*.zip"),
                new FileChooser.ExtensionFilter("JAR Files", "*.jar"),
                new FileChooser.ExtensionFilter("EXE Files", "*.exe"));

        File file = fileChooser.showOpenDialog(packagePathField.getScene().getWindow());
        if (file != null) {
            // Check file size (max 100 MB)
            long fileSizeInMB = file.length() / (1024 * 1024);
            if (fileSizeInMB > 100) {
                showErrorAlert("File size exceeds 100 MB limit!");
                return;
            }
            packageFile = file;
            packagePathField.setText(file.getName());
        }
    }

    @FXML
    private void handleCancel() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel");
        alert.setHeaderText("Are you sure you want to cancel?");
        alert.setContentText("All unsaved changes will be lost.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                clearForm();
                // Ideally navigate back, but clearForm is what was there
            }
        });
    }

    @FXML
    private void handleSubmit() {
        if (!validateForm()) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(currentApp == null ? "Submit App" : "Update App");
        alert.setHeaderText(currentApp == null ? "Submit app for review?" : "Update app details?");
        alert.setContentText(currentApp == null ? "Your app will be reviewed by our team within 1 or 2 days."
                : "Changes will be applied immediately.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (currentApp == null) {
                    submitNewApp();
                } else {
                    submitUpdateApp();
                }
            }
        });
    }

    private boolean validateForm() {
        if (!validateBasicFields()) {
            return false;
        }

        // For new apps, files are required. For edit, they are optional (keep
        // existing).
        if (currentApp == null) {
            if (iconFile == null) {
                showErrorAlert("Please select an app icon!");
                return false;
            }

            if (packageFile == null) {
                showErrorAlert("Please select an app package!");
                return false;
            }
        }

        return true;
    }

    private boolean validateBasicFields() {
        if (appNameField.getText().trim().isEmpty()) {
            showErrorAlert("Please enter app name!");
            return false;
        }

        if (fullDescArea.getText().trim().isEmpty()) {
            showErrorAlert("Please enter full description!");
            return false;
        }

        if (categoryComboBox.getValue() == null) {
            showErrorAlert("Please select a category!");
            return false;
        }

        return true;
    }

    private int getCategoryIdByName(String categoryName) {
        List<Category> categories = DatabaseHelper.getAllCategories();
        for (Category category : categories) {
            if (category.getName().equals(categoryName)) {
                return category.getId();
            }
        }
        return -1;
    }

    private void submitNewApp() {
        try {
            int developerId = UserSession.getInstance().getUserId();
            if (developerId == -1) {
                showErrorAlert("You must be logged in to upload an app!");
                return;
            }

            String appName = appNameField.getText().trim();
            String description = fullDescArea.getText().trim();
            String version = versionField.getText().trim();
            String categoryName = categoryComboBox.getValue();
            String packageName = "com.kuet." + appName.toLowerCase().replaceAll("[^a-z0-9]", "");
            double price = 0.0;

            int categoryId = getCategoryIdByName(categoryName);
            if (categoryId == -1) {
                showErrorAlert("Invalid category selected!");
                return;
            }
            long packageSize = packageFile.length();

            App newApp = DatabaseHelper.createApp(
                    appName, description, developerId, categoryId, version, packageSize, price,
                    null, packageName, null);

            if (newApp == null) {
                showErrorAlert("Failed to create app. Package name may already exist.");
                return;
            }

            int appId = newApp.getId();
            String iconPath = FileUploadService.uploadIcon(iconFile, appId);
            String packagePath = FileUploadService.uploadPackage(packageFile, appId);
            updateAppFilePaths(appId, iconPath, packagePath);

            System.out.println("App submitted successfully: " + appName + " (ID: " + appId + ")");
            showSuccessAlert("App submitted successfully!\n\nYour app is pending approval.");
            clearForm();

        } catch (Exception e) {
            System.err.println("Error submitting app: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Failed to submit app: " + e.getMessage());
        }
    }

    private void submitUpdateApp() {
        try {
            String appName = appNameField.getText().trim();
            String description = fullDescArea.getText().trim();
            String version = versionField.getText().trim();
            String categoryName = categoryComboBox.getValue();

            int categoryId = getCategoryIdByName(categoryName);
            if (categoryId == -1) {
                showErrorAlert("Invalid category selected!");
                return;
            }

            long size = (packageFile != null) ? packageFile.length() : currentApp.getSize();
            double price = currentApp.getPrice();

            // 1. Update Metadata
            boolean success = DatabaseHelper.updateApp(currentApp.getId(), appName, description, categoryId, version,
                    size, price);
            if (!success) {
                showErrorAlert("Failed to update app details.");
                return;
            }

            // 2. Handle File Updates
            String newIconPath = currentApp.getIconUrl();
            if (iconFile != null) {
                newIconPath = FileUploadService.uploadIcon(iconFile, currentApp.getId());
            }

            String newPackagePath = currentApp.getApkFilePath();
            if (packageFile != null) {
                newPackagePath = FileUploadService.uploadPackage(packageFile, currentApp.getId());
            }

            if (iconFile != null || packageFile != null) {
                updateAppFilePaths(currentApp.getId(), newIconPath, newPackagePath);
            }

            showSuccessAlert("App updated successfully!");
            clearForm();
            currentApp = null; // Reset edit mode
            submitBtn.setText("Submit for Review"); // Reset button text

        } catch (Exception e) {
            System.err.println("Error updating app: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Failed to update app: " + e.getMessage());
        }
    }

    private void clearForm() {
        appNameField.clear();
        fullDescArea.clear();
        categoryComboBox.setValue(null);
        versionField.clear();
        iconPathField.clear();
        packagePathField.clear();
        iconFile = null;
        packageFile = null;
        currentApp = null;
        submitBtn.setText("Submit for Review");
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateAppFilePaths(int appId, String iconPath, String packagePath) {
        DatabaseHelper.updateAppFilePaths(appId, iconPath, packagePath);
    }
}
