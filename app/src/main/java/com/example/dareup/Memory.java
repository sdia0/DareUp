package com.example.dareup;

public class Memory {
    private String title;
    private String task;
    private String description;

    boolean isExpanded;

    // Пустой конструктор необходим для Firebase
    public Memory() {
    }

    public Memory(String title, String task, String description) {
        this.title = title;
        this.task = task;
        this.description = description;
        this.isExpanded = false;
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

