package com.example.dareup;

import java.util.ArrayList;

public class Task {
    String id;
    String title;
    String notes;
    ArrayList<String> photoUrls;
    boolean isExpanded; // Добавляем это поле

    public Task(String id, String title, String notes, ArrayList<String> photoUrls) {
        this.id = id;
        this.title = title;
        this.notes = notes;
        this.photoUrls = photoUrls;
        this.isExpanded = false; // Изначально состояние свернуто
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public ArrayList<String> getPhotoUrls() {
        return photoUrls;
    }

    public void setPhotoUrls(ArrayList<String> photoUrls) {
        this.photoUrls = photoUrls;
    }

    public boolean isExpanded() { // Метод для получения состояния развертывания
        return isExpanded;
    }

    public void setExpanded(boolean expanded) { // Метод для установки состояния развертывания
        isExpanded = expanded;
    }
}
