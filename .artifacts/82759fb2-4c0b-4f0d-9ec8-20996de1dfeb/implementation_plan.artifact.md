# Kế hoạch Sửa lỗi Đồng bộ, Thông báo và Tối ưu Trải nghiệm (v23)

Kế hoạch này giải quyết dứt điểm lỗi thông báo không hiển thị, sự cố không đồng bộ ảnh đại diện giữa các tài khoản, và hoàn thiện thiết kế lịch sử chẩn đoán.

## User Review Required

> [!IMPORTANT]
> **Đồng bộ Ảnh đại diện**: Tôi sẽ thay đổi cách lưu trữ để đảm bảo khi Nick A đổi ảnh, Nick B vào diễn đàn sẽ thấy ngay ảnh mới. Lỗi hiện tại là do ảnh chỉ lưu ở bộ nhớ máy Nick A.
> **Kích hoạt Thông báo**: Kiểm tra lại toàn bộ luồng từ Backend đến App để đảm bảo thông báo hiện lên ngay khi có tương tác.
> **Thiết kế Lịch sử**: Thêm dòng thời gian (giờ/ngày) vào các thẻ lịch sử ở Trang chủ để không bị "khuyết" thông tin.

## Proposed Changes

### 1. Tối ưu Tốc độ Chẩn đoán (AI Speedup)
#### [MODIFY] [main.py](file:///D:/Plant_Disease_Pipeline/main.py)
- Tách phần gọi Gemini AI (phần lấy lời khuyên - rất chậm) ra khỏi API chẩn đoán chính.
- API `/predict` sẽ trả về tên bệnh ngay lập tức (< 1s).
- Thêm API `/expert-advice` để App gọi lấy lời khuyên sau (load từ từ).

### 2. Sửa lỗi Ảnh đại diện & Thông báo
#### [MODIFY] [backend_sql.py](file:///D:/Androi_DATN/backend_sql.py)
- Tối ưu API `update_photo`: Đảm bảo cập nhật đồng bộ ảnh vào bảng `Posts` để mọi người dùng khác đều thấy ảnh mới trên diễn đàn.
- Thêm Log cho API `notifications` để debug lý do thông báo không hiện.

#### [MODIFY] [EditProfileActivity.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/ui/profile/EditProfileActivity.java)
- Đảm bảo đẩy ảnh Base64 lên SQL Server thành công trước khi báo hoàn tất.

### 3. Hoàn thiện Giao diện Lịch sử & UX
#### [MODIFY] [item_disease_common.xml](file:///D:/Androi_DATN/app/src/main/res/layout/item_disease_common.xml)
- Bổ sung `TextView` hiển thị thời gian chẩn đoán (ví dụ: "10:30 - 20/07").

#### [MODIFY] [PersonalHistoryAdapter.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/ui/home/PersonalHistoryAdapter.java)
- Đổ dữ liệu thời gian vào giao diện mới.

#### [MODIFY] [HomeFragment.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/ui/home/HomeFragment.java)
- Kích hoạt nút **"Quét ngay"** trên Banner để mở Camera.

### 4. Tương tác Bàn phím & Nút bấm
#### [MODIFY] [LibraryFragment.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/ui/library/LibraryFragment.java)
- Ẩn bàn phím ngay khi nhấn gửi.
- Chuyển trạng thái nút thành **"Đang soạn câu trả lời..."**.

#### [MODIFY] [PostDetailActivity.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/ui/forum/PostDetailActivity.java)
- Ẩn bàn phím sau khi gửi bình luận.

## Verification Plan

### Manual Verification
1. **Ảnh đại diện**: Nick A đổi ảnh -> Nick B vào xem bài viết của A trên diễn đàn -> Phải thấy ảnh mới.
2. **Thông báo**: Thực hiện Like/Comment -> Kiểm tra mục Thông báo có dữ liệu mới không.
3. **Thiết kế**: Xem mục "Lịch sử của bạn" ở Trang chủ -> Kiểm tra xem đã có ngày giờ chưa.
4. **Tốc độ**: Chụp ảnh chẩn đoán -> Tên bệnh phải hiện ra gần như tức thì.
