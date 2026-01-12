package com.utsa.kpstore.playstore_desktop.controllers;

import com.utsa.kpstore.playstore_desktop.models.User;
import com.utsa.kpstore.playstore_desktop.services.DatabaseHelper;
import com.utsa.kpstore.playstore_desktop.services.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Hyperlink signUpLink;

    @FXML
    private Hyperlink adminLoginLink;

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Login Failed", "Please enter your email and password.", Alert.AlertType.WARNING);
            return;
        }

        // Validate credentials
        User user = DatabaseHelper.validateLogin(email, password);
        if (user != null) {
            // Save user to session
            UserSession.getInstance().setCurrentUser(user);
            System.out.println("Login successful: " + user.getFullName());
            Stage stage = (Stage) loginButton.getScene().getWindow();
            proceedToMainApp(stage);
        } else {
            // Check if user exists but is banned
            User bannedCheck = DatabaseHelper.getUserByEmail(email);
            if (bannedCheck != null && !bannedCheck.isActive()) {
                showAlert("Login Failed", "Your account has been banned. Please contact support.",
                        Alert.AlertType.ERROR);
            } else {
                showAlert("Login Failed", "Invalid email or password. Please try again.", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleSignUp() {
        try {
            Stage stage = (Stage) signUpLink.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/utsa/kpstore/playstore_desktop/createAccount.fxml"));
            Parent createAccountView = loader.load();
            Scene scene = new Scene(createAccountView);
            stage.setScene(scene);
            stage.setTitle("KUET PlayStore - Create Account");
        } catch (IOException e) {
            showAlert("Error", "Could not load sign up page", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdminLogin() {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/utsa/kpstore/playstore_desktop/admin-login-view.fxml"));
            Parent adminLoginView = loader.load();
            Scene scene = new Scene(adminLoginView);
            stage.setScene(scene);
            stage.setTitle("KUET PlayStore - Admin Login");
        } catch (IOException e) {
            showAlert("Error", "Could not load admin login page", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void proceedToMainApp(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/utsa/kpstore/playstore_desktop/main-layout.fxml"));
            Parent mainView = loader.load();
            Scene scene = new Scene(mainView);
            stage.setScene(scene);
            stage.setScene(scene);
            stage.setTitle("KUET PlayStore");
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert("Error", "Something went wrong", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
