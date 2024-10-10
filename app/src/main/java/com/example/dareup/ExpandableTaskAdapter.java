package com.example.dareup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ExpandableTaskAdapter extends RecyclerView.Adapter<ExpandableTaskAdapter.TaskViewHolder> {
    private List<Task> taskList;
    private Context context;

    public ExpandableTaskAdapter(List<Task> taskList, Context context) {
        this.taskList = taskList;
        this.context = context;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private LinearLayout detailsLayout;
        private TextView descriptionTextView;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.taskTitle);
            detailsLayout = itemView.findViewById(R.id.taskDetails);
            descriptionTextView = itemView.findViewById(R.id.taskDescription);

            itemView.setOnClickListener(v -> {
                Task task = taskList.get(getAdapterPosition());
                task.setExpanded(!task.isExpanded());
                notifyItemChanged(getAdapterPosition());
            });
        }

        public void bind(Task task) {
            titleTextView.setText(task.getTitle());
            descriptionTextView.setText(task.getNotes());

            // Установка видимости деталей
            if (task.isExpanded()) {
                detailsLayout.setVisibility(View.VISIBLE);
            } else {
                detailsLayout.setVisibility(View.GONE);
            }
        }
    }
}
