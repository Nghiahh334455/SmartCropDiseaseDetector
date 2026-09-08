package com.example.smartcrop.ui.forum;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.smartcrop.api.ApiService;
import com.example.smartcrop.api.RetrofitClient;
import com.example.smartcrop.databinding.ActivityCreatePostBinding;
import com.example.smartcrop.utils.FirebaseUtils;
import com.example.smartcrop.utils.ImageUtils;
import com.google.firebase.auth.FirebaseUser;

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
            // Sửa logic: Chỉ cần có nội dung HOẶC có ảnh là được phép đăng
            if (content.isEmpty() && selectedImageUri == null) {
                Toast.makeText(this, "Vui lòng nhập nội dung hoặc chọn một tấm ảnh", Toast.LENGTH_SHORT).show();
            } else {
                uploadPost(content);
            }
        });
    }

    private void setupUserInfo() {
        FirebaseUser user = FirebaseUtils.getCurrentUser();
        if (user != null) {
            binding.tvUserName.setText(user.getDisplayName() != null ? user.getDisplayName() : "Người dùng");
            
            String localPhoto = getSharedPreferences("SmartCropPrefs", MODE_PRIVATE)
                    .getString("profile_image_" + user.getUid(), null);

            if (localPhoto != null) {
                if (localPhoto.startsWith("BASE64:")) {
                    byte[] bytes = ImageUtils.base64ToBytes(localPhoto);
                    if (bytes != null) Glide.with(this).load(bytes).circleCrop().into(binding.ivUserAvatar);
                } else if (new File(localPhoto).exists()) {
                    Glide.with(this).load(new File(localPhoto)).circleCrop().into(binding.ivUserAvatar);
                }
            } else if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).circleCrop().into(binding.ivUserAvatar);
            }
        }
    }

    private void uploadPost(String content) {
        binding.btnPost.setVisibility(View.GONE);
        binding.pbPosting.setVisibility(View.VISIBLE);

        FirebaseUser user = FirebaseUtils.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        String userName = (user.getDisplayName() != null) ? user.getDisplayName() : "Người dùng Thần Nông AI";
        
        // Fix: Lấy ảnh Base64 từ SharedPreferences để đồng bộ đúng avatar
        String userPhotoUrl = getSharedPreferences("SmartCropPrefs", MODE_PRIVATE)
                .getString("profile_image_" + uid, null);
        if (userPhotoUrl == null && user.getPhotoUrl() != null) {
            userPhotoUrl = user.getPhotoUrl().toString();
        }

        if (selectedImageUri != null) {
            try {
                // Chuyển ảnh sang Base64 để lưu trực tiếp vào SQL Server
                String base64Image = ImageUtils.uriToBase64(this, selectedImageUri);
                if (!base64Image.isEmpty()) {
                    saveToSQL(uid, userName, userPhotoUrl, content, base64Image);
                } else {
                    throw new Exception("Không thể mã hóa ảnh");
                }
            } catch (Exception e) {
                binding.btnPost.setVisibility(View.VISIBLE);
                binding.pbPosting.setVisibility(View.GONE);
                Toast.makeText(this, "Lỗi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            saveToSQL(uid, userName, userPhotoUrl, content, "");
        }
    }

    private void saveToSQL(String uid, String userName, String userPhotoUrl, String content, String imageUrl) {
        ApiService apiService = RetrofitClient.getSqlService();
        apiService.createPost(uid, userName, content, userPhotoUrl, imageUrl, "Chia sẻ từ cộng đồng")
                .enqueue(new retrofit2.Callback<Map<String, String>>() {
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<Map<String, String>> call, @NonNull retrofit2.Response<Map<String, String>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(CreatePostActivity.this, "Đã đăng bài thành công!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            binding.btnPost.setVisibility(View.VISIBLE);
                            binding.pbPosting.setVisibility(View.GONE);
                            Toast.makeText(CreatePostActivity.this, "Lỗi Server (422/500): " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull retrofit2.Call<Map<String, String>> call, @NonNull Throwable t) {
                        binding.btnPost.setVisibility(View.VISIBLE);
                        binding.pbPosting.setVisibility(View.GONE);
                        Toast.makeText(CreatePostActivity.this, "Lỗi kết nối SQL Server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
