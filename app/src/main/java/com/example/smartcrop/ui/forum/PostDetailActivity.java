package com.example.smartcrop.ui.forum;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.smartcrop.R;
import com.example.smartcrop.api.ApiService;
import com.example.smartcrop.api.RetrofitClient;
import com.example.smartcrop.databinding.ActivityPostDetailBinding;
import com.example.smartcrop.models.CommentModel;
import com.example.smartcrop.models.NotificationModel;
import com.example.smartcrop.utils.ImageUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostDetailActivity extends AppCompatActivity {

    private ActivityPostDetailBinding binding;
    private List<CommentModel> commentList = new ArrayList<>();
    private List<String> commentIds = new ArrayList<>();
    private CommentAdapter adapter;
    private String postId;
    private String currentReplyParentId = null;
    private Map<String, Object> postData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarPostDetail.setNavigationOnClickListener(v -> finish());

        // Get data from intent
        Object postObj = getIntent().getSerializableExtra("post");
        if (!(postObj instanceof Map)) {
            finish();
            return;
        }
        postData = (Map<String, Object>) postObj;

        Object idObj = postData.get("id");
        if (idObj == null) idObj = getIntent().getStringExtra("postId");
        
        if (idObj != null) {
            String idStr = String.valueOf(idObj);
            try {
                // Sửa lỗi ID dạng 1.0 từ GSON/SQL
                double d = Double.parseDouble(idStr);
                postId = String.valueOf((int) d);
            } catch (Exception e) {
                postId = idStr;
            }
        } else {
            postId = (String) postData.get("originalPostId");
        }

        displayPost(postData);
        setupComments();

        if (postId != null) {
            listenForComments();
        } else {
            binding.tvCommentHeader.setText("Bình luận (Tính năng chỉ dành cho bài đăng gốc)");
            binding.layoutCommentInput.setVisibility(View.GONE);
        }

        binding.btnSendComment.setOnClickListener(v -> sendComment());
        
        loadUserAvatar();
    }

    private void loadUserAvatar() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String localPath = getSharedPreferences("SmartCropPrefs", MODE_PRIVATE)
                    .getString("profile_image_" + user.getUid(), null);

            if (localPath != null && new File(localPath).exists()) {
                Glide.with(this).load(new File(localPath)).into(binding.ivUserAvatar);
            } else if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).into(binding.ivUserAvatar);
            }
        }
    }

    private void setupComments() {
        adapter = new CommentAdapter(commentList, commentIds, new CommentAdapter.OnCommentInteractionListener() {
            @Override
            public void onLike(String commentId, boolean isLike) {
                toggleCommentReaction(commentId, isLike);
            }

            @Override
            public void onReply(CommentModel comment) {
                binding.etComment.setText("@" + comment.authorName + " ");
                binding.etComment.requestFocus();
                currentReplyParentId = String.valueOf(comment.id); // Lưu ID của bình luận cha
                
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(binding.etComment, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        });
        binding.rvComments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvComments.setAdapter(adapter);
    }

    private void toggleCommentReaction(String commentId, boolean active) {
        // SQL Server reaction logic via API
        // For now, let's keep it simple or implement if API ready
    }

    private void listenForComments() {
        if (postId == null || postId.isEmpty() || postId.equals("null")) return;
        
        int pId;
        try {
            double d = Double.parseDouble(postId);
            pId = (int) d;
        } catch (Exception e) {
            try {
                pId = Integer.parseInt(postId);
            } catch (Exception ex) {
                return;
            }
        }
        
        ApiService apiService = RetrofitClient.getSqlService();
        apiService.getComments(pId).enqueue(new retrofit2.Callback<List<CommentModel>>() {
            @Override
            public void onResponse(retrofit2.Call<List<CommentModel>> call, retrofit2.Response<List<CommentModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    commentList.clear();
                    commentIds.clear();
                    
                    List<CommentModel> allComments = response.body();
                    // Sắp xếp bình luận: Cha -> Các con của cha -> Cha tiếp theo
                    for (CommentModel c : allComments) {
                        if (c.parentCommentId == null) {
                            commentList.add(c);
                            commentIds.add(String.valueOf(c.id));
                            // Tìm các con của nó
                            for (CommentModel child : allComments) {
                                if (child.parentCommentId != null && child.parentCommentId.equals(c.id)) {
                                    commentList.add(child);
                                    commentIds.add(String.valueOf(child.id));
                                }
                            }
                        }
                    }
                    
                    adapter.notifyDataSetChanged();
                    binding.tvCommentHeader.setText("Bình luận (" + allComments.size() + ")");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<CommentModel>> call, Throwable t) {
                Toast.makeText(PostDetailActivity.this, "Lỗi tải bình luận từ SQL", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendComment() {
        String content = binding.etComment.getText().toString().trim();
        if (content.isEmpty()) return;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sửa lỗi ID dạng số thực khi gửi sang SQL
        int pId;
        try {
            double d = Double.parseDouble(postId);
            pId = (int) d;
        } catch (Exception e) {
            pId = Integer.parseInt(postId);
        }
        
        String authorPhotoUrl = getSharedPreferences("SmartCropPrefs", MODE_PRIVATE)
                .getString("profile_image_" + user.getUid(), "");
        if (authorPhotoUrl.startsWith("BASE64:")) authorPhotoUrl = authorPhotoUrl.substring(7);
        if (authorPhotoUrl.isEmpty() && user.getPhotoUrl() != null) {
            authorPhotoUrl = user.getPhotoUrl().toString();
        }
        
        final String finalAuthorPhotoUrl = authorPhotoUrl;
        Integer parentId = (currentReplyParentId != null) ? Integer.parseInt(currentReplyParentId) : null;

        ApiService apiService = RetrofitClient.getSqlService();
        apiService.addComment(pId, user.getDisplayName(), user.getUid(), content, finalAuthorPhotoUrl, String.valueOf(parentId))
                .enqueue(new retrofit2.Callback<Map<String, String>>() {
                    @Override
                    public void onResponse(retrofit2.Call<Map<String, String>> call, retrofit2.Response<Map<String, String>> response) {
                        if (response.isSuccessful()) {
                            binding.etComment.setText("");
                            binding.etComment.clearFocus();
                            currentReplyParentId = null;
                            
                            // Ẩn bàn phím
                            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                            if (imm != null) imm.hideSoftInputFromWindow(binding.etComment.getWindowToken(), 0);

                            listenForComments(); // Refresh

                            // Gửi thông báo tới tác giả bài viết
                            if (postData != null) {
                                Object uidObj = postData.get("uid");
                                String question = (String) postData.get("question");
                                if (uidObj != null && !uidObj.equals(user.getUid())) {
                                    sendNotification(String.valueOf(uidObj), "COMMENT", postId, question != null ? question : "bài viết");
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<Map<String, String>> call, Throwable t) {
                        Toast.makeText(PostDetailActivity.this, "Lỗi gửi bình luận", Toast.LENGTH_SHORT).show();
                    }
                });
        
        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(binding.etComment.getWindowToken(), 0);
        }
    }

    private void sendNotification(String targetUid, String type, String postId, String postContent) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String senderName = user.getDisplayName() != null ? user.getDisplayName() : "Một người dùng";
        String senderAvatar = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";

        int pId;
        try {
            double d = Double.parseDouble(postId);
            pId = (int) d;
        } catch (Exception e) {
            try {
                pId = Integer.parseInt(postId);
            } catch (Exception ex) {
                return;
            }
        }

        ApiService apiService = RetrofitClient.getSqlService();
        apiService.sendNotification(targetUid, senderName, senderAvatar, user.getUid(), type, pId, postContent)
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

    private void displayPost(Map<String, Object> post) {
        binding.postItem.tvPostAuthor.setText(String.valueOf(post.get("author")));
        binding.postItem.tvPostQuestion.setText(String.valueOf(post.get("question")));
        
        String diseaseName = (String) post.get("disease");
        if (diseaseName == null || diseaseName.isEmpty() || diseaseName.equals("Chia sẻ từ cộng đồng")) {
            binding.postItem.chipDisease.setVisibility(View.GONE);
        } else {
            binding.postItem.chipDisease.setVisibility(View.VISIBLE);
            binding.postItem.chipDisease.setText(diseaseName);
        }
        
        long timestamp = 0;
        try {
            Object ts = post.get("timestamp");
            if (ts instanceof Double) timestamp = ((Double) ts).longValue();
            else if (ts instanceof Long) timestamp = (Long) ts;
        } catch (Exception ignored) {}
        
        binding.postItem.tvPostTime.setText(DateUtils.getRelativeTimeSpanString(timestamp));

        int likes = 0;
        try {
            Object lCount = post.get("likesCount");
            likes = (lCount instanceof Double) ? ((Double) lCount).intValue() : (int) lCount;
        } catch (Exception ignored) {}
        
        int comments = 0;
        try {
            Object cCount = post.get("commentsCount");
            comments = (cCount instanceof Double) ? ((Double) cCount).intValue() : (int) cCount;
        } catch (Exception ignored) {}
        
        binding.postItem.tvLikeCount.setText(likes + " lượt thích");
        binding.postItem.tvCommentCount.setText(comments + " bình luận");

        String currentUid = FirebaseAuth.getInstance().getUid();
        boolean isLikedByMe = false;
        try {
            List<String> likedBy = (List<String>) post.get("likedBy");
            if (likedBy != null) isLikedByMe = likedBy.contains(currentUid);
        } catch (Exception ignored) {}

        if (isLikedByMe) {
            binding.postItem.btnLike.setIconResource(android.R.drawable.btn_star_big_on);
            binding.postItem.btnLike.setText("Đã thích");
            binding.postItem.btnLike.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.primary));
        } else {
            binding.postItem.btnLike.setIconResource(android.R.drawable.btn_star_big_off);
            binding.postItem.btnLike.setText("Thích");
            binding.postItem.btnLike.setTextColor(android.graphics.Color.GRAY);
        }

        // Avatar logic
        String userPhotoUrl = (String) post.get("userPhotoUrl");

        if (userPhotoUrl != null && userPhotoUrl.length() > 500) {
            byte[] bytes = ImageUtils.base64ToBytes(userPhotoUrl);
            if (bytes != null) Glide.with(this).load(bytes).placeholder(android.R.drawable.ic_menu_gallery).into(binding.postItem.ivPostAvatar);
        } else {
            Glide.with(this).load(userPhotoUrl).placeholder(android.R.drawable.ic_menu_gallery).into(binding.postItem.ivPostAvatar);
        }

        String imageUrl = (String) post.get("imageUrl");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            binding.postItem.ivPostImage.setVisibility(View.VISIBLE);
            if (imageUrl.length() > 500) {
                byte[] bytes = ImageUtils.base64ToBytes(imageUrl);
                if (bytes != null) Glide.with(this).load(bytes).into(binding.postItem.ivPostImage);
            } else if (imageUrl.startsWith("http")) {
                Glide.with(this).load(imageUrl).into(binding.postItem.ivPostImage);
            } else {
                int resId = getResources().getIdentifier(imageUrl, "drawable", getPackageName());
                if (resId != 0) Glide.with(this).load(resId).into(binding.postItem.ivPostImage);
            }
        } else {
            binding.postItem.ivPostImage.setVisibility(View.GONE);
        }
        
        binding.postItem.ivPostMenu.setVisibility(View.GONE);
        binding.postItem.btnShare.setOnClickListener(v -> sharePost(post));
    }

    private void sharePost(Map<String, Object> post) {
        String content = (String) post.get("question");
        String author = (String) post.get("author");
        android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Bài viết từ " + author + " trên Thần Nông AI: " + content);
        startActivity(android.content.Intent.createChooser(shareIntent, "Chia sẻ bài viết"));
    }
}
