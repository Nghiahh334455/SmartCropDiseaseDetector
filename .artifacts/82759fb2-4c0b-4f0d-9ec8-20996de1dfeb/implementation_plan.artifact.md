# Implementation Plan - Nâng cấp Logo, Thư viện AI & Hệ thống Tương tác (v11)

Kế hoạch này thực hiện việc làm đẹp màn hình đăng nhập, tích hợp tìm kiếm AI trong thư viện và nâng cấp hệ thống bình luận (trả lời & cảm xúc).

## User Review Required

> [!IMPORTANT]
> **Tìm kiếm AI**: Tôi sẽ thêm một nút "Hỏi AI" trong Thư viện. Khi người dùng nhập văn bản, ứng dụng sẽ gọi API để nhận câu trả lời về bệnh cây trồng.
> **Hệ thống Bình luận**: Nâng cấp lên mô hình phân cấp, cho phép "Trả lời" (Reply) và bày tỏ cảm xúc "Thích" hoặc "Buồn" trên từng bình luận.
> **Logo Đăng nhập**: Thay thế icon cũ bằng `logo_app` với thiết kế bo tròn, hiện đại.

## Proposed Changes

### 1. Màn hình Đăng nhập (Login UI)
#### [MODIFY] [activity_login.xml](file:///D:/Androi_DATN/app/src/main/res/layout/activity_login.xml)
- Sử dụng `ShapeableImageView` để hiển thị `logo_app`.
- Cân chỉnh bố cục, thêm CardView hoặc Shadow để giao diện chuyên nghiệp hơn.

### 2. Thư viện & Tìm kiếm AI (Library & AI Search)
#### [MODIFY] [fragment_library.xml](file:///D:/Androi_DATN/app/src/main/res/layout/fragment_library.xml)
- Thêm nút **"Hỏi chuyên gia AI"** bên cạnh thanh tìm kiếm.
- Cải thiện màu sắc và khoảng cách các mục.

#### [MODIFY] [LibraryFragment.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/ui/library/LibraryFragment.java)
- Triển khai `BottomSheetDialogFragment` để nhận câu hỏi và hiển thị phản hồi từ AI.

#### [MODIFY] [ApiService.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/api/ApiService.java)
- Thêm endpoint `@POST("/ask")` (hoặc tương tự) để phục vụ tìm kiếm bằng văn bản.

### 3. Hệ thống Bình luận Nâng cao (Advanced Comments)
#### [MODIFY] [CommentModel.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/models/CommentModel.java)
- Thêm `List<CommentModel> replies` để hỗ trợ trả lời lồng nhau.
- Thêm `Map<String, Integer> reactions` (Like, Sad) để lưu số lượng cảm xúc.

#### [MODIFY] [item_comment.xml](file:///D:/Androi_DATN/app/src/main/res/layout/item_comment.xml)
- Thêm hàng nút tương tác bên dưới nội dung bình luận: **Thích**, **Buồn**, **Trả lời**.
- Hiển thị icon và số lượng cảm xúc.

#### [MODIFY] [CommentAdapter.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/ui/forum/CommentAdapter.java)
- Xử lý logic nhấn nút cảm xúc và mở ô nhập để trả lời bình luận.

## Verification Plan

### Manual Verification
- **Login**: Kiểm tra logo mới hiển thị đẹp chưa.
- **Thư viện**: Nhấn "Hỏi AI", nhập câu hỏi và xem AI có trả lời đúng không.
- **Diễn đàn**: Thử nhấn "Thích" một bình luận. Thử nhấn "Trả lời" xem có hiện ô nhập không.
- **Kiểm tra dữ liệu**: Xác nhận các phản hồi và cảm xúc được lưu đúng trên Firestore.
