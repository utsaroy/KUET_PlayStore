package com.utsa.kpstore.playstore_desktop.controllers;

import com.utsa.kpstore.playstore_desktop.models.App;
import com.utsa.kpstore.playstore_desktop.services.DatabaseHelper;
import com.utsa.kpstore.playstore_desktop.services.FileUploadService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.nio.file.Path;
import java.util.List;

public class HomeViewController {
    @FXML
    private GridPane discoverAppsGrid;

    private MainLayoutController mainLayoutController;

    @FXML
    public void initialize() {
        loadApps();
    }

    public void setMainLayoutController(MainLayoutController controller) {
        this.mainLayoutController = controller;
    }

    private void loadApps() {
        List<App> apps = DatabaseHelper.getApprovedApps();
        displayApps(apps);
    }

    public void loadRecentApps() {
        List<App> apps = DatabaseHelper.getRecentApps(20);
        displayApps(apps);
    }

    public void loadAppsByCategory(int categoryId) {
        List<App> apps = DatabaseHelper.getAppsByCategory(categoryId);
        displayApps(apps);
    }

    public void displaySearchResults(List<App> apps, String query) {
        displayApps(apps);
        // Optionally update a title label if one existed to say "Results for " + query
    }

    private void displayApps(List<App> apps) {
        discoverAppsGrid.getChildren().clear();

        if (apps == null || apps.isEmpty()) {
            // Optional: Show "No apps found" message
            Text noAppsText = new Text("No apps found in this category.");
            noAppsText.setStyle("-fx-fill: white; -fx-font-size: 16px;");
            discoverAppsGrid.add(noAppsText, 0, 0);
            return;
        }

        int col = 0;
        int row = 0;
        int maxCols = 4;

        for (App app : apps) {
            Node card = createAppCard(app);
            discoverAppsGrid.add(card, col, row);

            col++;
            if (col >= maxCols) {
                col = 0;
                row++;
            }
        }
    }

    private Node createAppCard(App app) {
        VBox card = new VBox();
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setId(String.valueOf(app.getId())); // used for handleAppCardClick
        card.setOnMouseClicked(this::handleAppCardClick);
        card.getStyleClass().add("app-card");
        card.setStyle("-fx-cursor: hand;");
        card.setPrefWidth(280);
        Node imageNode;
        String iconUrl = app.getIconUrl();

        if (iconUrl != null && !iconUrl.isEmpty() && !iconUrl.equals("default_icon.png")) {
            try {
                // Try to load from local storage first
                Path iconPath = FileUploadService.getFullPath(iconUrl);
                if (java.nio.file.Files.exists(iconPath)) {
                    String fileUri = iconPath.toUri().toString();
                    ImageView imageView = new ImageView(new Image(fileUri));
                    imageView.setPreserveRatio(true);
                    imageView.setFitWidth(100);
                    imageNode = imageView;
                } else {
                    // Fallback to emoji/text if file missing
                    imageNode = createFallbackIcon();
                }
            } catch (Exception e) {
                imageNode = createFallbackIcon();
            }
        } else {
            imageNode = createFallbackIcon();
        }

        StackPane stack = new StackPane();
        stack.setPrefHeight(160);
        stack.getStyleClass().add("app-card-image");
        stack.setStyle("-fx-background-color: linear-gradient(to bottom right, #a8edea, #fed6e3);");

        if (imageNode instanceof ImageView) {
            ((ImageView) imageNode).setFitHeight(80);
            ((ImageView) imageNode).setFitWidth(80);
            stack.getChildren().add(imageNode);
        } else {
            stack.getChildren().add(imageNode);
        }
        imageNode = stack;

        // Content
        VBox content = new VBox(8);
        content.getStyleClass().add("app-card-content");

        Text title = new Text(app.getName());
        title.setStyle("-fx-font-size: 14px;");
        title.getStyleClass().add("app-title");

        Text developer = new Text("by " + app.getDeveloperName());
        developer.setStyle("-fx-font-size: 12px;");
        developer.getStyleClass().add("app-developer");

        content.getChildren().addAll(title, developer);
        card.getChildren().addAll(imageNode, content);

        return card;
    }

    private Node createFallbackIcon() {
        Text icon = new Text("📱");
        icon.setFont(new Font(50));
        return icon;
    }

    @FXML
    private void handleAppCardClick(MouseEvent event) {
        Node clickedNode = (Node) event.getSource();

        String appId = clickedNode.getId();
        if (appId != null && mainLayoutController != null) {
            mainLayoutController.showAppDetail(appId);
        }
    }
}
