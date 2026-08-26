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
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setSliceSpace(3f);

        PieData data = new PieData(dataSet);
        binding.pieChart.setData(data);
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setCenterText("Bệnh hại");
        binding.pieChart.animateY(1000);
        binding.pieChart.invalidate();
    }
}
