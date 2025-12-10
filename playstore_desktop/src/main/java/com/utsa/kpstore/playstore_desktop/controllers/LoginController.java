package com.utsa.kpstore.playstore_desktop.controllers;
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
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Login Failed", "Please enter you data.", Alert.AlertType.WARNING);
            return;
        }

        // TODO: authentication logic
        Stage stage = (Stage) loginButton.getScene().getWindow();
        proceedToMainApp(stage);
    }

    @FXML
    private void handleSignUp() {
    }


    private void proceedToMainApp(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/utsa/kpstore/playstore_desktop/main-layout.fxml"));
            Parent mainView = loader.load();
            Scene scene = new Scene(mainView);
            stage.setScene(scene);
            stage.setTitle("KUET PlayStore");
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
