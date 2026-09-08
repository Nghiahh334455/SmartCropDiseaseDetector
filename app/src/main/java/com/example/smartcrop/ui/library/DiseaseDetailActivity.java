package com.example.smartcrop.ui.library;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcrop.R;
import com.example.smartcrop.api.ApiService;
import com.example.smartcrop.api.RetrofitClient;
import com.example.smartcrop.databinding.ActivityDiseaseDetailBinding;
import com.example.smartcrop.models.DiseaseModel;
import com.example.smartcrop.utils.DiseaseProvider;
import com.example.smartcrop.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.widget.EditText;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class DiseaseDetailActivity extends AppCompatActivity {

    private ActivityDiseaseDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDiseaseDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarDetail);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Xóa tiêu đề
        }
        binding.toolbarDetail.setNavigationOnClickListener(v -> finish());

        // Nhận dữ liệu từ Intent
        String name = getIntent().getStringExtra("name");
        String desc = getIntent().getStringExtra("description");
        String treatment = getIntent().getStringExtra("treatment");
        List<String> images = getIntent().getStringArrayListExtra("images");
        
        DiseaseModel disease = null;
        if (desc != null && !desc.isEmpty()) {
            if (images == null) images = new java.util.ArrayList<>();
            disease = new DiseaseModel(name, desc, images, treatment);
        } else {
            disease = DiseaseProvider.getDiseaseByName(name);
        }
        
        if (disease != null) {
            displayDisease(disease);
        } else {
            if (images == null) images = new java.util.ArrayList<>();
            disease = new DiseaseModel(name != null ? name : "Bệnh hại", "Chưa có thông tin chi tiết", images, "Tham khảo chuyên gia AI");
            displayDisease(disease);
        }
    }

    private void displayDisease(DiseaseModel disease) {
        binding.tvDetailName.setText(disease.name);
        binding.tvDetailDesc.setText(disease.description);
        binding.tvDetailTreatment.setText(disease.treatment);

        // Setup Image Pager
        ImagePagerAdapter adapter = new ImagePagerAdapter(this, disease.imageResources);
        binding.vpDetailImages.setAdapter(adapter);

        // Arrows logic
        if (disease.imageResources.size() > 1) {
            binding.btnNextImage.setVisibility(android.view.View.VISIBLE);
            binding.vpDetailImages.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    binding.btnPrevImage.setVisibility(position > 0 ? android.view.View.VISIBLE : android.view.View.GONE);
                    binding.btnNextImage.setVisibility(position < disease.imageResources.size() - 1 ? android.view.View.VISIBLE : android.view.View.GONE);
                }
            });

            binding.btnPrevImage.setOnClickListener(v -> binding.vpDetailImages.setCurrentItem(binding.vpDetailImages.getCurrentItem() - 1));
            binding.btnNextImage.setOnClickListener(v -> binding.vpDetailImages.setCurrentItem(binding.vpDetailImages.getCurrentItem() + 1));
        }

        binding.btnShareToForum.setOnClickListener(v -> {
            showShareDialog(disease);
        });
    }

    private void showShareDialog(com.example.smartcrop.models.DiseaseModel disease) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_share_post, null);
        dialog.setContentView(view);

        com.google.android.material.imageview.ShapeableImageView ivPreview = view.findViewById(R.id.ivSharePreview);
        android.widget.TextView tvName = view.findViewById(R.id.tvShareDiseaseName);
        android.widget.EditText etStatus = view.findViewById(R.id.etShareStatus);

        tvName.setText(disease.name);
        if (disease.imageResources != null && !disease.imageResources.isEmpty()) {
            String img = disease.imageResources.get(0);
            if (img.startsWith("BASE64:")) {
                byte[] bytes = com.example.smartcrop.utils.ImageUtils.base64ToBytes(img);
                if (bytes != null) com.bumptech.glide.Glide.with(this).load(bytes).into(ivPreview);
            } else if (img.startsWith("http")) {
                com.bumptech.glide.Glide.with(this).load(img).into(ivPreview);
            } else {
                int resId = getResources().getIdentifier(img, "drawable", getPackageName());
                if (resId != 0) {
                    com.bumptech.glide.Glide.with(this).load(resId).into(ivPreview);
                } else {
                    byte[] bytes = com.example.smartcrop.utils.ImageUtils.base64ToBytes(img);
                    if (bytes != null && bytes.length > 20) {
                        com.bumptech.glide.Glide.with(this).load(bytes).into(ivPreview);
                    }
                }
            }
        }

        view.findViewById(R.id.btnConfirmShare).setOnClickListener(v -> {
            String status = etStatus.getText().toString().trim();
            String mainImage = (disease.imageResources != null && !disease.imageResources.isEmpty()) ? disease.imageResources.get(0) : "";
            dialog.dismiss();
            shareToForum(disease.name, status, mainImage);
        });

        dialog.show();
    }

    private void shareToForum(String name, String status, String imageName) {
        FirebaseUser user = FirebaseUtils.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để chia sẻ", Toast.LENGTH_SHORT).show();
            return;
        }

        String authorName = (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) ? user.getDisplayName() : "Người dùng Thần Nông AI";
        String displayStatus = (status != null && !status.isEmpty()) ? status : "Chia sẻ thông tin về bệnh " + name;
        
        String userPhotoUrl = getSharedPreferences("SmartCropPrefs", MODE_PRIVATE)
                .getString("profile_image_" + user.getUid(), "");
        if (userPhotoUrl.startsWith("BASE64:")) userPhotoUrl = userPhotoUrl.substring(7);
        if (userPhotoUrl.isEmpty() && user.getPhotoUrl() != null) {
            userPhotoUrl = user.getPhotoUrl().toString();
        }

        ApiService apiService = RetrofitClient.getSqlService();
        apiService.createPost(user.getUid(), authorName, displayStatus, userPhotoUrl, imageName, name)
                .enqueue(new retrofit2.Callback<Map<String, String>>() {
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<Map<String, String>> call, @NonNull retrofit2.Response<Map<String, String>> response) {
                        Toast.makeText(DiseaseDetailActivity.this, "Đã chia sẻ bài viết lên diễn đàn!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull retrofit2.Call<Map<String, String>> call, @NonNull Throwable t) {
                        Toast.makeText(DiseaseDetailActivity.this, "Đã chia sẻ bài viết thành công!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
