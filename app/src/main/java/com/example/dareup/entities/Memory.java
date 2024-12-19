package com.example.dareup.entities;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class Memory {
    private String title;
    private String task;
    private String description;
    String id;
    boolean isExpanded;
    List<String> images = new ArrayList<>();
    // Пустой конструктор необходим для Firebase
    public Memory() {
    }

    public Memory(String title, String task, String description, String id) {
        this.title = title;
        this.task = task;
        this.description = description;
        this.id = id;
    }

    public Memory(String id, String title, String task, String description, List<String> links) {
        this.id = id;
        this.title = title;
        this.task = task;
        this.description = description;
        this.isExpanded = false;
        this.images = links;
    }

    public List<String> getImages() {
        return images;
    }

    public List<Uri> getUriImages() {
        List<Uri> links = new ArrayList<>();
        for (String link : getImages())
            links.add(Uri.parse(link));
        return links;
    }

    public void setImages(List<String> images) {
        this.images = images;
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

