package com.example.smartcrop.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.smartcrop.databinding.ActivityEditProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    binding.ivEditProfile.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            finish();
            return;
        }

        // Load current data
        binding.etEditName.setText(currentUser.getDisplayName());
        
        // 1. Kiểm tra ảnh cục bộ
        String localPath = getSharedPreferences("SmartCropPrefs", MODE_PRIVATE)
                .getString("profile_image_" + currentUser.getUid(), null);
        
        if (localPath != null && new File(localPath).exists()) {
            Glide.with(this).load(new File(localPath)).into(binding.ivEditProfile);
        } else if (currentUser.getPhotoUrl() != null) {
            // 2. Load từ Firebase nếu có
            Glide.with(this).load(currentUser.getPhotoUrl()).into(binding.ivEditProfile);
        }

        binding.toolbarEditProfile.setNavigationOnClickListener(v -> finish());
        binding.btnChangePhoto.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        binding.btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String newName = binding.etEditName.getText().toString().trim();

        if (newName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        if (selectedImageUri != null) {
            // 1. Upload ảnh lên Firebase Storage
            StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                    .child("user_avatars/" + currentUser.getUid() + ".jpg");

            storageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Lấy URL an toàn nhất
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            updateFirebaseProfile(newName, uri);
                        }).addOnFailureListener(e -> {
                            showLoading(false);
                            Toast.makeText(this, "Lỗi lấy link ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        showLoading(false);
                        Toast.makeText(this, "Lỗi tải ảnh lên Cloud: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Chỉ cập nhật tên
            updateFirebaseProfile(newName, currentUser.getPhotoUrl());
        }
    }

    private void updateFirebaseProfile(String name, Uri photoUri) {
        String uriStr = (photoUri != null) ? photoUri.toString() : "";
        
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .setPhotoUri(Uri.parse(uriStr))
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(EditProfileActivity.this, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBarEdit.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnSaveProfile.setEnabled(!isLoading);
        binding.btnChangePhoto.setEnabled(!isLoading);
    }
}
