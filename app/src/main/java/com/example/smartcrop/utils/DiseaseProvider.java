package com.example.smartcrop.utils;

import com.example.smartcrop.models.DiseaseModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DiseaseProvider {
    public static List<DiseaseModel> getAllDiseases() {
        List<DiseaseModel> diseases = new ArrayList<>();

        // --- NHÓM CÀ CHUA (TOMATO) ---
        diseases.add(new DiseaseModel(
                "Bệnh sương mai Cà chua",
                "TRIỆU CHỨNG: Xuất hiện vết úng nước màu xanh tái ở mép hoặc chóp lá, sau đó chuyển nâu đen. Mặt dưới lá có lớp nấm trắng xám. Bệnh lây lan cực nhanh, có thể làm cháy toàn bộ lá chỉ trong 2-3 ngày.\n\nĐIỀU KIỆN: Nhiệt độ 15-22°C, độ ẩm >90%, sương mù hoặc mưa phùn.",
                Arrays.asList("img_tomato_late_blight_1", "img_tomato_late_blight_2"),
                "ĐIỀU TRỊ: Phun ngay Ridomil Gold 68WG hoặc Mancozeb. Cắt tỉa lá già sát gốc. Tránh tưới phun mưa chiều tối."));

        diseases.add(new DiseaseModel(
                "Bệnh đốm vòng Cà chua",
                "TRIỆU CHỨNG: Đốm tròn màu nâu đen với các đường vòng đồng tâm đặc trưng như hình bia bắn. Xuất hiện trên lá già tầng dưới trước.\n\nNGUYÊN NHÂN: Nấm Alternaria solani phát triển mạnh khi thời tiết nóng ẩm xen kẽ mưa nắng.",
                Arrays.asList("img_tomato_early_blight_1"),
                "ĐIỀU TRỊ: Phun Daconil 75WP hoặc Score 250EC. Bón phân cân đối, tránh thừa đạm."));

        diseases.add(new DiseaseModel(
                "Bệnh mốc lá Cà chua",
                "TRIỆU CHỨNG: Mặt trên lá có đốm vàng nhạt, mặt dưới phủ lớp nấm mịn màu nâu xám sau chuyển nâu tím. Lá khô cháy và rụng.\n\nĐIỀU KIỆN: Độ ẩm không khí rất cao, thông gió kém trong nhà màng.",
                Arrays.asList("img_tomato_leaf_mold_1"),
                "ĐIỀU TRỊ: Tăng cường thông gió. Phun Anvil 5SC hoặc thuốc gốc đồng. Sử dụng màng phủ đất."));

        diseases.add(new DiseaseModel(
                "Bệnh xoăn lá Cà chua",
                "TRIỆU CHỨNG: Lá non nhỏ, mép cuốn cong lên trên hình chén, phiến lá dày và giòn. Cây lùn, không ra hoa hoặc rụng quả hàng loạt.\n\nNGUYÊN NHÂN: Virus TYLCV lây qua bọ phấn trắng.",
                Arrays.asList("img_tomato_leaf_curl_1"),
                "ĐIỀU TRỊ: Hiện không có thuốc trị virus. Nhổ bỏ cây bệnh nặng để tránh lây lan.\n\nPHÒNG NGỪA: Diệt bọ phấn trắng bằng bẫy dính vàng."));

        diseases.add(new DiseaseModel(
                "Bệnh héo rũ Fusarium",
                "TRIỆU CHỨNG: Lá vàng từ dưới lên, ban đầu héo một bên cây. Thân bị nâu đen phần bó mạch khi cắt ngang.\n\nNGUYÊN NHÂN: Nấm Fusarium oxysporum tồn tại lâu trong đất.",
                Arrays.asList("img_tomato_fusarium_wilt_1"),
                "ĐIỀU TRỊ: Luân canh cây trồng lúa/ngô. Sử dụng giống kháng. Bón vôi khử trùng đất trước khi trồng."));

        diseases.add(new DiseaseModel(
                "Bệnh đốm lá Septoria",
                "TRIỆU CHỨNG: Nhiều đốm nhỏ tròn, tâm xám trắng viền nâu đậm. Làm lá khô cháy và rụng hàng loạt từ dưới lên.\n\nNGUYÊN NHÂN: Nấm Septoria lycopersici phát triển khi trời mưa ẩm.",
                Arrays.asList("img_tomato_septoria_1"),
                "ĐIỀU TRỊ: Tránh tưới nước lên lá. Phun thuốc gốc đồng định kỳ. Dọn sạch tàn dư cây bệnh."));

        // --- NHÓM LÚA & NGÔ ---
        diseases.add(new DiseaseModel(
                "Bệnh đạo ôn lúa",
                "TRIỆU CHỨNG: Vết bệnh hình mắt én màu xám tro. Gây cháy lá, thối cổ bông làm hạt lép hoàn toàn.\n\nNGUYÊN NHÂN: Nấm Pyricularia oryzae mạnh khi trời mát, sương mù nhiều.",
                Arrays.asList("img_rice_blast_1"),
                "ĐIỀU TRỊ: Ngưng bón đạm. Phun Beam 75WP hoặc Filia. Giữ mực nước ổn định trong ruộng."));

        diseases.add(new DiseaseModel(
                "Bệnh bạc lá lúa",
                "TRIỆU CHỨNG: Vết bệnh bắt đầu từ mép lá, lan dần vào trong và kéo dài theo gân lá có màu vàng trắng. Lá bị cháy khô dọc theo mép lá như bị hơ lửa.\n\nNGUYÊN NHÂN: Vi khuẩn Xanthomonas oryzae lây lan mạnh sau mưa bão.",
                Arrays.asList("img_rice_bacterial_blight_1"),
                "ĐIỀU TRỊ: Phun các thuốc đặc trị vi khuẩn như Totan, Xanthomix. Tránh bón đạm giai đoạn bệnh."));

        diseases.add(new DiseaseModel(
                "Bệnh rỉ sắt ngô",
                "TRIỆU CHỨNG: Ổ nấm màu vàng cam hoặc nâu đỏ nổi trên mặt lá như rỉ sắt. Lá khô cháy, bắp nhỏ.\n\nĐIỀU KIỆN: Nhiệt độ 18-25°C, ẩm độ cao kéo dài.",
                Arrays.asList("img_corn_rust_1"),
                "ĐIỀU TRỊ: Phun Tilt Super 300EC hoặc Anvil. Chọn giống kháng rỉ sắt. Vệ sinh đồng ruộng."));

        // --- NHÓM CÂY ĂN QUẢ ---
        diseases.add(new DiseaseModel(
                "Bệnh loét cam quýt",
                "TRIỆU CHỨNG: Vết loét tròn nâu sần sùi nổi trên lá và quả, quầng vàng tươi xung quanh.\n\nNGUYÊN NHÂN: Vi khuẩn Xanthomonas citri xâm nhập qua vết cắn của sâu vẽ bùa.",
                Arrays.asList("img_citrus_canker_1"),
                "ĐIỀU TRỊ: Phun Kasuran 47WP hoặc Champion. Diệt sâu vẽ bùa khi cây ra đọt non."));

        diseases.add(new DiseaseModel(
                "Bệnh thán thư xoài",
                "TRIỆU CHỨNG: Đốm đen trên lá hoa và quả. Gây rụng hoa và thối đen trái hàng loạt.\n\nNGUYÊN NHÂN: Nấm Colletotrichum phát triển mạnh mùa mưa ẩm.",
                Arrays.asList("img_mango_anthracnose_1"),
                "ĐIỀU TRỊ: Phun Amistar 250SC hoặc Antracol. Bao trái xoài. Cắt tỉa cành thoáng."));

        // --- NHÓM CÂY CÔNG NGHIỆP ---
        diseases.add(new DiseaseModel(
                "Bệnh rỉ sắt Cà phê",
                "TRIỆU CHỨNG: Mặt dưới lá có ổ nấm vàng cam như bột phấn. Lá cháy khô và rụng hàng loạt. Cây kiệt sức năng suất giảm.",
                Arrays.asList("img_coffee_rust_1"),
                "ĐIỀU TRỊ: Phun thuốc hoạt chất Hexaconazole hoặc gốc đồng kỹ mặt dưới lá."));

        diseases.add(new DiseaseModel(
                "Bệnh chết nhanh Hồ tiêu",
                "TRIỆU CHỨNG: Cây tiêu héo rũ, lá vàng rụng đồng loạt. Gốc thân thối đen, rễ hư hoàn toàn. Chết cực nhanh.\n\nNGUYÊN NHÂN: Nấm Phytophthora capsici.",
                Arrays.asList("img_pepper_foot_rot_1"),
                "ĐIỀU TRỊ: Tưới gốc bằng thuốc Ridomil Gold hoặc Agrifos 400. Thoát nước cực tốt cho vườn."));

        diseases.add(new DiseaseModel(
                "Bệnh nấm hồng Cao su",
                "TRIỆU CHỨNG: Lớp nấm màu hồng phủ quanh vỏ cành hoặc thân cây, làm khô vỏ và chết cành.",
                Arrays.asList("img_rubber_pink_disease_1"),
                "ĐIỀU TRỊ: Quét thuốc gốc đồng (Bordeaux) hoặc Anvil 5SC lên vùng bị bệnh mùa mưa."));

        // --- NHÓM HOA MÀU & HOA CẢNH ---
        diseases.add(new DiseaseModel(
                "Bệnh thán thư ớt",
                "TRIỆU CHỨNG: Vết tròn lõm nâu đen trên quả già, thối quả cực nhanh và lây lan diện rộng mùa mưa.",
                Arrays.asList("img_chili_anthracnose_1"),
                "ĐIỀU TRỊ: Thu gom quả thối tiêu hủy. Phun thuốc hoạt chất Azoxystrobin hoặc Difenoconazole."));

        diseases.add(new DiseaseModel(
                "Bệnh héo xanh vi khuẩn Ớt",
                "TRIỆU CHỨNG: Cây héo rũ đột ngột ban ngày, tươi lại ban đêm, sau 2-3 ngày chết hẳn nhưng lá vẫn xanh.",
                Arrays.asList("img_chili_wilt_1"),
                "ĐIỀU TRỊ: Nhổ bỏ cây bệnh. Tưới vôi bột hoặc thuốc đặc trị vi khuẩn vào hố hốc cây bệnh."));

        diseases.add(new DiseaseModel(
                "Bệnh đốm đen hoa hồng",
                "TRIỆU CHỨNG: Đốm tròn đen trên lá hồng, làm lá vàng và rụng rất nhanh. Thường bị mùa mưa.",
                Arrays.asList("img_rose_black_spot_1"),
                "ĐIỀU TRỊ: Tưới gốc, tránh làm ướt lá tối muộn. Phun Anvil hoặc Rose Care định kỳ."));

        diseases.add(new DiseaseModel(
                "Bệnh rỉ sắt hoa cúc",
                "TRIỆU CHỨNG: Các ổ nấm màu nâu đỏ dưới mặt lá hoa cúc. Cây còi cọc hoa nhỏ xấu.",
                Arrays.asList("img_chrysanthemum_rust_1"),
                "ĐIỀU TRỊ: Tăng cường lưu thông không khí. Phun thuốc trừ nấm Hexaconazole."));

        diseases.add(new DiseaseModel(
                "Bệnh cháy lá mai vàng",
                "TRIỆU CHỨNG: Mép lá mai bị khô cháy nâu xám từ ngoài vào trong. Thường bị trên lá già.",
                Arrays.asList("img_mai_leaf_scorch_1"),
                "ĐIỀU TRỊ: Bón thêm phân hữu cơ. Phun thuốc gốc đồng hoặc Metalaxyl bảo vệ lá non."));

        diseases.add(new DiseaseModel(
                "Bệnh khảm lá Đu đủ",
                "TRIỆU CHỨNG: Lá non bị biến dạng, khảm vàng xanh. Quả có các vòng tròn màu xanh đậm.",
                Arrays.asList("img_papaya_mosaic_1"),
                "ĐIỀU TRỊ: Không có thuốc chữa. Nhổ bỏ cây bệnh. Diệt rệp muội trung gian lây bệnh."));

        diseases.add(new DiseaseModel(
                "Bệnh khảm lá sắn",
                "TRIỆU CHỨNG: Lá sắn bị biến dạng, nhăn nheo, có các mảng màu vàng trắng xen kẽ màu xanh (khảm).",
                Arrays.asList("img_cassava_mosaic_1"),
                "ĐIỀU TRỊ: Tiêu hủy cây bệnh ngay. Không dùng hom giống từ cây bệnh. Kiểm soát bọ phấn trắng."));

        diseases.add(new DiseaseModel(
                "Bệnh thối nhũn bắp cải",
                "TRIỆU CHỨNG: Vết bệnh ban đầu như bị luộc nước sôi trên lá bắp cải, sau đó nhũn nước, thối đen.",
                Arrays.asList("img_cabbage_soft_rot_1"),
                "ĐIỀU TRỊ: Lên luống cao thoát nước. Xử lý đất bằng vôi bột trước khi trồng."));

        diseases.add(new DiseaseModel(
                "Bệnh phấn trắng nho",
                "TRIỆU CHỨNG: Lớp bột màu trắng như phấn phủ lên bề mặt lá, cành non và chùm quả nho.",
                Arrays.asList("img_grape_powdery_mildew_1"),
                "ĐIỀU TRỊ: Phun lưu huỳnh bột hoặc các loại thuốc chứa hoạt chất Myclobutanil."));

        diseases.add(new DiseaseModel(
                "Bệnh phấn trắng dưa chuột",
                "TRIỆU CHỨNG: Lớp phấn màu trắng xám bao phủ dày đặc trên phiến lá, cuống lá.",
                Arrays.asList("img_cucumber_powdery_mildew_1"),
                "ĐIỀU TRỊ: Phun nước vôi trong hoặc sử dụng các thuốc trừ nấm gốc lưu huỳnh."));

        diseases.add(new DiseaseModel(
                "Bệnh sương mai dưa chuột",
                "TRIỆU CHỨNG: Mặt trên lá xuất hiện các đốm vàng hình góc cạnh theo gân lá.",
                Arrays.asList("img_cucumber_downy_mildew_1"),
                "ĐIỀU TRỊ: Phun Ridomil Gold hoặc thuốc chứa hoạt chất Metalaxyl."));

        diseases.add(new DiseaseModel(
                "Bệnh phấn trắng dưa hấu",
                "TRIỆU CHỨNG: Lớp bột trắng bao phủ toàn bộ phiến lá dưa hấu làm lá khô héo sớm.",
                Arrays.asList("img_watermelon_powdery_mildew_1"),
                "ĐIỀU TRỊ: Phun nước vôi trong hoặc thuốc gốc lưu huỳnh."));

        diseases.add(new DiseaseModel(
                "Bệnh mốc xanh cam",
                "TRIỆU CHỨNG: Vết thối trên vỏ cam phủ lớp mốc màu xanh lá cây hoặc trắng.",
                Arrays.asList("img_citrus_green_mold_1"),
                "ĐIỀU TRỊ: Tránh làm dập nát quả khi thu hoạch. Bảo quản nơi khô ráo."));

        diseases.add(new DiseaseModel(
                "Bệnh loét sọc mặt cạo cao su",
                "TRIỆU CHỨNG: Các vết loét màu đen chạy dọc theo mặt cạo mủ, làm thối vỏ.",
                Arrays.asList("img_rubber_black_stripe_1"),
                "ĐIỀU TRỊ: Ngưng cạo mủ ở cây bệnh. Quét thuốc đặc trị Metalaxyl."));

        diseases.add(new DiseaseModel(
                "Bệnh nấm thối lan",
                "TRIỆU CHỨNG: Vết bệnh nhũn nước và có mùi hôi rất đặc trưng trên lá lan.",
                Arrays.asList("img_orchid_soft_rot_1"),
                "ĐIỀU TRỊ: Ngưng tưới nước. Cắt bỏ phần thối và bôi thuốc Streptomycin."));

        return diseases;
    }

    public static DiseaseModel getDiseaseByName(String name) {
        if (name == null) return null;
        for (DiseaseModel disease : getAllDiseases()) {
            if (disease.name.toLowerCase().contains(name.toLowerCase()) || 
                name.toLowerCase().contains(disease.name.toLowerCase())) {
                return disease;
            }
        }
        return null;
    }
}
