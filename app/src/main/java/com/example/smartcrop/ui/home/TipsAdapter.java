package com.example.smartcrop.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartcrop.databinding.ItemTipBinding;

import java.util.List;

public class TipsAdapter extends RecyclerView.Adapter<TipsAdapter.ViewHolder> {

    private final List<TipModel> tips;

    public TipsAdapter(List<TipModel> tips) {
        this.tips = tips;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTipBinding binding = ItemTipBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TipModel tip = tips.get(position);
        holder.binding.tvTipTitle.setText(tip.title);
        holder.binding.tvTipContent.setText(tip.content);

        // Resolve resource name to ID
        String imageName = tip.imageUrl;
        int resId = holder.itemView.getContext().getResources().getIdentifier(imageName, "drawable", holder.itemView.getContext().getPackageName());

        if (resId != 0) {
            Glide.with(holder.itemView.getContext())
                    .load(resId)
                    .into(holder.binding.ivTipImage);
        } else {
            // Fallback for URL or missing image
            Glide.with(holder.itemView.getContext())
                    .load(imageName)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.binding.ivTipImage);
        }

        holder.itemView.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                    .setTitle(tip.title)
                    .setMessage(tip.content)
                    .setPositiveButton("Đã hiểu", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return tips.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemTipBinding binding;
        ViewHolder(ItemTipBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
