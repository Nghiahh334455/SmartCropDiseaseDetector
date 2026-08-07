# Implementation Plan - Silent Diagnosis, FB Comments & SQL Server Migration (v13)

Kế hoạch này thực hiện các thay đổi cốt lõi về trải nghiệm người dùng và chuyển đổi toàn bộ hệ thống lưu trữ sang Microsoft SQL Server.

## User Review Required

> [!CAUTION]
> **Chuyển đổi sang SQL Server (MSSQL)**: Đây là một bước ngoặt lớn.
> 1. Tôi sẽ cung cấp **File Script SQL (.sql)** để bạn chạy trong SQL Server Management Studio (SSMS) nhằm tạo bảng.
> 2. Tôi sẽ cung cấp mã nguồn **FastAPI (Python)** mới để làm cầu nối giữa Android và SQL Server. Bạn sẽ cần cài đặt `pyodbc` và driver SQL Server cho Python.
> 3. Toàn bộ dữ liệu Diễn đàn, Bình luận, Thông báo sẽ được chuyển từ Firestore sang SQL Server. Firebase Auth vẫn giữ nguyên để bảo mật đăng nhập.

> [!IMPORTANT]
> **Bình luận & Chẩn đoán**:
> - Loại bỏ nút "Buồn" trong bình luận. Giữ lại "Thích" và "Trả lời".
> - Bình luận trả lời sẽ thụt lề ngay bên dưới bình luận gốc.
> - Chẩn đoán sẽ không còn hiện thông báo "Đã gửi Gmail" gây phiền phức. Nếu lá khỏe mạnh, hệ thống sẽ im lặng hoàn toàn.

## Proposed Changes

### 1. Chẩn đoán & Gmail (Diagnosis logic)
#### [MODIFY] [DiagnosisActivity.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/DiagnosisActivity.java)
- Loại bỏ lời gọi `showEmergencyDialog`.
- Thêm kiểm tra: Nếu kết quả là "Khỏe mạnh" (Healthy), không thực hiện gửi email hoặc hiện thông báo.

### 2. Bình luận kiểu Facebook (Social Interactions)
#### [MODIFY] [CommentModel.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/models/CommentModel.java)
- Thêm trường `parent_id` (int) để xác định bình luận này trả lời cho ai.

#### [MODIFY] [item_comment.xml](file:///D:/Androi_DATN/app/src/main/res/layout/item_comment.xml)
- Xóa nút "Buồn" (`btnSadComment`).
- Tinh chỉnh `margin` để hỗ trợ hiển thị lồng nhau.

#### [MODIFY] [CommentAdapter.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/ui/forum/CommentAdapter.java)
- Cập nhật logic: Nếu `parent_id != null`, tăng `paddingLeft` của view và ẩn nút "Trả lời".

### 3. Hệ thống Backend & SQL Server
#### [NEW] `mssql_schema.sql`
- Script tạo các bảng: `Users`, `Posts`, `Comments`, `Notifications`.

#### [NEW] `backend/main.py`
- Backend FastAPI kết nối MSSQL (thay thế logic Firestore cũ).

#### [MODIFY] [ApiService.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/api/ApiService.java)
- Định nghĩa lại các Endpoint để Android giao tiếp với SQL Server thông qua FastAPI.

## Verification Plan

### Manual Verification
- **Chẩn đoán**: Chụp ảnh lá bệnh -> Kiểm tra xem email có tự gửi ngầm không. Chụp lá khỏe -> Kiểm tra xem app có im lặng không.
- **Bình luận**: Nhấn "Trả lời" -> Viết nội dung -> Kiểm tra xem bình luận mới có nằm dưới và thụt lề không.
- **SQL Server**: Mở SSMS, chạy lệnh `SELECT * FROM Comments` để kiểm tra dữ liệu đã vào database chưa.
