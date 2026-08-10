# Kế hoạch khắc phục lỗi gửi Email Cảnh báo theo tài khoản Firebase (v17)

Người dùng báo cáo không nhận được email. Chúng ta sẽ đảm bảo hệ thống lấy chính xác Email từ Firebase Auth và gửi cảnh báo về đúng địa chỉ đó khi phát hiện bệnh.

## User Review Required

> [!IMPORTANT]
> **Ràng buộc Đăng nhập**: Nếu người dùng chưa đăng nhập, hệ thống sẽ hiển thị thông báo nhắc nhở: "Bạn đang dùng tài khoản khách, vui lòng đăng nhập để nhận cảnh báo qua Email".
> **Kiểm tra Log Backend**: Tôi sẽ thêm log vào Backend AI để bạn có thể nhìn thấy địa chỉ email mà App gửi lên trong cửa sổ Terminal.

## Open Questions

- Bạn đã thử đăng xuất và đăng nhập lại trên App chưa?
- Khi chẩn đoán, bạn có thấy dòng "DEBUG: Nhan yeu cau chan doan tu: [email của bạn]" hiện lên ở terminal chạy `main.py` không?

## Proposed Changes

### 1. Cải tiến Android App (DiagnosisActivity)
#### [MODIFY] [DiagnosisActivity.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/DiagnosisActivity.java)
- **Kiểm tra trạng thái đăng nhập**: Nếu `user == null`, hiển thị một thông báo nhẹ (Snackbar hoặc Toast) để người dùng biết email sẽ không được gửi.
- **Log dữ liệu**: Thêm log để kiểm tra chuỗi `userEmail` trước khi gọi API.

### 2. Nâng cấp Backend AI (D:/Plant_Disease_Pipeline/)
#### [MODIFY] [main.py](file:///D:/Plant_Disease_Pipeline/main.py)
- **Phản hồi trạng thái email**: Trả về `email_sent` và `target_email` trong JSON để App hiển thị thông báo: "Đã gửi email tới [địa chỉ email]".
- **Xử lý lỗi**: Bọc hàm gửi email trong khối `try-except` chặt chẽ hơn để không làm treo quá trình trả về kết quả chẩn đoán nếu gửi mail lỗi.

#### [MODIFY] [alert.py](file:///D:/Plant_Disease_Pipeline/alert.py)
- **Tối ưu kết nối**: Thử nghiệm chuyển đổi giữa Port 465 (SSL) và 587 (TLS) để đảm bảo tính ổn định.

### 3. Cập nhật Model Response
#### [MODIFY] [PredictResponse.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/models/PredictResponse.java)
- Thêm các trường: `email_sent` (boolean) và `receiver_email` (String).

## Verification Plan

### Manual Verification
1. **Trường hợp Khách**: Không đăng nhập -> Chẩn đoán -> App báo "Vui lòng đăng nhập để nhận email".
2. **Trường hợp Đăng nhập**: Dùng email thật -> Chẩn đoán -> Kiểm tra log Backend thấy đúng email -> Kiểm tra hộp thư đến.
3. **Kiểm tra Spam**: Hướng dẫn người dùng kiểm tra mục Thư rác nếu vẫn không thấy.
