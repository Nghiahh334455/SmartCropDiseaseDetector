# Implementation Plan - Nâng cấp Giao diện Thư viện & Chuyên gia AI (v11.1)

Kế hoạch này tập trung vào việc hiện đại hóa giao diện Thư viện bệnh và thay thế các hộp thoại hỏi đáp AI cũ kỹ bằng thiết kế Bottom Sheet chuyên nghiệp, màu sắc hơn.

## User Review Required

> [!IMPORTANT]
> **Giao diện Chat AI**: Thay vì dùng ô nhập văn bản đơn giản, tôi sẽ sử dụng **Bottom Sheet** với phong cách thiết kế hiện đại, có icon chuyên gia và hiệu ứng chuyển cảnh mượt mà.
> **Màu sắc Thư viện**: Cập nhật thanh tìm kiếm và header Thư viện với các dải màu gradient hoặc bo góc mềm mại hơn để tăng tính thẩm mỹ.

## Proposed Changes

### 1. Tài nguyên Giao diện (Resources)
#### [NEW] `bg_bottom_sheet.xml`
- Tạo nền trắng bo tròn 24dp cho các hộp thoại Bottom Sheet.

#### [NEW] `dialog_ai_chat.xml`
- Thiết kế màn hình đặt câu hỏi cho AI: Có Avatar "Thần Nông AI", nhãn trạng thái "Trực tuyến", và ô nhập liệu phong cách Material 3.

#### [NEW] `dialog_ai_response.xml`
- Thiết kế màn hình hiển thị câu trả lời: Nội dung nằm trong bong bóng chat (bubble) màu xanh nhạt, dễ đọc và đẹp mắt.

### 2. Thư viện bệnh (Library UI)
#### [MODIFY] [fragment_library.xml](file:///D:/Androi_DATN/app/src/main/res/layout/fragment_library.xml)
- Nâng cấp `AppBarLayout` và thanh tìm kiếm với hiệu ứng màu sắc sinh động hơn.
- Thêm icon AI bắt mắt hơn (ví dụ: biểu tượng robot hoặc hạt mầm công nghệ).

### 3. Logic Xử lý (Logic)
#### [MODIFY] [LibraryFragment.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/ui/library/LibraryFragment.java)
- Thay thế logic `AlertDialog` bằng việc hiển thị các `BottomSheetDialog`.
- Cải thiện trải nghiệm người dùng với các thông báo tải (loading) đẹp mắt trong lúc đợi AI phản hồi.

## Verification Plan

### Manual Verification
- **Thư viện**: Kiểm tra xem thanh tìm kiếm và header có "màu sắc" và đẹp hơn không.
- **Hỏi AI**: Nhấn vào nút AI -> Kiểm tra Bottom Sheet đặt câu hỏi có hiện lên mượt mà không.
- **Phản hồi**: Gửi câu hỏi -> Kiểm tra xem câu trả lời có hiện trong bong bóng chat đẹp mắt không.
