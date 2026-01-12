package com.utsa.kpstore.playstore_desktop.services;

import com.utsa.kpstore.playstore_desktop.models.User;


public class UserSession {
    private static UserSession instance;
    private User currentUser;
    
    private UserSession() {}
    
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public void clearSession() {
        currentUser = null;
    }
    
    public String getUserFullName() {
        return currentUser != null ? currentUser.getFullName() : "Guest";
    }
    
    public String getUserEmail() {
        return currentUser != null ? currentUser.getEmail() : "";
    }
    
    public int getUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }
}
