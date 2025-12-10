package com.utsa.kpstore.playstore_desktop.controllers;

import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.scene.control.Label;

public class AppDetailViewController {
    
    @FXML
    private Text appTitle;
    
    @FXML
    private Text appDeveloper;

    public void setAppData(String appId) {
        loadAppDetails(appId);
    }

    private void loadAppDetails(String appId) {
        System.out.println("Loading details for app: " + appId);
    }
}
