package com.example.smartcrop.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcrop.api.ApiService;
import com.example.smartcrop.api.RetrofitClient;
import com.example.smartcrop.databinding.ActivityAdminDashboardBinding;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {

    private ActivityAdminDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarAdmin.setNavigationOnClickListener(v -> finish());

        loadStats();

        binding.btnManageUsers.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, ManageUsersActivity.class));
        });
        binding.btnManagePosts.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, ManagePostsActivity.class));
        });
        binding.btnManageLibrary.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, ManageLibraryActivity.class));
        });
        binding.btnManageTips.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, ManageTipsActivity.class));
        });
    }

    private void loadStats() {
        ApiService apiService = RetrofitClient.getSqlService();
        apiService.getAdminStats().enqueue(new retrofit2.Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<Map<String, Object>> call, @NonNull retrofit2.Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body();
                    
                    Object totalUsers = data.get("totalUsers");
                    Object totalPosts = data.get("totalPosts");
                    
                    binding.tvTotalUsers.setText(String.valueOf(totalUsers));
                    binding.tvTotalPosts.setText(String.valueOf(totalPosts));

                    List<Map<String, Object>> diseaseStats = (List<Map<String, Object>>) data.get("diseaseStats");
                    if (diseaseStats != null) {
                        setupChart(diseaseStats);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<Map<String, Object>> call, @NonNull Throwable t) {
                Toast.makeText(AdminDashboardActivity.this, "Lỗi tải thống kê", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupChart(List<Map<String, Object>> stats) {
        if (stats == null || stats.isEmpty()) return;

        ArrayList<PieEntry> entries = new ArrayList<>();
        float totalCount = 0;
        for (Map<String, Object> stat : stats) {
            String name = (String) stat.get("name");
            float count = ((Double) stat.get("count")).floatValue();
            entries.add(new PieEntry(count, name));
            totalCount += count;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        
        // Tạo dải 10+ màu rực rỡ riêng biệt cho các bệnh
        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.VORDIPLOM_COLORS) colors.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS) colors.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS) colors.add(c);
        for (int c : ColorTemplate.LIBERTY_COLORS) colors.add(c);
        colors.add(android.graphics.Color.parseColor("#2ecc71"));
        colors.add(android.graphics.Color.parseColor("#e74c3c"));
        colors.add(android.graphics.Color.parseColor("#3498db"));
        colors.add(android.graphics.Color.parseColor("#f1c40f"));
        colors.add(android.graphics.Color.parseColor("#9b59b6"));
        colors.add(android.graphics.Color.parseColor("#1abc9c"));
        
        dataSet.setColors(colors);
        dataSet.setValueTextSize(13f);
        dataSet.setValueTextColor(android.graphics.Color.WHITE);
        dataSet.setSliceSpace(3f);
        
        // Hiện số nguyên trực tiếp trên từng miếng bánh
        dataSet.setDrawValues(true);
        dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        PieData data = new PieData(dataSet);
        binding.pieChart.setData(data);
        binding.pieChart.setDrawEntryLabels(false); // Ẩn chữ trên biểu đồ bánh
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setCenterText("Tổng cộng\n" + (int) totalCount + " lượt");
        binding.pieChart.setCenterTextSize(14f);
        
        // Tắt Legend mặc định của thư viện để dùng Chú thích tùy chỉnh 100% không bị cắt
        binding.pieChart.getLegend().setEnabled(false);
        binding.pieChart.animateY(1000);
        binding.pieChart.invalidate();

        // Nạp Chú thích động 100% hiển thị đủ tất cả tên bệnh
        binding.layoutLegend.removeAllViews();
        for (int i = 0; i < stats.size(); i++) {
            Map<String, Object> stat = stats.get(i);
            String name = (String) stat.get("name");
            int count = ((Double) stat.get("count")).intValue();
            int color = colors.get(i % colors.size());
            float percent = totalCount > 0 ? (count / totalCount) * 100f : 0f;

            android.widget.LinearLayout row = new android.widget.LinearLayout(this);
            row.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(0, 10, 0, 10);

            // Chấm màu tương ứng
            View colorDot = new View(this);
            android.widget.LinearLayout.LayoutParams dotParams = new android.widget.LinearLayout.LayoutParams(32, 32);
            dotParams.setMarginEnd(16);
            colorDot.setLayoutParams(dotParams);
            android.graphics.drawable.GradientDrawable dotBg = new android.graphics.drawable.GradientDrawable();
            dotBg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            dotBg.setColor(color);
            colorDot.setBackground(dotBg);

            // Tên bệnh
            android.widget.TextView tvName = new android.widget.TextView(this);
            android.widget.LinearLayout.LayoutParams nameParams = new android.widget.LinearLayout.LayoutParams(0, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            tvName.setLayoutParams(nameParams);
            tvName.setText(name);
            tvName.setTextSize(13f);
            tvName.setTextColor(getResources().getColor(com.example.smartcrop.R.color.primary_dark));
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);

            // Số lượt & Tỷ lệ %
            android.widget.TextView tvCount = new android.widget.TextView(this);
            tvCount.setText(String.format(java.util.Locale.US, "%d lượt (%.1f%%)", count, percent));
            tvCount.setTextSize(12f);
            tvCount.setTextColor(getResources().getColor(com.example.smartcrop.R.color.primary));

            row.addView(colorDot);
            row.addView(tvName);
            row.addView(tvCount);

            binding.layoutLegend.addView(row);
        }
    }
}
