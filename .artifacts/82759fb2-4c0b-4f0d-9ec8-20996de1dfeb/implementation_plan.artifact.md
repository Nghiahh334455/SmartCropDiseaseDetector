# Implementation Plan - Hệ thống Thông báo & Badge (v12)

Kế hoạch này thực hiện việc bổ sung hệ thống thông báo cho người dùng khi có tương tác (Like, Comment, Share) trên bài viết của họ, bao gồm dấu chấm đỏ báo hiệu (Badge) và màn hình danh sách thông báo.

## User Review Required

> [!IMPORTANT]
> **Chấm đỏ báo hiệu (Badge)**: Tôi sẽ thêm một chấm đỏ nhỏ ở mục "Cá nhân" trên thanh điều hướng phía dưới (Bottom Navigation) khi có thông báo mới chưa đọc.
> **Thông báo thời gian thực**: Sử dụng Firestore SnapshotListener để cập nhật thông báo ngay lập tức.
> **Điều hướng**: Khi nhấn vào một thông báo, ứng dụng sẽ mở đúng bài viết liên quan trong màn hình Chi tiết bài viết.

## Proposed Changes

### 1. Dữ liệu (Data Model)
#### [NEW] `NotificationModel.java`
- Chứa các thông tin: `id`, `type` (Like/Comment/Share), `senderName`, `senderAvatar`, `postId`, `timestamp`, `isRead`.

### 2. Giao diện (UI)
#### [NEW] `activity_notifications.xml`
- Màn hình hiển thị danh sách các thông báo theo thứ tự thời gian mới nhất.

#### [NEW] `NotificationAdapter.java`
- Adapter để hiển thị từng mục thông báo (Avatar người tương tác, nội dung thông báo, thời gian).

#### [MODIFY] `fragment_profile.xml`
- Thêm một nút **"Thông báo" (Icon chuông)** ở góc trên bên phải của màn hình Cá nhân để người dùng truy cập danh sách thông báo.

### 3. Logic Thông báo (Logic)
#### [MODIFY] `ForumAdapter.java` & `PostDetailActivity.java`
- Thêm code để tạo một bản ghi mới trong collection `notifications` trên Firestore mỗi khi có người nhấn Like, gửi Bình luận hoặc Share bài viết.
- Bản ghi này sẽ được gửi tới chủ sở hữu bài viết (`post.uid`).

#### [MODIFY] `MainActivity.java`
- Thêm logic lắng nghe collection `notifications` của người dùng hiện tại.
- Nếu có thông báo nào có `isRead == false`, hiển thị dấu chấm đỏ (Badge) lên icon "Cá nhân" ở BottomNavigationView.

#### [MODIFY] `NotificationActivity.java`
- Hiển thị danh sách thông báo. Khi nhấn vào mục nào thì cập nhật `isRead = true` và chuyển sang `PostDetailActivity`.

## Verification Plan

### Manual Verification
- **Like**: Dùng tài khoản A like bài viết của tài khoản B -> Kiểm tra tài khoản B có hiện chấm đỏ không.
- **Badge**: Nhấn vào tab "Cá nhân" -> Kiểm tra icon chuông có chấm đỏ không.
- **Màn hình**: Nhấn vào chuông -> Xem danh sách thông báo.
- **Điều hướng**: Nhấn vào thông báo comment -> Kiểm tra có mở đúng bài viết đó không.
