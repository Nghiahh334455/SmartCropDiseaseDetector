# Implementation Plan - Tách biệt Backend AI & SQL + Triệt tiêu lỗi Tải ảnh (v14.1)

Kế hoạch này thực hiện việc tách rời hoàn toàn hai hệ thống Backend để bạn có thể quản lý riêng biệt (AI trong VS Code, SQL trong Android Studio) và xử lý dứt điểm lỗi tải ảnh lên Cloud.

## User Review Required

> [!IMPORTANT]
> **Hệ thống 2 Server**:
> - **Server AI (Cổng 8000)**: Bạn sẽ chạy file `main.py` trong thư mục `D:/Plant_Disease_Pipeline` bằng VS Code.
> - **Server SQL (Cổng 8001)**: Bạn sẽ chạy file `backend_sql.py` trong Android Studio.
> **Lưu ý về Cáp USB**: Tôi sẽ cập nhật file bat để nó tự động "thông cầu" cho cả hai cổng 8000 và 8001 cùng lúc. Bạn không cần làm gì thêm ngoài việc cắm cáp.

> [!CAUTION]
> **Sửa lỗi Tải ảnh**: Tôi sẽ áp dụng phương thức "Xác thực luồng" (`continueWithTask`) cho tất cả các Activity có tính năng upload ảnh (Avatar, Diễn đàn, Chẩn đoán). Đây là cách an toàn nhất để tránh lỗi "Object does not exist".

## Proposed Changes

### 1. Backend AI (VS Code - Port 8000)
#### [MODIFY] `D:/Plant_Disease_Pipeline/main.py`
- Xóa bỏ toàn bộ các endpoint liên quan đến SQL (posts, comments, stats...).
- Chỉ giữ lại `/predict` (Chẩn đoán) và `/chat` (Hỏi đáp AI).

### 2. Backend SQL (Android Studio - Port 8001)
#### [NEW/MODIFY] `D:/Androi_DATN/backend_sql.py`
- Chỉ chứa các endpoint liên quan đến Database SQL Server.
- Đảm bảo chạy trên cổng **8001**.

### 3. Android - Cấu hình Kết nối
#### [MODIFY] [RetrofitClient.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/api/RetrofitClient.java)
- Cung cấp hai lối vào riêng biệt: `getAiService()` (Port 8000) và `getSqlService()` (Port 8001).

### 4. Android - Sửa lỗi & Nối dây
#### [MODIFY] `DiagnosisActivity.java`
- Chẩn đoán ảnh -> Gọi Port 8000.
- Lưu thống kê/Đăng bài -> Gọi Port 8001.
- Fix logic upload ảnh chẩn đoán sang Diễn đàn bằng `continueWithTask`.

#### [MODIFY] `CreatePostActivity.java` & `EditProfileActivity.java`
- Áp dụng `continueWithTask` để lấy link ảnh "tuyệt đối" từ Firebase.
- Chuyển hướng toàn bộ yêu cầu lưu dữ liệu sang Port 8001.

#### [MODIFY] `ForumFragment.java`, `PostDetailActivity.java`, `LibraryFragment.java`, `MainActivity.java`
- Đồng bộ hóa việc gọi API sang Port 8001 cho các tính năng cộng đồng.

### 5. Công cụ Khởi động
#### [MODIFY] `khoi_dong_he_thong.bat`
- Tự động chạy `adb reverse` cho cả hai cổng 8000 và 8001.
- Chỉ tự động bật Server SQL (Port 8001). Server AI bạn sẽ chủ động bật bên VS Code.

## Verification Plan

### Manual Verification
1. **VS Code**: Chạy `python main.py`. Kiểm tra log xem có chẩn đoán thành công không.
2. **Android Studio**: Chạy `python backend_sql.py` (hoặc qua file bat).
3. **App**:
    - Thử quét lá bệnh -> Kết quả phải hiện ra mượt mà (8000).
    - Thử đăng bài có ảnh -> Ảnh phải hiện lên ngay lập tức (8001).
    - Thử đổi Avatar -> Kiểm tra xem ảnh đã "lên mây" chưa.
