package com.example.smartcrop.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.smartcrop.api.ApiService;
import com.example.smartcrop.api.RetrofitClient;
import com.example.smartcrop.databinding.ActivityNotificationsBinding;
import com.example.smartcrop.models.NotificationModel;
import com.example.smartcrop.ui.forum.PostDetailActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationActivity extends AppCompatActivity {

    private ActivityNotificationsBinding binding;
    private List<NotificationModel> notificationList = new ArrayList<>();
    private List<String> notificationIds = new ArrayList<>();
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarNotifications.setNavigationOnClickListener(v -> finish());

        adapter = new NotificationAdapter(notificationList, this::onNotificationClick);
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        binding.rvNotifications.setAdapter(adapter);

        listenForNotifications();
    }

    private void listenForNotifications() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        ApiService apiService = RetrofitClient.getSqlService();
        apiService.getNotifications(uid).enqueue(new retrofit2.Callback<List<NotificationModel>>() {
            @Override
            public void onResponse(retrofit2.Call<List<NotificationModel>> call, retrofit2.Response<List<NotificationModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    notificationList.clear();
                    notificationIds.clear();
                    for (NotificationModel notification : response.body()) {
                        notificationList.add(notification);
                        notificationIds.add(notification.getId());
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<NotificationModel>> call, Throwable t) {
                Toast.makeText(NotificationActivity.this, "Lỗi tải thông báo từ SQL Server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onNotificationClick(NotificationModel notification) {
        // Mark as read in SQL Server
        int notifId = Integer.parseInt(notification.getId());
        ApiService apiService = RetrofitClient.getSqlService();
        apiService.markNotifAsRead(notifId).enqueue(new retrofit2.Callback<Map<String, String>>() {
            @Override
            public void onResponse(retrofit2.Call<Map<String, String>> call, retrofit2.Response<Map<String, String>> response) {}
            @Override
            public void onFailure(retrofit2.Call<Map<String, String>> call, Throwable t) {}
        });

        // Tải thông tin bài viết và chuyển hướng
        if (notification.getPostId() != null && !notification.getPostId().isEmpty()) {
            int pId = Integer.parseInt(notification.getPostId());
            apiService.getPostById(pId).enqueue(new retrofit2.Callback<Map<String, Object>>() {
                @Override
                public void onResponse(retrofit2.Call<Map<String, Object>> call, retrofit2.Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Intent intent = new Intent(NotificationActivity.this, PostDetailActivity.class);
                        intent.putExtra("post", new HashMap<>(response.body()));
                        intent.putExtra("postId", notification.getPostId());
                        startActivity(intent);
                    } else {
                        Toast.makeText(NotificationActivity.this, "Bài viết không còn tồn tại", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<Map<String, Object>> call, Throwable t) {
                    Toast.makeText(NotificationActivity.this, "Lỗi kết nối Server", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
