package com.example.smartcrop.ui.forum;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.widget.Toast;

import com.example.smartcrop.api.ApiService;
import com.example.smartcrop.api.RetrofitClient;
import com.example.smartcrop.databinding.FragmentForumBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForumFragment extends Fragment {

    private FragmentForumBinding binding;
    private ForumAdapter adapter;
    private List<Map<String, Object>> postList = new ArrayList<>();
    private List<String> postIds = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentForumBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.rvForum.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ForumAdapter(getContext(), postList, postIds);
        binding.rvForum.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(this::listenForPosts);
        binding.swipeRefresh.setColorSchemeResources(com.google.android.material.R.color.design_default_color_primary);

        binding.ivAddPost.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), CreatePostActivity.class));
        });

        listenForPosts();
    }

    private void listenForPosts() {
        binding.swipeRefresh.setRefreshing(true);
        
        ApiService apiService = RetrofitClient.getApiService();
        apiService.getPosts().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (isAdded()) binding.swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    postList.clear();
                    postIds.clear();
                    for (Map<String, Object> post : response.body()) {
                        postList.add(post);
                        // SQL ID is integer, convert to string for compatibility with existing adapter if needed
                        postIds.add(String.valueOf(post.get("id")));
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                if (isAdded()) binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(getContext(), "Lỗi tải dữ liệu từ SQL Server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
