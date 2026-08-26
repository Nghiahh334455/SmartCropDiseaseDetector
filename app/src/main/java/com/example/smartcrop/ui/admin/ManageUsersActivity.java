package com.example.smartcrop.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcrop.api.ApiService;
import com.example.smartcrop.api.RetrofitClient;
import com.example.smartcrop.databinding.ActivityManageUsersBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageUsersActivity extends AppCompatActivity {

    private ActivityManageUsersBinding binding;
    private List<Map<String, Object>> userList = new ArrayList<>();
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarManageUsers.setNavigationOnClickListener(v -> finish());

        adapter = new UserAdapter(userList);
        binding.rvManageUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvManageUsers.setAdapter(adapter);

        loadUsers();
    }

    private void loadUsers() {
        ApiService apiService = RetrofitClient.getSqlService();
        apiService.getAdminUsers().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userList.clear();
                    userList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(ManageUsersActivity.this, "Lỗi tải người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUser(String uid) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Xóa người dùng này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    ApiService apiService = RetrofitClient.getSqlService();
                    apiService.deleteUser(uid).enqueue(new Callback<Map<String, String>>() {
                        @Override
                        public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(ManageUsersActivity.this, "Đã xóa", Toast.LENGTH_SHORT).show();
                                loadUsers();
                            }
                        }
                        @Override
                        public void onFailure(Call<Map<String, String>> call, Throwable t) {}
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
        List<Map<String, Object>> list;
        UserAdapter(List<Map<String, Object>> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, Object> user = list.get(position);
            String name = (String) user.get("displayName");
            String email = (String) user.get("email");
            String uid = (String) user.get("uid");
            
            holder.text1.setText(name);
            holder.text2.setText(email);
            
            holder.itemView.setOnLongClickListener(v -> {
                deleteUser(uid);
                return true;
            });
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
