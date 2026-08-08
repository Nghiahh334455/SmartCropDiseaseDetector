package com.example.smartcrop;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.smartcrop.api.ApiService;
import com.example.smartcrop.api.RetrofitClient;
import com.example.smartcrop.databinding.ActivityMainV2Binding;
import com.google.android.material.badge.BadgeDrawable;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainV2Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainV2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNav, navController);
        }

        listenForNotifications();
    }

    private void listenForNotifications() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        ApiService apiService = RetrofitClient.getSqlService();
        apiService.getNotifications(uid).enqueue(new retrofit2.Callback<List<com.example.smartcrop.models.NotificationModel>>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<List<com.example.smartcrop.models.NotificationModel>> call, @NonNull retrofit2.Response<List<com.example.smartcrop.models.NotificationModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int unreadCount = 0;
                    for (com.example.smartcrop.models.NotificationModel n : response.body()) {
                        if (!n.isRead()) unreadCount++;
                    }
                    
                    BadgeDrawable badge = binding.bottomNav.getOrCreateBadge(R.id.nav_profile);
                    if (unreadCount > 0) {
                        badge.setVisible(true);
                        badge.setNumber(unreadCount);
                    } else {
                        badge.setVisible(false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<List<com.example.smartcrop.models.NotificationModel>> call, @NonNull Throwable t) {}
        });
    }
}
