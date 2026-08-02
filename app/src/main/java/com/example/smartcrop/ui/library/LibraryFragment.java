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

import com.example.smartcrop.databinding.FragmentLibraryBinding;
import com.example.smartcrop.models.DiseaseModel;

import java.util.ArrayList;
import java.util.List;

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
        
        List<DiseaseModel> diseases = new ArrayList<>();
        
        diseases.add(new DiseaseModel(
                "Bệnh đạo ôn lúa", 
                "Bệnh do nấm Pyricularia oryzae gây ra, xuất hiện các vết đốm hình mắt én trên lá, có màu xám tro ở giữa và nâu đỏ ở mép.", 
                "https://nongnghiep.vn/files/news/2021/04/01/dao-on-la-1617260544.jpg",
                "1. Sử dụng giống kháng bệnh.\n2. Không bón quá nhiều phân đạm.\n3. Khi bệnh xuất hiện, ngưng bón đạm và phun các thuốc như: Beam, Filia, Fuji-one."));

        diseases.add(new DiseaseModel(
                "Bệnh sương mai Tomato", 
                "Vết bệnh ban đầu là những đốm nhỏ màu xanh tái, sau đó lan rộng thành những mảng lớn màu nâu đen, làm lá bị cháy khô.", 
                "https://nongnghiep.farm/wp-content/uploads/2023/12/benh-suong-mai-ca-chua-Late-Blight.jpg",
                "1. Vệ sinh đồng ruộng sạch sẽ.\n2. Trồng mật độ vừa phải, thoáng khí.\n3. Sử dụng các loại thuốc gốc đồng hoặc hoạt chất Metalaxyl, Mancozeb."));

        diseases.add(new DiseaseModel(
                "Bệnh héo xanh vi khuẩn", 
                "Cây đang xanh tốt bỗng dưng héo rũ đột ngột vào ban ngày và tươi lại vào ban đêm, sau vài ngày cây chết hẳn.", 
                "https://nongnghiep.farm/wp-content/uploads/2023/11/benh-heo-xanh-vi-khuan.jpg",
                "1. Luân canh cây trồng khác họ.\n2. Xử lý đất bằng vôi bột trước khi trồng.\n3. Khi phát hiện cây bệnh phải nhổ bỏ và tiêu hủy ngay."));

        diseases.add(new DiseaseModel(
                "Bệnh bạc lá lúa", 
                "Do vi khuẩn Xanthomonas oryzae gây ra, vết bệnh bắt đầu từ mép lá rồi lan dần vào trong, có màu vàng trắng.", 
                "https://cdn.tgdd.vn/Files/2021/08/21/1376822/benh-bac-la-lua-la-gi-nguyen-nhan-va-cach-phong-tru-hieu-qua-202108212132333792.jpg",
                "1. Chọn giống kháng vi khuẩn.\n2. Bón phân cân đối N-P-K.\n3. Sử dụng các thuốc đặc trị vi khuẩn như: Xanthomix, Totan."));

        diseases.add(new DiseaseModel(
                "Bệnh rỉ sắt cà phê", 
                "Mặt dưới lá xuất hiện các đốm nhỏ màu vàng nhạt, sau đó phủ một lớp bột màu da cam như rỉ sắt.", 
                "https://nongnghiep.farm/wp-content/uploads/2023/12/benh-ri-sat-ca-phe.jpg",
                "1. Tỉa cành tạo tán cho thông thoáng.\n2. Bón phân đầy đủ để tăng sức đề kháng.\n3. Phun các loại thuốc chứa hoạt chất Hexaconazole hoặc gốc đồng."));
        
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
