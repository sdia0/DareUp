package com.example.dareup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class WelcomeActivity extends AppCompatActivity {
    private Button btnLogin, btnRegister, btnContinue;
    private EditText editLogin, editPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);

        // Инициализация FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

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

        // Обработка нажатия на кнопку "Вход"
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editLogin.getText().toString().trim();
                String password = editPassword.getText().toString().trim();
                loginUser(email, password);
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
                .addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Успешный вход, сохраняем состояние пользователя
                            SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean("isLoggedIn", true);
                            editor.apply();

                            // Переход в MainActivity
                            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                            finish(); // Закрываем WelcomeActivity
                        } else {
                            // Если вход не удался, показываем ошибку
                            Toast.makeText(WelcomeActivity.this, "Ошибка входа: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
