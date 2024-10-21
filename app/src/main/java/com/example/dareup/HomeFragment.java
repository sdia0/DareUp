package com.example.dareup;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private Button btnNewTask, completeTask, btnResetTimer; // Добавлена кнопка сброса
    private TextView tvTask, nickname, xp, level;
    private CountDownTimer countDownTimer;
    private long retryAfter; // Время в миллисекундах, полученное из другой активности
    String difficultyLevel, id;
    private long[] endTime = new long[1]; // Время окончания таймера
    private static final String PREFS_NAME = "TimerPrefs";
    private static final String KEY_END_TIME = "end_time";
    ImageButton ibProfilePicture;
    String activeTask;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(long retryAfter) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putLong("retryAfter", retryAfter); // Сохранение значения в аргументах
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        btnNewTask = view.findViewById(R.id.btnNewTask);
        completeTask = view.findViewById(R.id.btnCompleteTask);
        btnResetTimer = view.findViewById(R.id.btnResetTimer); // Инициализация кнопки сброса
        tvTask = view.findViewById(R.id.tvTask);
        ibProfilePicture = view.findViewById(R.id.profilePicture);
        level = view.findViewById(R.id.level);
        nickname = view.findViewById(R.id.nickname);
        xp = view.findViewById(R.id.xp);

        // Получение времени из аргументов
        if (getArguments() != null) {
            retryAfter = getArguments().getLong("retryAfter", 0);
            Log.d("Таймер", "Время получено: " + retryAfter);

            //difficultyLevel = prefs1.getString("difficultyLevel", null); // Значение по умолчанию теперь null

            // Получаем текущее время
            long currentTime = System.currentTimeMillis();

            // Проверяем, есть ли сохраненное время окончания
            SharedPreferences prefs2 = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            endTime[0] = prefs2.getLong(KEY_END_TIME, 0);

            // Проверяем, если retryAfter не равен 0
            if (endTime[0] > currentTime) {
                // Таймер активен
                long remainingTime = endTime[0] - currentTime;
                startCountDownTimer(remainingTime);
                disableButtons();
            } else {
                // Таймер неактивен, устанавливаем новое время окончания
                endTime[0] = currentTime + retryAfter; // Используем переданное значение retryAfter
                prefs2.edit().putLong(KEY_END_TIME, endTime[0]).apply();
                startCountDownTimer(retryAfter);
            }
        }

        ibProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog();
            }
        });

        User user = loadUserDataFromFile();
        if (user != null) {
            // Установка данных в текстовые поля
            nickname.setText(user.getName());
            level.setText("Level: " + user.getLevel());
            xp.setText(user.getXp() + "xp");
            id = user.getId();

            // Загружаем изображение профиля
            Glide.with(requireContext())
                    .load(user.getPhotoUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(300, 300)
                    .centerCrop()
                    .circleCrop()
                    .into(ibProfilePicture);

            // Установка задания из поля activeTask
            activeTask = user.getActiveTask();
            difficultyLevel = user.getActiveTaskDifficulty();
            if (activeTask != null && !activeTask.isEmpty()) {
                tvTask.setText(activeTask);
                btnNewTask.setText("Другое задание");
            } else {
                tvTask.setText("Нет активного задания");
                completeTask.setEnabled(false);
                completeTask.setAlpha(0.5f);
                btnNewTask.setText("Выбор задания");
            }
            if (difficultyLevel.equals("hard")) completeTask.setText("Проверить");
            else completeTask.setText("Выполнить");
        } else {
            Toast.makeText(getActivity(), "Не удалось загрузить данные о пользователе", Toast.LENGTH_SHORT).show();
        }

        completeTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (difficultyLevel.equals("hard")) {
                    final String[] check = new String[1];
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
                                    check[0] = taskSnapshot.child("check").getValue(String.class);
                                    if (check[0] != null) {
                                        if (check[0].equals("Сделать фото")) {
                                            Intent intent = new Intent(getActivity(), AiCheckActivity.class);
                                            startActivity(intent);
                                        }
                                        else {
                                            Intent intent = new Intent(getActivity(), CheckActivity.class);
                                            startActivity(intent);
                                        }
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
                } else {
                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

                    // Получаем текущего пользователя
                    FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                    // Проверяем, существует ли текущий пользователь
                    if (currentUser != null) {
                        String userId = currentUser.getUid(); // Получаем ID текущего пользователя
                        int xpToAdd = getXpForDifficulty(difficultyLevel); // Implement this method based on your logic
                        addXpToUserAndResetTask(userId, xpToAdd);
                    } else {
                        Log.d("UID", "Пользователь не авторизован");
                    }
                    Intent intent = new Intent(getActivity(), WinnerActivity.class);
                    startActivity(intent);
                }
            }
        });

        btnNewTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnNewTask.getText().toString().equals("Выбор задания")) {
                    setTries(4);
                }
                if (getTries() == 0) {
                    Toast.makeText(getActivity(), "Вы исчерпали количество попыток", Toast.LENGTH_SHORT).show();
                    retryAfter = 60 * 1000;
                    setTries(3);
                    addCompletedTaskToFirebase(id);
                    saveActiveTaskToFile("");

                    //difficultyLevel = prefs1.getString("difficultyLevel", null); // Значение по умолчанию теперь null

                    // Получаем текущее время
                    long currentTime = System.currentTimeMillis();

                    // Проверяем, есть ли сохраненное время окончания
                    SharedPreferences prefs2 = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    endTime[0] = prefs2.getLong(KEY_END_TIME, 0);

                    // Проверяем, если retryAfter не равен 0
                    if (endTime[0] > currentTime) {
                        // Таймер активен
                        long remainingTime = endTime[0] - currentTime;
                        startCountDownTimer(remainingTime);
                        disableButtons();
                    } else {
                        // Таймер неактивен, устанавливаем новое время окончания
                        endTime[0] = currentTime + retryAfter; // Используем переданное значение retryAfter
                        prefs2.edit().putLong(KEY_END_TIME, endTime[0]).apply();
                        startCountDownTimer(retryAfter);
                    }
                }
                else {
                    Intent intent = new Intent(getActivity(), DifficultyActivity.class);
                    startActivity(intent);
                }
            }
        });

        btnResetTimer.setOnClickListener(new View.OnClickListener() { // Обработка нажатия на кнопку сброса
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });

        /*FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            // Получаем ID текущего пользователя
            String userId = currentUser.getUid();

            // Ссылка на конкретного пользователя в базе данных
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

            // Получение данных о пользователе по userId
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Преобразование данных в объект User
                    User user = dataSnapshot.getValue(User.class);

                    if (user != null) {
                        // Установка данных в текстовые поля
                        nickname.setText(user.getName());
                        level.setText("Level: " + user.getLevel());  // пример установки email, если у вас есть это поле
                        xp.setText(user.getXp() + "xp");  // пример установки email, если у вас есть это поле
                        // Загружаем изображение профиля
                        Glide.with(requireContext())
                                .load(user.getPhotoUrl())
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .override(300, 300) // Задай нужные размеры
                                .centerCrop()
                                .circleCrop()
                                .into(ibProfilePicture);

                        // Установка задания из поля activeTask
                        String activeTask = user.getActiveTask(); // предполагаем, что в User есть поле activeTask
                        if (activeTask != null && !activeTask.isEmpty()) {
                            tvTask.setText(activeTask);
                        } else {
                            tvTask.setText("Выбрать задание");
                            completeTask.setEnabled(false);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Firebase", "Ошибка получения данных: " + error.getMessage());
                }
            });
        } else {
            Log.d("UID", "Пользователь не авторизован");
        }*/

        return view;
    }
    public int getTries() {
        File file = new File(getActivity().getFilesDir(), "user_data.json");
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

                return userJson.getInt("tries");
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        Toast.makeText(getActivity(), "Произошла ошибка", Toast.LENGTH_SHORT).show();
        return 3;
    }
    public void setTries(int tries) {
        File file = new File(getActivity().getFilesDir(), "user_data.json");
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

            userJson.put("tries", tries);

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
            File file = new File(getActivity().getFilesDir(), "user_data.json");
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

    private void showAlertDialog() {
        // Создание билдера для AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Профиль")
                .setMessage("Выберите одно из действий ниже");

        // Добавление кнопки "Выйти"
        builder.setPositiveButton("Выйти", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logOut();
                Toast.makeText(getActivity(), "Выход", Toast.LENGTH_SHORT).show();
            }
        });

        // Добавление кнопки "Удалить аккаунт"
        builder.setNegativeButton("Удалить аккаунт", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAccount();
                Toast.makeText(getActivity(), "Удаление аккаунта", Toast.LENGTH_SHORT).show();
            }
        });

        // Показать диалог
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void startCountDownTimer(long duration) {
        countDownTimer = new CountDownTimer(duration, 1000) { // 1000 миллисекунд = 1 секунда
            @Override
            public void onTick(long millisUntilFinished) {
                // Обновить текстовое поле с оставшимся временем
                tvTask.setText(formatTime(millisUntilFinished));
                disableButtons();
            }

            @Override
            public void onFinish() {
                SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                prefs.edit().remove(KEY_END_TIME).apply();
                enableButtons();
            }
        }.start();
    }

    private void resetTimer() {
        // Сброс таймера
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        // Удаление времени окончания из SharedPreferences
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_END_TIME).apply();

        // Активируем кнопки
        enableButtons();
    }

    private void addXpToUserAndResetTask(String userId, int xpToAdd) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("users").child(userId);

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
                        updateUserLevel(database, levelsGained, newXp);
                    } else {
                        // Если XP меньше 100, просто обновляем XP
                        updateXp(database, newXp);
                    }
                } else {
                    Toast.makeText(getActivity(), "Текущие XP не найдены.", Toast.LENGTH_SHORT).show();
                    Log.d("FirebaseGetXP", "Текущие XP не найдены");
                }
            } else {
                Toast.makeText(getActivity(), "Не удалось получить текущие XP.", Toast.LENGTH_SHORT).show();
                Log.d("FirebaseGetXP", "Ошибка получения XP: " + task.getException());
            }
        });

        addCompletedTaskToFirebase(id);
        saveActiveTaskToFile("");
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
        File file = new File(getActivity().getFilesDir(), "user_data.json");
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
        File file = new File(getActivity().getFilesDir(), "user_data.json");
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

    private void saveActiveTaskToFile(String activeTask) {
        File file = new File(getActivity().getFilesDir(), "user_data.json");
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
    private void addCompletedTaskToFirebase(String userId) {
        DatabaseReference completedTasksRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("completed_tasks");

        completedTasksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> completedTasks = new ArrayList<>();

                // Если уже есть выполненные задания, получаем их
                if (dataSnapshot.exists()) {
                    for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                        String existingTask = taskSnapshot.getValue(String.class);
                        completedTasks.add(existingTask);
                    }
                }

                // Добавляем новое задание в список
                completedTasks.add(activeTask);

                // Обновляем список выполненных заданий в Firebase
                completedTasksRef.setValue(completedTasks).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Log.d("FirebaseUpdate", "Выполненное задание добавлено.");
                    } else {
                        Log.d("FirebaseUpdate", "Ошибка добавления задания: " + updateTask.getException());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("FirebaseError", "Ошибка при чтении данных: " + databaseError.getMessage());
            }
        });
    }

    private void disableButtons() {
        completeTask.setEnabled(false);
        completeTask.setAlpha(0.5f);
        btnNewTask.setAlpha(0.5f);
        btnNewTask.setEnabled(false);
    }

    private void enableButtons() {
        tvTask.setText("Нет активного задания");
        btnNewTask.setText("Выбор задания");
        btnNewTask.setEnabled(true);
        btnNewTask.setAlpha(1.0f);
        //Toast.makeText(getActivity(), "Вы можете снова выбирать задания!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
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

    // Метод для выхода из аккаунта
    private void logOut() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        // Выход из Firebase
        firebaseAuth.signOut();

        // Перенаправление на WelcomeActivity
        Intent intent = new Intent(getActivity(), WelcomeActivity.class);
        startActivity(intent);
        requireActivity().finish(); // Завершить текущую активность
        Toast.makeText(getActivity(), "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show();
    }

    // Метод для удаления аккаунта
    private void deleteAccount() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail(); // Получаем email текущего пользователя

            // Показываем диалог для ввода пароля
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Подтвердите действие");

            final EditText passwordInput = new EditText(getContext());
            passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            builder.setView(passwordInput);

            builder.setPositiveButton("Подтвердить", (dialog, which) -> {
                String password = passwordInput.getText().toString().trim();

                if (!password.isEmpty()) {
                    // Создаем учетные данные для реаутентификации
                    AuthCredential credential = EmailAuthProvider.getCredential(userEmail, password);

                    // Реаутентификация
                    currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Успешная реаутентификация - теперь можно удалить аккаунт
                            currentUser.delete().addOnCompleteListener(deleteTask -> {
                                if (deleteTask.isSuccessful()) {
                                    String uid = currentUser.getUid();

                                    // Удаление данных из users по uid
                                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
                                    userRef.removeValue().addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            // После успешного удаления данных пользователя, удаляем сам аккаунт
                                            currentUser.delete().addOnCompleteListener(deleteTask1 -> {
                                                if (deleteTask1.isSuccessful()) {
                                                    Log.d("DeleteAccount", "Запись в таблице users успешно удалена");
                                                } else {
                                                    // Ошибка при удалении
                                                    Toast.makeText(getActivity(), "Ошибка при удалении аккаунта", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else {
                                            // Ошибка при удалении данных пользователя
                                            Toast.makeText(getActivity(), "Ошибка при удалении данных пользователя", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    userRef = FirebaseDatabase.getInstance().getReference("memories").child(uid);
                                    userRef.removeValue().addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            // После успешного удаления данных пользователя, удаляем сам аккаунт
                                            currentUser.delete().addOnCompleteListener(deleteTask1 -> {
                                                if (deleteTask1.isSuccessful()) {
                                                    Log.d("DeleteAccount", "Запись в таблице memories успешно удалена");
                                                } else {
                                                    // Ошибка при удалении
                                                    Toast.makeText(getActivity(), "Ошибка при удалении аккаунта", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else {
                                            // Ошибка при удалении данных пользователя
                                            Toast.makeText(getActivity(), "Ошибка при удалении данных пользователя", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    deleteFileUsingFileObject("memories.json");
                                    deleteFileUsingFileObject("user_data.json");
                                    Intent intent = new Intent(getActivity(), WelcomeActivity.class);
                                    startActivity(intent);
                                    requireActivity().finish();
                                } else {
                                    Toast.makeText(getActivity(), "Ошибка при удалении аккаунта", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(getActivity(), "Ошибка при реаутентификации. Неверный пароль.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), "Введите пароль", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());
            builder.show();
        }

    }
    public void deleteFileUsingFileObject(String fileName) {
        File file = new File(requireActivity().getFilesDir(), fileName); // Получаем файл по его пути
        boolean deleted = file.delete(); // Удаление файла

        if (deleted) {
            Log.d("FileDeletion", "Файл успешно удалён: " + fileName);
        } else {
            Log.e("FileDeletion", "Не удалось удалить файл: " + fileName);
        }
    }

    private String formatTime(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = (millis / (1000 * 60 * 60)) % 24;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds); // Форматирование в чч:мм:сс
    }
}
