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
import com.google.firebase.auth.FirebaseAuth;
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
        
        // Tìm thông tin đầy đủ từ Provider
        DiseaseModel disease = DiseaseProvider.getDiseaseByName(name);
        
        if (disease != null) {
            displayDisease(disease);
        } else {
            // Fallback nếu không tìm thấy trong Provider (dùng dữ liệu từ intent)
            binding.tvDetailName.setText(name);
            binding.tvDetailDesc.setText(getIntent().getStringExtra("description"));
            binding.tvDetailTreatment.setText(getIntent().getStringExtra("treatment"));
            Toast.makeText(this, "Sử dụng dữ liệu tạm thời", Toast.LENGTH_SHORT).show();
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
            int resId = getResources().getIdentifier(disease.imageResources.get(0), "drawable", getPackageName());
            if (resId != 0) ivPreview.setImageResource(resId);
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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để chia sẻ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Nếu nội dung trống thì để trống hoặc dùng nhãn mặc định
        String displayStatus = (status != null && !status.isEmpty()) ? status : "";
        String userPhotoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;

        ApiService apiService = RetrofitClient.getApiService();
        apiService.createPost(user.getUid(), user.getDisplayName(), displayStatus, userPhotoUrl, imageName, name)
                .enqueue(new retrofit2.Callback<Map<String, String>>() {
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<Map<String, String>> call, @NonNull retrofit2.Response<Map<String, String>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(DiseaseDetailActivity.this, "Đã chia sẻ lên diễn đàn!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(DiseaseDetailActivity.this, "Lỗi khi chia sẻ: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull retrofit2.Call<Map<String, String>> call, @NonNull Throwable t) {
                        Toast.makeText(DiseaseDetailActivity.this, "Lỗi kết nối Server", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
