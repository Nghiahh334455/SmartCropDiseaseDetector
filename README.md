# Smart Crop Disease Detector 🌿

Ứng dụng Android sử dụng Trí tuệ nhân tạo (AI) để chẩn đoán bệnh trên lá cây trồng thông qua hình ảnh.

## 🚀 Tính năng chính
*   **Chẩn đoán AI**: Chụp ảnh hoặc chọn ảnh từ thư viện để nhận diện bệnh và hướng dẫn điều trị ngay lập tức.
*   **Vẽ Bounding Box**: Tự động vẽ khung khoanh vùng vị trí lá bị bệnh trên màn hình.
*   **Thư viện bệnh**: Tra cứu thông tin chi tiết, triệu chứng và cách xử lý của nhiều loại bệnh phổ biến (Đạo ôn, Sương mai, Rỉ sắt...).
*   **Lịch sử chẩn đoán**: Lưu trữ kết quả các lần chẩn đoán trước đó để theo dõi.
*   **Quản lý cá nhân**: Chỉnh sửa tên hiển thị và ảnh đại diện (lưu trữ nội bộ).
*   **Hệ thống tài khoản**: Đăng ký và đăng nhập bảo mật qua Firebase Authentication.

## 🛠 Yêu cầu hệ thống
*   **Android**: API 24 (Android 7.0) trở lên.
*   **Server**: Máy tính chạy Server Python (FastAPI/Uvicorn).
*   **Kết nối**: Điện thoại và máy tính chạy server phải cùng mạng Wi-Fi (hoặc kết nối qua cáp USB sử dụng ADB Reverse).

## ⚙️ Cài đặt & Cấu hình

### 1. Phía Android (Android Studio)
1.  Mở dự án trong Android Studio.
2.  **Firebase**: Tải file `google-services.json` từ Firebase Console của bạn và đặt vào thư mục `app/`.
3.  **Cấu hình IP Server**: 
    *   Mở file `app/src/main/java/com/example/smartcrop/api/RetrofitClient.java`.
    *   Sửa `BASE_URL` thành IP máy tính của bạn (ví dụ: `http://192.168.1.5:8000`).
    *   Nếu dùng cáp USB, dùng `http://127.0.0.1:8000` và chạy lệnh `adb reverse tcp:8000 tcp:8000`.

### 2. Phía Server (Python)
Dự án có 2 server Python cần chạy song song:

**A. Server AI (Port 8000)**
1. Di chuyển vào thư mục: `cd D:/Plant_Disease_Pipeline`
2. Cài đặt thư viện: `pip install -r requirements.txt`
3. Chạy server:
   ```bash
   uvicorn main:app --host 0.0.0.0 --port 8000
   ```

**B. Server SQL (Port 8001)**
1. Di chuyển vào thư mục: `cd D:/Androi_DATN`
2. Tạo file `.env` từ `.env.example` và điền đúng thông tin SQL / R2 của bạn.
3. Cài đặt thư viện: `pip install -r requirements.txt`
4. Chạy server:
   ```bash
   python backend_sql.py
   ```

#### Cấu hình biến môi trường
```bash
# SQL Server
SQL_SERVER=localhost
SQL_DATABASE=ThanNongAI
SQL_UID=sa
SQL_PWD=Admin123
SQL_DRIVER="ODBC Driver 17 for SQL Server"

# Cloudflare R2
R2_ACCOUNT_ID=...
R2_ACCESS_KEY_ID=...
R2_SECRET_ACCESS_KEY=...
R2_BUCKET_NAME=smartcrop
R2_PUBLIC_DOMAIN=https://your-public.r2.dev
```

## 📂 Cấu trúc thư mục chính
*   `app/src/main/java/com/example/smartcrop/ui/`: Chứa các màn hình giao diện (Auth, Library, Profile, History).
*   `app/src/main/java/com/example/smartcrop/api/`: Xử lý kết nối mạng với Server Python.
*   `app/src/main/java/com/example/smartcrop/database/`: Cơ sở dữ liệu Room lưu lịch sử cục bộ.
*   `app/src/main/res/layout/`: Các file thiết kế giao diện XML.

---
**Phát triển bởi Trong Nghia**
