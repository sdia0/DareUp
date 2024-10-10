package com.example.dareup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WelcomeActivity extends AppCompatActivity {
    private Button btnLogin, btnRegister, btnContinue;
    private EditText editLogin, editPassword;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);

        // Инициализация FirebaseAuth и Realtime Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Получаем сохраненные данные о пользователе
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);

        // Проверяем, существует ли текущий пользователь в Firebase
        if (isLoggedIn && mAuth.getCurrentUser() == null) {
            // Если пользователь отсутствует в Firebase, сбрасываем флаг isLoggedIn
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isLoggedIn", false);
            editor.apply();
            isLoggedIn = false; // Обновляем локальную переменную
        }

        // Если пользователь авторизован, сразу переходим в MainActivity
        if (isLoggedIn) {
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            finish(); // Закрываем WelcomeActivity
            return;
        }

        // Инициализация элементов интерфейса
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnContinue = findViewById(R.id.btnContinue);
        editLogin = findViewById(R.id.editLogin);
        editPassword = findViewById(R.id.editPassword);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Выполняем асинхронную проверку пользователя
                loginUser(editLogin.getText().toString().trim(), editPassword.getText().toString().trim());
            }
        });

        // Обработка нажатия на кнопку "Регистрация"
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, RegisterActivity.class));
            }
        });

        // Обработка нажатия на кнопку "Продолжить без регистрации"
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            }
        });
    }

    // Метод для входа пользователя
    private void loginUser(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, введите электронную почту и пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        // Вход через Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Получаем UID текущего пользователя
                            String uid = mAuth.getCurrentUser().getUid();

                            // Сохраняем UID в SharedPreferences
                            SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean("isLoggedIn", true);
                            editor.putString("user_uid", uid);  // Сохраняем UID
                            editor.apply();

                            // Получаем данные пользователя из Realtime Database по UID
                            getUserDataFromFirebase(uid);
                        } else {
                            // Если вход не удался, показываем ошибку
                            Toast.makeText(WelcomeActivity.this, "Ошибка входа: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Получение данных пользователя из Firebase Realtime Database по UID
    private void getUserDataFromFirebase(String uid) {
        mDatabase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Получаем данные пользователя
                    String id = snapshot.child("id").getValue(String.class);
                    String name = snapshot.child("name").getValue(String.class);
                    String photoUrl = snapshot.child("photoUrl").getValue(String.class);
                    String activeTask = snapshot.child("activeTask").getValue(String.class);
                    String activeTaskDifficulty = snapshot.child("activeTaskDifficulty").getValue(String.class);
                    int xp = snapshot.child("xp").getValue(Integer.class);
                    int level = snapshot.child("level").getValue(Integer.class);

                    // Создаем объект User для хранения данных
                    User user = new User(id, name, level, xp, photoUrl, activeTask, activeTaskDifficulty);

                    // Сохраняем данные пользователя локально
                    saveUserDataLocally(user);

                    // Переход на MainActivity
                    startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(WelcomeActivity.this, "Данные пользователя не найдены", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("WelcomeActivity", "Ошибка чтения данных: " + error.getMessage());
            }
        });
    }

    // Сохранение данных пользователя в локальный JSON-файл
    private void saveUserDataLocally(User user) {
        try {
            File file = new File(getFilesDir(), "user_data.json");
            FileWriter writer = new FileWriter(file);
            writer.write(new Gson().toJson(user)); // Преобразование объекта User в JSON и запись в файл
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
