package com.example.dareup;

public class Memory {
    private String title;
    private String task;
    private String description;

    // Пустой конструктор необходим для Firebase
    public Memory() {
    }

    public Memory(String title, String task, String description) {
        this.title = title;
        this.task = task;
        this.description = description;
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
}

