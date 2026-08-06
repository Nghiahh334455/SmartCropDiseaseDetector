package com.example.smartcrop.ui.forum;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.smartcrop.databinding.ActivityCreatePostBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreatePostActivity extends AppCompatActivity {

    private ActivityCreatePostBinding binding;
    private Uri selectedImageUri;
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    binding.rlImagePreview.setVisibility(View.VISIBLE);
                    Glide.with(this).load(uri).into(binding.ivPreview);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreatePostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUserInfo();

        binding.toolbarCreatePost.setNavigationOnClickListener(v -> finish());

        binding.btnPickImage.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        binding.btnRemoveImage.setOnClickListener(v -> {
            selectedImageUri = null;
            binding.rlImagePreview.setVisibility(View.GONE);
        });

        binding.btnPost.setOnClickListener(v -> {
            String content = binding.etPostContent.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập nội dung bài viết", Toast.LENGTH_SHORT).show();
            } else {
                uploadPost(content);
            }
        });
    }

    private void setupUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            binding.tvUserName.setText(user.getDisplayName() != null ? user.getDisplayName() : "Người dùng");
            
            String localPath = getSharedPreferences("SmartCropPrefs", MODE_PRIVATE)
                    .getString("profile_image_" + user.getUid(), null);

            if (localPath != null && new File(localPath).exists()) {
                Glide.with(this).load(new File(localPath)).into(binding.ivUserAvatar);
            } else if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).into(binding.ivUserAvatar);
            }
        }
    }

    private void uploadPost(String content) {
        binding.btnPost.setVisibility(View.GONE);
        binding.pbPosting.setVisibility(View.VISIBLE);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user != null ? user.getUid() : "anonymous";
        String userName = user != null && user.getDisplayName() != null ? user.getDisplayName() : "Người dùng Thần Nông AI";
        String userPhotoUrl = (user != null && user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : null;

        if (selectedImageUri != null) {
            String fileName = "forum_" + System.currentTimeMillis() + ".jpg";
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("forum_images/" + fileName);

            try {
                java.io.InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                if (inputStream == null) throw new Exception("Không thể mở ảnh");
                
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                byte[] data = baos.toByteArray();

                storageRef.putBytes(data)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Ensure we wait for the file to be available
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            saveToFirestore(uid, userName, userPhotoUrl, content, uri.toString());
                        }).addOnFailureListener(e -> {
                            binding.btnPost.setVisibility(View.VISIBLE);
                            binding.pbPosting.setVisibility(View.GONE);
                            Toast.makeText(this, "Lỗi lấy link ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        binding.btnPost.setVisibility(View.VISIBLE);
                        binding.pbPosting.setVisibility(View.GONE);
                        Toast.makeText(this, "Lỗi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            } catch (Exception e) {
                binding.btnPost.setVisibility(View.VISIBLE);
                binding.pbPosting.setVisibility(View.GONE);
                Toast.makeText(this, "Lỗi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            saveToFirestore(uid, userName, userPhotoUrl, content, null);
        }
    }

    private void saveToFirestore(String uid, String userName, String userPhotoUrl, String content, String imageUrl) {
        Map<String, Object> post = new HashMap<>();
        post.put("uid", uid);
        post.put("author", userName);
        post.put("userPhotoUrl", userPhotoUrl);
        post.put("question", content);
        post.put("imageUrl", imageUrl);
        post.put("disease", "Chia sẻ từ cộng đồng"); // This will be hidden by ForumAdapter
        post.put("timestamp", System.currentTimeMillis());
        post.put("likes", 0);
        post.put("commentsCount", 0);
        post.put("likedBy", new ArrayList<String>());

        FirebaseFirestore.getInstance().collection("forum_posts")
                .add(post)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Đã đăng bài thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.btnPost.setVisibility(View.VISIBLE);
                    binding.pbPosting.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi lưu bài viết: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
