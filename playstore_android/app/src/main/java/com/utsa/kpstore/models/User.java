package com.utsa.kpstore.models;

public class User {
    String name, email;
    boolean developer;
    String developerRequestStatus; // "none", "pending", "approved", "rejected"

    public User(){

    }
    public User(String name, String email, boolean developer, String developerRequestStatus) {
        this.name = name;
        this.email = email;
        this.developer = developer;
        this.developerRequestStatus = developerRequestStatus;
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

    public boolean isDeveloper() {
        return developer;
    }

    public void setDeveloper(boolean developer) {
        this.developer = developer;
    }

    public String getDeveloperRequestStatus() {
        return developerRequestStatus;
    }

    public void setDeveloperRequestStatus(String developerRequestStatus) {
        this.developerRequestStatus = developerRequestStatus;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", developer=" + developer +
                ", developerRequestStatus='" + developerRequestStatus + '\'' +
                '}';
    }
}
