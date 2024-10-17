package com.example.dareup;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class EditTaskActivity extends AppCompatActivity {
    Button btnSave, btnCancel;
    EditText etTitle, etNotes;
    TextView tvCompletedTask;

    // Firebase instances
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_task);
        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        etTitle = findViewById(R.id.etTitle);
        etNotes = findViewById(R.id.etNotes);
        tvCompletedTask = findViewById(R.id.tvTaskTitle);

        // Загружаем activeTask при запуске активности
        fetchActiveTaskAndDisplay();

        // Сохраняем данные при нажатии на кнопку
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchActiveTaskAndSaveMemory();
                Intent intent = new Intent(EditTaskActivity.this, MainActivity.class);
                intent.putExtra("dataChanged", true); // Передаем информацию о том, что данные изменились
                setResult(RESULT_OK, intent);
                startActivity(intent);
                finish();
                }
            }
        );

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchActiveTaskAndSaveMemory();
                Intent intent = new Intent(EditTaskActivity.this, MainActivity.class);
                intent.putExtra("dataChanged", true); // Передаем информацию о том, что данные изменились
                setResult(RESULT_OK, intent);
                startActivity(intent);
                finish();
            }
        });
    }

    // Метод для подгрузки activeTask в TextView сразу при запуске активности
    private void fetchActiveTaskAndDisplay() {
        // Получаем текущего пользователя
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Получаем ID пользователя
            String userId = currentUser.getUid();

            // Ссылка на таблицу users для получения activeTask
            DatabaseReference userRef = mDatabase.child("users").child(userId);

            // Читаем значение activeTask из Firebase
            userRef.child("activeTask").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Проверяем, существует ли значение activeTask
                    if (dataSnapshot.exists()) {
                        String activeTask = dataSnapshot.getValue(String.class);

                        // Устанавливаем activeTask в TextView сразу
                        tvCompletedTask.setText(activeTask);
                    } else {
                        Log.e("Firebase", "activeTask not found.");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("Firebase", "Error fetching activeTask: " + databaseError.getMessage());
                }
            });
        } else {
            Log.e("Firebase", "User not logged in.");
        }
    }

    // Метод для сохранения памяти и сброса activeTask
    private void fetchActiveTaskAndSaveMemory() {
        String title = etTitle.getText().toString().trim();
        String description = etNotes.getText().toString().trim();
        // Получаем текущего пользователя
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Получаем ID пользователя
            String userId = currentUser.getUid();

            // Ссылка на таблицу users для получения activeTask
            DatabaseReference userRef = mDatabase.child("users").child(userId);

            // Читаем значение activeTask из Firebase
            userRef.child("activeTask").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Проверяем, существует ли значение activeTask
                    if (dataSnapshot.exists()) {
                        String activeTask = dataSnapshot.getValue(String.class);

                        // Сохраняем данные в memories
                        saveMemoryToFirebase(title, activeTask, description);
                        saveMemoryToFile(title, activeTask, description);
                    } else {
                        Log.e("Firebase", "activeTask not found.");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("Firebase", "Error fetching activeTask: " + databaseError.getMessage());
                }
            });
        }
    }

    private void saveMemoryToFirebase(String title, String task, String description) {
        FirebaseUser user = mAuth.getCurrentUser();
        String uid = user.getUid();
        String memoryId = mDatabase.child("memories").child(uid).push().getKey();

        Memory memory = new Memory(memoryId, task, title, description); // Уровень 1 и очки 0

        mDatabase.child("memories").child(uid).push().setValue(memory)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.e("Adding task", "Succeed to add memory to database", task.getException());
                        } else {
                            Log.e("Adding task", "Failed to add memory to database", task.getException());
                        }
                    }
                });
    }
    // Метод для добавления объекта Memory в JSON-файл
    private void saveMemoryToFile(String title, String task, String description) {

        // Создаем объект Memory
        Memory newMemory = new Memory("", title, task, description);

        // Прочитать существующий JSON-файл
        List<Memory> memoryList = new ArrayList<>();

        String fileName = "memories.json";

        FileInputStream fis = null;
        BufferedReader reader = null;
        try {
            fis = openFileInput(fileName);
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
                Type memoryListType = new TypeToken<ArrayList<Memory>>() {}.getType();
                memoryList = new Gson().fromJson(json, memoryListType);
            }
        } catch (IOException e) {
            Log.e("EditTaskActivity", "Error reading file: " + e.getMessage(), e);
        } finally {
            try {
                if (reader != null) reader.close();
                if (fis != null) fis.close();
            } catch (IOException e) {
                Log.e("EditTaskActivity", "Error closing file: " + e.getMessage(), e);
            }
        }

        // Добавляем новый объект Memory в список
        memoryList.add(newMemory);

        // Преобразуем обновленный список обратно в JSON
        Gson gson = new Gson();
        String updatedJson = gson.toJson(memoryList);

        // Записываем обновленный JSON обратно в файл
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        try {
            fos = openFileOutput(fileName, Context.MODE_PRIVATE);
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
}
