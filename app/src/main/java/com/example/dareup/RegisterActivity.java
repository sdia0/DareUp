package com.example.dareup;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private Button btnAddUser;
    private EditText editInputName, editInputLogin, editInputPassword, editRepeatPassword;
    private TextView loadPhoto;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Для хранения URI изображения
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editInputName = findViewById(R.id.editName);
        editInputLogin = findViewById(R.id.editLogin);
        editInputPassword = findViewById(R.id.editPassword);
        editRepeatPassword = findViewById(R.id.editRepeatPassword);
        btnAddUser = findViewById(R.id.btnRegister);
        loadPhoto = findViewById(R.id.tvLoadPhoto);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        loadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        btnAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editInputName.getText().toString().trim();
                String login = editInputLogin.getText().toString().trim();
                String password = editInputPassword.getText().toString().trim();
                String repPassword = editRepeatPassword.getText().toString().trim();

                if (!name.isEmpty() && !login.isEmpty() && !password.isEmpty() && !repPassword.isEmpty()) {
                    if (!password.equals(repPassword)) {
                        Toast.makeText(RegisterActivity.this, "Пароли не совпадают!", Toast.LENGTH_SHORT).show();
                    } else {
                        registerUser(login, password, name);
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Должны быть заполнены все поля!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void registerUser(String email, String password, String name) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            String uid = user.getUid();

                            User newUser = new User(uid, name, 1, 0, "", "", ""); // Уровень 1 и очки 0

                            mDatabase.child(uid).setValue(newUser)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(RegisterActivity.this, "Письмо для подтверждения отправлено!", Toast.LENGTH_SHORT).show();
                                                // Отправляем письмо подтверждения и завершаем активность после получения подтверждения
                                                sendVerificationEmail();
                                                if (imageUri != null) {
                                                    uploadImageToFirebase(imageUri, uid); // Передаем uid для загрузки изображения
                                                }
                                            } else {
                                                Log.e("RegisterActivity", "Failed to add user to database", task.getException());
                                            }
                                        }
                                    });
                        } else {
                            Log.e("RegisterActivity", "Registration failed", task.getException());
                        }
                    }
                });
    }

    private void sendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(RegisterActivity.this, "Письмо для подтверждения отправлено!", Toast.LENGTH_SHORT).show();
                            finish();
                            finish(); // Завершить активность после показа тоста
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("RegisterActivity", "Email verification failed", e);
                        }
                    });
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData(); // Сохраняем URI выбранного изображения

            // Обновляем TextView
            loadPhoto.setText("Изображение выбрано!"); // Устанавливаем текст
            loadPhoto.setBackgroundColor(Color.parseColor("#8B0000")); // Меняем цвет view
            loadPhoto.setBackgroundColor(Color.parseColor("#FFE4C4")); // Меняем цвет view
            loadPhoto.setTextColor(Color.parseColor("#4169E1"));
        }
    }

    private void uploadImageToFirebase(Uri imageUri, String uid) { // Добавляем uid как параметр
        if (imageUri != null) {
            // Reference to Firebase Storage
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile_images/" + uid + ".jpg");

            // Upload the file
            storageRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get the download URL and store it in the Realtime Database
                            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri downloadUrl) {
                                    String imageUrl = downloadUrl.toString();
                                    saveImageUrlToDatabase(uid, imageUrl); // Передаем uid для сохранения
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RegisterActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void saveImageUrlToDatabase(String uid, String imageUrl) { // Добавляем uid как параметр
        mDatabase.child(uid).child("photoUrl").setValue(imageUrl)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "URL изображения успешно сохранён!", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(RegisterActivity.this, "URL изображения успешно сохранён!", Toast.LENGTH_SHORT).show();
                            finish(); // Закрываем активность после завершения
                        } else {
                            Log.e("RegisterActivity", "Failed to save image URL", task.getException());
                        }
                    }
                });
    }
}
