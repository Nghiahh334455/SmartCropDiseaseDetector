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
            String localPath = saveImageToInternalStorage(selectedImageUri);
            if (localPath != null) {
                // Save path to SharedPreferences
                getSharedPreferences("SmartCropPrefs", MODE_PRIVATE)
                        .edit()
                        .putString("profile_image_" + currentUser.getUid(), localPath)
                        .apply();
                
                updateProfile(newName);
            } else {
                showLoading(false);
                Toast.makeText(this, "Lỗi lưu ảnh nội bộ", Toast.LENGTH_SHORT).show();
            }
        } else {
            updateProfile(newName);
        }
    }

    private String saveImageToInternalStorage(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            File file = new File(getFilesDir(), "profile_" + currentUser.getUid() + ".jpg");
            OutputStream os = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
            os.close();
            is.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateProfile(String name) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(EditProfileActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
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
