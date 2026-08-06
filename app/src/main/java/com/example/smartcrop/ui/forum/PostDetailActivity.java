package com.example.smartcrop.ui.forum;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.smartcrop.databinding.ActivityPostDetailBinding;
import com.example.smartcrop.models.CommentModel;
import com.example.smartcrop.models.NotificationModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PostDetailActivity extends AppCompatActivity {

    private ActivityPostDetailBinding binding;
    private List<CommentModel> commentList = new ArrayList<>();
    private List<String> commentIds = new ArrayList<>();
    private CommentAdapter adapter;
    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarPostDetail.setNavigationOnClickListener(v -> finish());

        // Get data from intent
        Map<String, Object> post = (Map<String, Object>) getIntent().getSerializableExtra("post");
        if (post == null) {
            finish();
            return;
        }

        // We need postId to fetch comments
        // If shared from library, we might not have a real postId yet.
        // For simplicity, let's assume we pass it or use a fallback.
        postId = (String) post.get("originalPostId");
        if (postId == null) {
            // Find postId if passed directly
            postId = getIntent().getStringExtra("postId");
        }

        displayPost(post);
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
                toggleCommentReaction(commentId, "likedBy", isLike);
            }

            @Override
            public void onSad(String commentId, boolean isSad) {
                toggleCommentReaction(commentId, "sadBy", isSad);
            }

            @Override
            public void onReply(CommentModel comment) {
                binding.etComment.setText("@" + comment.authorName + " ");
                binding.etComment.requestFocus();
                // Show keyboard
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(binding.etComment, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        });
        binding.rvComments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvComments.setAdapter(adapter);
    }

    private void toggleCommentReaction(String commentId, String field, boolean active) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        com.google.firebase.firestore.DocumentReference ref = FirebaseFirestore.getInstance().collection("forum_posts")
                .document(postId)
                .collection("comments")
                .document(commentId);

        if (active) {
            ref.update(field + "." + uid, true);
            // If liking, remove sad and vice-versa
            if (field.equals("likedBy")) ref.update("sadBy." + uid, FieldValue.delete());
            else ref.update("likedBy." + uid, FieldValue.delete());
        } else {
            ref.update(field + "." + uid, FieldValue.delete());
        }
    }

    private void listenForComments() {
        FirebaseFirestore.getInstance().collection("forum_posts")
                .document(postId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    commentList.clear();
                    commentIds.clear();
                    for (DocumentSnapshot doc : value) {
                        commentList.add(doc.toObject(CommentModel.class));
                        commentIds.add(doc.getId());
                    }
                    adapter.notifyDataSetChanged();
                    binding.tvCommentHeader.setText("Bình luận (" + commentList.size() + ")");
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

        CommentModel comment = new CommentModel(
                user.getDisplayName() != null ? user.getDisplayName() : "Người dùng",
                content,
                System.currentTimeMillis(),
                user.getUid(),
                user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null
        );

        binding.etComment.setText("");
        binding.etComment.clearFocus();
        
        // Hide keyboard
        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(binding.etComment.getWindowToken(), 0);
        }

        FirebaseFirestore.getInstance().collection("forum_posts")
                .document(postId)
                .collection("comments")
                .add(comment)
                .addOnSuccessListener(doc -> {
                    // Update comment count on post
                    FirebaseFirestore.getInstance().collection("forum_posts")
                            .document(postId)
                            .update("commentsCount", FieldValue.increment(1));
                    
                    // Send notification
                    FirebaseFirestore.getInstance().collection("forum_posts").document(postId).get()
                            .addOnSuccessListener(postDoc -> {
                                String ownerUid = postDoc.getString("uid");
                                String pContent = postDoc.getString("question");
                                if (ownerUid != null && !ownerUid.equals(user.getUid())) {
                                    sendNotification(ownerUid, "COMMENT", postId, pContent);
                                }
                            });
                });
    }

    private void sendNotification(String targetUid, String type, String postId, String postContent) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String senderName = user.getDisplayName() != null ? user.getDisplayName() : "Một người dùng";
        String senderAvatar = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";

        NotificationModel notification = new NotificationModel(
                "", type, senderName, senderAvatar, user.getUid(), postId, postContent, System.currentTimeMillis(), false
        );

        FirebaseFirestore.getInstance().collection("users")
                .document(targetUid)
                .collection("notifications")
                .add(notification);
    }

    private void displayPost(Map<String, Object> post) {
        binding.postItem.tvPostAuthor.setText((String) post.get("author"));
        binding.postItem.tvPostQuestion.setText((String) post.get("question"));
        
        String diseaseName = (String) post.get("disease");
        if (diseaseName == null || diseaseName.isEmpty() || diseaseName.equals("Chia sẻ từ cộng đồng")) {
            binding.postItem.chipDisease.setVisibility(View.GONE);
        } else {
            binding.postItem.chipDisease.setVisibility(View.VISIBLE);
            binding.postItem.chipDisease.setText(diseaseName);
        }
        
        long timestamp = (long) post.get("timestamp");
        binding.postItem.tvPostTime.setText(DateUtils.getRelativeTimeSpanString(timestamp));

        List<String> likedBy = (List<String>) post.get("likedBy");
        if (likedBy == null) likedBy = new ArrayList<>();
        
        binding.postItem.tvLikeCount.setText(likedBy.size() + " lượt thích");
        binding.postItem.tvCommentCount.setText(post.get("commentsCount") + " bình luận");

        String currentUid = FirebaseAuth.getInstance().getUid();
        boolean isLiked = likedBy.contains(currentUid);
        if (isLiked) {
            binding.postItem.btnLike.setIconResource(android.R.drawable.btn_star_big_on);
            binding.postItem.btnLike.setText("Đã thích");
        }

        // Avatar logic
        String userPhotoUrl = (String) post.get("userPhotoUrl");
        String uid = (String) post.get("uid");

        if (currentUid != null && currentUid.equals(uid)) {
            String localPath = getSharedPreferences("SmartCropPrefs", MODE_PRIVATE)
                    .getString("profile_image_" + currentUid, null);
            if (localPath != null && new File(localPath).exists()) {
                Glide.with(this).load(new File(localPath)).into(binding.postItem.ivPostAvatar);
            } else {
                Glide.with(this).load(userPhotoUrl).placeholder(android.R.drawable.ic_menu_gallery).into(binding.postItem.ivPostAvatar);
            }
        } else {
            Glide.with(this).load(userPhotoUrl).placeholder(android.R.drawable.ic_menu_gallery).into(binding.postItem.ivPostAvatar);
        }

        String imageUrl = (String) post.get("imageUrl");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            binding.postItem.ivPostImage.setVisibility(android.view.View.VISIBLE);
            if (imageUrl.startsWith("http")) {
                Glide.with(this).load(imageUrl).into(binding.postItem.ivPostImage);
            } else {
                int resId = getResources().getIdentifier(imageUrl, "drawable", getPackageName());
                if (resId != 0) Glide.with(this).load(resId).into(binding.postItem.ivPostImage);
            }
        } else {
            binding.postItem.ivPostImage.setVisibility(android.view.View.GONE);
        }
        
        binding.postItem.ivPostMenu.setVisibility(android.view.View.GONE);
        
        // Share logic
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
