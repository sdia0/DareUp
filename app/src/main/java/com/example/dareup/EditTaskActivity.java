package com.example.dareup;

import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EditTaskActivity extends AppCompatActivity {
    private RecyclerView rvPhotos;
    private PhotoAdapter photoAdapter;
    private List<Uri> photoList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_task);

        // Инициализируем RecyclerView
        rvPhotos = findViewById(R.id.rvPhotos);
        rvPhotos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Пример данных: URI уже загруженных фото
        photoList = new ArrayList<>();
        // photoList.add(Uri.parse("content://...")); // Здесь можно добавить реальные Uri фото

        // Настройка адаптера
        photoAdapter = new PhotoAdapter(this, photoList, new PhotoAdapter.OnAddPhotoClickListener() {
            @Override
            public void onAddPhotoClick() {
                // Логика загрузки нового фото
            }
        });

        rvPhotos.setAdapter(photoAdapter);
    }
}