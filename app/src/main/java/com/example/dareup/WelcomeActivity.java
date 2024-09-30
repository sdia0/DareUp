package com.example.dareup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class WelcomeActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    Button btnLogin, btnRegister, btnContinue;
    EditText editLogin, editPassword;
    ArrayList<User> userList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnContinue = findViewById(R.id.btnContinue);
        editLogin = findViewById(R.id.editLogin);
        editPassword = findViewById(R.id.editPassword);
        userList = new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Выполняем асинхронную проверку пользователя
                checkUserExist(editLogin.getText().toString(), editPassword.getText().toString());
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, RegisterActivity.class));
            }
        });
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            }
        });
    }
    // Метод для загрузки списка расходов из Firebase
    private void checkUserExist(String login, String password) {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() { // Используем addListenerForSingleValueEvent вместо addValueEventListener
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean userFound = false;
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null && Objects.equals(user.login, login) && Objects.equals(user.password, password)) {
                        userFound = true;
                        break; // Если пользователь найден, выходим из цикла
                    }
                }

                if (userFound) {
                    // Переходим на MainActivity
                    startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                } else {
                    // Показываем Toast, если пользователь не найден
                    Toast.makeText(WelcomeActivity.this, "Такого пользователя не существует", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Обрабатываем ошибку
                Toast.makeText(WelcomeActivity.this, "Ошибка подключения к базе данных", Toast.LENGTH_SHORT).show();
            }
        });
    }
}