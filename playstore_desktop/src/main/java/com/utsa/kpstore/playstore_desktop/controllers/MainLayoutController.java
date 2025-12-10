package com.utsa.kpstore.playstore_desktop.controllers;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class MainLayoutController {

    @FXML
    private StackPane contentArea;

    @FXML
    private Button homeBtn;

    @FXML
    private Button categoriesBtn;

    @FXML
    private Button featuredBtn;

    @FXML
    private Button newReleasesBtn;

    @FXML
    private Button myAppsBtn;


    @FXML
    public void initialize() {
        // Load home view by default
        showHome();
        // Set home button as active
        setActiveButton(homeBtn);
    }

    @FXML
    private void showHome() {
        loadView("/com/utsa/kpstore/playstore_desktop/home-view.fxml");
        setActiveButton(homeBtn);
    }

    @FXML
    private void showCategories() {
        loadView("/com/utsa/kpstore/playstore_desktop/categories-view.fxml");
        setActiveButton(categoriesBtn);
    }

    @FXML
    private void showFeatured() {
        loadView("/com/utsa/kpstore/playstore_desktop/home-view.fxml");
        setActiveButton(featuredBtn);
    }

    @FXML
    private void showNewReleases() {
        loadView("/com/utsa/kpstore/playstore_desktop/home-view.fxml");
        setActiveButton(newReleasesBtn);
    }

    @FXML
    private void showMyApps() {
        loadView("/com/utsa/kpstore/playstore_desktop/home-view.fxml");
        setActiveButton(myAppsBtn);
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof HomeViewController) {
                ((HomeViewController) controller).setMainLayoutController(this);
            }
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.err.println("Error loading view: " + fxmlFile);
        }
    }

    private void setActiveButton(Button activeBtn) {
        // Remove active class from all navigation buttons
        homeBtn.getStyleClass().remove("active");
        categoriesBtn.getStyleClass().remove("active");
        featuredBtn.getStyleClass().remove("active");
        newReleasesBtn.getStyleClass().remove("active");
        myAppsBtn.getStyleClass().remove("active");

        // Add active class to the clicked button
        if (!activeBtn.getStyleClass().contains("active")) {
            activeBtn.getStyleClass().add("active");
        }
    }

    public void showAppDetail(String appId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/utsa/kpstore/playstore_desktop/app-detail-view.fxml"));
            Parent view = loader.load();

            AppDetailViewController controller = loader.getController();
            controller.setAppData(appId);
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading app detail view");
        }
    }
}
