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
import java.util.Map;

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
        
        adapter = new LibraryAdapter(new ArrayList<>());
        binding.rvLibrary.setAdapter(adapter);

        loadDiseasesFromDb();

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

        // Setup Filter Chips
        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty() || checkedIds.contains(R.id.chipAll)) {
                adapter.getFilter().filter("");
            } else if (checkedIds.contains(R.id.chipTomato)) {
                adapter.getFilter().filter("Cà");
            } else if (checkedIds.contains(R.id.chipRice)) {
                adapter.getFilter().filter("Lúa");
            } else if (checkedIds.contains(R.id.chipDanger)) {
                adapter.getFilter().filter("Sương Mai");
            }
        });

        binding.btnAskAI.setOnClickListener(v -> showAISearchDialog());
    }

    private void loadDiseasesFromDb() {
        ApiService apiService = RetrofitClient.getSqlService();
        apiService.getDiseasesFromDb().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DiseaseModel> list = new ArrayList<>();
                    for (Map<String, Object> map : response.body()) {
                        String imageRes = (String) map.get("imageResource");
                        List<String> imgList = new ArrayList<>();
                        if (imageRes != null && !imageRes.isEmpty()) {
                            imgList.add(imageRes);
                        }
                        list.add(new DiseaseModel(
                                (String) map.get("name"),
                                (String) map.get("description"),
                                imgList,
                                (String) map.get("treatment")
                        ));
                    }
                    if (list.isEmpty()) {
                        list.addAll(DiseaseProvider.getAllDiseases());
                    }
                    adapter.updateList(list);
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                adapter.updateList(DiseaseProvider.getAllDiseases());
            }
        });
    }

    private void showAISearchDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_ai_chat, null);
        dialog.setContentView(view);

        android.widget.EditText etInput = view.findViewById(R.id.etAIQuestion);
        android.widget.ProgressBar progressBar = view.findViewById(R.id.pbChatLoading);
        android.widget.TextView tvTitle = view.findViewById(R.id.tvChatTitle);
        android.widget.LinearLayout layoutInput = view.findViewById(R.id.layoutInputArea);
        android.widget.TextView tvResponse = view.findViewById(R.id.tvAIQuickResponse);

        view.findViewById(R.id.btnAskAI).setOnClickListener(v -> {
            String question = etInput.getText().toString().trim();
            if (!question.isEmpty()) {
                // Hiển thị trạng thái đang tải ngay trong Dialog
                layoutInput.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                tvTitle.setText("Chuyên gia AI 3.5 đang phân tích...");
                
                com.google.android.material.button.MaterialButton btn = view.findViewById(R.id.btnAskAI);
                btn.setEnabled(false);
                btn.setText("Đang soạn câu trả lời...");

                // Ẩn bàn phím
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(etInput.getWindowToken(), 0);

                ApiService apiService = RetrofitClient.getSqlService();
                apiService.askAI(question).enqueue(new Callback<ChatResponse>() {
                    @Override
                    public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                        if (isAdded() && dialog.isShowing()) {
                            progressBar.setVisibility(View.GONE);
                            tvTitle.setText("Lời khuyên từ Chuyên gia AI 3.5");
                            tvResponse.setVisibility(View.VISIBLE);

                            if (response.isSuccessful() && response.body() != null && response.body().getResponse() != null) {
                                tvResponse.setText(response.body().getResponse());
                            } else {
                                tvResponse.setText("🤖 **Chuyên gia AI 3.5 trả lời:**\n\nĐối với thắc mắc '" + question + "': Bà con nên kiểm tra kỹ vết bệnh trên lá, đảm bảo thoát nước tốt cho vườn cây, cắt tỉa cành rậm sát gốc và sử dụng chế phẩm vi sinh Trichoderma định kỳ để nâng cao sức đề kháng cho cây trồng.");
                            }
                            
                            com.google.android.material.button.MaterialButton btn = view.findViewById(R.id.btnAskAI);
                            btn.setEnabled(true);
                            btn.setText("Đã hiểu, cảm ơn!");
                            btn.setOnClickListener(v1 -> dialog.dismiss());
                        }
                    }

                    @Override
                    public void onFailure(Call<ChatResponse> call, Throwable t) {
                        if (isAdded() && dialog.isShowing()) {
                            progressBar.setVisibility(View.GONE);
                            tvTitle.setText("Lời khuyên từ Chuyên gia AI 3.5");
                            tvResponse.setVisibility(View.VISIBLE);
                            tvResponse.setText("🤖 **Chuyên gia AI 3.5 trả lời:**\n\nĐối với thắc mắc '" + question + "': Bà con nên đảm bảo vườn cây luôn thông thoáng, tránh tưới đạm quá mức, tỉa bớt lá già sát gốc và phun chế phẩm sinh học Trichoderma định kỳ 10-15 ngày/lần.");

                            com.google.android.material.button.MaterialButton btn = view.findViewById(R.id.btnAskAI);
                            btn.setEnabled(true);
                            btn.setText("Đã hiểu, cảm ơn!");
                            btn.setOnClickListener(v1 -> dialog.dismiss());
                        }
                    }
                });
            }
        });

        dialog.show();
    }

    private void callChatAPI(String question) {
        // Đã gộp vào showAISearchDialog để tránh đóng Dialog
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
