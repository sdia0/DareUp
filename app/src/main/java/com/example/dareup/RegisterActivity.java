package com.example.dareup;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;

public class RegisterActivity extends AppCompatActivity {
    Button btnAddUser;
    EditText editInputName, editInputLogin, editInputPassword, editRepeatPassword;
    TextView loadPhoto;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        editInputName = findViewById(R.id.editName);
        editInputLogin = findViewById(R.id.editLogin);
        editInputPassword = findViewById(R.id.editPassword);
        editRepeatPassword = findViewById(R.id.editRepeatPassword);
        btnAddUser = findViewById(R.id.btnRegister);
        loadPhoto = findViewById(R.id.btnLoadPhoto);
        btnAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editInputName.getText().toString().trim();
                String login = editInputLogin.getText().toString().trim();
                String password = editInputPassword.getText().toString().trim();
                String repPassword = editRepeatPassword.getText().toString().trim();
                if (name.isEmpty() || login.isEmpty() || password.isEmpty() || repPassword.isEmpty())
                    addUser(name, login, password);
                else Toast.makeText(RegisterActivity.this, "Должны быть заполнены все поля!", Toast.LENGTH_SHORT);
            }
        });

    }
    private void addUser(String name, String login, String password) {
        String id = mDatabase.push().getKey();
        User newUser = new User(id, login, name, 0, 0, " ");

        if (id != null) {
            mDatabase.child(id).setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Регистрация успешно завершена!", Toast.LENGTH_SHORT).show();
                        Log.d("RegisterActivity", "User added: " + id);
                    } else {
                        Log.e("RegisterActivity", "Failed to add user", task.getException());
                    }
                }
            });
            editInputName.setText("");
            editInputLogin.setText("");
            editInputPassword.setText("");
            editRepeatPassword.setText("");
        }
    }
}