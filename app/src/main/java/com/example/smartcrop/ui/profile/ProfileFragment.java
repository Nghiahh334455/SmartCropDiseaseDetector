package com.example.smartcrop.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.smartcrop.databinding.FragmentProfileBinding;
import com.example.smartcrop.ui.admin.AdminDashboardActivity;
import com.example.smartcrop.ui.auth.LoginActivity;
import com.example.smartcrop.ui.history.HistoryActivity;
import com.example.smartcrop.utils.FirebaseUtils;
import com.example.smartcrop.utils.ImageUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

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

        mAuth = FirebaseUtils.getAuth();

        updateUserInfo();

        binding.btnHistory.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), HistoryActivity.class));
        });

        binding.btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), EditProfileActivity.class));
        });

        binding.btnLogout.setOnClickListener(v -> {
            if (mAuth != null) {
                mAuth.signOut();
            }
            if (getContext() != null) {
                getContext().getSharedPreferences("SmartCropPrefs", android.content.Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean("is_admin", false)
                        .putString("admin_uid", null)
                        .putString("user_uid", null)
                        .apply();
            }
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        binding.btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), NotificationActivity.class));
        });

        binding.btnAdminDashboard.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), AdminDashboardActivity.class));
        });

        binding.btnAdminDashboard.setVisibility(View.GONE);
        binding.dividerAdmin.setVisibility(View.GONE);

        boolean isAdmin = getContext().getSharedPreferences("SmartCropPrefs", android.content.Context.MODE_PRIVATE)
                .getBoolean("is_admin", false);
        if (isAdmin) {
            binding.btnAdminDashboard.setVisibility(View.VISIBLE);
            binding.dividerAdmin.setVisibility(View.VISIBLE);
        }

        listenForUnreadNotifications();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUserInfo();
    }

    private void updateUserInfo() {
        if (!isAdded() || binding == null || getContext() == null) return;

        String uid = FirebaseUtils.getUid(getContext());
        FirebaseUser currentUser = FirebaseUtils.getCurrentUser();

        boolean isAdmin = getContext().getSharedPreferences("SmartCropPrefs", android.content.Context.MODE_PRIVATE)
                .getBoolean("is_admin", false);

        if (isAdmin) {
            binding.tvProfileName.setText("Quản trị viên");
            binding.tvProfileEmail.setText("admin@thannongai.vn");
        } else if (currentUser != null) {
            binding.tvProfileEmail.setText(currentUser.getEmail());
            if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                binding.tvProfileName.setText(currentUser.getDisplayName());
            }
        }

        if (uid != null) {
            String photoData = getContext().getSharedPreferences("SmartCropPrefs", android.content.Context.MODE_PRIVATE)
                    .getString("profile_image_" + uid, null);

            if (photoData != null && !photoData.isEmpty()) {
                ImageUtils.loadUserAvatar(getContext(), binding.ivProfile, photoData);
            } else if (currentUser != null && currentUser.getPhotoUrl() != null) {
                ImageUtils.loadUserAvatar(getContext(), binding.ivProfile, currentUser.getPhotoUrl().toString());
            } else {
                fetchAvatarFromSql(uid);
            }
        } else {
            ImageUtils.loadUserAvatar(getContext(), binding.ivProfile, null);
        }
    }

    private void fetchAvatarFromSql(String uid) {
        com.example.smartcrop.api.RetrofitClient.getSqlService().getUser(uid).enqueue(new retrofit2.Callback<java.util.Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<java.util.Map<String, Object>> call, @NonNull retrofit2.Response<java.util.Map<String, Object>> response) {
                if (isAdded() && binding != null && getContext() != null && response.isSuccessful() && response.body() != null) {
                    String photoBase64 = (String) response.body().get("photoBase64");
                    if (photoBase64 != null && !photoBase64.isEmpty()) {
                        getContext().getSharedPreferences("SmartCropPrefs", android.content.Context.MODE_PRIVATE)
                                .edit()
                                .putString("profile_image_" + uid, photoBase64)
                                .apply();
                        ImageUtils.loadUserAvatar(getContext(), binding.ivProfile, photoBase64);
                    } else {
                        ImageUtils.loadUserAvatar(getContext(), binding.ivProfile, null);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<java.util.Map<String, Object>> call, @NonNull Throwable t) {
                if (isAdded() && binding != null && getContext() != null) {
                    ImageUtils.loadUserAvatar(getContext(), binding.ivProfile, null);
                }
            }
        });
    }

    private void listenForUnreadNotifications() {
        String uid = com.example.smartcrop.utils.FirebaseUtils.getUid(getContext());
        if (uid == null) return;

        FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .collection("notifications")
                .whereEqualTo("read", false)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    if (isAdded() && binding != null) {
                        binding.viewNotificationBadge.setVisibility(value.size() > 0 ? View.VISIBLE : View.GONE);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
