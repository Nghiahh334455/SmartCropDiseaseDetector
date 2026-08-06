package com.example.smartcrop.ui.library;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
            EditText etStatus = new EditText(this);
            etStatus.setHint("Nhập nội dung câu hỏi hoặc status của bạn...");
            etStatus.setPadding(40, 40, 40, 40);

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Đăng lên bảng tin")
                    .setView(etStatus)
                    .setPositiveButton("Đăng ngay", (dialog, which) -> {
                        String status = etStatus.getText().toString().trim();
                        // Lấy ảnh đầu tiên làm ảnh đại diện bài đăng
                        String mainImage = disease.imageResources.get(0);
                        shareToForum(disease.name, status, mainImage);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void shareToForum(String name, String status, String imageName) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để chia sẻ", Toast.LENGTH_SHORT).show();
            return;
        }

        String displayStatus = (status != null && !status.isEmpty()) ? status : "Mọi người cùng xem thông tin về: " + name;

        Map<String, Object> post = new HashMap<>();
        post.put("uid", user.getUid());
        post.put("author", user.getDisplayName() != null ? user.getDisplayName() : "Người dùng Thần Nông AI");
        post.put("question", displayStatus);
        post.put("imageUrl", imageName); // Gửi tên resource thay vì placeholder
        post.put("disease", name);
        post.put("timestamp", System.currentTimeMillis());
        post.put("likes", 0);
        post.put("commentsCount", 0);
        post.put("likedBy", new java.util.ArrayList<String>());

        FirebaseFirestore.getInstance().collection("forum_posts")
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Đã chia sẻ lên bảng tin!", Toast.LENGTH_SHORT).show();
                });
    }
}
