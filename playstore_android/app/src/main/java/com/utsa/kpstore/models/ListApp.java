package com.utsa.kpstore.models;

public class ListApp {
    private String name, title, category;
    private String id;

    public ListApp(){}
    public ListApp(String name, String title, String id, String category) {
        this.name = name;
        this.title = title;
        this.id = id;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
