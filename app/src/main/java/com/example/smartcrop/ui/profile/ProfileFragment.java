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

import com.bumptech.glide.Glide;
import com.example.smartcrop.databinding.FragmentProfileBinding;
import com.example.smartcrop.ui.auth.LoginActivity;
import com.example.smartcrop.ui.history.HistoryActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;

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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
