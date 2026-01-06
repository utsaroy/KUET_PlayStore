package com.utsa.kpstore.models;

import java.io.Serializable;
import java.util.List;

public class AppDetails {
    private String appId;
    private String appName;
    private String version;
    private String description;
    private String shortDescription;
    private String developerName;
    private String developerEmail;
    private String category;
    private List<String> screenshotUrls;
    private String apkUrl;
    private long fileSize;
    private String lastUpdated;
    private String publishedDate;
    private double rating;
    private int totalRatings;
    private int totalDownloads;

    public AppDetails() {
    }

    public AppDetails(String appId, String appName, String version, String description, String shortDescription, String developerName, String developerEmail, String category, List<String> screenshotUrls, String apkUrl, long fileSize, String lastUpdated, String publishedDate, double rating, int totalRatings, int totalDownloads) {
        this.appId = appId;
        this.appName = appName;
        this.version = version;
        this.description = description;
        this.shortDescription = shortDescription;
        this.developerName = developerName;
        this.developerEmail = developerEmail;
        this.category = category;
        this.screenshotUrls = screenshotUrls;
        this.apkUrl = apkUrl;
        this.fileSize = fileSize;
        this.lastUpdated = lastUpdated;
        this.publishedDate = publishedDate;
        this.rating = rating;
        this.totalRatings = totalRatings;
        this.totalDownloads = totalDownloads;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(String developerName) {
        this.developerName = developerName;
    }

    public String getDeveloperEmail() {
        return developerEmail;
    }

    public void setDeveloperEmail(String developerEmail) {
        this.developerEmail = developerEmail;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getScreenshotUrls() {
        return screenshotUrls;
    }

    public void setScreenshotUrls(List<String> screenshotUrls) {
        this.screenshotUrls = screenshotUrls;
    }

    public String getApkUrl() {
        return apkUrl;
    }

    public void setApkUrl(String apkUrl) {
        this.apkUrl = apkUrl;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(int totalRatings) {
        this.totalRatings = totalRatings;
    }

    public int getTotalDownloads() {
        return totalDownloads;
    }

    public void setTotalDownloads(int totalDownloads) {
        this.totalDownloads = totalDownloads;
    }


    public String getFormattedFileSize() {

        return fileSize + " MB";

    }

    public String getFormattedDownloads() {
        if (totalDownloads < 1000) {
            return String.valueOf(totalDownloads);
        } else if (totalDownloads < 1000000) {
            return String.format("%.1fK", totalDownloads / 1000.0);
        } else if (totalDownloads < 1000000000) {
            return String.format("%.1fM", totalDownloads / 1000000.0);
        } else {
            return String.format("%.1fB", totalDownloads / 1000000000.0);
        }
    }

    @Override
    public String toString() {
        return "AppDetails{" +
                "appId='" + appId + '\'' +
                ", appName='" + appName + '\'' +
                ", version='" + version + '\'' +
                ", category='" + category + '\'' +
                ", rating=" + rating +
                ", totalDownloads=" + totalDownloads +
                '}';
    }
}
