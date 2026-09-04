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
        boolean isAdmin = getSharedPreferences("SmartCropPrefs", MODE_PRIVATE).getBoolean("is_admin", false);
        com.google.firebase.auth.FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = isAdmin ? "12345N" : (user != null ? user.getUid() : getSharedPreferences("SmartCropPrefs", MODE_PRIVATE).getString("user_uid", null));

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

    private int parseId(String idStr) {
        if (idStr == null) return 0;
        try {
            return (int) Double.parseDouble(idStr);
        } catch (Exception e) {
            return 0;
        }
    }

    private void onNotificationClick(NotificationModel notification) {
        // Mark as read in SQL Server
        int notifId = parseId(notification.getId());
        if (notifId > 0) {
            ApiService apiService = RetrofitClient.getSqlService();
            apiService.markNotifAsRead(notifId).enqueue(new retrofit2.Callback<Map<String, String>>() {
                @Override
                public void onResponse(retrofit2.Call<Map<String, String>> call, retrofit2.Response<Map<String, String>> response) {}
                @Override
                public void onFailure(retrofit2.Call<Map<String, String>> call, Throwable t) {}
            });
        }

        // Chuyển hướng trực tiếp đến bài viết
        if (notification.getPostId() != null && !notification.getPostId().isEmpty()) {
            Intent intent = new Intent(NotificationActivity.this, PostDetailActivity.class);
            intent.putExtra("postId", String.valueOf(parseId(notification.getPostId())));
            startActivity(intent);
        }
    }
}
