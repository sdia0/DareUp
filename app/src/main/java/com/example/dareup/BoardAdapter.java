package com.example.dareup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.ViewHolder> {
    private List<User> users;
    private Context context;

    public BoardAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.tvNickname.setText(user.getName());
        holder.tvLevel.setText(String.valueOf(user.getLevel()));
        holder.tvXP.setText(String.valueOf(user.getXp()));

        // Загружаем изображение профиля
        Glide.with(context)
                .load(user.getPhotoUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(300, 300) // Задай нужные размеры
                .centerCrop()
                .circleCrop()
                .into(holder.ibProfile);

        holder.tvPosition.setText(String.valueOf(position + 1)); // позиция в списке
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ibProfile;
        TextView tvNickname;
        TextView tvLevel;
        TextView tvXP;
        TextView tvPosition;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ibProfile = itemView.findViewById(R.id.profilePicture);
            tvNickname = itemView.findViewById(R.id.tvNickname);
            tvLevel = itemView.findViewById(R.id.tvLevel);
            tvXP = itemView.findViewById(R.id.tvXP);
            tvPosition = itemView.findViewById(R.id.tvPosition);
        }
    }
}
