package com.example.dareup;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    // Отправка изображения на сервер для анализа
    @Multipart
    @POST("/analyze")
    Call<AnalysisResponse> analyzeImage(@Part MultipartBody.Part image);

}

