package com.example.dareup.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dareup.R;
import com.example.dareup.entities.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class AiCheckActivity extends AppCompatActivity {
    private static final int RESULT_LOAD_IMAGE = 123;
    public static final int IMAGE_CAPTURE_CODE = 654;
    private static final int PERMISSION_CODE = 321;
    TextView tvCheck;
    Button btnCompleteCheck;
    private Uri image_uri;
    ImageLabeler labeler;
    String difficultyLevel, activeTask, check_prompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_check);
        tvCheck = findViewById(R.id.tvLoadCheck);
        btnCompleteCheck = findViewById(R.id.btnContinue);

        tvCheck.setText("Сделать фото");

        User user = loadUserDataFromFile();
        if (user != null) {
            activeTask = user.getActiveTask();
            difficultyLevel = user.getActiveTaskDifficulty();
        } else {
            Toast.makeText(this, "Не удалось загрузить данные о пользователе", Toast.LENGTH_SHORT).show();
            return; // Выйти из метода, если данные о пользователе не загружены
        }

        // Получаем ссылку на задачи
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("tasks").child("hard");

        // Запрос на поиск задачи по полю "description"
        Query query = databaseReference.orderByChild("description").equalTo(activeTask);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Проверяем, существует ли такой узел
                if (dataSnapshot.exists()) {
                    for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                        check_prompt = taskSnapshot.child("prompt").getValue(String.class);
                    }
                } else {
                    Log.d("Firebase", "Task not found");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error occurred: " + databaseError.getMessage());
            }
        });

        tvCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED) {
                        String[] permission = {android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permission, PERMISSION_CODE);
                    } else {
                        openCamera();
                    }
                } else {
                    openCamera();
                }
            }
        });

        btnCompleteCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AiCheckActivity.this, WinnerActivity.class);
                startActivity(intent);
            }
        });

        labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешения даны, открываем камеру
                openCamera();
            } else {
                // Разрешения не даны
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            image_uri = data.getData();
            //innerImage.setImageURI(image_uri);
            Bitmap bitmap = uriToBitmap(image_uri);
            doInference(bitmap);
        }

        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == RESULT_OK) {
            //innerImage.setImageURI(image_uri);
            Bitmap bitmap = uriToBitmap(image_uri);
            doInference(bitmap);
        }

    }

    public void doInference(Bitmap input) {
        Bitmap rotated = rotateBitmap(input);
        InputImage image = InputImage.fromBitmap(rotated, 0);

        labeler.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                    @Override
                    public void onSuccess(List<ImageLabel> labels) {
                        boolean flag = true;
                        for (ImageLabel label : labels) {
                            String text = label.getText();
                            float confidence = label.getConfidence();
                            //int index = label.getIndex();
                            if (text.equals(check_prompt) && confidence > 0.5) {
                                // Toast.makeText(AiCheckActivity.this, text, Toast.LENGTH_SHORT).show();
                                success();
                                break;
                            } else {
                                // Toast.makeText(AiCheckActivity.this, check_prompt + " " + text, Toast.LENGTH_SHORT).show();
                                flag = false;
                            }
                            //resultTv.append(text + "      " + confidence + "\n");
                        }
                        if (!flag) {
                            Toast.makeText(AiCheckActivity.this, "Проверка не пройдена...", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...
                    }
                });
    }

    //TODO rotate image if image captured on sumsong devices
    //Most phone cameras are landscape, meaning if you take the photo in portrait, the resulting photos will be rotated 90 degrees.
    @SuppressLint("Range")
    public Bitmap rotateBitmap(Bitmap input) {
        String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
        Cursor cur = getContentResolver().query(image_uri, orientationColumn, null, null, null);
        int orientation = -1;
        if (cur != null && cur.moveToFirst()) {
            orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]));
        }
        Log.d("tryOrientation", orientation + "");
        Matrix rotationMatrix = new Matrix();
        rotationMatrix.setRotate(orientation);
        Bitmap cropped = Bitmap.createBitmap(input, 0, 0, input.getWidth(), input.getHeight(), rotationMatrix, true);
        return cropped;
    }

    //TODO takes URI of the image and returns bitmap
    private Bitmap uriToBitmap(Uri selectedFileUri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(selectedFileUri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);

            parcelFileDescriptor.close();
            return image;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    void success() {
        btnCompleteCheck.setEnabled(true);
        btnCompleteCheck.setAlpha(1.0f);
        // Обновляем TextView
        tvCheck.setText("Успех!"); // Устанавливаем текст
        tvCheck.setBackgroundColor(Color.parseColor("#8B0000")); // Меняем цвет view
        tvCheck.setBackgroundColor(Color.parseColor("#FFE4C4")); // Меняем цвет view
        tvCheck.setTextColor(Color.parseColor("#4169E1"));
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        // Получаем текущего пользователя
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        // Проверяем, существует ли текущий пользователь
        if (currentUser != null) {
            String userId = currentUser.getUid(); // Получаем ID текущего пользователя
            int xpToAdd = getXpForDifficulty(difficultyLevel); // Implement this method based on your logic
            addXpToUserAndResetTask(userId, xpToAdd);
        }
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

    private void addXpToUserAndResetTask(String userId, int xpToAdd) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("users").child(userId);

        database.child("xp").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Integer currentXp = task.getResult().getValue(Integer.class);

                if (currentXp != null) {
                    int newXp = currentXp + xpToAdd;
                    database.child("xp").setValue(newXp).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Log.d("FirebaseUpdate", "XP обновлено успешно!");
                        } else {
                            Log.d("FirebaseUpdate", "Ошибка обновления XP: " + updateTask.getException());
                        }
                    });
                    updateFile(newXp);
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

    private void updateFile(int xp) {
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
            userJson.put("activeTask", "");

            // Записываем обновленный объект JSON обратно в файл
            FileWriter writer = new FileWriter(file);
            writer.write(userJson.toString());
            writer.close();

            Log.d("FileWrite", "Данные пользователя сохранены в файл: " + userJson.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private User loadUserDataFromFile() {
        User user = null;
        try {
            // Определяем файл, из которого будем читать
            File file = new File(getFilesDir(), "user_data.json");
            FileReader reader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            // Читаем файл построчно
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            // Преобразуем JSON-строку в объект User
            String jsonString = stringBuilder.toString();
            Gson gson = new Gson(); // Используем библиотеку Gson
            user = gson.fromJson(jsonString, User.class);

            // Закрываем ресурсы
            bufferedReader.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return user;
    }
}