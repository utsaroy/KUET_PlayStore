package com.utsa.kpstore.playstore_desktop.controllers;

import com.utsa.kpstore.playstore_desktop.services.DatabaseHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.regex.Pattern;

public class CreateAccountController {
    @FXML
    private TextField fullNameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Button signUpButton;
    
    @FXML
    private Hyperlink loginLink;
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    @FXML
    private void initialize() {
        // Hide status label initially
        statusLabel.setVisible(false);
    }
    
    @FXML
    private void handleSignUp(ActionEvent event) {
        // Clear previous status
        statusLabel.setVisible(false);
        
        // Get input values
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        // Validate inputs
        if (!validateInputs(fullName, email, password)) {
            return;
        }
        
        // Check if email already exists
        if (DatabaseHelper.emailExists(email)) {
            showError("This email is already registered. Please use a different email or login.");
            return;
        }
        
        // Register user in database
        com.utsa.kpstore.playstore_desktop.models.User user = DatabaseHelper.registerUser(fullName, email, password);
        
        if (user != null) {
            showSuccessAndProceedToLogin();
        } else {
            showError("Registration failed. Email may already be registered.");
        }
    }
    
    private boolean validateInputs(String fullName, String email, String password) {
        // Validate full name
        if (fullName.isEmpty()) {
            showError("Please enter your full name");
            return false;
        }
        
        if (fullName.length() < 3) {
            showError("Full name must be at least 3 characters long");
            return false;
        }
        
        // Validate email
        if (email.isEmpty()) {
            showError("Please enter your email address");
            return false;
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Please enter a valid email address");
            return false;
        }
        
        // Validate password
        if (password.isEmpty()) {
            showError("Please enter a password");
            return false;
        }
        
        if (password.length() < 6) {
            showError("Password must be at least 6 characters long");
            return false;
        }
        
        // Check password strength
        if (!isPasswordStrong(password)) {
            showError("Password must contain at least one letter and one number");
            return false;
        }
        
        return true;
    }
    
    private boolean isPasswordStrong(String password) {
        boolean hasLetter = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        
        return hasLetter && hasDigit;
    }
    
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 13px;");
        statusLabel.setVisible(true);
    }
    
    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-size: 13px; -fx-font-weight: bold;");
        statusLabel.setVisible(true);
    }
    
    private void showSuccessAndProceedToLogin() {
        showSuccess("Account created successfully! Redirecting to login...");
        
        // Wait 1.5 seconds then navigate to login
        new Thread(() -> {
            try {
                Thread.sleep(1500);
                javafx.application.Platform.runLater(() -> navigateToLogin());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    @FXML
    private void handleBackToLogin(ActionEvent event) {
        navigateToLogin();
    }
    
    private void navigateToLogin() {
        try {
            Stage stage = (Stage) signUpButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/utsa/kpstore/playstore_desktop/login-view.fxml"));
            Parent loginView = loader.load();
            Scene scene = new Scene(loginView);
            stage.setScene(scene);
            stage.setTitle("KUET PlayStore - Sign In");
        } catch (IOException e) {
            showError("Error navigating to login page");
            e.printStackTrace();
        }
    }
}
