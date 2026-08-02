package com.example.smartcrop.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartcrop.DiagnosisActivity;
import com.example.smartcrop.databinding.FragmentHomeBinding;
import com.example.smartcrop.ui.history.HistoryActivity;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.cardCamera.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), DiagnosisActivity.class);
            intent.putExtra("action", "camera");
            startActivity(intent);
        });

        binding.cardUpload.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), DiagnosisActivity.class);
            intent.putExtra("action", "upload");
            startActivity(intent);
        });

        binding.cardHistory.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), HistoryActivity.class));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
