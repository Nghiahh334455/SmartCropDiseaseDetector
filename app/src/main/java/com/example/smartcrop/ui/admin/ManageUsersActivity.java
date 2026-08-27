package com.example.smartcrop.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartcrop.R;
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
                    binding.tvTotalUsersCount.setText("Tổng số tài khoản: " + userList.size());
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(ManageUsersActivity.this, "Lỗi tải danh sách người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUser(String uid, String name) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa tài khoản")
                .setMessage("Bạn có chắc chắn muốn xóa tài khoản '" + name + "' khỏi hệ thống?")
                .setPositiveButton("Xóa ngay", (dialog, which) -> {
                    ApiService apiService = RetrofitClient.getSqlService();
                    apiService.deleteUser(uid).enqueue(new Callback<Map<String, String>>() {
                        @Override
                        public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(ManageUsersActivity.this, "Đã xóa người dùng", Toast.LENGTH_SHORT).show();
                                loadUsers();
                            }
                        }
                        @Override
                        public void onFailure(Call<Map<String, String>> call, Throwable t) {
                            Toast.makeText(ManageUsersActivity.this, "Không thể xóa tài khoản này", Toast.LENGTH_SHORT).show();
                        }
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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_user, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, Object> user = list.get(position);
            String name = (String) user.get("displayName");
            if (name == null || name.isEmpty()) name = "Người dùng";
            String email = (String) user.get("email");
            if (email == null || email.isEmpty()) email = "Chưa có email";
            String uid = (String) user.get("uid");
            Boolean isAdmin = (Boolean) user.get("isAdmin");

            holder.tvName.setText(name);
            holder.tvEmail.setText(email);

            if (Boolean.TRUE.equals(isAdmin)) {
                holder.tvRoleBadge.setText("ADMIN");
                holder.tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_red);
                holder.btnDelete.setVisibility(View.GONE); // Không cho xóa Admin
            } else {
                holder.tvRoleBadge.setText("Người dùng");
                holder.tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_green);
                holder.btnDelete.setVisibility(View.VISIBLE);
            }

            Glide.with(ManageUsersActivity.this)
                    .load(android.R.drawable.ic_menu_gallery)
                    .circleCrop()
                    .into(holder.ivAvatar);

            final String userName = name;
            holder.btnDelete.setOnClickListener(v -> deleteUser(uid, userName));
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvEmail, tvRoleBadge;
            ImageView ivAvatar;
            View btnDelete;

            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvUserName);
                tvEmail = v.findViewById(R.id.tvUserEmail);
                tvRoleBadge = v.findViewById(R.id.tvRoleBadge);
                ivAvatar = v.findViewById(R.id.ivUserAvatar);
                btnDelete = v.findViewById(R.id.btnDeleteUser);
            }
        }
    }
}
