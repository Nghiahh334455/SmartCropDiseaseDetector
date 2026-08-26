package com.example.smartcrop.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.smartcrop.R;
import com.example.smartcrop.api.ApiService;
import com.example.smartcrop.api.RetrofitClient;
import com.example.smartcrop.databinding.ActivityManageLibraryBinding;
import com.example.smartcrop.ui.library.LibraryAdapter;
import com.example.smartcrop.models.DiseaseModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageLibraryActivity extends AppCompatActivity {

    private ActivityManageLibraryBinding binding;
    private List<DiseaseModel> diseaseList = new ArrayList<>();
    private LibraryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageLibraryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarManageLibrary.setNavigationOnClickListener(v -> finish());

        adapter = new LibraryAdapter(diseaseList);
        binding.rvManageLibrary.setLayoutManager(new LinearLayoutManager(this));
        binding.rvManageLibrary.setAdapter(adapter);

        loadDiseases();

        binding.btnAddDisease.setOnClickListener(v -> showDiseaseDialog(null));
    }

    private void loadDiseases() {
        ApiService apiService = RetrofitClient.getSqlService();
        apiService.getDiseasesFromDb().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    diseaseList.clear();
                    for (Map<String, Object> map : response.body()) {
                        diseaseList.add(new DiseaseModel(
                                (String) map.get("name"),
                                (String) map.get("description"),
                                new ArrayList<>(), // Empty list instead of null
                                (String) map.get("treatment")
                        ));
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(ManageLibraryActivity.this, "Lỗi tải thư viện", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDiseaseDialog(DiseaseModel existing) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_disease, null);
        EditText etName = view.findViewById(R.id.etDiseaseName);
        EditText etDesc = view.findViewById(R.id.etDiseaseDesc);
        EditText etTreatment = view.findViewById(R.id.etDiseaseTreatment);

        if (existing != null) {
            etName.setText(existing.name);
            etDesc.setText(existing.description);
            etTreatment.setText(existing.treatment);
        }

        new AlertDialog.Builder(this)
                .setTitle(existing == null ? "Thêm bệnh mới" : "Sửa thông tin")
                .setView(view)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    saveDisease(etName.getText().toString(), etDesc.getText().toString(), etTreatment.getText().toString());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void saveDisease(String name, String desc, String treatment) {
        ApiService apiService = RetrofitClient.getSqlService();
        apiService.addDiseaseToDb(name, desc, treatment, "").enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ManageLibraryActivity.this, "Đã lưu!", Toast.LENGTH_SHORT).show();
                    loadDiseases();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(ManageLibraryActivity.this, "Lỗi lưu dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
