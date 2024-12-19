package com.example.dareup;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.dareup.R;
import com.example.dareup.adapters.BoardAdapter;
import com.example.dareup.entities.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardFragment extends Fragment {

    private List<User> users;
    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private BoardAdapter adapter;
    private boolean isFabOpen = false;

    public LeaderboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        // Инициализация RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView); // Убедитесь, что в вашем XML есть RecyclerView с таким ID
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // Устанавливаем LayoutManager

        // Инициализация Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Инициализация списка пользователей
        users = new ArrayList<>();

        // Получение данных из базы данных
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear(); // Очищаем список перед добавлением новых данных

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class); // Преобразование данных в объект User
                    if (user != null) {
                        users.add(user); // Добавление пользователя в список
                    }
                }

                users.sort((user1, user2) -> {
                    // Сначала сравниваем уровни
                    int levelComparison = Integer.compare(user2.getLevel(), user1.getLevel());

                    // Если уровни одинаковые, сравниваем XP
                    if (levelComparison == 0) {
                        return Integer.compare(user2.getXp(), user1.getXp());
                    }

                    // Если уровни не одинаковые, возвращаем результат сравнения уровней
                    return levelComparison;
                });


                // Обновляем адаптер с новыми данными
                if (adapter == null) {
                    adapter = new BoardAdapter(view.getContext(), users); // Создаем адаптер
                    recyclerView.setAdapter(adapter); // Устанавливаем адаптер для RecyclerView
                } else {
                    adapter.notifyDataSetChanged(); // Уведомляем адаптер об изменениях
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Ошибка получения данных: " + error.getMessage());
            }
        });

        return view;
    }
}
