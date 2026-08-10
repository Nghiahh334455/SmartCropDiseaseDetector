package com.example.smartcrop.ui.history;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartcrop.database.HistoryEntity;
import com.example.smartcrop.databinding.ItemHistoryBinding;

import com.example.smartcrop.utils.ImageUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final List<HistoryEntity> historyList;

    public HistoryAdapter(List<HistoryEntity> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHistoryBinding binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryEntity history = historyList.get(position);
        holder.binding.tvHistoryDisease.setText(history.diseaseName);
        holder.binding.tvHistoryConfidence.setText(String.format(Locale.getDefault(), "Độ chính xác: %.1f%%", history.confidence));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.binding.tvHistoryDate.setText(sdf.format(new Date(history.timestamp)));

        String imgBase64 = history.imageBase64;
        if (imgBase64 != null && imgBase64.length() > 100) {
            byte[] bytes = ImageUtils.base64ToBytes(imgBase64);
            if (bytes != null) {
                Glide.with(holder.itemView.getContext())
                        .load(bytes)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(holder.binding.ivHistoryImage);
            }
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(android.R.drawable.ic_menu_gallery)
                    .into(holder.binding.ivHistoryImage);
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemHistoryBinding binding;
        ViewHolder(ItemHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
