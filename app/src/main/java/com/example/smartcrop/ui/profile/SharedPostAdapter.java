package com.example.smartcrop.ui.profile;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartcrop.databinding.ItemPostBinding;
import com.example.smartcrop.ui.forum.PostDetailActivity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SharedPostAdapter extends RecyclerView.Adapter<SharedPostAdapter.ViewHolder> {

    private final List<Map<String, Object>> sharedPosts;
    private final List<String> docIds;
    private OnDeleteClickListener deleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position, String docId);
    }

    public SharedPostAdapter(List<Map<String, Object>> sharedPosts, List<String> docIds, OnDeleteClickListener listener) {
        this.sharedPosts = sharedPosts;
        this.docIds = docIds;
        this.deleteClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPostBinding binding = ItemPostBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> post = sharedPosts.get(position);

        String originalAuthor = (String) post.get("author");
        holder.binding.tvPostAuthor.setText("Bạn đã chia sẻ từ " + originalAuthor);
        holder.binding.tvPostQuestion.setText((String) post.get("question"));
        holder.binding.chipDisease.setText((String) post.get("disease"));

        String imageUrl = (String) post.get("imageUrl");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            holder.binding.ivPostImage.setVisibility(android.view.View.VISIBLE);
            if (imageUrl.startsWith("http")) {
                Glide.with(holder.itemView.getContext()).load(imageUrl).into(holder.binding.ivPostImage);
            } else {
                int resId = holder.itemView.getContext().getResources().getIdentifier(imageUrl, "drawable", holder.itemView.getContext().getPackageName());
                if (resId != 0) {
                    Glide.with(holder.itemView.getContext()).load(resId).into(holder.binding.ivPostImage);
                }
            }
        } else {
            holder.binding.ivPostImage.setVisibility(android.view.View.GONE);
        }

        // Hide interaction buttons for profile view
        holder.binding.btnLike.setVisibility(android.view.View.GONE);
        holder.binding.btnComment.setVisibility(android.view.View.GONE);
        holder.binding.btnShare.setVisibility(android.view.View.GONE);
        
        // Show menu for deletion
        holder.binding.ivPostMenu.setVisibility(android.view.View.VISIBLE);
        holder.binding.ivPostMenu.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(position, docIds.get(position));
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), PostDetailActivity.class);
            // We need to pass the post as a Serializable HashMap
            intent.putExtra("post", (Serializable) new HashMap<>(post));
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return sharedPosts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemPostBinding binding;
        ViewHolder(ItemPostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
