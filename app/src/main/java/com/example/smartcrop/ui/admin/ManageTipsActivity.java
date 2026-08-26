package com.example.smartcrop.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcrop.R;
import com.example.smartcrop.api.ApiService;
import com.example.smartcrop.api.RetrofitClient;
import com.example.smartcrop.databinding.ActivityManageTipsBinding;
import com.example.smartcrop.ui.home.TipModel;

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

        binding.btnAddTip.setOnClickListener(v -> showTipDialog());
    }

    private void loadTips() {
        ApiService apiService = RetrofitClient.getSqlService();
        apiService.getTipsFromDb().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tipsList.clear();
                    for (Map<String, Object> map : response.body()) {
                        tipsList.add(new TipModel(
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

    private void showTipDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_tip, null);
        EditText etTitle = view.findViewById(R.id.etTipTitle);
        EditText etContent = view.findViewById(R.id.etTipContent);

        new AlertDialog.Builder(this)
                .setTitle("Thêm mẹo mới")
                .setView(view)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    saveTip(etTitle.getText().toString(), etContent.getText().toString());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void saveTip(String title, String content) {
        ApiService apiService = RetrofitClient.getSqlService();
        apiService.addTipToDb(title, content, "img_tip_check").enqueue(new Callback<Map<String, String>>() {
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
