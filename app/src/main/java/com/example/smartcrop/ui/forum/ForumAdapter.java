package com.example.smartcrop.ui.forum;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Base64;

import com.bumptech.glide.Glide;
import com.example.smartcrop.R;
import com.example.smartcrop.api.ApiService;
import com.example.smartcrop.api.RetrofitClient;
import com.example.smartcrop.databinding.ItemPostBinding;
import com.example.smartcrop.models.DiseaseModel;
import com.example.smartcrop.ui.library.DiseaseDetailActivity;
import com.example.smartcrop.utils.DiseaseProvider;
import com.example.smartcrop.models.NotificationModel;
import com.example.smartcrop.utils.ImageUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForumAdapter extends RecyclerView.Adapter<ForumAdapter.ViewHolder> {

    private final List<Map<String, Object>> postList;
    private final List<String> postIds;
    private final Context context;
    private final String currentUid;
    private final java.util.Set<String> locallyLikedPosts = new java.util.HashSet<>();

    public ForumAdapter(Context context, List<Map<String, Object>> postList, List<String> postIds) {
        this.context = context;
        this.postList = postList;
        this.postIds = postIds;
        this.currentUid = FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPostBinding binding = ItemPostBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> post = postList.get(position);
        String postId = postIds.get(position);

        String author = (String) post.get("author");
        String question = (String) post.get("question");
        String diseaseName = (String) post.get("disease");
        
        // SQL timestamp is a bit different
        long timestamp = 0;
        Object ts = post.get("timestamp");
        if (ts instanceof Double) timestamp = ((Double) ts).longValue();
        else if (ts instanceof Long) timestamp = (Long) ts;

        holder.binding.tvPostAuthor.setText(author);
        holder.binding.tvPostQuestion.setText(question);
        holder.binding.tvPostTime.setText(DateUtils.getRelativeTimeSpanString(timestamp));

        // Disease Chip logic: Hide if it's a general post
        if (diseaseName == null || diseaseName.isEmpty() || diseaseName.equals("Chia sẻ từ cộng đồng")) {
            holder.binding.chipDisease.setVisibility(android.view.View.GONE);
        } else {
            holder.binding.chipDisease.setVisibility(android.view.View.VISIBLE);
            holder.binding.chipDisease.setText(diseaseName);
        }

        Object lCount = post.get("likesCount");
        int likes = (lCount instanceof Double) ? ((Double) lCount).intValue() : (int) ((lCount instanceof Integer) ? lCount : 0);
        
        Object cCount = post.get("commentsCount");
        int comments = (cCount instanceof Double) ? ((Double) cCount).intValue() : (int) ((cCount instanceof Integer) ? cCount : 0);

        holder.binding.tvLikeCount.setText(likes + " lượt thích");
        holder.binding.tvCommentCount.setText(comments + " bình luận");

        // Like logic: Kết hợp dữ liệu từ Server và trạng thái Click tạm thời
        List<String> likedBy = (List<String>) post.get("likedBy");
        boolean isLikedByMe = (likedBy != null && likedBy.contains(currentUid)) || locallyLikedPosts.contains(postId);
        
        if (isLikedByMe) {
            holder.binding.btnLike.setIconResource(android.R.drawable.btn_star_big_on);
            holder.binding.btnLike.setText("Đã thích");
            int primaryColor = androidx.core.content.ContextCompat.getColor(this.context, R.color.primary);
            android.content.res.ColorStateList colorStateList = android.content.res.ColorStateList.valueOf(primaryColor);
            holder.binding.btnLike.setIconTint(colorStateList);
            holder.binding.btnLike.setTextColor(primaryColor);
            holder.binding.btnLike.setRippleColor(colorStateList);
        } else {
            holder.binding.btnLike.setIconResource(android.R.drawable.btn_star_big_off);
            holder.binding.btnLike.setText("Thích");
            holder.binding.btnLike.setIconTint(android.content.res.ColorStateList.valueOf(Color.GRAY));
            holder.binding.btnLike.setTextColor(Color.GRAY);
        }

        holder.binding.btnLike.setOnClickListener(v -> toggleLike(postId, position));

        // Avatar logic: Ưu tiên lấy ảnh đại diện mới nhất nếu là bài viết của chính mình
        String ownerUid = (String) post.get("uid");
        String userPhotoUrl = (String) post.get("userPhotoUrl");
        
        if (currentUid != null && currentUid.equals(ownerUid)) {
            // Kiểm tra SharedPreferences xem có ảnh Base64 mới nhất không
            String localPhoto = context.getSharedPreferences("SmartCropPrefs", Context.MODE_PRIVATE)
                    .getString("profile_image_" + currentUid, null);
            if (localPhoto != null && localPhoto.startsWith("BASE64:")) {
                userPhotoUrl = localPhoto;
            }
        }

        if (userPhotoUrl != null && !userPhotoUrl.isEmpty()) {
            if (userPhotoUrl.startsWith("BASE64:") || userPhotoUrl.length() > 500) {
                byte[] imageBytes = ImageUtils.base64ToBytes(userPhotoUrl);
                if (imageBytes != null) {
                    Glide.with(context).load(imageBytes)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .circleCrop()
                            .into(holder.binding.ivPostAvatar);
                }
            } else {
                Glide.with(context).load(userPhotoUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .circleCrop()
                        .into(holder.binding.ivPostAvatar);
            }
        } else {
            Glide.with(context).load(android.R.drawable.ic_menu_gallery)
                    .circleCrop()
                    .into(holder.binding.ivPostAvatar);
        }

        String imageUrl = (String) post.get("imageUrl");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            holder.binding.ivPostImage.setVisibility(android.view.View.VISIBLE);
            if (imageUrl.length() > 500) { // Base64
                byte[] imageBytes = ImageUtils.base64ToBytes(imageUrl);
                if (imageBytes != null) Glide.with(context).load(imageBytes).placeholder(android.R.drawable.ic_menu_gallery).into(holder.binding.ivPostImage);
            } else if (imageUrl.startsWith("http")) {
                Glide.with(context).load(imageUrl).placeholder(android.R.drawable.ic_menu_gallery).into(holder.binding.ivPostImage);
            } else {
                // It's a resource name
                int resId = context.getResources().getIdentifier(imageUrl, "drawable", context.getPackageName());
                if (resId != 0) {
                    Glide.with(context).load(resId).placeholder(android.R.drawable.ic_menu_gallery).into(holder.binding.ivPostImage);
                } else {
                    holder.binding.ivPostImage.setVisibility(android.view.View.GONE);
                }
            }
        } else {
            holder.binding.ivPostImage.setVisibility(android.view.View.GONE);
        }

        // Click listeners
        holder.binding.btnShare.setOnClickListener(v -> sharePost(post, postId));
        
        holder.binding.ivPostMenu.setOnClickListener(v -> showMenu(v, post, postId, position));

        // Quick View Disease
        holder.binding.chipDisease.setOnClickListener(v -> viewDiseaseDetail(diseaseName));
        holder.binding.ivPostImage.setOnClickListener(v -> viewDiseaseDetail(diseaseName));

        holder.binding.btnComment.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("post", (Serializable) new HashMap<>(post));
            intent.putExtra("postId", postId);
            context.startActivity(intent);
        });
        
        // Open Detail on item click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("post", (Serializable) new HashMap<>(post));
            intent.putExtra("postId", postId);
            context.startActivity(intent);
        });
    }

    private void viewDiseaseDetail(String diseaseName) {
        DiseaseModel disease = DiseaseProvider.getDiseaseByName(diseaseName);
        if (disease != null) {
            Intent intent = new Intent(context, DiseaseDetailActivity.class);
            intent.putExtra("name", disease.name);
            intent.putExtra("description", disease.description);
            if (disease.imageResources != null && !disease.imageResources.isEmpty()) {
                intent.putExtra("image", disease.imageResources.get(0));
            }
            intent.putExtra("treatment", disease.treatment);
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "Không tìm thấy thông tin bệnh này", Toast.LENGTH_SHORT).show();
        }
    }

    private void showMenu(android.view.View view, Map<String, Object> post, String postId, int position) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenu().add("Lưu bài viết");
        popup.getMenu().add("Ẩn bài viết");

        // Nếu là chủ bài viết, hiện nút Xóa
        if (currentUid != null && currentUid.equals(post.get("uid"))) {
            popup.getMenu().add("Xóa bài viết");
        }
        
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Lưu bài viết")) {
                savePost(post, postId);
            } else if (item.getTitle().equals("Ẩn bài viết")) {
                postList.remove(position);
                postIds.remove(position);
                notifyItemRemoved(position);
            } else if (item.getTitle().equals("Xóa bài viết")) {
                new androidx.appcompat.app.AlertDialog.Builder(context)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa bài viết này không?")
                        .setPositiveButton("Xóa", (dialog, which) -> deletePost(postId))
                        .setNegativeButton("Hủy", null)
                        .show();
            }
            return true;
        });
        popup.show();
    }

    private void deletePost(String postId) {
        FirebaseFirestore.getInstance().collection("forum_posts")
                .document(postId)
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Đã xóa bài viết", Toast.LENGTH_SHORT).show());
    }

    private void savePost(Map<String, Object> post, String postId) {
        if (currentUid == null) return;
        Map<String, Object> saveData = new HashMap<>(post);
        saveData.put("savedByUid", currentUid);
        saveData.put("originalPostId", postId);

        FirebaseFirestore.getInstance().collection("user_saved_posts")
                .add(saveData)
                .addOnSuccessListener(doc -> Toast.makeText(context, "Đã lưu bài viết!", Toast.LENGTH_SHORT).show());
    }

    private void toggleLike(String postId, int position) {
        if (currentUid == null) return;
        
        Map<String, Object> post = postList.get(position);
        
        // Sửa lỗi Parse ID từ SQL/GSON
        int tempId;
        try {
            double d = Double.parseDouble(postId);
            tempId = (int) d;
        } catch (Exception e) {
            tempId = Integer.parseInt(postId);
        }
        final int pId = tempId;

        // Optimistic update: Cập nhật UI ngay lập tức
        if (locallyLikedPosts.contains(postId)) {
            locallyLikedPosts.remove(postId);
        } else {
            locallyLikedPosts.add(postId);
        }
        notifyItemChanged(position);

        ApiService apiService = RetrofitClient.getSqlService();
        apiService.toggleLike(currentUid, pId).enqueue(new retrofit2.Callback<Map<String, String>>() {
            @Override
            public void onResponse(retrofit2.Call<Map<String, String>> call, retrofit2.Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String action = response.body().get("action");
                    if ("liked".equals(action)) {
                        String ownerUid = (String) post.get("uid");
                        String question = (String) post.get("question");
                        if (ownerUid != null && !ownerUid.equals(currentUid)) {
                            sendNotification(ownerUid, "LIKE", pId, question != null ? question : "bài viết");
                        }
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Map<String, String>> call, Throwable t) {
                // Rollback on failure
                if (locallyLikedPosts.contains(postId)) locallyLikedPosts.remove(postId);
                else locallyLikedPosts.add(postId);
                notifyItemChanged(position);
                Toast.makeText(context, "Lỗi đồng bộ lượt thích", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sharePost(Map<String, Object> post, String postId) {
        String content = (String) post.get("question");
        String author = (String) post.get("author");
        String ownerUid = (String) post.get("uid");
        
        int pId;
        try {
            pId = (int) Double.parseDouble(postId);
        } catch (Exception e) {
            pId = Integer.parseInt(postId);
        }

        // Save to my profile
        if (currentUid != null) {
            Map<String, Object> shareData = new HashMap<>(post);
            shareData.put("sharedByUid", currentUid);
            shareData.put("originalPostId", String.valueOf(pId));
            shareData.put("shareTimestamp", System.currentTimeMillis());
            FirebaseFirestore.getInstance().collection("user_shares").add(shareData);

            if (ownerUid != null && !ownerUid.equals(currentUid)) {
                sendNotification(ownerUid, "SHARE", pId, content);
            }
        }

        // External Android Share
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Bài viết từ " + author + " trên Thần Nông AI: " + content);
        context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ bài viết"));
    }

    private void sendNotification(String targetUid, String type, int postId, String postContent) {
        com.google.firebase.auth.FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String senderName = user.getDisplayName() != null ? user.getDisplayName() : "Một người dùng";
        
        // Fix: Lấy ảnh Base64 từ SharedPreferences để đồng bộ đúng avatar trong thông báo
        String senderAvatar = context.getSharedPreferences("SmartCropPrefs", Context.MODE_PRIVATE)
                .getString("profile_image_" + user.getUid(), "");
        if (senderAvatar.isEmpty() && user.getPhotoUrl() != null) {
            senderAvatar = user.getPhotoUrl().toString();
        }

        ApiService apiService = RetrofitClient.getSqlService();
        apiService.sendNotification(targetUid, senderName, senderAvatar, user.getUid(), type, postId, postContent)
                .enqueue(new retrofit2.Callback<Map<String, String>>() {
                    @Override
                    public void onResponse(retrofit2.Call<Map<String, String>> call, retrofit2.Response<Map<String, String>> response) {
                        // Success
                    }

                    @Override
                    public void onFailure(retrofit2.Call<Map<String, String>> call, Throwable t) {
                        // Log
                    }
                });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemPostBinding binding;
        ViewHolder(ItemPostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
