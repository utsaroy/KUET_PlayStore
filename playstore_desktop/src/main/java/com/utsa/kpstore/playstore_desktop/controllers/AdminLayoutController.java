package com.utsa.kpstore.playstore_desktop.controllers;

import com.utsa.kpstore.playstore_desktop.services.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class AdminLayoutController {

    @FXML
    private javafx.scene.control.ScrollPane contentArea;

    @FXML
    private Label headerLabel;

    @FXML
    private Label adminNameLabel;

    @FXML
    private Button dashboardBtn;

    @FXML
    private Button usersBtn;

    @FXML
    private Button appsBtn;

    @FXML
    private Button adminsBtn;

    @FXML
    private Button logoutBtn;

    private Button activeBtn;

    @FXML
    public void initialize() {
        if (UserSession.getInstance().isLoggedIn()) {
            adminNameLabel.setText(UserSession.getInstance().getCurrentUser().getFullName());
        }

        // Initialize with default view (Dashboard)
        showDashboard();
    }

    @FXML
    private void showDashboard() {
        loadView("/com/utsa/kpstore/playstore_desktop/admin-home-view.fxml", "Dashboard");
        setActiveButton(dashboardBtn);
    }

    @FXML
    private void showUsers() {
        loadView("/com/utsa/kpstore/playstore_desktop/admin-manage-users-view.fxml", "Manage Users");
        setActiveButton(usersBtn);
    }

    @FXML
    private void showApps() {
        loadView("/com/utsa/kpstore/playstore_desktop/admin-approve-apps-view.fxml", "Approve Apps");
        setActiveButton(appsBtn);
    }

    @FXML
    private void showAdmins() {
        loadView("/com/utsa/kpstore/playstore_desktop/admin-manage-admins-view.fxml", "Manage Administrators");
        setActiveButton(adminsBtn);
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("You will need to login again to access the admin panel.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                performLogout();
            }
        });
    }

    private void performLogout() {
        UserSession.getInstance().clearSession();
        try {
            Stage stage = (Stage) contentArea.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/utsa/kpstore/playstore_desktop/admin-login-view.fxml"));
            Parent loginView = loader.load();
            Scene scene = new Scene(loginView);
            stage.setScene(scene);
            stage.setScene(scene);
            stage.setTitle("KUET PlayStore - Admin Login");
            stage.setWidth(800);
            stage.setHeight(600);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.setContent(view);
            headerLabel.setText(title);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading view: " + fxmlPath);
        }
    }

    private void setActiveButton(Button btn) {
        if (activeBtn != null) {
            activeBtn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-font-size: 14px; -fx-padding: 12 15; -fx-cursor: hand;");
        }
        activeBtn = btn;
        if (activeBtn != null) {
            activeBtn.setStyle(
                    "-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12 15; -fx-cursor: hand; -fx-border-color: #3498db; -fx-border-width: 0 0 0 4;");
        }
    }
}
