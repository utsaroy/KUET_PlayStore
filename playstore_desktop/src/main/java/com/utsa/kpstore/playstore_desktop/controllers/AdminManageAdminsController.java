package com.utsa.kpstore.playstore_desktop.controllers;

import com.utsa.kpstore.playstore_desktop.models.User;
import com.utsa.kpstore.playstore_desktop.services.DatabaseHelper;
import com.utsa.kpstore.playstore_desktop.services.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.List;
import java.util.Optional;

public class AdminManageAdminsController {

    @FXML
    private Button backBtn;

    @FXML
    private VBox adminsListContainer;

    @FXML
    public void initialize() {
        loadAdmins();
    }

    private void loadAdmins() {
        adminsListContainer.getChildren().clear();
        List<User> admins = DatabaseHelper.getAllAdmins();

        for (User admin : admins) {
            adminsListContainer.getChildren().add(createAdminRow(admin));
        }
    }

    private HBox createAdminRow(User admin) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 20, 10, 20));
        row.setStyle(
                "-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 3, 0, 0, 1);");

        Label idLabel = new Label(String.valueOf(admin.getId()));
        idLabel.setPrefWidth(50);
        idLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333;");

        Label nameLabel = new Label(admin.getFullName());
        nameLabel.setPrefWidth(200);
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        nameLabel.setStyle("-fx-text-fill: #333333;");

        Label emailLabel = new Label(admin.getEmail());
        emailLabel.setPrefWidth(250);
        emailLabel.setStyle("-fx-text-fill: #333333;");

        Label statusLabel = new Label(admin.isActive() ? "Active" : "Inactive");
        statusLabel.setPrefWidth(100);
        statusLabel.setStyle(admin.isActive() ? "-fx-text-fill: green; -fx-font-weight: bold;"
                : "-fx-text-fill: red; -fx-font-weight: bold;");

        Button actionBtn = new Button("Demote");
        actionBtn.setPrefWidth(100);

        // Disable demote button if it's the current user
        if (UserSession.getInstance().getCurrentUser().getEmail().equalsIgnoreCase(admin.getEmail())) {
            actionBtn.setDisable(true);
            actionBtn.setText("(You)");
        } else {
            actionBtn.setStyle("-fx-background-color: #ffebee; -fx-text-fill: #d32f2f; -fx-cursor: hand;");
            actionBtn.setOnAction(e -> handleDemoteAdmin(admin));
        }

        row.getChildren().addAll(idLabel, nameLabel, emailLabel, statusLabel, actionBtn);
        return row;
    }

    private void handleDemoteAdmin(User admin) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Demote Admin");
        alert.setHeaderText("Remove Admin Privileges?");
        alert.setContentText("Are you sure you want to demote " + admin.getFullName() + " to a regular user?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (DatabaseHelper.demoteAdmin(admin.getEmail())) {
                    loadAdmins();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Admin demoted successfully.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to demote admin.");
                }
            }
        });
    }

    @FXML
    private void handleAddAdmin() {

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Admin");
        dialog.setHeaderText("Enter email address of the user");
        dialog.setContentText("Email:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(email -> {
            if (DatabaseHelper.emailExists(email)) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Promote User");
                alert.setHeaderText("User found: " + email);
                alert.setContentText("Do you want to promote this user to Admin?");

                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        if (DatabaseHelper.makeUserAdmin(email)) {
                            loadAdmins();
                            showAlert(Alert.AlertType.INFORMATION, "Success", "User promoted to Admin successfully.");
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to promote user.");
                        }
                    }
                });
            } else {
                showRegisterAdminDialog(email);
            }
        });
    }

    private void showRegisterAdminDialog(String email) {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Register New Admin");
        dialog.setHeaderText("Create a new Admin account for: " + email);

        ButtonType registerButtonType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerButtonType) {
                return new Pair<>(nameField.getText(), passwordField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(pair -> {
            String name = pair.getKey();
            String password = pair.getValue();

            if (name.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Name and Password cannot be empty.");
                return;
            }

            if (DatabaseHelper.registerAdmin(name, email, password)) {
                loadAdmins();
                showAlert(Alert.AlertType.INFORMATION, "Success", "New Admin registered successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to register new Admin.");
            }
        });
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
