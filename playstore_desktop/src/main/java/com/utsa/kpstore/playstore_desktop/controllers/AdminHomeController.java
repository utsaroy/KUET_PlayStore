package com.utsa.kpstore.playstore_desktop.controllers;

import com.utsa.kpstore.playstore_desktop.services.DatabaseHelper;
import com.utsa.kpstore.playstore_desktop.services.UserSession;
import javafx.fxml.FXML;

import javafx.scene.control.Label;

public class AdminHomeController {

    @FXML
    private Label adminNameLabel;

    @FXML
    private Label totalUsersLabel;

    @FXML
    private Label totalAppsLabel;

    @FXML
    private Label totalDownloadsLabel;

    @FXML
    public void initialize() {
        // Load admin information
        if (UserSession.getInstance().isLoggedIn()) {
            adminNameLabel.setText(UserSession.getInstance().getCurrentUser().getFullName());
        }

        // Load statistics
        loadStatistics();
    }

    private void loadStatistics() {
        // Load total users count
        int totalUsers = DatabaseHelper.getTotalUsersCount();
        totalUsersLabel.setText(String.valueOf(totalUsers));

        // Load total apps count
        int totalApps = DatabaseHelper.getTotalAppsCount();
        totalAppsLabel.setText(String.valueOf(totalApps));


        totalDownloadsLabel.setText("0");
    }
}
