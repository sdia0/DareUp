package com.example.dareup;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_PHOTO = 1;
    private static final int VIEW_TYPE_ADD_BUTTON = 2;

    private List<Uri> photoList; // Список фото (Uri может быть источником локального или загруженного изображения)
    private Context context;
    private OnAddPhotoClickListener addPhotoClickListener;

    public interface OnAddPhotoClickListener {
        void onAddPhotoClick();
    }

    public PhotoAdapter(Context context, List<Uri> photoList, OnAddPhotoClickListener addPhotoClickListener) {
        this.context = context;
        this.photoList = photoList;
        this.addPhotoClickListener = addPhotoClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        // Последний элемент будет кнопкой для добавления фото
        if (position == photoList.size()) {
            return VIEW_TYPE_ADD_BUTTON;
        } else {
            return VIEW_TYPE_PHOTO;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_PHOTO) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
            return new PhotoViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_add_photo_button, parent, false);
            return new AddButtonViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_PHOTO) {
            // Отображаем фото
            Uri photoUri = photoList.get(position);
            ((PhotoViewHolder) holder).bind(photoUri);
        } else {
            // Настраиваем кнопку для добавления фото
            ((AddButtonViewHolder) holder).bind();
        }
    }

    @Override
    public int getItemCount() {
        // +1 для кнопки "Загрузить фото"
        return photoList.size() + 1;
    }

    // ViewHolder для фото
    class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewPhoto);
        }

        void bind(Uri photoUri) {
            imageView.setImageURI(photoUri); // Загружаем фото через Uri
        }
    }

    // ViewHolder для кнопки "Загрузить фото"
    class AddButtonViewHolder extends RecyclerView.ViewHolder {
        Button btnAddPhoto;

        AddButtonViewHolder(@NonNull View itemView) {
            super(itemView);
            btnAddPhoto = itemView.findViewById(R.id.tvLoadPhoto);
        }

        void bind() {
            btnAddPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addPhotoClickListener.onAddPhotoClick(); // Обрабатываем клик на кнопку "Загрузить фото"
                }
            });
        }
    }
}
