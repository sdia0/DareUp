package com.example.dareup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.BufferedReader;

public class SelectTaskActivity extends AppCompatActivity {
    private TextView tvTask;
    private Button btnNo, btnYes;
    private String difficultyLevel;
    private int attemptsRemaining = 3;
    private long retryAfter = 60 * 1000;
    private CountDownTimer countDownTimer;
    private String selectedTask; // Хранит выбранное задание

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
                // Сброс активного задания в базе данных
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                if (currentUser != null) {
                    String userId = currentUser.getUid();
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
                    databaseReference.child("activeTask").setValue(selectedTask); // Устанавливаем пустую строку
                    databaseReference.child("activeTaskDifficulty").setValue(difficultyLevel); // Устанавливаем пустую строку
                }

                saveActiveTaskToFile(selectedTask, difficultyLevel);

                Intent intent = new Intent(SelectTaskActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
    private boolean canGoBack = false; // Флаг для контроля перехода назад

    @Override
    public void onBackPressed() {
        if (canGoBack) {
            super.onBackPressed(); // Если можно вернуться назад, вызываем super
        } else {
            // Здесь ничего не делаем или показываем сообщение
            Toast.makeText(this, "Вы не можете вернуться на предыдущий экран", Toast.LENGTH_SHORT).show();
        }
    }
    private void loadRandomTask() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("tasks");

        // Получаем задания по выбранной сложности
        databaseReference.child(difficultyLevel).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<String> tasksList = new ArrayList<>();

                    for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                        String taskDescription = taskSnapshot.child("description").getValue(String.class);
                        tasksList.add(taskDescription);
                    }

                    // Выбор случайного задания
                    if (!tasksList.isEmpty()) {
                        Random random = new Random();
                        int randomIndex = random.nextInt(tasksList.size());
                        selectedTask = tasksList.get(randomIndex); // Сохраняем описание задания
                        tvTask.setText(selectedTask); // Обновляем текст задания на экране
                    }
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

    private void startRetryTimer() {
        btnNo.setEnabled(false); // Деактивируем кнопку "другое задание"
        Intent intent = new Intent(SelectTaskActivity.this, MainActivity.class);
        intent.putExtra("retryAfter", retryAfter); // Передача значения retryAfter через Intent
        startActivity(intent);
    }

    private void saveActiveTaskToFile(String activeTask, String activeTaskDifficulty) {
        File file = new File(SelectTaskActivity.this.getFilesDir(), "user_data.json");
        JSONObject userJson = new JSONObject();

        try {
            // Проверяем, существует ли файл, и если да, читаем его
            if (file.exists()) {
                // Чтение данных из файла
                StringBuilder jsonBuilder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;

                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
                reader.close();
                // Создаем объект JSON из строки
                userJson = new JSONObject(jsonBuilder.toString());
            }

            // Обновляем или добавляем активное задание
            userJson.put("activeTask", activeTask);
            userJson.put("activeTaskDifficulty", activeTaskDifficulty);

            // Записываем обновленный объект JSON обратно в файл
            FileWriter writer = new FileWriter(file);
            writer.write(userJson.toString());
            writer.close();

            Log.d("FileWrite", "Данные пользователя сохранены в файл: " + userJson.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel(); // Остановить таймер, если активность уничтожается
        }
    }
}
