package com.example.dareup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExpandableMemoryAdapter extends RecyclerView.Adapter<ExpandableMemoryAdapter.MemoryViewHolder> {
    private List<Memory> memoryList;  // Список объектов Memory
    private Context context;

    public ExpandableMemoryAdapter(List<Memory> memoryList, Context context) {
        this.memoryList = memoryList;
        this.context = context;
    }

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

    @Override
    public int getItemCount() {
        return memoryList.size();  // Возвращаем размер списка
    }

    class MemoryViewHolder extends RecyclerView.ViewHolder {
        private TextView userTitle, taskTitle, taskDescription;
        private LinearLayout detailsLayout;

        public MemoryViewHolder(@NonNull View itemView) {
            super(itemView);
            userTitle = itemView.findViewById(R.id.userTitle);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            detailsLayout = itemView.findViewById(R.id.detailsLayout);
            taskDescription = itemView.findViewById(R.id.taskDescription);

            // Добавляем обработчик клика для раскрытия/сворачивания элемента
            itemView.setOnClickListener(v -> {
                Memory memory = memoryList.get(getAdapterPosition());
                memory.setExpanded(!memory.isExpanded());  // Меняем состояние "развернут/свернут"
                notifyItemChanged(getAdapterPosition());
            });
        }

        public void bind(Memory memory) {
            // Привязываем данные из объекта Memory к элементам View
            userTitle.setText(memory.getTitle());
            taskTitle.setText(memory.getTask());
            taskDescription.setText(memory.getDescription());

            // Устанавливаем видимость описания в зависимости от состояния "развернут/свернут"
            if (memory.isExpanded()) {
                detailsLayout.setVisibility(View.VISIBLE);
            } else {
                detailsLayout.setVisibility(View.GONE);
            }
        }
    }
}
