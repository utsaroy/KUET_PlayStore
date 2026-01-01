package com.utsa.kpstore.models;

import java.util.List;

public class Developer {
    private String developerId;
    private String name;
    private String email;
    private String bio;
    private boolean approved;
    private boolean banned;
    private int totalApps;

    public Developer() {
    }

    public Developer(String developerId, String name, String email, String bio) {
        this.developerId = developerId;
        this.name = name;
        this.email = email;
        this.bio = bio;
        this.approved = false;
        this.banned = false;
        this.totalApps = 0;
    }

    public String getDeveloperId() {
        return developerId;
    }

    public void setDeveloperId(String developerId) {
        this.developerId = developerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public int getTotalApps() {
        return totalApps;
    }

    public void setTotalApps(int totalApps) {
        this.totalApps = totalApps;
    }
}
