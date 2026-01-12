package com.utsa.kpstore.playstore_desktop.models;

import java.sql.Timestamp;

public class App {
    private int id;
    private String name;
    private String description;
    private int developerId;
    private String developerName;
    private int categoryId;
    private String categoryName;
    private String version;
    private long size; // in bytes
    private int downloads;
    private double price;
    private String iconUrl;
    private String packageName;
    private String apkFilePath;
    private boolean isApproved;
    private String adminFeedback;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public App() {
    }

    public App(int id, String name, String description, int developerId, String developerName,
            int categoryId, String categoryName, String version, long size, int downloads,
            double price, String iconUrl, String packageName, String apkFilePath,
            boolean isApproved, String adminFeedback, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.developerId = developerId;
        this.developerName = developerName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.version = version;
        this.size = size;
        this.downloads = downloads;
        this.price = price;
        this.iconUrl = iconUrl;
        this.packageName = packageName;
        this.apkFilePath = apkFilePath;
        this.isApproved = isApproved;
        this.adminFeedback = adminFeedback;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDeveloperId() {
        return developerId;
    }

    public void setDeveloperId(int developerId) {
        this.developerId = developerId;
    }

    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(String developerName) {
        this.developerName = developerName;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getFormattedSize() {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    public int getDownloads() {
        return downloads;
    }

    public void setDownloads(int downloads) {
        this.downloads = downloads;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isFree() {
        return price == 0.0;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getApkFilePath() {
        return apkFilePath;
    }

    public void setApkFilePath(String apkFilePath) {
        this.apkFilePath = apkFilePath;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public String getAdminFeedback() {
        return adminFeedback;
    }

    public void setAdminFeedback(String adminFeedback) {
        this.adminFeedback = adminFeedback;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "App{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", developerName='" + developerName + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", version='" + version + '\'' +
                ", size=" + getFormattedSize() +
                ", downloads=" + downloads +
                ", price=" + price +
                ", isApproved=" + isApproved +
                ", adminFeedback='" + adminFeedback + '\'' +
                '}';
    }
}
