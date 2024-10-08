package com.example.dareup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SelectTaskActivity extends AppCompatActivity {
    private TextView tvTask;
    private Button btnNo, btnYes;
    private String difficultyLevel;
    private int attemptsRemaining = 3;
    private long retryAfter = 60 * 1000;
    private CountDownTimer countDownTimer;
    private String selectedTask; // Хранит выбранное задание
    private String selectedTaskId; // Хранит ID выбранного задания

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_task);

        // Получаем уровень сложности из Intent
        difficultyLevel = getIntent().getStringExtra("difficultyLevel");

        tvTask = findViewById(R.id.tvTask);
        btnNo = findViewById(R.id.btnNo);
        btnYes = findViewById(R.id.btnYes);

        // Загрузка задания при создании активности
        loadRandomTask();

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (attemptsRemaining > 0) {
                    attemptsRemaining--;
                    loadRandomTask();
                } else {
                    // Уведомляем пользователя о завершении попыток
                    Toast.makeText(SelectTaskActivity.this, "Вы исчерпали все попытки! Попробуйте позже.", Toast.LENGTH_SHORT).show();
                    startRetryTimer();
                }
            }
        });

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Сохранение задания и ID в SharedPreferences перед переходом
                SharedPreferences prefs = getSharedPreferences("TaskPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("task", selectedTask); // Сохраняем задание
                editor.putString("taskId", selectedTaskId); // Сохраняем ID задания
                editor.putString("difficultyLevel", difficultyLevel); // Сохраняем ID задания
                editor.apply();
                Intent intent = new Intent(SelectTaskActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadRandomTask() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("tasks");

        // Получаем задания по выбранной сложности
        databaseReference.child(difficultyLevel).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<String> tasksList = new ArrayList<>();
                    List<String> taskIds = new ArrayList<>(); // Для хранения ID заданий

                    for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                        String taskDescription = taskSnapshot.child("description").getValue(String.class);
                        String taskId = taskSnapshot.getKey(); // Получаем ID задания
                        tasksList.add(taskDescription);
                        taskIds.add(taskId); // Сохраняем ID
                    }

                    // Выбор случайного задания
                    selectRandomTask(tasksList, taskIds); // Передаем оба списка
                } else {
                    Toast.makeText(SelectTaskActivity.this, "Нет доступных заданий для этого уровня сложности.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SelectTaskActivity.this, "Ошибка: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectRandomTask(List<String> tasksList, List<String> taskIds) {
        if (!tasksList.isEmpty()) {
            Random random = new Random();
            int randomIndex = random.nextInt(tasksList.size());
            selectedTask = tasksList.get(randomIndex); // Сохраняем описание задания
            selectedTaskId = taskIds.get(randomIndex); // Сохраняем ID задания

            tvTask.setText(selectedTask); // Обновляем текст задания на экране
        }
    }

    private void startRetryTimer() {
        btnNo.setEnabled(false); // Деактивируем кнопку "другое задание"
        Intent intent = new Intent(SelectTaskActivity.this, MainActivity.class);
        intent.putExtra("retryAfter", retryAfter); // Передача значения retryAfter через Intent
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel(); // Остановить таймер, если активность уничтожается
        }
    }
}
