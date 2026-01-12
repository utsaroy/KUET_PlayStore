package com.utsa.kpstore.playstore_desktop.controllers;

import com.utsa.kpstore.playstore_desktop.models.User;
import com.utsa.kpstore.playstore_desktop.services.DatabaseHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class AdminManageUsersController {

    @FXML
    private Button backBtn;

    @FXML
    private VBox usersListContainer;

    @FXML
    public void initialize() {
        loadUsers();
    }

    private void loadUsers() {
        usersListContainer.getChildren().clear();
        List<User> users = DatabaseHelper.getAllUsers();

        if (users.isEmpty()) {
            Label placeholder = new Label("No users found.");
            placeholder.setStyle("-fx-text-fill: #5f6368; -fx-font-size: 14px;");
            usersListContainer.getChildren().add(placeholder);
            return;
        }

        for (User user : users) {
            usersListContainer.getChildren().add(createUserRow(user));
        }
    }

    private HBox createUserRow(User user) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 20, 10, 20));
        row.setStyle(
                "-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 3, 0, 0, 1);");

        Label idLabel = new Label(String.valueOf(user.getId()));
        idLabel.setPrefWidth(50);
        idLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333;");

        Label nameLabel = new Label(user.getFullName());
        nameLabel.setPrefWidth(200);
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        nameLabel.setStyle("-fx-text-fill: #333333;");

        Label emailLabel = new Label(user.getEmail());
        emailLabel.setPrefWidth(250);
        emailLabel.setStyle("-fx-text-fill: #333333;");

        Label statusLabel = new Label(user.isActive() ? "Active" : "Banned");
        statusLabel.setPrefWidth(100);
        statusLabel.setStyle(user.isActive() ? "-fx-text-fill: green; -fx-font-weight: bold;"
                : "-fx-text-fill: red; -fx-font-weight: bold;");

        Button actionBtn = new Button(user.isActive() ? "Ban User" : "Unban User");
        actionBtn.setPrefWidth(100);
        if (user.isActive()) {
            actionBtn.setStyle("-fx-background-color: #ffebee; -fx-text-fill: #d32f2f; -fx-cursor: hand;");
            actionBtn.setOnAction(e -> handleBanUser(user));
        } else {
            actionBtn.setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-cursor: hand;");
            actionBtn.setOnAction(e -> handleUnbanUser(user));
        }

        row.getChildren().addAll(idLabel, nameLabel, emailLabel, statusLabel, actionBtn);
        return row;
    }

    private void handleBanUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Ban User");
        alert.setHeaderText("Ban " + user.getFullName() + "?");
        alert.setContentText("Banned users will not be able to log in. Are you sure?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (DatabaseHelper.updateUserStatus(user.getId(), false)) {
                    loadUsers();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "User banned successfully.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to ban user.");
                }
            }
        });
    }

    private void handleUnbanUser(User user) {
        if (DatabaseHelper.updateUserStatus(user.getId(), true)) {
            loadUsers();
            showAlert(Alert.AlertType.INFORMATION, "Success", "User unbanned successfully.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to unban user.");
        }
    }

    @FXML
    private void handleBack() {
        try {
            Stage stage = (Stage) backBtn.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/utsa/kpstore/playstore_desktop/admin-home-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("KUET PlayStore - Admin Dashboard");
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
}
