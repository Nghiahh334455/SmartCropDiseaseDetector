package com.example.smartcrop.ui.history;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.smartcrop.database.AppDatabase;
import com.example.smartcrop.database.HistoryEntity;
import com.example.smartcrop.databinding.ActivityHistoryBinding;

import java.util.List;
import java.util.concurrent.Executors;

public class HistoryActivity extends AppCompatActivity {

    private ActivityHistoryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarHistory);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbarHistory.setNavigationOnClickListener(v -> finish());
        }

        binding.rvHistory.setLayoutManager(new LinearLayoutManager(this));
        loadHistory();
    }

    private void loadHistory() {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        
        Executors.newSingleThreadExecutor().execute(() -> {
            List<HistoryEntity> historyList = AppDatabase.getInstance(this).historyDao().getHistoryByUid(uid);
            runOnUiThread(() -> {
                HistoryAdapter adapter = new HistoryAdapter(historyList);
                binding.rvHistory.setAdapter(adapter);
            });
        });
    }
}
