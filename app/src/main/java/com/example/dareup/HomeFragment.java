package com.example.dareup;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MenuItem; // Импорт для MenuItem
import android.widget.PopupMenu; // Импорт для PopupMenu

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.dareup.R;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class HomeFragment extends Fragment {
    private Button btnNewTask, completeTask, btnResetTimer; // Добавлена кнопка сброса
    private TextView tvTask, nickname, xp, level;
    private CountDownTimer countDownTimer;
    private long retryAfter; // Время в миллисекундах, полученное из другой активности
    String task, taskId, difficultyLevel;
    private long[] endTime = new long[1]; // Время окончания таймера
    private static final String PREFS_NAME = "TimerPrefs";
    private static final String KEY_END_TIME = "end_time";
    ImageButton ibProfilePicture;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(long retryAfter, String task, String taskId) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putLong("retryAfter", retryAfter); // Сохранение значения в аргументах
        args.putString("task", task);
        args.putString("taskId", taskId);
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

            // Получаем данные из SharedPreferences
            SharedPreferences prefs1 = getActivity().getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE);
            task = prefs1.getString("task", null); // Значение по умолчанию теперь null
            taskId = prefs1.getString("taskId", null); // Значение по умолчанию теперь null
            difficultyLevel = prefs1.getString("difficultyLevel", null); // Значение по умолчанию теперь null

            // Устанавливаем текст задания
            if (task != null || tvTask.getText().toString().equals("00:00:00")) {
                tvTask.setText(task);
            } else {
                tvTask.setText("Выбрать задание"); // Значение по умолчанию, если задания нет
            }

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

        completeTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

                // Получаем текущего пользователя
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                // Проверяем, существует ли текущий пользователь
                if (currentUser != null) {
                    String userId = currentUser.getUid(); // Получаем ID текущего пользователя
                    int xpToAdd = getXpForDifficulty(difficultyLevel); // Implement this method based on your logic
                    addXpToUser(userId, xpToAdd);

                    Intent intent = new Intent(getActivity(), WinnerActivity.class);
                    startActivity(intent);

                } else {
                    Log.d("UID", "Пользователь не авторизован");
                }
            }
        });

        btnNewTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DifficultyActivity.class);
                startActivity(intent);
            }
        });

        btnResetTimer.setOnClickListener(new View.OnClickListener() { // Обработка нажатия на кнопку сброса
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
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
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Firebase", "Ошибка получения данных: " + error.getMessage());
                }
            });
        } else {
            Log.d("UID", "Пользователь не авторизован");
        }


        return view;
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

    private void addXpToUser(String userId, int xpToAdd) {
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
                } else {
                    Toast.makeText(getActivity(), "Текущие XP не найдены.", Toast.LENGTH_SHORT).show();
                    Log.d("FirebaseGetXP", "Текущие XP не найдены");
                }
            } else {
                Toast.makeText(getActivity(), "Не удалось получить текущие XP.", Toast.LENGTH_SHORT).show();
                Log.d("FirebaseGetXP", "Ошибка получения XP: " + task.getException());
            }
        });
    }


    private void disableButtons() {
        completeTask.setEnabled(false);
        btnNewTask.setEnabled(false);
    }

    private void enableButtons() {
        completeTask.setEnabled(true);
        btnNewTask.setEnabled(true);
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
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            // Удаление аккаунта
            currentUser.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Перенаправление на WelcomeActivity
                    Intent intent = new Intent(getActivity(), WelcomeActivity.class);
                    startActivity(intent);
                    requireActivity().finish(); // Завершить текущую активность
                    Toast.makeText(getActivity(), "Аккаунт успешно удален", Toast.LENGTH_SHORT).show();
                } else {
                    // Ошибка при удалении
                    Toast.makeText(getActivity(), "Ошибка при удалении аккаунта", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String formatTime(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = (millis / (1000 * 60 * 60)) % 24;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds); // Форматирование в чч:мм:сс
    }
}
