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
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map<String, Object> stat : stats) {
            String name = (String) stat.get("name");
            float count = ((Double) stat.get("count")).floatValue();
            entries.add(new PieEntry(count, name));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        
        // Tạo dải 10+ màu riêng biệt cho các bệnh
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
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(android.graphics.Color.WHITE);
        dataSet.setSliceSpace(3f);
        
        // Cấu hình định dạng số (hiện số trên biểu đồ, ẩn chữ)
        dataSet.setDrawValues(true);
        dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        PieData data = new PieData(dataSet);
        binding.pieChart.setData(data);
        binding.pieChart.setDrawEntryLabels(false); // Ẩn tên bệnh trên biểu đồ
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setCenterText("Tỷ lệ\nbệnh hại");
        binding.pieChart.setCenterTextSize(14f);
        
        // Bật Legend (chú thích) xếp dọc bên dưới, có ngắt dòng
        com.github.mikephil.charting.components.Legend legend = binding.pieChart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(com.github.mikephil.charting.components.Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setWordWrapEnabled(true);
        legend.setTextSize(12f);
        legend.setFormSize(12f);
        legend.setFormToTextSpace(5f);
        legend.setYEntrySpace(5f);

        binding.pieChart.animateY(1000);
        binding.pieChart.invalidate();
    }
}
