package com.example.dareup;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TasksFragment extends Fragment {
    private RecyclerView recyclerView;
    private ExpandableMemoryAdapter adapter;
    String fileName;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private SwipeRefreshLayout swipeRefreshLayout;
    public static TasksFragment newInstance(boolean showLeaderboard) {
        TasksFragment fragment = new TasksFragment();
        Bundle args = new Bundle();
        args.putBoolean("showLeaderboard", showLeaderboard); // Сохранение значения в аргументах
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateData(); // Метод для обновления данных
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инфлейт XML разметки фрагмента
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        recyclerView = view.findViewById(R.id.recycler_view); // Инициализация RecyclerView

        // Инициализация FirebaseAuth и Realtime Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (getArguments() != null) {
            boolean showLeaderboard = getArguments().getBoolean("showLeaderboard", true);
            if (showLeaderboard) fileName = "memories.json";
            else fileName = "guest_memories.json";
        }

        // Инициализация SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        // Обработка события свайпа вниз
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Обновляем данные
            syncWithFirebase();

            // Убираем индикатор обновления после завершения
            swipeRefreshLayout.setRefreshing(false);
        });

        /*FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            // Получаем ID текущего пользователя
            String userId = currentUser.getUid();

            // Ссылка на конкретного пользователя в базе данных
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("memories").child(userId);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<Memory> memoryList = new ArrayList<>();  // Создаем список для Memory

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Memory memory = snapshot.getValue(Memory.class);  // Получаем объект Memory из Firebase
                        if (memory != null) {
                            memoryList.add(memory);  // Добавляем объект в список
                        }
                    }

                    // Создаем и устанавливаем адаптер
                    ExpandableMemoryAdapter adapter = new ExpandableMemoryAdapter(memoryList, getContext());
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(adapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Обработка ошибок
                }
            });
        }*/

        adapter = new ExpandableMemoryAdapter(loadMemoriesFromFile(), getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        return view; // Возвращаем корневое представление для фрагмента
    }
    public void syncWithFirebase() {
        String uid = mAuth.getCurrentUser().getUid();
        mDatabase.child("memories").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<Memory> memoryList = new ArrayList<>();  // Создаем список для хранения всех memories

                    // Проходим по каждому дочернему узлу (т.е. по каждому объекту Memory)
                    for (DataSnapshot memorySnapshot : snapshot.getChildren()) {
                        String title = memorySnapshot.child("title").getValue(String.class);
                        String task = memorySnapshot.child("task").getValue(String.class);
                        String description = memorySnapshot.child("description").getValue(String.class);

                        if (title.isEmpty()) {
                            title = "пусто";

                        }
                        if (task.isEmpty()) {
                            task = "пусто";

                        }
                        if (description.isEmpty()) {
                            description = "пусто";

                        }
                        Memory memory = new Memory("", title, task, description);

                        // Добавляем объект Memory в список
                        memoryList.add(memory);
                    }

                    // Сохраняем список memories локально в JSON-файл
                    saveMemoriesLocally(memoryList, "memories.json");
                } else {
                    //Toast.makeText(WelcomeActivity.this, "Данные не найдены", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("WelcomeActivity", "Ошибка чтения данных: " + error.getMessage());
            }
        });
        loadMemoriesFromFile();
    }
    private void saveMemoriesLocally(List<Memory> memoryList, String fileName) {
        Gson gson = new Gson();
        String updatedJson = gson.toJson(memoryList);

        // Записываем обновленный JSON обратно в файл
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        try {
            fos = getActivity().openFileOutput(fileName, Context.MODE_PRIVATE);
            osw = new OutputStreamWriter(fos);
            osw.write(updatedJson);
            Log.d("EditTaskActivity", "Memory list updated and saved to file: " + fileName);
        } catch (IOException e) {
            Log.e("EditTaskActivity", "Error writing to file: " + e.getMessage(), e);
        } finally {
            try {
                if (osw != null) osw.close();
                if (fos != null) fos.close();
            } catch (IOException e) {
                Log.e("EditTaskActivity", "Error closing file: " + e.getMessage(), e);
            }
        }
    }
    private List<Memory> loadMemoriesFromFile() {
        // Прочитать существующий JSON-файл
        List<Memory> memoryList = new ArrayList<>();

        FileInputStream fis = null;
        BufferedReader reader = null;
        try {
            fis = requireActivity().openFileInput(fileName);
            reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder builder = new StringBuilder();
            String line;

            // Чтение строки за строкой и добавление в StringBuilder
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            // Преобразование прочитанного текста в список Memory
            String json = builder.toString();
            if (!json.isEmpty()) {
                Type memoryListType = new TypeToken<ArrayList<Memory>>() {
                }.getType();
                memoryList = new Gson().fromJson(json, memoryListType);
            }
        } catch (IOException e) {
            Log.e("TaskFragment", "Error reading file: " + e.getMessage(), e);
        } finally {
            try {
                if (reader != null) reader.close();
                if (fis != null) fis.close();
            } catch (IOException e) {
                Log.e("TaskFragment", "Error closing file: " + e.getMessage(), e);
            }
        }
        return memoryList;
    }
    public void updateData() {
        // Чтение обновленных данных из файла
        List<Memory> updatedData = loadMemoriesFromFile();
        adapter.updateData(updatedData);
        adapter.notifyDataSetChanged(); // Обновляем интерфейс
    }
}
