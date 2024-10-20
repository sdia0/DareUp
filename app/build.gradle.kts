plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
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
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.4.1")
    implementation("junit:junit:4.13.2")
    implementation("nl.dionsegijn:konfetti-xml:2.0.0@aar")
    implementation("nl.dionsegijn:konfetti-core:2.0.2")
    implementation("androidx.preference:preference:1.2.0")
    implementation(platform("com.google.firebase:firebase-bom:32.0.0"))
    implementation("com.github.bumptech.glide:glide:4.15.1")

    implementation("com.google.firebase:firebase-auth:21.3.0")
    implementation("com.google.firebase:firebase-storage:20.2.0")
    implementation("com.google.firebase:firebase-firestore:25.0.0")

    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.getkeepsafe.taptargetview:taptargetview:1.13.3")

    implementation("com.google.android.material:material:1.9.0'") // Версия может отличаться

    // Retrofit для выполнения HTTP-запросов
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // Конвертер Gson для обработки JSON-ответов
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.google.firebase:firebase-auth:22.0.0")

    // OkHttp (опционально, для логирования запросов)
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")
    // ML Kit для сканирования штрихкодов
    implementation("com.google.mlkit:barcode-scanning:17.0.3")

    // Библиотека для работы с камерой
    implementation("androidx.camera:camera-camera2:1.1.0")
    implementation("androidx.camera:camera-lifecycle:1.1.0")
    implementation("androidx.camera:camera-view:1.0.0-alpha30")

    implementation("com.google.firebase:firebase-storage:20.2.1")

    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.location)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}