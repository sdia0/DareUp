package com.example.dareup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TasksFragment extends Fragment {
    private RecyclerView recyclerView;
    private ExpandableTaskAdapter taskAdapter;
    private List<Task> taskList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инфлейт XML разметки фрагмента
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        recyclerView = view.findViewById(R.id.recycler_view); // Инициализация RecyclerView
        taskList = new ArrayList<>(); // Инициализация списка задач

        // Пример добавления задач с несколькими фотографиями
        ArrayList<String> photos1 = new ArrayList<>();
        photos1.add("https://example.com/photo1.jpg");
        photos1.add("https://example.com/photo2.jpg");

        ArrayList<String> photos2 = new ArrayList<>();
        photos2.add("https://example.com/photo3.jpg");

        taskList.add(new Task("1", "Task 1", "Description for task 1", photos1));
        taskList.add(new Task("2", "Task 2", "Description for task 2", photos2));
        taskList.add(new Task("3", "Task 3", "Description for task 3", new ArrayList<>()));

        taskAdapter = new ExpandableTaskAdapter(taskList, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // Установка менеджера компоновки
        recyclerView.setAdapter(taskAdapter); // Привязка адаптера к RecyclerView

        return view; // Возвращаем корневое представление для фрагмента
    }
}
