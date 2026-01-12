package com.utsa.kpstore.playstore_desktop.models;

import java.sql.Timestamp;

public class Rating {
    private int id;
    private int appId;
    private int userId;
    private String userName;
    private int rating; // 1-5 stars
    private String reviewText;
    private Timestamp createdAt;

    public Rating() {
    }

    public Rating(int id, int appId, int userId, String userName, int rating, String reviewText, Timestamp createdAt) {
        this.id = id;
        this.appId = appId;
        this.userId = userId;
        this.userName = userName;
        this.rating = rating;
        this.reviewText = reviewText;
        this.createdAt = createdAt;
    }

    public Rating(int appId, int userId, int rating, String reviewText) {
        this.appId = appId;
        this.userId = userId;
        this.rating = rating;
        this.reviewText = reviewText;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        this.rating = rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Rating{" +
                "id=" + id +
                ", appId=" + appId +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", rating=" + rating +
                ", reviewText='" + reviewText + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
