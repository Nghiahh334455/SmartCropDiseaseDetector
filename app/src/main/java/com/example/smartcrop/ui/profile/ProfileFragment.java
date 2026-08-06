package com.example.smartcrop.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.smartcrop.databinding.FragmentProfileBinding;
import com.example.smartcrop.ui.auth.LoginActivity;
import com.example.smartcrop.ui.history.HistoryActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        binding.rvSharedPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSavedPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        loadSharedPosts();
        loadSavedPosts();

        if (currentUser != null) {
            binding.tvProfileEmail.setText(currentUser.getEmail());
            if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                binding.tvProfileName.setText(currentUser.getDisplayName());
            }
            
            // 1. Kiểm tra ảnh lưu cục bộ trước
            String localPath = getContext().getSharedPreferences("SmartCropPrefs", android.content.Context.MODE_PRIVATE)
                    .getString("profile_image_" + currentUser.getUid(), null);

            if (localPath != null && new File(localPath).exists()) {
                Glide.with(this)
                        .load(new File(localPath))
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(binding.ivProfile);
            } else if (currentUser.getPhotoUrl() != null) {
                // 2. Nếu không có ảnh cục bộ, thử load từ Firebase (nếu có)
                Glide.with(this)
                        .load(currentUser.getPhotoUrl())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(binding.ivProfile);
            }
        }

        binding.btnHistory.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), HistoryActivity.class));
        });

        binding.btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), EditProfileActivity.class));
        });

        binding.btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        binding.btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), NotificationActivity.class));
        });

        listenForUnreadNotifications();
    }

    private void listenForUnreadNotifications() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .collection("notifications")
                .whereEqualTo("read", false)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    if (isAdded()) {
                        binding.viewNotificationBadge.setVisibility(value.size() > 0 ? View.VISIBLE : View.GONE);
                    }
                });
    }

    private void loadSharedPosts() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore.getInstance().collection("user_shares")
                .whereEqualTo("sharedByUid", uid)
                .orderBy("shareTimestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> shares = new ArrayList<>();
                    List<String> ids = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        shares.add(doc.getData());
                        ids.add(doc.getId());
                    }
                    if (isAdded()) {
                        SharedPostAdapter adapter = new SharedPostAdapter(shares, ids, (position, docId) -> {
                            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                                    .setTitle("Xác nhận xóa")
                                    .setMessage("Bạn có chắc chắn muốn xóa bài viết đã chia sẻ này không?")
                                    .setPositiveButton("Xóa", (dialog, which) -> deleteItem("user_shares", docId))
                                    .setNegativeButton("Hủy", null)
                                    .show();
                        });
                        binding.rvSharedPosts.setAdapter(adapter);
                    }
                });
    }

    private void loadSavedPosts() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore.getInstance().collection("user_saved_posts")
                .whereEqualTo("savedByUid", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> saved = new ArrayList<>();
                    List<String> ids = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        saved.add(doc.getData());
                        ids.add(doc.getId());
                    }
                    if (isAdded()) {
                        SharedPostAdapter adapter = new SharedPostAdapter(saved, ids, (position, docId) -> {
                            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                                    .setTitle("Xác nhận xóa")
                                    .setMessage("Bạn có chắc chắn muốn xóa bài viết đã lưu này không?")
                                    .setPositiveButton("Xóa", (dialog, which) -> deleteItem("user_saved_posts", docId))
                                    .setNegativeButton("Hủy", null)
                                    .show();
                        });
                        binding.rvSavedPosts.setAdapter(adapter);
                    }
                });
    }

    private void deleteItem(String collection, String docId) {
        FirebaseFirestore.getInstance().collection(collection).document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Đã xóa mục này", Toast.LENGTH_SHORT).show();
                    if (collection.equals("user_shares")) loadSharedPosts();
                    else loadSavedPosts();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
