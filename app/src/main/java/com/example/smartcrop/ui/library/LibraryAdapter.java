package com.example.smartcrop.ui.library;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartcrop.databinding.ItemDiseaseBinding;
import com.example.smartcrop.models.DiseaseModel;

import java.util.ArrayList;
import java.util.List;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.ViewHolder> implements Filterable {

    private final List<DiseaseModel> diseaseList;
    private final List<DiseaseModel> diseaseListFull;
    private OnItemLongClickListener longClickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(DiseaseModel disease, int position);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public LibraryAdapter(List<DiseaseModel> diseaseList) {
        this.diseaseList = diseaseList;
        this.diseaseListFull = new ArrayList<>(diseaseList);
    }

    public void updateList(List<DiseaseModel> newList) {
        this.diseaseList.clear();
        this.diseaseList.addAll(newList);
        this.diseaseListFull.clear();
        this.diseaseListFull.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDiseaseBinding binding = ItemDiseaseBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DiseaseModel disease = diseaseList.get(position);
        holder.binding.tvDiseaseTitle.setText(disease.name);
        holder.binding.tvDiseaseShortDesc.setText(disease.description);

        // Load thumbnail (ảnh đầu tiên trong list nội bộ)
        if (disease.imageResources != null && !disease.imageResources.isEmpty()) {
            String thumbnailName = disease.imageResources.get(0);
            int resId = holder.itemView.getContext().getResources().getIdentifier(thumbnailName, "drawable", holder.itemView.getContext().getPackageName());
            if (resId != 0) {
                Glide.with(holder.itemView.getContext())
                        .load(resId)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(holder.binding.ivDiseaseThumbnail);
            } else {
                Glide.with(holder.itemView.getContext())
                        .load(android.R.drawable.ic_menu_gallery)
                        .into(holder.binding.ivDiseaseThumbnail);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DiseaseDetailActivity.class);
            intent.putExtra("name", disease.name);
            v.getContext().startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(disease, position);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return diseaseList.size();
    }

    @Override
    public Filter getFilter() {
        return diseaseFilter;
    }

    private final Filter diseaseFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<DiseaseModel> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(diseaseListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (DiseaseModel item : diseaseListFull) {
                    if (item.name.toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            diseaseList.clear();
            diseaseList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemDiseaseBinding binding;
        ViewHolder(ItemDiseaseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
