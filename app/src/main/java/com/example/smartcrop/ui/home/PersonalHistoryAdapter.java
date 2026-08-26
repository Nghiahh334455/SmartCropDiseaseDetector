package com.example.smartcrop.ui.home;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartcrop.database.HistoryEntity;
import com.example.smartcrop.databinding.ItemDiseaseCommonBinding;
import com.example.smartcrop.ui.history.HistoryActivity;
import com.example.smartcrop.utils.ImageUtils;

import java.util.List;

public class PersonalHistoryAdapter extends RecyclerView.Adapter<PersonalHistoryAdapter.ViewHolder> {

    private final List<HistoryEntity> historyList;

    public PersonalHistoryAdapter(List<HistoryEntity> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDiseaseCommonBinding binding = ItemDiseaseCommonBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryEntity history = historyList.get(position);
        holder.binding.tvCommonDiseaseName.setText(history.diseaseName);
        holder.binding.tvCommonDiseaseCount.setText(String.format("%.1f%%", history.confidence));
        
        // Hiển thị ngày giờ
        holder.binding.tvCommonDiseaseDate.setVisibility(android.view.View.VISIBLE);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm - dd/MM", java.util.Locale.getDefault());
        holder.binding.tvCommonDiseaseDate.setText(sdf.format(new java.util.Date(history.timestamp)));

        if (history.imageBase64 != null && history.imageBase64.length() > 100) {
            byte[] bytes = ImageUtils.base64ToBytes(history.imageBase64);
            if (bytes != null) Glide.with(holder.itemView.getContext()).load(bytes).into(holder.binding.ivCommonDisease);
        } else {
            holder.binding.ivCommonDisease.setImageResource(android.R.drawable.ic_menu_gallery);
        }
        
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), HistoryActivity.class);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemDiseaseCommonBinding binding;
        ViewHolder(ItemDiseaseCommonBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
