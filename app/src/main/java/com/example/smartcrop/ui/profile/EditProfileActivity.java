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
import com.example.smartcrop.api.ApiService;
import com.example.smartcrop.api.RetrofitClient;
import com.example.smartcrop.databinding.ActivityEditProfileBinding;
import com.example.smartcrop.utils.ImageUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.io.File;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        binding.etEditName.setText(currentUser.getDisplayName());
        
        // Load current Avatar
        String userPhotoUrl = currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : "";
        if (userPhotoUrl.length() > 500) {
            byte[] bytes = ImageUtils.base64ToBytes(userPhotoUrl);
            if (bytes != null) Glide.with(this).load(bytes).placeholder(android.R.drawable.ic_menu_gallery).into(binding.ivEditProfile);
        } else {
            Glide.with(this).load(userPhotoUrl).placeholder(android.R.drawable.ic_menu_gallery).into(binding.ivEditProfile);
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
            String base64Image = ImageUtils.uriToBase64(this, selectedImageUri);
            if (!base64Image.isEmpty()) {
                // Lưu vào SQL Server
                ApiService apiService = RetrofitClient.getSqlService();
                apiService.updateProfilePhoto(currentUser.getUid(), base64Image).enqueue(new Callback<Map<String, String>>() {
                    @Override
                    public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                        // Sau đó cập nhật Firebase
                        updateFirebaseProfile(newName, base64Image);
                    }

                    @Override
                    public void onFailure(Call<Map<String, String>> call, Throwable t) {
                        showLoading(false);
                        Toast.makeText(EditProfileActivity.this, "Lỗi SQL: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            updateFirebaseProfile(newName, null);
        }
    }

    private void updateFirebaseProfile(String name, String photoBase64) {
        // CHỈ cập nhật Tên lên Firebase (Base64 quá dài gây lỗi Cập nhật thất bại)
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Lưu Base64 vào SharedPreferences để đồng bộ UI trên App
                        if (photoBase64 != null) {
                            getSharedPreferences("SmartCropPrefs", MODE_PRIVATE)
                                    .edit()
                                    .putString("profile_image_" + currentUser.getUid(), "BASE64:" + photoBase64)
                                    .apply();
                        }
                        
                        showLoading(false);
                        Toast.makeText(EditProfileActivity.this, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        showLoading(false);
                        String error = task.getException() != null ? task.getException().getMessage() : "Lỗi đồng bộ";
                        Toast.makeText(EditProfileActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBarEdit.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnSaveProfile.setEnabled(!isLoading);
        binding.btnChangePhoto.setEnabled(!isLoading);
    }
}
