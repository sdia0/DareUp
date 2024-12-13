package com.example.dareup.activities;

import static com.google.android.gms.common.util.CollectionUtils.listOf;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.example.dareup.R;

import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.core.models.Size;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class WinnerActivity extends AppCompatActivity {
    KonfettiView viewKonfetti;
    Button btnOk;
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
        setContentView(R.layout.activity_winner);
        viewKonfetti = findViewById(R.id.konfettiView);
        btnOk = findViewById(R.id.btnOk);
        Shape.DrawableShape drawableShape = new Shape.DrawableShape(AppCompatResources.getDrawable(this, R.drawable.ic_for_konfetti), true);
        EmitterConfig emitterConfig = new Emitter(300, TimeUnit.MILLISECONDS).max(300);
        viewKonfetti.start(
                new PartyFactory(emitterConfig)
                        .shapes(Shape.Circle.INSTANCE, Shape.Square.INSTANCE, drawableShape)
                        .spread(360)
                        .position(0.1, 0, 1, 1)
                        .sizes(new Size(8, 50, 10))
                        .timeToLive(2500)
                        .fadeOutEnabled(true)
                        .build()
        );
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent(WinnerActivity.this, EditTaskActivity.class);
                    startActivity(intent);
                }
            });
    }
}