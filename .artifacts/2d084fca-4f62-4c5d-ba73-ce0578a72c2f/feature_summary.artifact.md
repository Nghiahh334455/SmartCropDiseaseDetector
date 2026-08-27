# Tổng hợp Tính năng Chính - Smart Crop Disease Detector 🌿

Hệ thống **Thần Nông AI** là một giải pháp toàn diện hỗ trợ người nông dân chẩn đoán, điều trị bệnh cây trồng và kết nối cộng đồng. Dưới đây là danh sách các tính năng cốt lõi đã được triển khai:

---

### 1. Hệ thống Chẩn đoán AI Siêu tốc
*   **Chẩn đoán đa phương thức**: Hỗ trợ chụp ảnh trực tiếp từ Camera hoặc chọn ảnh từ Thư viện điện thoại.
*   **Pipeline AI kép (YOLOv8 + EfficientNet)**:
    *   **YOLOv8**: Tự động xác định và khoanh vùng (Bounding Box) vị trí lá cây bị bệnh.
    *   **EfficientNet**: Phân loại chính xác loại bệnh với độ tin cậy cao.
*   **Tốc độ xử lý**: Nhận diện bệnh trong chưa đầy 1 giây.
*   **Cảnh báo mức độ**: Hiển thị thẻ màu (Xanh/Cam/Đỏ) tương ứng với mức độ nguy hiểm của bệnh.

### 2. Tư vấn Chuyên gia AI (Gemini 1.5 Flash)
*   **Lời khuyên chuyên sâu**: Tự động sinh lộ trình điều trị 3 bước (Cắt tỉa, Pha thuốc, Thời điểm phun) dựa trên tình trạng thực tế của cây.
*   **Hỏi đáp AI (AI Chatbot)**: Người dùng có thể nhắn tin hỏi chuyên gia về bất kỳ vấn đề kỹ thuật nông nghiệp nào ngay trong ứng dụng.
*   **Lazy Loading**: Lời khuyên được tải song song sau khi có kết quả chẩn đoán, đảm bảo trải nghiệm mượt mà.

### 3. Diễn đàn Cộng đồng Nông dân
*   **Chia sẻ kết quả**: Đăng trực tiếp kết quả chẩn đoán kèm hình ảnh lên diễn đàn để hỏi ý kiến cộng đồng.
*   **Tương tác thời gian thực**: Hỗ trợ Thích (Like), Bình luận (Comment) đa cấp (Reply).
*   **Đồng bộ Avatar**: Ảnh đại diện được cập nhật tức thì trên toàn bộ các bài viết cũ khi người dùng thay đổi hồ sơ.
*   **Chia sẻ ngoại mạng**: Hỗ trợ chia sẻ nội dung bài viết sang các ứng dụng khác như Zalo, Facebook.

### 4. Hệ thống Thông báo Thông minh
*   **Thông báo tức thì**: Nhận thông báo ngay khi có người khác tương tác với bài viết của mình.
*   **Deep Linking**: Chạm vào thông báo sẽ mở trực tiếp trang chi tiết của bài viết đó.
*   **Cảnh báo Email**: Tự động gửi email cảnh báo khẩn cấp cho người dùng khi phát hiện dịch bệnh lây lan mạnh.

### 5. Quản lý Lịch sử & Thư viện
*   **Lịch sử Chẩn đoán**: Lưu trữ toàn bộ các lần quét (tên bệnh, ảnh, thời gian, lời khuyên) vào Room Database (cục bộ) và đồng bộ lên Cloud SQL.
*   **Thư viện Bệnh hại**: Tra cứu thông tin chi tiết về triệu chứng, nguyên nhân và thuốc đặc trị cho hàng chục loại bệnh cây trồng phổ biến.
*   **Mẹo chăm sóc cây**: Slider hiển thị các mẹo canh tác hữu ích hàng ngày trên trang chủ.

### 6. Bảng Điều khiển cho Quản trị viên (Admin Panel)
*   **Tài khoản đặc quyền**: Đăng nhập bằng mã `12345N` để truy cập hệ thống quản trị.
*   **Thống kê trực quan**: Biểu đồ Pie Chart hiển thị tỷ lệ các loại bệnh đang phổ biến nhất trong khu vực.
*   **Quản lý Nội dung (CRUD)**:
    *   Thêm/Sửa/Xóa các bệnh trong Thư viện.
    *   Quản lý danh sách Mẹo chăm sóc cây.
*   **Kiểm soát cộng đồng**: Xem danh sách người dùng và xóa các bài viết không phù hợp trên diễn đàn.

### 7. Công nghệ Nền tảng
*   **Mobile**: Java, ViewBinding, Navigation Component, Room DB, Retrofit 2.
*   **Backend**: FastAPI (Python), SQL Server (Tối ưu truy vấn JOIN), Gemini AI API.
*   **Image**: Xử lý ảnh Base64 chất lượng cao, thư viện Glide.
