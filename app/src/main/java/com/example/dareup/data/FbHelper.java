package com.example.dareup.data;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.dareup.MainActivity;
import com.example.dareup.activities.WelcomeActivity;
import com.example.dareup.entities.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FbHelper {
    private final DatabaseReference databaseReference;

    public FbHelper(String nodeName) {
        // Инициализация ссылки на узел базы данных
        databaseReference = FirebaseDatabase.getInstance().getReference(nodeName);
    }

    // Метод для добавления данных
    public <T> void addData(String key, T value) {
        databaseReference.child(key).setValue(value);
    }

    // Метод для обновления данных

    // Метод для удаления данных
    public void deleteData(String key) {
        databaseReference.child(key).removeValue();
    }

    // Метод для получения ссылки на узел
    public DatabaseReference getDatabaseReference() {
        return databaseReference;
    }
}

