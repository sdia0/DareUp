package com.example.dareup;

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
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getTask() {
        return task;
    }

    public String getDescription() {
        return description;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        this.isExpanded = expanded;
    }
}

