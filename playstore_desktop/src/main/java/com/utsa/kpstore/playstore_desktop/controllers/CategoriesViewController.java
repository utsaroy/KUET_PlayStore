package com.utsa.kpstore.playstore_desktop.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class CategoriesViewController {
    
    @FXML
    private void handleCategoryClick(MouseEvent event) {
        Node clickedNode = (Node) event.getSource();
        String categoryId = clickedNode.getId();
        
        // Extract category name from the clicked VBox
        String categoryName = extractCategoryName(categoryId);
        
        System.out.println("Category clicked: " + categoryName);
        showCategoryApps(categoryName);
    }
    
    private String extractCategoryName(String categoryId) {
        // Map category IDs to names
        return switch (categoryId) {
            case "category1" -> "Educational";
            case "category2" -> "Productivity";
            case "category3" -> "Utilities";
            case "category4" -> "Games";
            case "category5" -> "Creative";
            case "category6" -> "Social";
            case "category7" -> "Business";
            case "category8" -> "Music";
            case "category9" -> "Health";
            default -> "Unknown";
        };
    }
    
    private void showCategoryApps(String categoryName) {
        System.out.println("Showing apps in category: " + categoryName);
    }
}
