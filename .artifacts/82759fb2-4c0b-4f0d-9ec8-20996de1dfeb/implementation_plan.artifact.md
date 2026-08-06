# Implementation Plan - Tối ưu hóa Hệ thống Thông báo (v12.1)

Kế hoạch này thực hiện việc di chuyển lối vào thông báo vào mục "Tài khoản" để dễ thấy hơn, đồng thời bổ sung chấm đỏ báo hiệu trực tiếp trong menu.

## User Review Required

> [!IMPORTANT]
> **Vị trí Thông báo mới**: Thay vì dùng icon chuông nhỏ ở góc ảnh đại diện (khó thấy), tôi sẽ chuyển "Thông báo" thành một mục lớn trong danh sách "Tài khoản".
> **Chấm đỏ trong Menu**: Một chấm đỏ sẽ hiện ngay cạnh chữ "Thông báo" trong menu Cá nhân nếu bạn có thông báo chưa đọc.
> **Điều hướng**: Đảm bảo việc nhấn vào thông báo trong danh sách sẽ mở đúng bài viết tương ứng.

## Proposed Changes

### 1. Giao diện Cá nhân (Profile UI)
#### [MODIFY] [fragment_profile.xml](file:///D:/Androi_DATN/app/src/main/res/layout/fragment_profile.xml)
- Loại bỏ icon chuông FrameLayout ở góc header.
- Thêm một `RelativeLayout` chứa `MaterialButton` (Thông báo) và một `View` (Badge chấm đỏ) vào trong CardView "Tài khoản".
- Sắp xếp thứ tự: Chỉnh sửa thông tin -> Thông báo -> Lịch sử chẩn đoán.

### 2. Logic Cá nhân (Profile Logic)
#### [MODIFY] [ProfileFragment.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/ui/profile/ProfileFragment.java)
- Ánh xạ nút "Thông báo" mới trong card menu.
- Cập nhật logic `listenForUnreadNotifications` để ẩn hiện chấm đỏ ngay trong mục menu này.

### 3. Kiểm tra Logic Gửi Thông báo
#### [VERIFY] [ForumAdapter.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/ui/forum/ForumAdapter.java) & [PostDetailActivity.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/ui/forum/PostDetailActivity.java)
- Đảm bảo khi gửi thông báo, field `read` được đặt là `false` để Badge có thể nhận diện.

## Verification Plan

### Manual Verification
- **Giao diện**: Vào trang Cá nhân, kiểm tra xem mục "Thông báo" có hiện trong danh sách "Tài khoản" không.
- **Badge**: Nhờ người khác Like bài viết -> Kiểm tra xem có chấm đỏ hiện ra ở mục "Thông báo" và icon "Cá nhân" ở dưới không.
- **Điều hướng**: Nhấn vào "Thông báo" -> Nhấn vào một thông báo cụ thể -> Kiểm tra xem có mở đúng bài viết không.
