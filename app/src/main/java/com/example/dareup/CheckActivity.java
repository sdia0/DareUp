package com.example.dareup;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.Manifest;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;


public class CheckActivity extends AppCompatActivity {
    String activeTask, difficultyLevel;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    TextView tvCheck;
    Button btnCompleteCheck;
    String check_prompt, check;
    private static final int CAMERA_REQUEST_CODE = 101;
    // Состояние, определяющее, какую операцию мы хотим выполнить

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Если разрешение уже предоставлено, получаем местоположение
            getLastLocation();
        }
    }

    // Обработка результата запроса разрешения
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Разрешение на доступ к местоположению отклонено", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Проверяем, что должно быть сделано: сканирование QR-кода или фотография
                startQrCodeScanner(); // Начинаем сканирование
            } else {
                Toast.makeText(this, "Разрешение на использование камеры отклонено", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_check);
        tvCheck = findViewById(R.id.tvLoadCheck);
        btnCompleteCheck = findViewById(R.id.btnContinue);

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
                        // Получаем значение поля "check" для найденной задачи
                        check = taskSnapshot.child("check").getValue(String.class);
                        check_prompt = taskSnapshot.child("check_prompt").getValue(String.class);
                        if (check != null) {
                            tvCheck.setText(check);
                        }
                        else {
                            Log.d("Firebase", "check value is not 'heo'");
                        }
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
                if (check.equals("Проверить местоположение")) {
                    requestLocationPermission();
                } else if (check.equals("Сканировать qr-код")) {
                    requestCameraPermission();
                } else {
                    Toast.makeText(CheckActivity.this, "Бла-бла", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCompleteCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CheckActivity.this, WinnerActivity.class);
                startActivity(intent);
            }
        });

    }
    private static final int CAMERA_PERMISSION_CODE = 100;

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            startQrCodeScanner();
        }
    }
    private void startQrCodeScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE); // Устанавливаем формат QR-кода
        integrator.setPrompt("Сканируйте QR-код"); // Сообщение, которое появится на экране
        integrator.setCameraId(0); // Установка ID камеры (0 - задняя, 1 - передняя)
        integrator.setBeepEnabled(false); // Отключение звука при сканировании
        integrator.setOrientationLocked(false);
        integrator.setBarcodeImageEnabled(true); // Включение изображения штрих-кода
        integrator.initiateScan(); // Запуск сканирования
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Сканирование отменено", Toast.LENGTH_SHORT).show();
            } else {
                String scannedText = result.getContents();
                confirmScannedCode(scannedText);
            }
        }
    }
    private void confirmScannedCode(String scannedText) {
        if (check_prompt != null) {
            if (scannedText.equals(check_prompt)) {
                success();
            } else {
                Toast.makeText(this, "Код не совпадает!", Toast.LENGTH_SHORT).show();
            }
        }
        else Toast.makeText(this, "Check_prompt is null", Toast.LENGTH_SHORT).show();
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

        DatabaseReference finalDatabase = database;
        database.child("xp").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Integer currentXp = task.getResult().getValue(Integer.class);

                if (currentXp != null) {
                    int newXp = currentXp + xpToAdd;

                    // Если XP превысили 100, сначала обновляем уровень
                    if (newXp >= 100) {
                        int levelsGained = newXp / 100;  // Сколько уровней прибавить
                        newXp = newXp % 100;             // Остаток XP после повышения уровня

                        // Сначала обновляем уровень
                        updateUserLevel(finalDatabase, levelsGained, newXp);
                    } else {
                        // Если XP меньше 100, просто обновляем XP
                        updateXp(finalDatabase, newXp);
                    }
                } else {
                    Toast.makeText(this, "Текущие XP не найдены.", Toast.LENGTH_SHORT).show();
                    Log.d("FirebaseGetXP", "Текущие XP не найдены");
                }
            } else {
                Toast.makeText(this, "Не удалось получить текущие XP.", Toast.LENGTH_SHORT).show();
                Log.d("FirebaseGetXP", "Ошибка получения XP: " + task.getException());
            }
        });
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            // Получаем ID текущего пользователя
            String uid = currentUser.getUid();

            database = FirebaseDatabase.getInstance().getReference();

            // Создаем объект для сохранения
            HashMap<String, Object> taskData = new HashMap<>();
            taskData.put("activeTask", activeTask);

            // Сохраняем активное задание по пути users/uid/completedTasks
            database.child("users").child(uid).child("completedTasks").push().setValue(taskData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("FirebaseWrite", "Данные успешно сохранены в Firebase.");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirebaseWrite", "Ошибка сохранения данных: ", e);
                    });
        }
        saveActiveTaskToFile("");
    }
    private void saveActiveTaskToFile(String activeTask) {
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
            userJson.put("activeTask", activeTask);

            //Toast.makeText(getActivity(), "Данные получены" + userJson.toString(), Toast.LENGTH_SHORT).show();

            // Записываем обновленный объект JSON обратно в файл
            FileWriter writer = new FileWriter(file);
            writer.write(userJson.toString());
            writer.close();

            Log.d("FileWrite", "Данные пользователя сохранены в файл: " + userJson.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
    // Метод для обновления уровня и последующего обновления XP
    private void updateUserLevel(DatabaseReference database, int levelsGained, int newXp) {
        database.child("level").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Integer currentLevel = task.getResult().getValue(Integer.class);

                if (currentLevel != null) {
                    int newLevel = currentLevel + levelsGained;

                    // Обновляем уровень пользователя
                    database.child("level").setValue(newLevel).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Log.d("FirebaseUpdate", "Уровень обновлен до: " + newLevel);

                            // После успешного обновления уровня вызываем сохранение уровня локально
                            saveLevelToFile(newLevel);

                            // После успешного обновления уровня обновляем XP
                            updateXp(database, newXp);
                        } else {
                            Log.d("FirebaseUpdate", "Ошибка обновления уровня: " + updateTask.getException());
                        }
                    });
                } else {
                    Log.d("FirebaseGetLevel", "Текущий уровень не найден");
                }
            } else {
                Log.d("FirebaseGetLevel", "Ошибка получения уровня: " + task.getException());
            }
        });
    }

    // Метод для обновления XP
    private void updateXp(DatabaseReference database, int newXp) {
        database.child("xp").setValue(newXp).addOnCompleteListener(updateTask -> {
            if (updateTask.isSuccessful()) {
                Log.d("FirebaseUpdate", "XP обновлено успешно!");
            } else {
                Log.d("FirebaseUpdate", "Ошибка обновления XP: " + updateTask.getException());
            }
        });

        saveXpToFile(newXp);
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

    private void saveLevelToFile(int level) {
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
            userJson.put("level", level);

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
    private void getLastLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Проверяем, есть ли доступ к местоположению
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }


        if (!isLocationEnabled()) {
            Toast.makeText(this, "Включите геолокацию", Toast.LENGTH_SHORT).show();
        }
        else {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            // Здесь можно проверить местоположение
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            checkLocation(latitude, longitude);
                        } else
                            Toast.makeText(this, "Неправильное местоположение", Toast.LENGTH_SHORT).show();
                    });
        }
    }
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    private void checkLocation(double userLatitude, double userLongitude) {
        if (check_prompt != null) {
            String[] array = check_prompt.split(" ");
            double targetLatitude = Double.parseDouble(array[0]);
            double targetLongitude = Double.parseDouble(array[1]);

            // Проверка на близость к целевым координатам (можно изменить логику проверки)
            float[] results = new float[1];
            Location.distanceBetween(userLatitude, userLongitude, targetLatitude, targetLongitude, results);
            float distanceInMeters = results[0];

            if (distanceInMeters < 100) success();
            else Toast.makeText(CheckActivity.this, "Неверное местоположение", Toast.LENGTH_SHORT).show();
        }
        else Toast.makeText(this, "Check_prompt is null", Toast.LENGTH_SHORT).show();
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
}