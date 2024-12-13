package com.example.dareup;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dareup.R;
import com.example.dareup.adapters.ExpandableMemoryAdapter;
import com.example.dareup.entities.Memory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TasksFragment extends Fragment {
    private RecyclerView recyclerView;
    private ExpandableMemoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инфлейт XML разметки фрагмента
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        recyclerView = view.findViewById(R.id.recycler_view); // Инициализация RecyclerView

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
    private List<Memory> loadMemoriesFromFile() {

        // Прочитать существующий JSON-файл
        List<Memory> memoryList = new ArrayList<>();

        FileInputStream fis = null;
        BufferedReader reader = null;
        try {
            fis = requireActivity().openFileInput("memories.json");
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
