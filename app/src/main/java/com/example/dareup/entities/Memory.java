package com.example.dareup.entities;

public class Memory {
    private String title;
    private String task;
    private String description;
    String id;
    boolean isExpanded;

    // Пустой конструктор необходим для Firebase
    public Memory() {
    }

    public Memory(String id, String title, String task, String description) {
        this.id = id;
        this.title = title;
        this.task = task;
        this.description = description;
        this.isExpanded = false;
    }

    public String getId() {
        if (id != null) return id;
        else return "";
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        if (title != null) return title;
        else return "";
    }

    public String getTask() {
        if (task != null) return task;
        else return "";
    }

    public String getDescription() {
        if (description != null) return description;
        else return "";
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        this.isExpanded = expanded;
    }
}

