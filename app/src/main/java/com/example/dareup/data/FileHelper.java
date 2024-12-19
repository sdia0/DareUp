package com.example.dareup.data;

import android.content.Context;
import android.util.Log;

import com.example.dareup.entities.Memory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FileHelper {
    private final Context context;

    public FileHelper(Context context) {
        this.context = context;
    }

    private List<Memory> loadMemoriesFromFile() {

        // Прочитать существующий JSON-файл
        List<Memory> memoryList = new ArrayList<>();

        FileInputStream fis = null;
        BufferedReader reader = null;
        try {
            fis = context.openFileInput("memories.json");
            reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder builder = new StringBuilder();
            String line;

            // Чтение строки за строкой и добавление в StringBuilder
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            // Преобразование прочитанного текста в список Memory
            String json = builder.toString();
            if (!json.isEmpty()) {
                Type memoryListType = new TypeToken<ArrayList<Memory>>() {
                }.getType();
                memoryList = new Gson().fromJson(json, memoryListType);
            }
        } catch (IOException e) {
            Log.e("TaskFragment", "Error reading file: " + e.getMessage(), e);
        } finally {
            try {
                if (reader != null) reader.close();
                if (fis != null) fis.close();
            } catch (IOException e) {
                Log.e("TaskFragment", "Error closing file: " + e.getMessage(), e);
            }
        }
        return memoryList;
    }
    private List<String> loadImagesFromFile() {

        // Прочитать существующий JSON-файл
        List<String> links = new ArrayList<>();

        FileInputStream fis = null;
        BufferedReader reader = null;
        try {
            fis = context.openFileInput("images_links.json");
            reader = new BufferedReader(new InputStreamReader(fis));
            String line;

            // Чтение строки за строкой и добавление в StringBuilder
            while ((line = reader.readLine()) != null) {
                links.add(line);
            }
        } catch (IOException e) {
            Log.e("TaskFragment", "Error reading file: " + e.getMessage(), e);
        } finally {
            try {
                if (reader != null) reader.close();
                if (fis != null) fis.close();
            } catch (IOException e) {
                Log.e("TaskFragment", "Error closing file: " + e.getMessage(), e);
            }
        }
        return links;
    }
}
