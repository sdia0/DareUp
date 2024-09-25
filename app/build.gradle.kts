plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.dareup"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.dareup"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // Retrofit для выполнения HTTP-запросов
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // Конвертер Gson для обработки JSON-ответов
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp (опционально, для логирования запросов)
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")
    // ML Kit для сканирования штрихкодов
    implementation("com.google.mlkit:barcode-scanning:17.0.3")

    // Библиотека для работы с камерой
    implementation("androidx.camera:camera-camera2:1.1.0")
    implementation("androidx.camera:camera-lifecycle:1.1.0")
    implementation("androidx.camera:camera-view:1.0.0-alpha30")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}