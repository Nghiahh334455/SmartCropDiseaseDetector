# Implementation Plan - Khắc phục lỗi 422, Tải ảnh & Nâng cấp Giao diện Chia sẻ (v13.2)

Kế hoạch này thực hiện việc sửa lỗi truyền dữ liệu (422) sang SQL Server, tối ưu hóa quy trình tải ảnh và cải thiện thẩm mỹ cho tính năng chia sẻ bệnh.

## User Review Required

> [!IMPORTANT]
> **Khắc phục lỗi 422**: Lỗi này xảy ra do một số thông tin (như tên người dùng hoặc ảnh đại diện) bị gửi đi với giá trị rỗng hoặc sai định dạng. Tôi sẽ chuẩn hóa dữ liệu trên cả Android và Backend để đảm bảo mọi bài đăng đều thành công.
> **Tải ảnh từ máy**: Tôi sẽ nâng cấp logic tải ảnh để đảm bảo ứng dụng chỉ tiếp tục lưu dữ liệu sau khi tệp ảnh đã được ghi thành công trên Cloud.
> **Giao diện Chia sẻ mới**: Thay vì hộp thoại nhập chữ đơn giản, tính năng chia sẻ từ Thư viện/Chẩn đoán sẽ được lột xác với giao diện chuyên nghiệp như Facebook.

## Proposed Changes

### 1. Khắc phục lỗi 422 (FastAPI Validation)
#### [MODIFY] [backend_v13.py](file:///D:/Androi_DATN/backend_v13.py)
- Chuyển các trường `author`, `question`, `uid` sang dạng tùy chọn (`Optional`) hoặc có giá trị mặc định để tránh lỗi khi Android gửi thiếu trường.
- Thêm log chi tiết để theo dõi dữ liệu nhận được từ App.

#### [MODIFY] [ApiService.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/api/ApiService.java)
- Sử dụng `@Field` với giá trị dự phòng (fallback) để không bao giờ gửi giá trị `null` lên server.

### 2. Sửa lỗi Tải ảnh (Firebase Storage & SQL Sync)
#### [MODIFY] [CreatePostActivity.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/ui/forum/CreatePostActivity.java) & [EditProfileActivity.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/ui/profile/EditProfileActivity.java)
- Sử dụng phương thức `taskSnapshot.getMetadata().getReference().getDownloadUrl()` để lấy link ảnh bền vững nhất.
- Bổ sung thông báo lỗi chi tiết khi quá trình tải ảnh thất bại.

### 3. Nâng cấp Giao diện Chia sẻ (Elegant Sharing UI)
#### [MODIFY] [DiseaseDetailActivity.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/ui/library/DiseaseDetailActivity.java) & [DiagnosisActivity.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/DiagnosisActivity.java)
- Thiết kế lại hộp thoại chia sẻ: Hiển thị ảnh xem trước của bệnh, tên bệnh rõ ràng và ô nhập trạng thái phong cách Material 3.
- Cho phép đăng bài ngay cả khi không nhập nội dung văn bản.

## Verification Plan

### Manual Verification
- **Đăng bài**: Thử đăng bài chỉ có chữ, chỉ có ảnh, hoặc cả hai.
- **Thay Avatar**: Cập nhật ảnh hồ sơ và kiểm tra xem các bài viết mới có hiện Avatar mới không.
- **Chia sẻ**: Vào Thư viện, nhấn "Chia sẻ" và kiểm tra xem bài viết có xuất hiện trên Diễn đàn mà không cần nhập chữ không.
- **Lỗi 422**: Theo dõi log Backend để đảm bảo không còn phản hồi 422 từ server.
