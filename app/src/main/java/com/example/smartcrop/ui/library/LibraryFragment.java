package com.example.smartcrop.ui.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.widget.Toast;

import com.example.smartcrop.R;
import com.example.smartcrop.api.ApiService;
import com.example.smartcrop.api.RetrofitClient;
import com.example.smartcrop.databinding.FragmentLibraryBinding;
import com.example.smartcrop.models.ChatResponse;
import com.example.smartcrop.models.DiseaseModel;
import com.example.smartcrop.utils.DiseaseProvider;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class LibraryFragment extends Fragment {

    private FragmentLibraryBinding binding;
    private LibraryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLibraryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.rvLibrary.setLayoutManager(new LinearLayoutManager(getContext()));
        
        List<DiseaseModel> diseases = DiseaseProvider.getAllDiseases();
        
        adapter = new LibraryAdapter(diseases);
        binding.rvLibrary.setAdapter(adapter);

        // Setup Search
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });

        binding.btnAskAI.setOnClickListener(v -> showAISearchDialog());
    }

    private void showAISearchDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_ai_chat, null);
        dialog.setContentView(view);

        android.widget.EditText etInput = view.findViewById(R.id.etAIQuestion);
        view.findViewById(R.id.btnAskAI).setOnClickListener(v -> {
            String question = etInput.getText().toString().trim();
            if (!question.isEmpty()) {
                dialog.dismiss();
                callChatAPI(question);
            }
        });

        dialog.show();
    }

    private void callChatAPI(String question) {
        Toast.makeText(getContext(), "Thần Nông AI đang phản hồi...", Toast.LENGTH_SHORT).show();
        
        ApiService apiService = RetrofitClient.getAiService();
        apiService.askAI(question).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showAIResponse(response.body().getResponse());
                } else {
                    Toast.makeText(getContext(), "Chuyên gia bận, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAIResponse(String answer) {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_ai_response, null);
        dialog.setContentView(view);

        android.widget.TextView tvAnswer = view.findViewById(R.id.tvAIAnswer);
        tvAnswer.setText(answer);

        view.findViewById(R.id.btnCloseAI).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
