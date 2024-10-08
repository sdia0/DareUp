package com.example.dareup;

public class User {
    String id, name, photoUrl, activeTask;
    int level, xp;

    public User() {}

    public User(String id, String name, int level, int xp, String photoUrl, String activeTask) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.xp = xp;
        this.photoUrl = photoUrl;
        this.activeTask = activeTask;
    }

    public String getActiveTask() {
        return activeTask;
    }

    public void setActiveTask(String activeTask) {
        this.activeTask = activeTask;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
