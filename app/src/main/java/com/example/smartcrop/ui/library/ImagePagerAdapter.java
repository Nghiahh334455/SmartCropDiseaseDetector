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
        if (imageName != null && !imageName.isEmpty()) {
            if (imageName.startsWith("BASE64:")) {
                byte[] bytes = com.example.smartcrop.utils.ImageUtils.base64ToBytes(imageName);
                if (bytes != null) {
                    Glide.with(context).load(bytes).placeholder(android.R.drawable.ic_menu_gallery).into(holder.imageView);
                } else {
                    holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else if (imageName.startsWith("http://") || imageName.startsWith("https://")) {
                Glide.with(context).load(imageName).placeholder(android.R.drawable.ic_menu_gallery).into(holder.imageView);
            } else {
                int resId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
                if (resId != 0) {
                    Glide.with(context).load(resId).placeholder(android.R.drawable.ic_menu_gallery).into(holder.imageView);
                } else {
                    byte[] bytes = com.example.smartcrop.utils.ImageUtils.base64ToBytes(imageName);
                    if (bytes != null && bytes.length > 20) {
                        Glide.with(context).load(bytes).placeholder(android.R.drawable.ic_menu_gallery).into(holder.imageView);
                    } else {
                        Glide.with(context).load(android.R.drawable.ic_menu_gallery).into(holder.imageView);
                    }
                }
            }
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
