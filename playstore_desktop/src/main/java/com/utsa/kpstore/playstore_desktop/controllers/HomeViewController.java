package com.utsa.kpstore.playstore_desktop.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class HomeViewController {
    
    private MainLayoutController mainLayoutController;

    //will be called by MainLayoutController after loading this view
    public void setMainLayoutController(MainLayoutController controller) {
        this.mainLayoutController = controller;
    }

    @FXML
    private void handleAppCardClick(MouseEvent event) {
        Node clickedNode = (Node) event.getSource();

        String appId = clickedNode.getId();
        if (mainLayoutController != null) {
            mainLayoutController.showAppDetail(appId);
        }
    }
}
