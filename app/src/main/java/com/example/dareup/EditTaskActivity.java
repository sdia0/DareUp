package com.example.dareup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditTaskActivity extends AppCompatActivity {
    Button btnSave, btnCancel;
    EditText etTitle, etNotes;
    TextView tvCompletedTask;

    // Firebase instances
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

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
                intent.putExtra("selectedTask", "Выбрать задание");
                startActivity(intent);
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
                        saveMemoryToFirebase(activeTask, userId);
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

    private void saveMemoryToFirebase(String task, String userId) {
        // Получаем данные из EditText
        String title = etTitle.getText().toString().trim();
        String description = etNotes.getText().toString().trim();

        // Проверяем, что поля не пустые
        if (title.isEmpty() || task == null || task.isEmpty() || description.isEmpty()) {
            Log.e("Firebase", "One or more fields are empty. Data not saved.");
            return; // Останавливаем выполнение, если какое-то поле пустое
        }

        // Создаем объект Memory
        Memory memory = new Memory(title, task, description);

        // Сохраняем данные в Firebase Database в таблицу "memories"
        DatabaseReference memoryRef = mDatabase.child("memories").child(userId).push();
        memoryRef.setValue(memory)
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Log.d("Firebase", "Memory saved successfully.");

                        // После успешного сохранения удаляем activeTask (только для этого пользователя)
                        mDatabase.child("users").child(userId).child("activeTask").setValue("")
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        Log.d("Firebase", "activeTask reset to empty.");
                                    } else {
                                        Log.e("Firebase", "Error resetting activeTask: " + updateTask.getException());
                                    }
                                });

                    } else {
                        Log.e("Firebase", "Error saving memory.", task1.getException());
                    }
                });
    }
}

