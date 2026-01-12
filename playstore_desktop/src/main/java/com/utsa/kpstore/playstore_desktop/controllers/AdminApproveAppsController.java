package com.utsa.kpstore.playstore_desktop.controllers;

import com.utsa.kpstore.playstore_desktop.models.App;
import com.utsa.kpstore.playstore_desktop.services.DatabaseHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.stage.FileChooser;
import com.utsa.kpstore.playstore_desktop.services.FileUploadService;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import java.io.File;
import java.util.List;

public class AdminApproveAppsController {

    @FXML
    private Button backBtn;

    @FXML
    private VBox appsListContainer;

    @FXML
    public void initialize() {
        loadUnapprovedApps();
    }

    private void loadUnapprovedApps() {
        appsListContainer.getChildren().clear();
        List<App> apps = DatabaseHelper.getUnapprovedApps();

        if (apps.isEmpty()) {
            Label placeholder = new Label("No pending apps found.");
            placeholder.setStyle("-fx-text-fill: #5f6368; -fx-font-size: 14px;");
            appsListContainer.getChildren().add(placeholder);
            return;
        }

        for (App app : apps) {
            appsListContainer.getChildren().add(createAppRow(app));
        }
    }

    private HBox createAppRow(App app) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 20, 10, 20));
        row.setStyle(
                "-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 3, 0, 0, 1);");

        // Icon
        ImageView iconView = new ImageView();
        iconView.setFitWidth(40);
        iconView.setFitHeight(40);
        try {
            if (app.getIconUrl() != null && !app.getIconUrl().isEmpty()) {
                File file = new File(app.getIconUrl());
                if (file.exists()) {
                    iconView.setImage(new Image(file.toURI().toString()));
                }
            }
        } catch (Exception e) {
            // Ignore incorrect image path
        }
        HBox iconContainer = new HBox(iconView);
        iconContainer.setPrefWidth(60);
        iconContainer.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(app.getName());
        nameLabel.setPrefWidth(200);
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Label developerLabel = new Label(app.getDeveloperName());
        developerLabel.setPrefWidth(150);
        developerLabel.setStyle("-fx-text-fill: #333333;");

        Label versionLabel = new Label(app.getVersion());
        versionLabel.setPrefWidth(100);
        versionLabel.setStyle("-fx-text-fill: #333333;");

        Label categoryLabel = new Label(app.getCategoryName());
        categoryLabel.setPrefWidth(100);
        categoryLabel.setStyle("-fx-text-fill: #333333;");

        Button viewBtn = new Button("View");
        viewBtn.setPrefWidth(80);
        viewBtn.setStyle(
                "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        viewBtn.setOnAction(e -> handleViewApp(app));

        Button actionBtn = new Button("Action");
        actionBtn.setPrefWidth(80);
        actionBtn.setStyle(
                "-fx-background-color: #FF9800; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        actionBtn.setOnAction(e -> handleActionApp(app));

        HBox actions = new HBox(10, viewBtn, actionBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(iconContainer, nameLabel, developerLabel, versionLabel, categoryLabel, actions);
        return row;
    }

    private void handleViewApp(App app) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/utsa/kpstore/playstore_desktop/app-detail-view.fxml"));
            Parent root = loader.load();

            AppDetailViewController controller = loader.getController();
            controller.setAppId(app.getId());

            // Capture the content area reference BEFORE switching views
            final ScrollPane layoutContentArea = (ScrollPane) appsListContainer.getScene().lookup("#contentArea");

            controller.setAdminViewMode(true, () -> {
                // Return to list
                try {
                    FXMLLoader backLoader = new FXMLLoader(
                            getClass().getResource("/com/utsa/kpstore/playstore_desktop/admin-approve-apps-view.fxml"));
                    Parent backRoot = backLoader.load();

                    if (layoutContentArea != null) {
                        layoutContentArea.setContent(backRoot);
                    } else {
                        // Fallback if layoutContentArea reference is lost or invalid
                        Stage stage = (Stage) root.getScene().getWindow();
                        stage.getScene().setRoot(backRoot);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to return to list.");
                }
            });

            // Switch to app detail view in content area
            if (layoutContentArea != null) {
                layoutContentArea.setContent(root);
            } else {
                Stage stage = (Stage) appsListContainer.getScene().getWindow();
                stage.getScene().setRoot(root);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load app view.");
        }
    }

    private void handleActionApp(App app) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Action App: " + app.getName());
        dialog.setHeaderText("Action for " + app.getName());

        ButtonType approveButtonType = new ButtonType("Approve", ButtonBar.ButtonData.OK_DONE);
        ButtonType requestChangesButtonType = new ButtonType("Request Changes", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(approveButtonType, requestChangesButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextArea feedbackArea = new TextArea();
        feedbackArea.setPromptText("Enter feedback (Required for Request Changes)");
        feedbackArea.setWrapText(true);
        feedbackArea.setPrefHeight(100);

        grid.add(new Label("Feedback / Note:"), 0, 0);
        grid.add(feedbackArea, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            String feedback = feedbackArea.getText().trim();

            if (response == approveButtonType) {
                if (DatabaseHelper.updateAppStatus(app.getId(), 1, feedback)) {
                    loadUnapprovedApps();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "App approved successfully.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to approve app.");
                }
            } else if (response == requestChangesButtonType) {
                if (feedback.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Warning", "Feedback is required when requesting changes.");
                    return;
                }
                if (DatabaseHelper.updateAppStatus(app.getId(), 0, feedback)) {
                    loadUnapprovedApps();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Feedback sent. Status: Changes Requested.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to send feedback.");
                }
            }
        });
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/utsa/kpstore/playstore_desktop/admin-home-view.fxml"));
            Parent root = loader.load();

            ScrollPane contentArea = (ScrollPane) backBtn.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.setContent(root);
            } else {
                Stage stage = (Stage) backBtn.getScene().getWindow();
                stage.getScene().setRoot(root);
            }
            // Ideally we should also update the title in AdminLayoutController, but looking
            // it up is hard.
            // It's acceptable for now.
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to navigate back.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void downloadApk(App app) {
        if (app.getApkFilePath() == null || app.getApkFilePath().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "No APK file found for this app.");
            return;
        }

        try {
            File sourceFile = FileUploadService.getFullPath(app.getApkFilePath()).toFile();
            if (!sourceFile.exists()) {
                showAlert(Alert.AlertType.ERROR, "Error", "APK file does not exist on server.");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save APK File");
            fileChooser.setInitialFileName(app.getPackageName() + "_" + app.getVersion() + ".apk");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("APK Files", "*.apk"));

            File destFile = fileChooser.showSaveDialog(appsListContainer.getScene().getWindow());

            if (destFile != null) {
                Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "APK downloaded successfully to: " + destFile.getAbsolutePath());
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to download APK: " + e.getMessage());
        }
    }
}
