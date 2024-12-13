package com.example.dareup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dareup.R;

public class DifficultyActivity extends AppCompatActivity {

    Button btnEasy, btnMedium, btnHard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_difficalty);
        btnEasy = findViewById(R.id.btnEasy);
        btnMedium = findViewById(R.id.btnMedium);
        btnHard = findViewById(R.id.btnHard);

        btnEasy.setOnClickListener(v -> startSelectTaskActivity("easy"));
        btnMedium.setOnClickListener(v -> startSelectTaskActivity("medium"));
        btnHard.setOnClickListener(v -> startSelectTaskActivity("hard"));
    }

    private void startSelectTaskActivity(String difficulty) {
        Intent intent = new Intent(DifficultyActivity.this, SelectTaskActivity.class);
        intent.putExtra("difficultyLevel", difficulty); // Передаем сложность
        startActivity(intent);
    }
}