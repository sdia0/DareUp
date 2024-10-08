buildscript {
    dependencies {
        classpath(libs.google.services)
        classpath("com.android.tools.build:gradle:8.0.2") // или текущая версия Android Gradle Plugin
        classpath("com.google.gms:google-services:4.4.2") // Зависимость Google Services
    }
    repositories {
        google()
        mavenCentral()
    }
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
}