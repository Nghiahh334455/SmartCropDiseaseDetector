package com.example.smartcrop.ui.home;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartcrop.databinding.ItemDiseaseCommonBinding;
import com.example.smartcrop.models.DiseaseModel;
import com.example.smartcrop.ui.library.DiseaseDetailActivity;
import com.example.smartcrop.utils.DiseaseProvider;

import java.util.List;
import java.util.Map;

public class CommonDiseaseAdapter extends RecyclerView.Adapter<CommonDiseaseAdapter.ViewHolder> {

    private final List<Map<String, Object>> diseaseStats;

    public CommonDiseaseAdapter(List<Map<String, Object>> diseaseStats) {
        this.diseaseStats = diseaseStats;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDiseaseCommonBinding binding = ItemDiseaseCommonBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> stat = diseaseStats.get(position);
        String name = (String) stat.get("name");
        long count = (long) stat.get("count");

        holder.binding.tvCommonDiseaseName.setText(name);
        holder.binding.tvCommonDiseaseCount.setText(count + " lượt chẩn đoán");
        
        // Load image from Provider
        DiseaseModel diseaseInfo = DiseaseProvider.getDiseaseByName(name);
        if (diseaseInfo != null) {
            if (diseaseInfo.imageResources != null && !diseaseInfo.imageResources.isEmpty()) {
                String thumbnailName = diseaseInfo.imageResources.get(0);
                int resId = holder.itemView.getContext().getResources().getIdentifier(thumbnailName, "drawable", holder.itemView.getContext().getPackageName());
                if (resId != 0) {
                    Glide.with(holder.itemView.getContext()).load(resId).into(holder.binding.ivCommonDisease);
                } else {
                    Glide.with(holder.itemView.getContext()).load(android.R.drawable.ic_menu_gallery).into(holder.binding.ivCommonDisease);
                }
            }
            
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), DiseaseDetailActivity.class);
                intent.putExtra("name", diseaseInfo.name);
                v.getContext().startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return diseaseStats.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemDiseaseCommonBinding binding;
        ViewHolder(ItemDiseaseCommonBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
