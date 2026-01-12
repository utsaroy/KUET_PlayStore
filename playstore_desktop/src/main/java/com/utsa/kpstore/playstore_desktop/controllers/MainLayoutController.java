package com.utsa.kpstore.playstore_desktop.controllers;

import com.utsa.kpstore.playstore_desktop.models.App;
import com.utsa.kpstore.playstore_desktop.models.Category;
import com.utsa.kpstore.playstore_desktop.services.DatabaseHelper;
import com.utsa.kpstore.playstore_desktop.services.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class MainLayoutController {

    @FXML
    private ScrollPane contentArea;

    @FXML
    private javafx.scene.control.TextField searchBox;

    @FXML
    private Button homeBtn;

    @FXML
    private Button newReleasesBtn;

    @FXML
    private VBox categoriesContainer;

    @FXML
    private Button developerDashboardBtn;

    @FXML
    private Button myAppsBtn;

    @FXML
    private Button profileBtn;

    @FXML
    private Button logoutBtn;

    private Button activeBtn;
    private Object currentController;

    @FXML
    public void initialize() {
        // Init with Home View
        showHome();

        // Load Categories
        loadCategories();

        // Add search listener
        if (searchBox != null) {
            searchBox.textProperty().addListener((observable, oldValue, newValue) -> {
                handleSearch();
            });
        }
    }

    private void loadCategories() {
        List<Category> categories = DatabaseHelper.getAllCategories();
        if (categoriesContainer != null) {
            categoriesContainer.getChildren().clear();

            for (Category category : categories) {
                Button catBtn = new Button(category.getName());
                catBtn.setMaxWidth(Double.MAX_VALUE);
                catBtn.getStyleClass().add("nav-button");
                catBtn.setStyle("-fx-font-size: 14px; -fx-padding: 8 15; -fx-alignment: CENTER-LEFT;");

                catBtn.setOnAction(e -> {
                    showCategoryApps(category.getId());
                    if (activeBtn != null)
                        activeBtn.getStyleClass().remove("active");
                    catBtn.getStyleClass().add("active");
                    activeBtn = catBtn;
                });

                categoriesContainer.getChildren().add(catBtn);
            }
        }
    }

    private void showCategoryApps(int categoryId) {
        Object controller = loadView("/com/utsa/kpstore/playstore_desktop/home-view.fxml");
        if (controller instanceof HomeViewController) {
            ((HomeViewController) controller).loadAppsByCategory(categoryId);
        }
    }

    @FXML
    private void showHome() {
        loadView("/com/utsa/kpstore/playstore_desktop/home-view.fxml");
        setActiveButton(homeBtn);
    }

    @FXML
    private void handleSearch() {
        String query = searchBox.getText().trim();

        // If query is empty, and we are on HomeView, reset to default home view
        if (query.isEmpty()) {
            if (currentController instanceof HomeViewController) {
                showHome();
            }
            return;
        }

        // Only allow search if we are currently on a HomeViewController (Grid view)
        if (currentController instanceof HomeViewController) {
            List<App> results = DatabaseHelper.searchApps(query);
            ((HomeViewController) currentController).displaySearchResults(results, query);
        }
    }

    /* Removed Categories Button Action */

    @FXML
    private void showNewReleases() {
        Object controller = loadView("/com/utsa/kpstore/playstore_desktop/home-view.fxml");
        if (controller instanceof HomeViewController) {
            ((HomeViewController) controller).loadRecentApps();
        }
        setActiveButton(newReleasesBtn);
    }

    @FXML
    private void showMyApps() {
        // Should point to My Apps page
        loadView("/com/utsa/kpstore/playstore_desktop/my-apps-view.fxml");
        setActiveButton(myAppsBtn);
    }

    @FXML
    private void showDeveloperDashboard() {
        Object controller = loadView("/com/utsa/kpstore/playstore_desktop/developer-dashboard.fxml");
        setActiveButton(developerDashboardBtn);
    }

    @FXML
    private void showProfile() {
        loadView("/com/utsa/kpstore/playstore_desktop/profile-page.fxml");
        if (activeBtn != null) {
            activeBtn.getStyleClass().remove("active");
            activeBtn = null;
        }
    }

    @FXML
    private void handleLogout() {
        // Clear user session
        UserSession.getInstance().clearSession();

        // Navigate back to login page
        try {
            Stage stage = (Stage) contentArea.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/utsa/kpstore/playstore_desktop/login-view.fxml"));
            Parent loginView = loader.load();
            Scene scene = new Scene(loginView);
            stage.setScene(scene);
            stage.setScene(scene);
            stage.setTitle("KUET PlayStore - Sign In");
            stage.setWidth(800);
            stage.setHeight(600);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAppDetail(String appIdStr) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/utsa/kpstore/playstore_desktop/app-detail-view.fxml"));
            Parent view = loader.load();

            AppDetailViewController controller = loader.getController();
            int appId = Integer.parseInt(appIdStr);
            controller.setAppId(appId);
            controller.setMainLayoutController(this);

            this.currentController = controller;

            contentArea.setContent(view);

            // Clear active button as we are in detail view
            if (activeBtn != null) {
                activeBtn.getStyleClass().remove("active");
                activeBtn = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showEditApp(App app) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/utsa/kpstore/playstore_desktop/upload-app-view.fxml"));
            Parent view = loader.load();

            UploadAppViewController controller = loader.getController();
            controller.setAppToEdit(app);

            this.currentController = controller;

            contentArea.setContent(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showUploadApp() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/utsa/kpstore/playstore_desktop/upload-app-view.fxml"));
            Parent view = loader.load();
            this.currentController = loader.getController();
            contentArea.setContent(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof HomeViewController) {
                ((HomeViewController) controller).setMainLayoutController(this);
            } else if (controller instanceof DeveloperDashboardController) {
                ((DeveloperDashboardController) controller).setMainLayoutController(this);
            }

            this.currentController = controller;
            contentArea.setContent(view);
            return controller;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.err.println("Error loading view: " + fxmlFile);
            e.printStackTrace();
            return null;
        }
    }

    private void setActiveButton(Button activeBtn) {
        // Remove active class from all navigation buttons
        if (homeBtn != null)
            homeBtn.getStyleClass().remove("active");
        if (newReleasesBtn != null)
            newReleasesBtn.getStyleClass().remove("active");
        if (developerDashboardBtn != null)
            developerDashboardBtn.getStyleClass().remove("active");
        if (myAppsBtn != null)
            myAppsBtn.getStyleClass().remove("active");
        if (activeBtn != null)
            activeBtn.getStyleClass().remove("active");

        // Also clear category buttons active state
        if (categoriesContainer != null) {
            categoriesContainer.getChildren().forEach(node -> node.getStyleClass().remove("active"));
        }

        this.activeBtn = activeBtn;
        if (activeBtn != null) {
            activeBtn.getStyleClass().add("active");
        }
    }
}
