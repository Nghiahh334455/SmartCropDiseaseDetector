package com.example.smartcrop.ui.library;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartcrop.R;

import java.util.List;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ViewHolder> {

    private final List<String> imageResources;
    private final Context context;

    public ImagePagerAdapter(Context context, List<String> imageResources) {
        this.context = context;
        this.imageResources = imageResources;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_slider, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageName = imageResources.get(position);
        
        // Chuyển đổi tên file String sang resource ID
        int resId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
        
        if (resId != 0) {
            Glide.with(context).load(resId).into(holder.imageView);
        } else {
            // Nếu không tìm thấy ảnh thật, hiển thị ảnh mặc định
            Glide.with(context).load(android.R.drawable.ic_menu_gallery).into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return imageResources.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivSliderImage);
        }
    }
}
