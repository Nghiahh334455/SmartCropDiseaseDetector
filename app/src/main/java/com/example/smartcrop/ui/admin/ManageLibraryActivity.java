package com.example.smartcrop.ui.admin;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.smartcrop.R;
import com.example.smartcrop.api.ApiService;
import com.example.smartcrop.api.RetrofitClient;
import com.example.smartcrop.databinding.ActivityManageLibraryBinding;
import com.example.smartcrop.ui.library.LibraryAdapter;
import com.example.smartcrop.models.DiseaseModel;
import com.example.smartcrop.utils.ImageUtils;

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
    
    private Uri selectedImageUri;
    private ImageView currentPreviewImageView;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    if (currentPreviewImageView != null) {
                        Glide.with(this).load(uri).into(currentPreviewImageView);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageLibraryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarManageLibrary.setNavigationOnClickListener(v -> finish());

        adapter = new LibraryAdapter(diseaseList);
        adapter.setOnItemLongClickListener((disease, position) -> showOptionsDialog(disease));
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
                        String imageRes = (String) map.get("imageResource");
                        List<String> imgList = new ArrayList<>();
                        if (imageRes != null && !imageRes.isEmpty()) imgList.add(imageRes);
                        diseaseList.add(new DiseaseModel(
                                (String) map.get("name"),
                                (String) map.get("description"),
                                imgList,
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

    private void showOptionsDialog(DiseaseModel disease) {
        String[] options = {"Sửa", "Xóa"};
        new AlertDialog.Builder(this)
                .setTitle(disease.name)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) showDiseaseDialog(disease);
                    else deleteDisease(disease);
                })
                .show();
    }

    private void deleteDisease(DiseaseModel disease) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Xóa bệnh '" + disease.name + "'?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    ApiService apiService = RetrofitClient.getSqlService();
                    apiService.deleteDiseaseAdmin(disease.name).enqueue(new Callback<Map<String, String>>() {
                        @Override
                        public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(ManageLibraryActivity.this, "Đã xóa!", Toast.LENGTH_SHORT).show();
                                loadDiseases();
                            }
                        }
                        @Override
                        public void onFailure(Call<Map<String, String>> call, Throwable t) {}
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDiseaseDialog(DiseaseModel existing) {
        selectedImageUri = null;
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_disease, null);
        EditText etName = view.findViewById(R.id.etDiseaseName);
        EditText etDesc = view.findViewById(R.id.etDiseaseDesc);
        EditText etTreatment = view.findViewById(R.id.etDiseaseTreatment);
        ImageView ivPreview = view.findViewById(R.id.ivDiseasePreview);
        View btnSelectImage = view.findViewById(R.id.btnSelectDiseaseImage);

        currentPreviewImageView = ivPreview;

        if (existing != null) {
            etName.setText(existing.name);
            etDesc.setText(existing.description);
            etTreatment.setText(existing.treatment);
            if (existing.imageResources != null && !existing.imageResources.isEmpty()) {
                String img = existing.imageResources.get(0);
                if (img.startsWith("BASE64:")) {
                    byte[] bytes = ImageUtils.base64ToBytes(img);
                    if (bytes != null) Glide.with(this).load(bytes).into(ivPreview);
                } else {
                    int resId = getResources().getIdentifier(img, "drawable", getPackageName());
                    if (resId != 0) Glide.with(this).load(resId).into(ivPreview);
                }
            }
        }

        btnSelectImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        new AlertDialog.Builder(this)
                .setTitle(existing == null ? "Thêm bệnh mới" : "Sửa thông tin")
                .setView(view)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String imgStr = "";
                    if (selectedImageUri != null) {
                        String b64 = ImageUtils.uriToBase64(this, selectedImageUri);
                        if (!b64.isEmpty()) imgStr = "BASE64:" + b64;
                    } else if (existing != null && existing.imageResources != null && !existing.imageResources.isEmpty()) {
                        imgStr = existing.imageResources.get(0);
                    }
                    saveDisease(etName.getText().toString(), etDesc.getText().toString(), etTreatment.getText().toString(), imgStr);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void saveDisease(String name, String desc, String treatment, String imageResource) {
        ApiService apiService = RetrofitClient.getSqlService();
        apiService.addDiseaseToDb(name, desc, treatment, imageResource).enqueue(new Callback<Map<String, String>>() {
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
