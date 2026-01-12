package com.utsa.kpstore.playstore_desktop.controllers;

import com.utsa.kpstore.playstore_desktop.models.User;
import com.utsa.kpstore.playstore_desktop.services.DatabaseHelper;
import com.utsa.kpstore.playstore_desktop.services.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Optional;

public class ProfileController {

    @FXML
    private Circle profileAvatar;

    @FXML
    private Text fullNameLabel;

    @FXML
    private Text emailLabel;

    @FXML
    private Text joinDateLabel;

    @FXML
    private Button changeNameBtn;

    @FXML
    private Button changePasswordBtn;

    @FXML
    private Button logoutBtn;

    @FXML
    private void initialize() {
        loadUserProfile();
    }

    private void loadUserProfile() {
        User currentUser = UserSession.getInstance().getCurrentUser();

        if (currentUser != null) {
            fullNameLabel.setText(currentUser.getFullName());
            emailLabel.setText(currentUser.getEmail());

            // Format join date
            if (currentUser.getCreatedAt() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy");
                String formattedDate = dateFormat.format(currentUser.getCreatedAt());
                joinDateLabel.setText("Member since " + formattedDate);
            }
        } else {
            fullNameLabel.setText("Guest User");
            emailLabel.setText("Not logged in");
            joinDateLabel.setText("--");
        }
    }

    @FXML
    private void handleChangeName() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null)
            return;

        TextInputDialog dialog = new TextInputDialog(currentUser.getFullName());
        dialog.setTitle("Change Name");
        dialog.setHeaderText("Enter your new name:");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (name.trim().isEmpty()) {
                showErrorAlert("Error", "Name cannot be empty.");
                return;
            }

            if (DatabaseHelper.updateUserName(currentUser.getId(), name.trim())) {
                // Update session
                currentUser.setFullName(name.trim());
                // Update UI
                fullNameLabel.setText(name.trim());
                showInfoAlert("Success", "Name updated successfully.");
            } else {
                showErrorAlert("Error", "Failed to update name.");
            }
        });
    }

    @FXML
    private void handleChangePassword() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null)
            return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Enter your current and new password");

        ButtonType changePasswordButtonType = new ButtonType("Change Password", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changePasswordButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        PasswordField currentPasswordField = new PasswordField();
        PasswordField newPasswordField = new PasswordField();
        PasswordField confirmPasswordField = new PasswordField();

        grid.add(new Label("Current Password:"), 0, 0);
        grid.add(currentPasswordField, 1, 0);
        grid.add(new Label("New Password:"), 0, 1);
        grid.add(newPasswordField, 1, 1);
        grid.add(new Label("Confirm Password:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Convert the result to a button type when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == changePasswordButtonType) {
                return ButtonType.OK;
            }
            return null;
        });

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            String currentPass = currentPasswordField.getText();
            String newPass = newPasswordField.getText();
            String confirmPass = confirmPasswordField.getText();

            if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                showErrorAlert("Error", "All fields are required.");
                return;
            }

            if (!newPass.equals(confirmPass)) {
                showErrorAlert("Error", "New passwords do not match.");
                return;
            }

            if (DatabaseHelper.updateUserPassword(currentUser.getId(), currentPass, newPass)) {
                showInfoAlert("Success", "Password updated successfully.");
            } else {
                showErrorAlert("Error", "Failed to update password. Please check your current password.");
            }
        }
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("You will need to login again to access your account.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                performLogout();
            }
        });
    }

    private void performLogout() {
        // Clear user session
        UserSession.getInstance().clearSession();

        // Navigate back to login page
        try {
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/utsa/kpstore/playstore_desktop/login-view.fxml"));
            Parent loginView = loader.load();
            Scene scene = new Scene(loginView);
            stage.setScene(scene);
            stage.setTitle("KUET PlayStore - Sign In");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Could not logout. Please try again.");
        }
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
