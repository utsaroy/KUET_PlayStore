package com.utsa.kpstore.models;

public class ListApp {
    private String name, title;
    private int id;

    public ListApp(){}
    public ListApp(String name, String title, int id) {
        this.name = name;
        this.title = title;
        this.id = id;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
