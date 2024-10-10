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

        taskAdapter = new ExpandableTaskAdapter(taskList, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // Установка менеджера компоновки
        recyclerView.setAdapter(taskAdapter); // Привязка адаптера к RecyclerView

        return view; // Возвращаем корневое представление для фрагмента
    }
}
