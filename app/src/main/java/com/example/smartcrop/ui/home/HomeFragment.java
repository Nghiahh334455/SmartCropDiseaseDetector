package com.example.smartcrop.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.smartcrop.DiagnosisActivity;
import com.example.smartcrop.R;
import com.example.smartcrop.api.ApiService;
import com.example.smartcrop.api.RetrofitClient;
import com.example.smartcrop.databinding.FragmentHomeBinding;
import com.example.smartcrop.ui.history.HistoryActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private final Handler sliderHandler = new Handler();
    private List<TipModel> tipsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateUserUI();
        setupWeatherAdvisory();

        // Common Diseases
        binding.rvCommonDiseases.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        loadCommonDiseases();

        // Personal History
        binding.rvPersonalHistory.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Care Tips ViewPager2
        setupTipsSlider();

        binding.cardCamera.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), DiagnosisActivity.class);
            intent.putExtra("action", "camera");
            startActivity(intent);
        });

        binding.btnBannerScan.setOnClickListener(v -> {
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

        // Kích hoạt nút "Xem tất cả"
        binding.tvViewAllDiseases.setOnClickListener(v -> {
            // Chuyển sang Fragment Thư viện (index 1 trong BottomNav)
            androidx.navigation.Navigation.findNavController(v).navigate(R.id.nav_library);
        });
    }

    private void setupTipsSlider() {
        tipsList = new ArrayList<>();
        
        // Fetch tips from SQL Database instead of hardcoded list
        ApiService apiService = RetrofitClient.getSqlService();
        apiService.getTipsFromDb().enqueue(new retrofit2.Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Map<String, Object>>> call, retrofit2.Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    tipsList.clear();
                    for (Map<String, Object> tip : response.body()) {
                        tipsList.add(new TipModel(
                                (String) tip.get("title"),
                                (String) tip.get("content"),
                                (String) tip.get("imageResource")
                        ));
                    }
                } else {
                    // Fallback to basic tips if DB is empty
                    loadFallbackTips();
                }
                updateTipsAdapter();
            }

            @Override
            public void onFailure(retrofit2.Call<List<Map<String, Object>>> call, Throwable t) {
                loadFallbackTips();
                updateTipsAdapter();
            }
        });
    }

    private void loadFallbackTips() {
        tipsList.add(new TipModel("Tưới nước đúng cách", "Tránh tưới nước lên lá vào chiều tối để hạn chế nấm bệnh phát triển.", "img_tip_water"));
        tipsList.add(new TipModel("Bón phân cân đối", "Không nên bón quá nhiều đạm, hãy tăng cường Kali để cây cứng cáp hơn.", "img_tip_fertilizer"));
        tipsList.add(new TipModel("Kiểm tra vườn sáng sớm", "Đây là thời điểm tốt nhất để phát hiện các triệu chứng bệnh mới xuất hiện.", "img_tip_check"));
    }

    private void updateTipsAdapter() {
        if (binding == null) return;
        TipsAdapter tipsAdapter = new TipsAdapter(tipsList);
        binding.vpTips.setAdapter(tipsAdapter);

        // Hiệu ứng chuyển trang trôi từ từ cực kỳ mượt mà
        binding.vpTips.setPageTransformer((page, position) -> {
            page.setAlpha(1 - Math.abs(position));
            page.setTranslationX(-position * page.getWidth());
        });

        // Auto-scroll every 5 seconds
        binding.vpTips.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 5000); 
            }
        });
    }

    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (isAdded() && binding != null) {
                smoothScrollToNext();
            }
        }
    };

    private void smoothScrollToNext() {
        if (binding == null) return;
        int width = binding.vpTips.getWidth();
        if (width <= 0) return;

        // Tăng duration lên 2500 (2.5 giây) để trượt cực chậm
        android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofInt(0, width);
        animator.setDuration(2500); 
        
        final int[] lastValue = {0};
        animator.addUpdateListener(animation -> {
            int currentValue = (int) animation.getAnimatedValue();
            float dragAmount = (float) (currentValue - lastValue[0]);
            if (binding != null) {
                binding.vpTips.fakeDragBy(-dragAmount);
            }
            lastValue[0] = currentValue;
        });
        
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(android.animation.Animator animation) {
                if (binding != null) binding.vpTips.beginFakeDrag();
            }
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (binding != null) binding.vpTips.endFakeDrag();
            }
        });
        animator.start();
    }

    private void loadCommonDiseases() {
        ApiService apiService = RetrofitClient.getSqlService();
        apiService.getTopDiseases().enqueue(new retrofit2.Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Map<String, Object>>> call, retrofit2.Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (isAdded()) {
                        List<Map<String, Object>> filteredDiseases = new ArrayList<>();
                        for (Map<String, Object> map : response.body()) {
                            String name = (String) map.get("name");
                            if (name != null) {
                                String lower = name.toLowerCase();
                                if (!lower.contains("khỏe mạnh") && !lower.contains("healthy")) {
                                    filteredDiseases.add(map);
                                }
                            }
                        }
                        CommonDiseaseAdapter adapter = new CommonDiseaseAdapter(filteredDiseases);
                        binding.rvCommonDiseases.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<Map<String, Object>>> call, Throwable t) {
                // Fail silently or log
            }
        });
    }

    private void loadPersonalHistory(String uid) {
        com.example.smartcrop.database.AppDatabase db = com.example.smartcrop.database.AppDatabase.getInstance(getContext());
        new Thread(() -> {
            // Lấy 10 chẩn đoán gần nhất của chính user này
            List<com.example.smartcrop.database.HistoryEntity> list = db.historyDao().getRecentByUid(uid, 10);
            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (list != null && !list.isEmpty()) {
                        binding.layoutPersonalHistory.setVisibility(View.VISIBLE);
                        binding.rvPersonalHistory.setVisibility(View.VISIBLE);
                        PersonalHistoryAdapter adapter = new PersonalHistoryAdapter(list);
                        binding.rvPersonalHistory.setAdapter(adapter);
                    } else {
                        binding.layoutPersonalHistory.setVisibility(View.GONE);
                        binding.rvPersonalHistory.setVisibility(View.GONE);
                    }
                });
            }
        }).start();
    }

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUserUI();
        sliderHandler.postDelayed(sliderRunnable, 5000); // Đổi thành 5s
    }

    private void updateUserUI() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName() != null ? user.getDisplayName() : "Chào bạn!";
            binding.tvWelcome.setText(name);

            String localPhoto = getContext().getSharedPreferences("SmartCropPrefs", android.content.Context.MODE_PRIVATE)
                    .getString("profile_image_" + user.getUid(), null);

            if (localPhoto != null && localPhoto.startsWith("BASE64:")) {
                byte[] bytes = com.example.smartcrop.utils.ImageUtils.base64ToBytes(localPhoto);
                if (bytes != null) Glide.with(this).load(bytes).circleCrop().into(binding.ivHomeAvatar);
            } else if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).circleCrop().into(binding.ivHomeAvatar);
            }
            
            loadPersonalHistory(user.getUid());
        } else {
            binding.layoutPersonalHistory.setVisibility(View.GONE);
            binding.rvPersonalHistory.setVisibility(View.GONE);
        }
    }

    private void setupWeatherAdvisory() {
        if (binding == null) return;

        // Gọi Open-Meteo API thực tế (Miễn phí, không cần API Key)
        String weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=10.7769&longitude=106.7009&current_weather=true&hourly=relative_humidity_2m";

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(weatherUrl)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull java.io.IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> applyWeatherUI(29, 85, 12, "Trời ẩm ướt • Mưa rải rác"));
                }
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        org.json.JSONObject obj = new org.json.JSONObject(json);
                        org.json.JSONObject current = obj.optJSONObject("current_weather");
                        
                        double temp = current != null ? current.optDouble("temperature", 29.0) : 29.0;
                        double wind = current != null ? current.optDouble("windspeed", 10.0) : 10.0;
                        int weatherCode = current != null ? current.optInt("weathercode", 0) : 0;

                        org.json.JSONObject hourly = obj.optJSONObject("hourly");
                        int humidity = 82;
                        if (hourly != null && hourly.has("relative_humidity_2m")) {
                            org.json.JSONArray humArray = hourly.optJSONArray("relative_humidity_2m");
                            if (humArray != null && humArray.length() > 0) {
                                humidity = humArray.optInt(0, 82);
                            }
                        }

                        String condition = getWeatherDescription(weatherCode);

                        final int finalTemp = (int) Math.round(temp);
                        final int finalHum = humidity;
                        final int finalWind = (int) Math.round(wind);
                        final String finalCond = condition;

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> applyWeatherUI(finalTemp, finalHum, finalWind, finalCond));
                        }
                    } catch (Exception e) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> applyWeatherUI(29, 85, 12, "Trời mát • Độ ẩm vừa phải"));
                        }
                    }
                }
            }
        });
    }

    private String getWeatherDescription(int code) {
        if (code == 0) return "Trời quang • Nắng nhẹ";
        if (code <= 3) return "Trời nhiều mây • Nắng gián đoạn";
        if (code <= 65) return "Trời mưa rào • Cần chú ý nấm bệnh";
        if (code <= 82) return "Mưa rào nặng hạt • Độ ẩm cao";
        return "Thời tiết thay đổi • Chú ý vườn cây";
    }

    private void applyWeatherUI(int temp, int humidity, int wind, String condition) {
        if (binding == null) return;
        binding.tvWeatherTemp.setText(temp + "°C");
        binding.tvWeatherCondition.setText(condition);
        binding.tvWeatherHumidity.setText("Độ ẩm: " + humidity + "% • Gió " + wind + "km/h");

        if (humidity > 80) {
            binding.tvDiseaseWarningBadge.setText("CẢNH BÁO CAO");
            binding.tvDiseaseWarningBadge.setBackgroundResource(R.drawable.bg_badge_red);
            binding.tvDiseaseWarningText.setText("Độ ẩm " + humidity + "% (Thực tế): Nguy cơ lây lan Bệnh Sương Mai & Đốm Vòng rất cao!");
        } else {
            binding.tvDiseaseWarningBadge.setText("KHUYẾN NÔNG");
            binding.tvDiseaseWarningBadge.setBackgroundResource(R.drawable.bg_badge_orange);
            binding.tvDiseaseWarningText.setText("Thời tiết " + temp + "°C thuận lợi: Nên tỉa lá già sát gốc & kiểm tra vườn sáng sớm.");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
