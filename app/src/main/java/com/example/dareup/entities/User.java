package com.example.dareup.entities;

import java.util.List;

public class User {
    String id, name, photoUrl, activeTask, activeTaskDifficulty, idForFriend;
    int level, xp, tries;
    private List<String> completedTasks;

    public User() {}
    public User(String id, String name, int level, int xp, String photoUrl, String activeTask, String activeTaskDifficulty, String idForFriend, List<String> completedTasks) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.xp = xp;
        this.photoUrl = photoUrl;
        this.activeTask = activeTask;
        this.activeTaskDifficulty = activeTaskDifficulty;
        this.idForFriend = idForFriend;
        this.completedTasks = completedTasks;
    }


    public void setTries(int tries) {
        this.tries = tries;
    }

    public String getIdForFriend() {
        return idForFriend;
    }

    public void setIdForFriend(String idForFriend) {
        this.idForFriend = idForFriend;
    }

    public String getActiveTaskDifficulty() {
        return activeTaskDifficulty;
    }

    public void setActiveTaskDifficulty(String activeTaskDifficulty) {
        this.activeTaskDifficulty = activeTaskDifficulty;
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
