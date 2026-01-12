package com.utsa.kpstore.playstore_desktop.controllers;

import com.utsa.kpstore.playstore_desktop.models.App;
import com.utsa.kpstore.playstore_desktop.services.DatabaseHelper;
import com.utsa.kpstore.playstore_desktop.services.FileUploadService;
import com.utsa.kpstore.playstore_desktop.services.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

import java.nio.file.Path;
import java.util.List;

public class MyAppsViewController {

    @FXML
    private VBox appsContainer;

    private void loadMyApps() {
        int userId = UserSession.getInstance().getUserId();
        if (userId == -1) {
            appsContainer.getChildren().clear();
            appsContainer.getChildren().add(new Label("Please login to view your apps."));
            return;
        }

        List<App> apps = DatabaseHelper.getUserDownloadedApps(userId);

        appsContainer.getChildren().clear();

        if (apps.isEmpty()) {
            VBox emptyState = new VBox(20);
            emptyState.setAlignment(javafx.geometry.Pos.CENTER);
            emptyState.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 15; -fx-padding: 40;");

            Text icon = new Text("📦");
            icon.setStyle("-fx-font-size: 48px; -fx-fill: #757575;");

            Text message = new Text("No apps downloaded yet");
            message.setStyle("-fx-font-size: 14px; -fx-fill: #757575;");

            emptyState.getChildren().addAll(icon, message);
            appsContainer.getChildren().add(emptyState);
        } else {
            for (App app : apps) {
                appsContainer.getChildren().add(createAppRow(app));
            }
        }
    }

    private VBox createAppRow(App app) {
        VBox row = new VBox(15);
        row.setStyle(
                "-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 10, 0, 0, 2);");

        HBox content = new HBox(20);
        content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // App Icon
        ImageView iconView = new ImageView();
        iconView.setFitWidth(60);
        iconView.setFitHeight(60);
        iconView.setPreserveRatio(true);

        try {
            Path iconPath = FileUploadService.getFullPath(app.getIconUrl());
            if (java.nio.file.Files.exists(iconPath)) {
                iconView.setImage(new Image(iconPath.toUri().toString()));
            } else {
                iconView.setImage(new Image(
                        getClass().getResourceAsStream("/com/utsa/kpstore/playstore_desktop/images/logo.png")));
            }
        } catch (Exception e) {
            // Fallback
        }

        // App Details
        VBox details = new VBox(5);
        HBox.setHgrow(details, javafx.scene.layout.Priority.ALWAYS);

        Text name = new Text(app.getName());
        name.setFill(javafx.scene.paint.Color.web("#212121"));
        name.setFont(Font.font("System Bold", 18));

        Text versionInfo = new Text("Version " + app.getVersion());
        versionInfo.setFill(javafx.scene.paint.Color.web("#757575"));
        versionInfo.setStyle("-fx-font-size: 13px;");

        details.getChildren().addAll(name, versionInfo);

        // Action Button (View)
        Button viewBtn = new Button("View App");
        viewBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
        viewBtn.setOnAction(e -> handleViewApp(app));

        row.getChildren().add(content);

        // Add View Button at bottom right or right side
        HBox actions = new HBox(viewBtn);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        row.getChildren().add(actions);

        return row;
    }

    private void handleViewApp(App app) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/utsa/kpstore/playstore_desktop/app-detail-view.fxml"));
            Parent root = loader.load();

            AppDetailViewController controller = loader.getController();
            controller.setAppId(app.getId());
            Stage stage = (Stage) appsContainer.getScene().getWindow();

            controller.setMainLayoutController(null); // Or pass if we had it.

            controller.setAdminViewMode(true, () -> {
                try {
                    FXMLLoader backLoader = new FXMLLoader(
                            getClass().getResource("/com/utsa/kpstore/playstore_desktop/my-apps-view.fxml"));
                    Parent backRoot = backLoader.load();
                    //restore state (e.g. showingDownloads = false)
                    MyAppsViewController backController = backLoader.getController();

                    stage.getScene().setRoot(backRoot);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            stage.getScene().setRoot(root);

        } catch (Exception e) {
            e.printStackTrace();
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Failed to load app details.");
            alert.show();
        }
    }
}
