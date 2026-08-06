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

        binding.ivAskAI.setOnClickListener(v -> showAISearchDialog());
    }

    private void showAISearchDialog() {
        android.widget.EditText etInput = new android.widget.EditText(getContext());
        etInput.setHint("Ví dụ: Cách trị bệnh đạo ôn lúa...");
        etInput.setPadding(40, 40, 40, 40);

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Hỏi Chuyên Gia Thần Nông AI")
                .setView(etInput)
                .setPositiveButton("Hỏi AI", (dialog, which) -> {
                    String question = etInput.getText().toString().trim();
                    if (!question.isEmpty()) {
                        callChatAPI(question);
                    }
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void callChatAPI(String question) {
        Toast.makeText(getContext(), "Đang hỏi chuyên gia AI...", Toast.LENGTH_SHORT).show();
        
        ApiService apiService = RetrofitClient.getApiService();
        apiService.askAI(question).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showAIResponse(response.body().getResponse());
                } else {
                    Toast.makeText(getContext(), "AI bận, vui lòng thử lại sau!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAIResponse(String answer) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Lời khuyên từ Thần Nông AI")
                .setMessage(answer)
                .setPositiveButton("Đã hiểu", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
