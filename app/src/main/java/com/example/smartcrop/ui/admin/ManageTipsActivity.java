package com.example.smartcrop.ui.admin;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartcrop.R;
import com.example.smartcrop.api.ApiService;
import com.example.smartcrop.api.RetrofitClient;
import com.example.smartcrop.databinding.ActivityManageTipsBinding;
import com.example.smartcrop.ui.home.TipModel;
import com.example.smartcrop.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageTipsActivity extends AppCompatActivity {

    private ActivityManageTipsBinding binding;
    private List<TipModel> tipsList = new ArrayList<>();
    private TipsManageAdapter adapter;

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
        binding = ActivityManageTipsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarManageTips.setNavigationOnClickListener(v -> finish());

        adapter = new TipsManageAdapter(tipsList);
        binding.rvManageTips.setLayoutManager(new LinearLayoutManager(this));
        binding.rvManageTips.setAdapter(adapter);

        loadTips();

        binding.btnAddTip.setOnClickListener(v -> showTipDialog(null));
    }

    private void loadTips() {
        ApiService apiService = RetrofitClient.getSqlService();
        apiService.getTipsFromDb().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tipsList.clear();
                    for (Map<String, Object> map : response.body()) {
                        Object idObj = map.get("id");
                        int id = (idObj instanceof Double) ? ((Double) idObj).intValue() : (int) idObj;
                        tipsList.add(new TipModel(
                                id,
                                (String) map.get("title"),
                                (String) map.get("content"),
                                (String) map.get("imageResource")
                        ));
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(ManageTipsActivity.this, "Lỗi tải mẹo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTipDialog(TipModel existing) {
        selectedImageUri = null;
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_tip, null);
        EditText etTitle = view.findViewById(R.id.etTipTitle);
        EditText etContent = view.findViewById(R.id.etTipContent);
        ImageView ivPreview = view.findViewById(R.id.ivTipPreview);
        View btnSelectImage = view.findViewById(R.id.btnSelectTipImage);

        currentPreviewImageView = ivPreview;

        if (existing != null) {
            etTitle.setText(existing.title);
            etContent.setText(existing.content);
            if (existing.imageUrl != null && !existing.imageUrl.isEmpty()) {
                if (existing.imageUrl.startsWith("BASE64:")) {
                    byte[] bytes = ImageUtils.base64ToBytes(existing.imageUrl);
                    if (bytes != null) Glide.with(this).load(bytes).into(ivPreview);
                } else {
                    int resId = getResources().getIdentifier(existing.imageUrl, "drawable", getPackageName());
                    if (resId != 0) Glide.with(this).load(resId).into(ivPreview);
                }
            }
        }

        btnSelectImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        new AlertDialog.Builder(this)
                .setTitle(existing == null ? "Thêm mẹo mới" : "Sửa mẹo")
                .setView(view)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String imgStr = "img_tip_check";
                    if (selectedImageUri != null) {
                        String b64 = ImageUtils.uriToBase64(this, selectedImageUri);
                        if (!b64.isEmpty()) imgStr = "BASE64:" + b64;
                    } else if (existing != null && existing.imageUrl != null && !existing.imageUrl.isEmpty()) {
                        imgStr = existing.imageUrl;
                    }
                    saveTip(etTitle.getText().toString(), etContent.getText().toString(), imgStr);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void saveTip(String title, String content, String imageResource) {
        ApiService apiService = RetrofitClient.getSqlService();
        apiService.addTipToDb(title, content, imageResource).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ManageTipsActivity.this, "Đã lưu!", Toast.LENGTH_SHORT).show();
                    loadTips();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(ManageTipsActivity.this, "Lỗi lưu dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    class TipsManageAdapter extends RecyclerView.Adapter<TipsManageAdapter.ViewHolder> {
        List<TipModel> list;
        TipsManageAdapter(List<TipModel> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TipModel tip = list.get(position);
            holder.text1.setText(tip.title);
            holder.text2.setText(tip.content);
            
            holder.itemView.setOnLongClickListener(v -> {
                showTipOptions(tip, position);
                return true;
            });
        }

        private void showTipOptions(TipModel tip, int position) {
            String[] options = {"Sửa", "Xóa"};
            new AlertDialog.Builder(ManageTipsActivity.this)
                    .setTitle(tip.title)
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) showTipDialog(tip);
                        else deleteTip(tip);
                    })
                    .show();
        }

        private void deleteTip(TipModel tip) {
            new AlertDialog.Builder(ManageTipsActivity.this)
                    .setTitle("Xác nhận")
                    .setMessage("Xóa mẹo này?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        ApiService apiService = RetrofitClient.getSqlService();
                        apiService.deleteTipAdmin(tip.id).enqueue(new Callback<Map<String, String>>() {
                            @Override
                            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(ManageTipsActivity.this, "Đã xóa!", Toast.LENGTH_SHORT).show();
                                    loadTips();
                                }
                            }
                            @Override
                            public void onFailure(Call<Map<String, String>> call, Throwable t) {}
                        });
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            ViewHolder(View v) {
                super(v);
                text1 = v.findViewById(android.R.id.text1);
                text2 = v.findViewById(android.R.id.text2);
            }
        }
    }
}
