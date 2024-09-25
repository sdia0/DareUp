package com.example.dareup;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dareup.AnalysisResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class CheckActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int QR_SCAN_REQUEST_CODE = 101;
    private Button takePhotoButton;
    private Button completeTaskButton;
    private ApiService apiService;

    private FusedLocationProviderClient fusedLocationClient;
    private Button checkLocationButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

        takePhotoButton = findViewById(R.id.takePhotoButton);
        completeTaskButton = findViewById(R.id.completeTaskButton);

        // Инициализация Retrofit
        initRetrofit();

        takePhotoButton.setOnClickListener(v -> {
            // Вызов камеры
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        });

        checkLocationButton = findViewById(R.id.checkLocationButton);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Кнопка для проверки местоположения
        checkLocationButton.setOnClickListener(v -> checkLocation());
    }

    // Инициализация Retrofit
    private void initRetrofit() {
        // Логгирование запросов для отладки
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://yourserver.com")  // Замените на адрес вашего сервера
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    private void checkLocation() {
        // Проверяем разрешения
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Запрос разрешений
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);

            return;
        }

        // Получаем местоположение
        fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    // Логика проверки местоположения, например, вывод на экран
                    showLocationSuccessDialog(latitude, longitude);
                } else {
                    // Если местоположение недоступно
                    showLocationSettingsDialog();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocation(); // Повторяем попытку получения местоположения
            } else {
                // Показываем диалог с предложением включить разрешения вручную
                showLocationPermissionDeniedDialog();
            }
        }
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение предоставлено — запускаем камеру
                startQRCodeScanner();
            } else {
                // Если разрешение не предоставлено
                Toast.makeText(this, "Разрешение на использование камеры необходимо для сканирования QR-кода", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Диалог при успешном получении координат
    private void showLocationSuccessDialog(double latitude, double longitude) {
        new AlertDialog.Builder(this)
                .setTitle("Местоположение")
                .setMessage("Широта: " + latitude + "\nДолгота: " + longitude)
                .setPositiveButton("ОК", null)
                .create()
                .show();
    }

    // Диалог для предложения включить разрешения вручную
    private void showLocationPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Разрешение отклонено")
                .setMessage("Приложению необходимо разрешение на доступ к геолокации. Хотите включить его вручную?")
                .setPositiveButton("Настройки", (dialog, which) -> {
                    // Переход в настройки приложения
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("Отмена", null)
                .create()
                .show();
    }

    // Диалог для предложения включить службы местоположения
    private void showLocationSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Местоположение недоступно")
                .setMessage("Ваше местоположение не удалось определить. Проверьте настройки GPS.")
                .setPositiveButton("Настройки", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Отмена", null)
                .create()
                .show();
    }

    // Сохранение Bitmap в файл
    private File saveBitmapToFile(Bitmap bitmap) throws IOException {
        // Создаем временный файл для хранения изображения
        File file = new File(getCacheDir(), "photo.jpg");
        file.createNewFile();

        // Преобразуем Bitmap в JPEG
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] bitmapData = bos.toByteArray();

        // Записываем данные в файл
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bitmapData);
        fos.flush();
        fos.close();

        return file;
    }

    // Отправка изображения на сервер
    private void sendImageToServer(File imageFile) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);

        Call<AnalysisResponse> call = apiService.analyzeImage(body);
        call.enqueue(new Callback<AnalysisResponse>() {
            @Override
            public void onResponse(Call<AnalysisResponse> call, Response<AnalysisResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String result = response.body().getResult();

                    if ("squirrel".equals(result)) {
                        // Успех: показываем галочку и зеленую кнопку
                        takePhotoButton.setText("✔");
                        takePhotoButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                    } else {
                        // Неудача: показываем диалог
                        showFailureDialog();
                    }
                } else {
                    Toast.makeText(CheckActivity.this, "Ошибка анализа изображения", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AnalysisResponse> call, Throwable t) {
                Toast.makeText(CheckActivity.this, "Ошибка при соединении с сервером", Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", "Ошибка: " + t.getMessage());
            }
        });
    }

    // Диалог при неудачной проверке
    private void showFailureDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Неудача")
                .setMessage("Вы не прошли проверку. Попробуйте еще раз.")
                .create()
                .show();
    }

    // Проверяем разрешение
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Запрашиваем разрешение на использование камеры
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // Разрешение уже предоставлено — запускаем камеру
            startQRCodeScanner();
        }
    }

    private void startQRCodeScanner() {
        // Запуск стороннего приложения для сканирования QR-кодов
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // Режим сканирования только QR-кодов
        try {
            startActivityForResult(intent, QR_SCAN_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            // Если приложение для сканирования не найдено, покажем сообщение
            Toast.makeText(this, "Приложение для сканирования QR-кодов не установлено", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QR_SCAN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String qrCodeData = data.getStringExtra("SCAN_RESULT");

                // Проверка результата
                checkQRCode(qrCodeData);
            } else {
                // QR-код не был отсканирован успешно
                showFailedDialog();
            }
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            try {
                // Сохранение изображения в файл для отправки
                File imageFile = saveBitmapToFile(imageBitmap);

                // Отправка файла на сервер
                sendImageToServer(imageFile);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Ошибка при обработке изображения", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkQRCode(String qrCodeData) {
        // Проверяем, что в QR-коде есть "белка"
        if (qrCodeData != null && qrCodeData.contains("белка")) {
            // QR-код валиден — делаем кнопку зеленой и заменяем текст на галочку
            Button scanButton = findViewById(R.id.scanQrButton);
            scanButton.setText("✔");
            scanButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            // Если QR-код неверен — показываем диалог с ошибкой
            showFailedDialog();
        }
    }

    private void showFailedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Ошибка")
                .setMessage("Не удалось пройти проверку")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

}
