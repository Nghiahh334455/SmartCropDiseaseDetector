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

import com.example.smartcrop.databinding.FragmentForumBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        FirebaseFirestore.getInstance().collection("forum_posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (isAdded()) binding.swipeRefresh.setRefreshing(false);
                    if (error != null || value == null) return;

                    postList.clear();
                    postIds.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : value) {
                        postList.add(doc.getData());
                        postIds.add(doc.getId());
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
