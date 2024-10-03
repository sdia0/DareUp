package com.example.dareup;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TaskActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ExpandableTaskAdapter taskAdapter;
    private List<Task> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        recyclerView = findViewById(R.id.recycler_view);
        taskList = new ArrayList<>();

        // Пример добавления задач с несколькими фотографиями
        ArrayList<String> photos1 = new ArrayList<>();
        photos1.add("https://example.com/photo1.jpg");
        photos1.add("https://example.com/photo2.jpg");

        ArrayList<String> photos2 = new ArrayList<>();
        photos2.add("https://example.com/photo3.jpg");

        taskList.add(new Task("1", "Task 1", "Description for task 1", photos1));
        taskList.add(new Task("2", "Task 2", "Description for task 2", photos2));
        taskList.add(new Task("3", "Task 3", "Description for task 3", new ArrayList<>()));

        taskAdapter = new ExpandableTaskAdapter(taskList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);
    }
}
