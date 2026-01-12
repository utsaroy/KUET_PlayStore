package com.utsa.kpstore.playstore_desktop.controllers;

import com.utsa.kpstore.playstore_desktop.models.App;
import com.utsa.kpstore.playstore_desktop.services.DatabaseHelper;
import com.utsa.kpstore.playstore_desktop.services.FileUploadService;
import com.utsa.kpstore.playstore_desktop.services.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class DeveloperDashboardController {

    @FXML
    private VBox appsContainer;

    @FXML
    private Button uploadNewAppBtn;

    private MainLayoutController mainLayoutController;

    @FXML
    private void initialize() {
        loadDeveloperApps();
    }

    public void setMainLayoutController(MainLayoutController controller) {
        this.mainLayoutController = controller;
    }

    public void refresh() {
        loadDeveloperApps();
    }

    private void loadDeveloperApps() {
        int developerId = UserSession.getInstance().getUserId();
        if (developerId == -1)
            return;

        List<App> apps = DatabaseHelper.getAppsByDeveloper(developerId);
        appsContainer.getChildren().clear();

        if (apps.isEmpty()) {
            VBox emptyState = new VBox(15);
            emptyState.setAlignment(javafx.geometry.Pos.CENTER);
            emptyState.setStyle("-fx-background-color: #FAFAFA; -fx-background-radius: 10; -fx-padding: 40;");

            Text icon = new Text("📦");
            icon.setStyle("-fx-font-size: 48px; -fx-fill: #757575;");

            Text message = new Text("No apps published yet");
            message.setStyle("-fx-font-size: 14px; -fx-fill: #757575;");

            Button uploadBtn = new Button("Upload Your First App");
            uploadBtn.setStyle(
                    "-fx-background-color: #6200EE; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 20; -fx-padding: 10 20; -fx-cursor: hand;");
            uploadBtn.setOnAction(e -> handleUploadNewApp());

            emptyState.getChildren().addAll(icon, message, uploadBtn);
            appsContainer.getChildren().add(emptyState);
        } else {
            for (App app : apps) {
                appsContainer.getChildren().add(createAppRow(app));
            }
        }
    }

    private VBox createAppRow(App app) {
        VBox row = new VBox(15);
        row.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 10; -fx-padding: 20;");

        HBox content = new HBox(20);
        content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // App Icon
        ImageView iconView = new ImageView();
        iconView.setFitWidth(50);
        iconView.setFitHeight(50);
        iconView.setPreserveRatio(true);

        try {
            Path iconPath = FileUploadService.getFullPath(app.getIconUrl());
            if (java.nio.file.Files.exists(iconPath)) {
                iconView.setImage(new Image(iconPath.toUri().toString()));
            } else {
                // Fallback or placeholder
            }
        } catch (Exception e) {
            // Ignore format errors
        }

        // App Details
        VBox details = new VBox(5);
        HBox.setHgrow(details, javafx.scene.layout.Priority.ALWAYS);

        Text name = new Text(app.getName());
        name.setFill(javafx.scene.paint.Color.web("#212121"));
        name.setFont(Font.font("System Bold", 16));

        HBox stats = new HBox(15);
        Text downloads = new Text(app.getDownloads() + " downloads");
        downloads.setFill(javafx.scene.paint.Color.web("#757575"));
        downloads.setStyle("-fx-font-size: 13px;");

        Text rating = new Text(String.format("%.1f rating", DatabaseHelper.getAverageRating(app.getId())));
        rating.setFill(javafx.scene.paint.Color.web("#757575"));
        rating.setStyle("-fx-font-size: 13px;");

        stats.getChildren().addAll(downloads, rating);

        if (app.isApproved()) {
            Label approvedLabel = new Label("Published");
            approvedLabel.setStyle(
                    "-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; -fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 10;");
            stats.getChildren().add(approvedLabel);
        } else {
            Label pendingLabel = new Label("Pending Review");
            pendingLabel.setStyle(
                    "-fx-background-color: #FFF3E0; -fx-text-fill: #EF6C00; -fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 10;");
            stats.getChildren().add(pendingLabel);
        }

        if (app.getAdminFeedback() != null && !app.getAdminFeedback().isEmpty()) {
            Button feedbackBtn = new Button("Admin Feedback");
            feedbackBtn.setStyle(
                    "-fx-background-color: #FFEBEE; -fx-text-fill: #D32F2F; -fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 10; -fx-cursor: hand; -fx-border-color: #D32F2F; -fx-border-radius: 10;");
            feedbackBtn.setOnAction(e -> showInfoAlert("Admin Feedback", app.getAdminFeedback()));
            stats.getChildren().add(feedbackBtn);
        }

        details.getChildren().addAll(name, stats);

        // Actions
        VBox actions = new VBox(8);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        HBox actionButtons = new HBox(10);

        Button viewBtn = new Button("View");
        viewBtn.setStyle(
                "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 20; -fx-padding: 8 15; -fx-cursor: hand;");
        viewBtn.setOnAction(e -> handleViewApp(app));

        Button editBtn = new Button("Edit");
        editBtn.setStyle(
                "-fx-background-color: #6200EE; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 20; -fx-padding: 8 15; -fx-cursor: hand;");
        editBtn.setOnAction(e -> handleEditApp(app));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #f44336; -fx-font-size: 12px; -fx-border-color: #f44336; -fx-border-radius: 20; -fx-padding: 7 14; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> handleDeleteApp(app));

        actionButtons.getChildren().addAll(viewBtn, editBtn, deleteBtn);
        actions.getChildren().add(actionButtons);

        content.getChildren().addAll(iconView, details, actions);
        row.getChildren().add(content);

        return row;
    }

    @FXML
    private void handleUploadNewApp() {
        if (mainLayoutController != null) {
            mainLayoutController.showUploadApp();
        }
    }

    private void handleEditApp(App app) {
        if (mainLayoutController != null) {
            mainLayoutController.showEditApp(app);
        }
    }

    private void handleDeleteApp(App app) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete App");
        alert.setHeaderText("Delete " + app.getName() + "?");
        alert.setContentText("Are you sure you want to delete this app? This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (DatabaseHelper.deleteApp(app.getId())) {
                loadDeveloperApps();
                showInfoAlert("Success", "App deleted successfully.");
            } else {
                showInfoAlert("Error", "Failed to delete app.");
            }
        }
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleViewApp(App app) {
        if (mainLayoutController != null) {
            mainLayoutController.showAppDetail(String.valueOf(app.getId()));
        }
    }
}
