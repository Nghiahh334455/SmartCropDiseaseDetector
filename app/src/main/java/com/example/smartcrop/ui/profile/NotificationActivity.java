package com.example.smartcrop.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

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

        FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    notificationList.clear();
                    notificationIds.clear();
                    for (DocumentSnapshot doc : value) {
                        NotificationModel notification = doc.toObject(NotificationModel.class);
                        if (notification != null) {
                            notification.setId(doc.getId());
                            notificationList.add(notification);
                            notificationIds.add(doc.getId());
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void onNotificationClick(NotificationModel notification) {
        // Mark as read
        FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("notifications")
                .document(notification.getId())
                .update("read", true);

        // Fetch post and navigate
        FirebaseFirestore.getInstance().collection("forum_posts")
                .document(notification.getPostId())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Map<String, Object> postData = doc.getData();
                        Intent intent = new Intent(this, PostDetailActivity.class);
                        intent.putExtra("post", (Serializable) postData);
                        intent.putExtra("postId", doc.getId());
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Bài viết này không còn tồn tại", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
