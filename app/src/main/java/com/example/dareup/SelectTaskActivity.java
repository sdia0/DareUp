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
    private int currentTries;
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

        decrementTries();

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTries > 0) {
                    decrementTries();
                    loadRandomTask();
                } else {
                    // Уведомляем пользователя о завершении попыток
                    Toast.makeText(SelectTaskActivity.this, "Вы исчерпали все попытки! Попробуйте позже.", Toast.LENGTH_SHORT).show();
                    currentTries = 3;
                    saveActiveTaskToFile("", "");
                    resetTries();
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
    // Метод для уменьшения значения tries на 1
    public void decrementTries() {
        File file = new File(getFilesDir(), "user_data.json");
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

            currentTries = userJson.getInt("tries");
            if (currentTries > 0) {
                currentTries--;
            }
            Toast.makeText(this, "У вас осталось попыток: " + currentTries, Toast.LENGTH_SHORT).show();

            userJson.put("tries", currentTries);

            // Записываем обновленный объект JSON обратно в файл
            FileWriter writer = new FileWriter(file);
            writer.write(userJson.toString());
            writer.close();

            Log.d("FileWrite", "Данные пользователя сохранены в файл: " + userJson.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
    public void resetTries() {
        File file = new File(getFilesDir(), "user_data.json");
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

            userJson.put("tries", 3);

            // Записываем обновленный объект JSON обратно в файл
            FileWriter writer = new FileWriter(file);
            writer.write(userJson.toString());
            writer.close();

            Log.d("FileWrite", "Данные пользователя сохранены в файл: " + userJson.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
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
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        // Получаем текущего пользователя
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        // Проверяем, существует ли текущий пользователь
        if (currentUser != null) {
            String userId = currentUser.getUid(); // Получаем ID текущего пользователя
            int xpToMinus = getXpForDifficulty(difficultyLevel); // Implement this method based on your logic
            minusXp(userId, xpToMinus);
            Toast.makeText(this, "Минус " + xpToMinus + "xp", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("UID", "Пользователь не авторизован");
        }
        Intent intent = new Intent(SelectTaskActivity.this, MainActivity.class);
        intent.putExtra("retryAfter", retryAfter); // Передача значения retryAfter через Intent
        startActivity(intent);
    }
    private int getXpForDifficulty(String difficultyLevel) {
        switch (difficultyLevel) {
            case "easy":
                return 5; // XP for easy
            case "medium":
                return 10; // XP for medium
            case "hard":
                return 20; // XP for hard
            default:
                return 0; // No XP if the difficultyLevel is unknown
        }
    }
    private void minusXp(String userId, int xpToMinus) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("users").child(userId);

        database.child("xp").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Integer currentXp = task.getResult().getValue(Integer.class);

                if (currentXp != null) {
                    int newXp = currentXp - xpToMinus;
                    if (newXp < 0) newXp = 0;
                    database.child("xp").setValue(newXp).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Log.d("FirebaseUpdate", "XP обновлено успешно!");
                        } else {
                            Log.d("FirebaseUpdate", "Ошибка обновления XP: " + updateTask.getException());
                        }
                    });
                    saveXpToFile(newXp);
                } else {
                    Toast.makeText(this, "Текущие XP не найдены.", Toast.LENGTH_SHORT).show();
                    Log.d("FirebaseGetXP", "Текущие XP не найдены");
                }
            } else {
                Toast.makeText(this, "Не удалось получить текущие XP.", Toast.LENGTH_SHORT).show();
                Log.d("FirebaseGetXP", "Ошибка получения XP: " + task.getException());
            }
        });
    }

    private void saveXpToFile(int xp) {
        File file = new File(getFilesDir(), "user_data.json");
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
            userJson.put("xp", xp);

            // Записываем обновленный объект JSON обратно в файл
            FileWriter writer = new FileWriter(file);
            writer.write(userJson.toString());
            writer.close();

            Log.d("FileWrite", "Данные пользователя сохранены в файл: " + userJson.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
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
