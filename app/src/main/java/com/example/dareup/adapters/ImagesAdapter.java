package com.example.dareup.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.dareup.R;
import com.example.dareup.entities.Memory;
import com.example.dareup.entities.User;

import java.util.ArrayList;
import java.util.List;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {
    private List<Uri> links;
    private Context context;

    public ImagesAdapter(Context context, List<Uri> links) {
        this.context = context;
        this.links = links;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.image.setOnClickListener(v -> {
            Dialog dialog = new Dialog(holder.itemView.getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.look_at_image);

            ImageView imageView = dialog.findViewById(R.id.image);
            try {
                Glide.with(context)
                        .load(links.get(position))
                        .into(imageView);
            }
            catch (Exception e) {
                requestPermission();
            }

            imageView.setOnClickListener(v1 -> dialog.dismiss());
            dialog.show();
        });
        try {
            int radiusInPx = (int) (16 * context.getResources().getDisplayMetrics().density);
            Glide.with(context)
                    .load(links.get(position))
                    .placeholder(R.drawable.default_profile_picture)
                    .error(R.drawable.default_profile_picture)
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(radiusInPx))) // Радиус скругления
                    .into(holder.image);
        } catch (Exception e) {
            requestPermission();
        }
    }
    private static final int PERMISSION_REQUEST_CODE = 100;
    private void requestPermission() {
        if (!(context instanceof Activity)) {
            Toast.makeText(context, "Context must be an instance of Activity to request permissions", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{
                    android.Manifest.permission.READ_MEDIA_IMAGES
            }, PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }
    @Override
    public int getItemCount() {
        if (links == null) {
            links = new ArrayList<>();
            return 0;
        }
        else return links.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
        }
    }
}
