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

        // Load user info
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName() != null ? user.getDisplayName() : "Chào bạn!";
            binding.tvWelcome.setText(name);

            String localPath = getContext().getSharedPreferences("SmartCropPrefs", android.content.Context.MODE_PRIVATE)
                    .getString("profile_image_" + user.getUid(), null);

            if (localPath != null && new File(localPath).exists()) {
                Glide.with(this).load(new File(localPath)).into(binding.ivHomeAvatar);
            } else if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).into(binding.ivHomeAvatar);
            }
        }

        // Common Diseases
        binding.rvCommonDiseases.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        loadCommonDiseases();

        // Care Tips ViewPager2
        setupTipsSlider();

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

    private void setupTipsSlider() {
        tipsList = new ArrayList<>();
        tipsList.add(new TipModel("Tưới nước đúng cách", "Tránh tưới nước lên lá vào chiều tối để hạn chế nấm bệnh phát triển.", "img_tip_water"));
        tipsList.add(new TipModel("Bón phân cân đối", "Không nên bón quá nhiều đạm, hãy tăng cường Kali để cây cứng cáp hơn.", "img_tip_fertilizer"));
        tipsList.add(new TipModel("Kiểm tra vườn sáng sớm", "Đây là thời điểm tốt nhất để phát hiện các triệu chứng bệnh mới xuất hiện.", "img_tip_check"));
        tipsList.add(new TipModel("Vệ sinh dụng cụ", "Luôn khử trùng kéo cắt cành sau khi tỉa cây bệnh để tránh lây chéo.", "img_tip_tools"));
        tipsList.add(new TipModel("Cải tạo đất", "Bón vôi định kỳ để khử trùng đất và cân bằng độ pH cho cây trồng.", "img_tip_soil"));
        tipsList.add(new TipModel("Sử dụng phân hữu cơ", "Giúp đất tơi xốp và tăng cường hệ vi sinh vật có lợi cho bộ rễ.", "img_tip_organic"));
        tipsList.add(new TipModel("Mật độ trồng", "Không nên trồng quá dày để đảm bảo vườn luôn thông thoáng, tránh ẩm thấp.", "img_tip_density"));
        tipsList.add(new TipModel("Luân canh cây trồng", "Thay đổi loại cây mỗi vụ để cắt đứt nguồn sâu bệnh lưu trú trong đất.", "img_tip_rotation"));
        tipsList.add(new TipModel("Diệt bọ phấn trắng", "Sử dụng bẫy dính vàng để kiểm soát vật trung gian truyền bệnh virus.", "img_tip_pest"));
        tipsList.add(new TipModel("Tỉa cành tạo tán", "Loại bỏ cành vô hiệu, cành sâu bệnh giúp cây tập trung nuôi quả tốt hơn.", "img_tip_pruning"));
        tipsList.add(new TipModel("Xử lý hạt giống", "Ngâm hạt trong nước ấm (2 sôi 3 lạnh) để kích thích nảy mầm và diệt khuẩn.", "img_tip_seed"));
        tipsList.add(new TipModel("Phun thuốc đúng lúc", "Chỉ nên phun thuốc vào lúc sáng sớm hoặc chiều mát khi trời lặng gió.", "img_tip_timing"));
        tipsList.add(new TipModel("Chế phẩm sinh học", "Ưu tiên dùng nấm đối kháng Trichoderma để bảo vệ bộ rễ khỏi nấm bệnh.", "img_tip_bio"));
        tipsList.add(new TipModel("Kiểm soát cỏ dại", "Luôn dọn sạch cỏ dại xung quanh gốc cây vì đây là nơi trú ngụ của sâu bệnh.", "img_tip_weeds"));
        tipsList.add(new TipModel("Ủ phân tại nhà", "Tận dụng rác thải nhà bếp để làm phân bón hữu cơ sạch cho vườn nhà.", "img_tip_compost"));
        tipsList.add(new TipModel("Bón phân qua lá", "Giúp cây hấp thụ dinh dưỡng nhanh hơn sau các đợt bị dịch bệnh.", "img_tip_foliar"));
        tipsList.add(new TipModel("Tưới nhỏ giọt", "Giúp tiết kiệm nước và giữ bề mặt lá khô ráo, hạn chế nấm bệnh.", "img_tip_drip"));
        tipsList.add(new TipModel("Rãnh thoát nước", "Đảm bảo ruộng vườn có hệ thống thoát nước tốt tránh ngập úng rễ.", "img_tip_drainage"));
        tipsList.add(new TipModel("Sâu đục thân", "Thăm vườn thường xuyên, xử lý ngay bằng thuốc lưu dẫn khi thấy lỗ đục.", "img_tip_borer"));
        tipsList.add(new TipModel("Bón phân đón hoa", "Tăng cường bón thêm Lân và vi lượng khi cây bắt đầu nhú nụ hoa.", "img_tip_flower"));
        tipsList.add(new TipModel("Bảo vệ thiên địch", "Hạn chế lạm dụng thuốc hóa học để bảo vệ kiến vàng, bọ rùa.", "img_tip_natural"));
        tipsList.add(new TipModel("Phòng bệnh sau mưa", "Sau các đợt mưa dông kéo dài nên phun thuốc gốc đồng phòng nấm.", "img_tip_rain"));
        tipsList.add(new TipModel("Nhật ký đồng ruộng", "Ghi chép lịch bón phân và dịch bệnh để rút kinh nghiệm vụ sau.", "img_tip_records"));
        tipsList.add(new TipModel("Kiểm soát độ ẩm", "Lưu ý hệ thống thông gió nhà màng để tránh các bệnh mốc lá.", "img_tip_humidity"));
        tipsList.add(new TipModel("Che phủ màng", "Sử dụng màng phủ nông nghiệp để giữ ẩm và hạn chế cỏ dại.", "img_tip_mulch"));
        tipsList.add(new TipModel("Nhận biết thiếu Đạm", "Lá vàng toàn bộ từ già đến non, cây lùn còi là biểu hiện thiếu Đạm.", "img_tip_nitrogen"));
        tipsList.add(new TipModel("Nhận biết thiếu Lân", "Lá chuyển màu tím đỏ hoặc xanh sậm lạ thường là biểu hiện thiếu Lân.", "img_tip_phosphorus"));
        tipsList.add(new TipModel("Nhận biết thiếu Kali", "Mép lá bị cháy khô như bị hơ lửa là triệu chứng điển hình thiếu Kali.", "img_tip_potassium"));
        tipsList.add(new TipModel("Dùng bẫy bả", "Sử dụng bả thực phẩm dẫn dụ ruồi đục quả tránh lạm dụng thuốc phun.", "img_tip_trap"));
        tipsList.add(new TipModel("Tưới nước sáng sớm", "Giúp lá nhanh khô, hạn chế nấm bệnh phát triển so với tưới chiều tối.", "img_tip_morning"));

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
        ApiService apiService = RetrofitClient.getApiService();
        apiService.getTopDiseases().enqueue(new retrofit2.Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Map<String, Object>>> call, retrofit2.Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (isAdded()) {
                        CommonDiseaseAdapter adapter = new CommonDiseaseAdapter(response.body());
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

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, 5000); // Đổi thành 5s
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
