package com.utsa.kpstore.playstore_desktop;

import com.utsa.kpstore.playstore_desktop.services.DatabaseHelper;
import com.utsa.kpstore.playstore_desktop.services.FileUploadService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Launcher extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        //Initialize database
        DatabaseHelper.initializeDatabase();
        
        //Initialize file upload directories
        FileUploadService.initializeUploadDirectories();
        
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("KUET PlayStore - Sign In");
        stage.setScene(scene);
        stage.show();
    }
}
