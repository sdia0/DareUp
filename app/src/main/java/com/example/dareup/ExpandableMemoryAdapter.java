package com.example.dareup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ExpandableMemoryAdapter extends RecyclerView.Adapter<ExpandableMemoryAdapter.MemoryViewHolder> {
    private List<Memory> memoryList;  // Список объектов Memory
    private Context context;

    public ExpandableMemoryAdapter(List<Memory> memoryList, Context context) {
        this.memoryList = memoryList;
        this.context = context;
    }
    public ExpandableMemoryAdapter() { }

    @NonNull
    @Override
    public MemoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false);
        return new MemoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemoryViewHolder holder, int position) {
        Memory memory = memoryList.get(position);  // Получаем объект Memory по позиции
        holder.bind(memory);  // Привязываем данные к View
    }

    // Метод для обновления данных в адаптере
    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Memory> newData) {
        this.memoryList.clear();
        this.memoryList.addAll(newData);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return memoryList.size();  // Возвращаем размер списка
    }

    class MemoryViewHolder extends RecyclerView.ViewHolder {
        private TextView userTitle, taskTitle, taskDescription;
        private LinearLayout detailsLayout;

        @SuppressLint("ClickableViewAccessibility")
        public MemoryViewHolder(@NonNull View itemView) {
            super(itemView);
            userTitle = itemView.findViewById(R.id.userTitle);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            detailsLayout = itemView.findViewById(R.id.detailsLayout);
            taskDescription = itemView.findViewById(R.id.taskDescription);

            // Добавляем обработчик двойного нажатия
            final GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    // Вызываем метод при двойном нажатии
                    deleteMemoryFromFile(getAdapterPosition());
                    return true;
                }
            });

            // Устанавливаем обработчик нажатий для элемента списка
            itemView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

            // Добавляем обработчик клика для раскрытия/сворачивания элемента
            itemView.setOnClickListener(v -> {
                Memory memory = memoryList.get(getAdapterPosition());
                if (!(memory.getTask().isEmpty() || memory.getDescription().isEmpty())) {
                    memory.setExpanded(!memory.isExpanded());  // Меняем состояние "развернут/свернут"
                }
                notifyItemChanged(getAdapterPosition());
            });
        }

        public void bind(Memory memory) {
            // Привязываем данные из объекта Memory к элементам View
            if (memory.getTitle().isEmpty()) {
                userTitle.setText(memory.getTask());
                taskTitle.setText("");
                taskDescription.setText("");
            }
            else {
                userTitle.setText(memory.getTitle());
                taskTitle.setText(memory.getTask());
                taskDescription.setText(memory.getDescription());
            }

            // Устанавливаем видимость описания в зависимости от состояния "развернут/свернут"
            if (memory.isExpanded()) {
                detailsLayout.setVisibility(View.VISIBLE);
            } else {
                detailsLayout.setVisibility(View.GONE);
            }
        }
        private void deleteMemoryFromFile(int position) {
            List<Memory> memoryList = new ArrayList<>();
            String fileName = "memories.json";

            // Чтение файла
            try (FileInputStream fis = context.openFileInput(fileName);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {

                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                String json = builder.toString();
                if (!json.isEmpty()) {
                    Type memoryListType = new TypeToken<ArrayList<Memory>>() {}.getType();
                    memoryList = new Gson().fromJson(json, memoryListType);
                }
            } catch (IOException e) {
                Log.e("ExpandableMemoryAdapter", "Error reading file: " + e.getMessage(), e);
            }

            // Проверяем, что позиция валидна
            if (position >= 0 && position < memoryList.size()) {
                Memory memoryToDelete = memoryList.get(position);  // Сохраняем память для удаления из Firebase

                // Удаляем элемент из списка локально
                memoryList.remove(position);

                // Преобразуем обновленный список обратно в JSON и сохраняем локально
                String updatedJson = new Gson().toJson(memoryList);
                try (FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
                     OutputStreamWriter osw = new OutputStreamWriter(fos)) {
                    osw.write(updatedJson);
                    Log.d("ExpandableMemoryAdapter", "Memory list updated and saved to file: " + fileName);
                } catch (IOException e) {
                    Log.e("ExpandableMemoryAdapter", "Error writing to file: " + e.getMessage(), e);
                }
                // Обновляем адаптер, чтобы изменения отобразились сразу на экране
                updateData(memoryList);

                // Удаляем запись из Firebase асинхронно
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("memories")
                        .child(userId).child(memoryToDelete.getId());

                List<Memory> finalMemoryList = memoryList;
                databaseReference.removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("ExpandableMemoryAdapter", "Memory deleted from Firebase successfully.");
                    } else {
                        Log.e("ExpandableMemoryAdapter", "Failed to delete memory from Firebase.", task.getException());

                        // В случае ошибки можно добавить логику восстановления удалённого элемента
                        // Например, показать уведомление пользователю или вернуть запись в список.
                        finalMemoryList.add(position, memoryToDelete); // Восстанавливаем запись локально
                        String restoredJson = new Gson().toJson(finalMemoryList);
                        try (FileOutputStream fosRestore = context.openFileOutput(fileName, Context.MODE_PRIVATE);
                             OutputStreamWriter oswRestore = new OutputStreamWriter(fosRestore)) {
                            oswRestore.write(restoredJson);
                        } catch (IOException e) {
                            Log.e("ExpandableMemoryAdapter", "Error restoring deleted memory: " + e.getMessage(), e);
                        }
                        updateData(finalMemoryList); // Обновляем адаптер снова
                    }
                });
            } else {
                Log.e("ExpandableMemoryAdapter", "Invalid position: " + position);
            }
        }
    }
}
