# Walkthrough - Hệ thống Thông báo Thời gian thực (v12)

Tôi đã hoàn thành việc triển khai hệ thống thông báo thông minh, giúp bạn luôn cập nhật được các tương tác mới nhất trên bài viết của mình.

## Các tính năng mới

### 1. Badge Chấm đỏ Báo hiệu
- **Bottom Navigation**: Một chấm đỏ nhỏ kèm số lượng thông báo chưa đọc sẽ xuất hiện ngay trên mục **"Cá nhân"** ở thanh điều hướng dưới cùng.
- **Icon Chuông**: Trong màn hình Cá nhân, tôi đã thêm một biểu tượng hình chuông. Nếu có thông báo mới, chuông này cũng sẽ hiển thị chấm đỏ báo hiệu.

### 2. Trung tâm Thông báo Chi tiết
- **Màn hình mới**: Khi nhấn vào icon chuông, bạn sẽ được đưa tới màn hình **"Thông báo"**.
- **Nội dung phong phú**: Danh sách hiển thị rõ ràng ai đã Like, ai đã Bình luận hoặc Chia sẻ bài viết nào của bạn, kèm theo thời gian cụ thể (ví dụ: "vừa xong", "2 giờ trước").
- **Phân biệt trạng thái**: Các thông báo chưa đọc sẽ có màu nền xanh nhạt và chấm đỏ bên cạnh để bạn dễ dàng nhận biết.

### 3. Điều hướng Thông minh
- **Nhấn là tới**: Khi bạn nhấn vào một thông báo, ứng dụng sẽ tự động mở đúng bài viết đó trong màn hình chi tiết để bạn có thể xem và phản hồi ngay lập tức.
- **Tự động đánh dấu**: Sau khi nhấn xem, thông báo đó sẽ tự động được đánh dấu là "Đã đọc" và chấm đỏ sẽ biến mất.

---

## Kết quả kiểm tra & Git
- **Build**: Thành công (Build Successful).
- **Git**: Đã commit và push toàn bộ thay đổi lên branch `master`.
- **Dữ liệu**: Hệ thống sử dụng Firestore Sub-collection nên tốc độ cực nhanh và tiết kiệm dữ liệu.

> [!TIP]
> **Thử ngay**: Bạn hãy dùng một tài khoản khác để Like hoặc Bình luận vào bài viết của mình. Bạn sẽ thấy chấm đỏ hiện lên ngay lập tức ở tab "Cá nhân" mà không cần phải tắt app mở lại!
