package com.utsa.kpstore.playstore_desktop.controllers;

import com.utsa.kpstore.playstore_desktop.models.User;
import com.utsa.kpstore.playstore_desktop.services.DatabaseHelper;
import com.utsa.kpstore.playstore_desktop.services.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import javafx.stage.Stage;
import javafx.scene.control.Alert;

public class AdminLoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validate input
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password");
            return;
        }

        // Validate admin credentials
        User admin = DatabaseHelper.validateAdminLogin(email, password);

        if (admin != null) {
            // Save admin to session
            UserSession.getInstance().setCurrentUser(admin);

            // Navigate to admin home
            try {
                Stage stage = (Stage) emailField.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/utsa/kpstore/playstore_desktop/admin-layout.fxml"));
                Parent adminHome = loader.load();
                Scene scene = new Scene(adminHome);
                stage.setScene(scene);
                stage.setScene(scene);
                stage.setTitle("KUET PlayStore - Admin Dashboard");
                stage.setMaximized(true);
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error loading admin dashboard");
            }
        } else {
            // Check if user exists but is banned
            User bannedCheck = DatabaseHelper.getUserByEmail(email);
            if (bannedCheck != null && !bannedCheck.isActive()) {
                showAlert("Login Failed", "Your account has been banned. Please contact support.",
                        Alert.AlertType.ERROR);
            } else {
                showAlert("Login Failed", "Invalid email or password, or not an admin account.", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleBackToUserLogin() {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/utsa/kpstore/playstore_desktop/login-view.fxml"));
            Parent loginView = loader.load();
            Scene scene = new Scene(loginView);
            stage.setScene(scene);
            stage.setTitle("KUET PlayStore - Sign In");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
