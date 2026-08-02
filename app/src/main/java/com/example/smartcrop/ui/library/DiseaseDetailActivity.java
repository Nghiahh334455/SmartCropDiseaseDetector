package com.example.smartcrop.ui.library;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.smartcrop.databinding.ActivityDiseaseDetailBinding;

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
        }
        binding.toolbarDetail.setNavigationOnClickListener(v -> finish());

        // Nhận dữ liệu từ Intent
        String name = getIntent().getStringExtra("name");
        String desc = getIntent().getStringExtra("description");
        String image = getIntent().getStringExtra("image");
        String treatment = getIntent().getStringExtra("treatment");

        // Hiển thị dữ liệu
        binding.collapsingToolbar.setTitle(name);
        binding.tvDetailDesc.setText(desc);
        binding.tvDetailTreatment.setText(treatment);

        Glide.with(this)
                .load(image)
                .into(binding.ivDetailImage);
    }
}
