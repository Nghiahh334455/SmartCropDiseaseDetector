package com.example.smartcrop.ui.admin;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.smartcrop.api.ApiService;
import com.example.smartcrop.api.RetrofitClient;
import com.example.smartcrop.databinding.ActivityManagePostsBinding;
import com.example.smartcrop.ui.forum.ForumAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagePostsActivity extends AppCompatActivity {

    private ActivityManagePostsBinding binding;
    private List<Map<String, Object>> postList = new ArrayList<>();
    private List<String> postIds = new ArrayList<>();
    private ForumAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManagePostsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarManagePosts.setNavigationOnClickListener(v -> finish());

        adapter = new ForumAdapter(this, postList, postIds);
        binding.rvManagePosts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvManagePosts.setAdapter(adapter);

        // Add long click to delete in adapter or handle it here?
        // Let's modify ForumAdapter to optionally support admin deletion
        loadPosts();
    }

    private void loadPosts() {
        ApiService apiService = RetrofitClient.getSqlService();
        apiService.getPosts().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    postList.clear();
                    postIds.clear();
                    for (Map<String, Object> post : response.body()) {
                        postList.add(post);
                        postIds.add(String.valueOf(post.get("id")));
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(ManagePostsActivity.this, "Lỗi tải bài viết", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
