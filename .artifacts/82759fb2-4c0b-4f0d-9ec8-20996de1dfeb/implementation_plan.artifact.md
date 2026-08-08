# Implementation Plan - Chuyển đổi lưu trữ ảnh sang SQL Server (Base64) (v15)

Kế hoạch này thực hiện việc loại bỏ hoàn toàn phụ thuộc vào Firebase Storage (để tránh mất phí) và chuyển sang lưu trữ ảnh trực tiếp vào cơ sở dữ liệu SQL Server dưới dạng chuỗi Base64.

## User Review Required

> [!WARNING]
> **Thay đổi phương thức lưu trữ**: Toàn bộ ảnh cũ trên Firebase Storage sẽ không được sử dụng. Chúng ta sẽ chuyển sang lưu ảnh dưới dạng văn bản (Base64) cực dài trong SQL Server.
> **Hiệu năng**: Việc lưu ảnh trực tiếp vào DB có thể làm dung lượng Database tăng nhanh. Tôi sẽ áp dụng nén ảnh tối đa (80%) trước khi lưu để đảm bảo tốc độ tải nhanh nhất.
> **Cập nhật Database**: Bạn sẽ cần chạy lại script SQL để làm sạch và chuẩn hóa các cột chứa dữ liệu ảnh.

## Proposed Changes

### 1. Cơ sở dữ liệu (SQL Server)
#### [MODIFY] [mssql_schema.sql](file:///D:/Androi_DATN/mssql_schema.sql)
- Đảm bảo các cột `userPhotoUrl`, `imageUrl`, `authorPhotoUrl`, `lastImageUrl` đều là `NVARCHAR(MAX)` để chứa chuỗi Base64 khổng lồ.

### 2. Backend (FastAPI)
#### [MODIFY] [backend_sql.py](file:///D:/Androi_DATN/backend_sql.py)
- Tăng giới hạn kích thước dữ liệu nhận được (nếu cần).
- Cập nhật các hàm `create_post`, `add_comment`, `increment_disease_stats` để nhận dữ liệu Base64 thay vì URL.

### 3. Android - Tiện ích mã hóa (Utils)
#### [NEW] `ImageUtils.java`
- Thêm hàm `uriToBase64`: Chuyển ảnh từ Uri sang chuỗi Base64.
- Thêm hàm `bitmapToBase64`: Chuyển Bitmap sang chuỗi Base64.

### 4. Android - Cập nhật Giao diện Đẩy dữ liệu (Upload)
#### [MODIFY] [EditProfileActivity.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/ui/profile/EditProfileActivity.java)
- Gỡ bỏ `FirebaseStorage`.
- Mã hóa ảnh đại diện sang Base64 và gửi về SQL Server thông qua một API cập nhật Profile mới (sẽ thêm vào Backend).

#### [MODIFY] [CreatePostActivity.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/ui/forum/CreatePostActivity.java)
- Gỡ bỏ `FirebaseStorage`.
- Chuyển ảnh bài đăng sang Base64 và gọi `apiService.createPost`.

#### [MODIFY] [DiagnosisActivity.java](file:///D:/Androi_DATN/app/src/main/java/com/example/smartcrop/DiagnosisActivity.java)
- Gỡ bỏ logic upload lên Cloud khi chia sẻ bệnh hoặc cập nhật thống kê. Chuyển sang dùng Base64.

### 5. Android - Cập nhật Giao diện Hiển thị (Display)
#### [MODIFY] `ForumAdapter.java`, `CommentAdapter.java`, `CommonDiseaseAdapter.java`, `PostDetailActivity.java`
- Cấu hình **Glide** để nhận diện và hiển thị chuỗi Base64:
  ```java
  byte[] imageBytes = Base64.decode(base64String, Base64.DEFAULT);
  Glide.with(context).load(imageBytes)...
  ```

## Verification Plan

### Manual Verification
- **Avatar**: Đổi ảnh hồ sơ -> Kiểm tra xem trong SQL Server cột `userPhotoUrl` có chứa chuỗi ký tự dài không.
- **Diễn đàn**: Đăng bài có ảnh -> Kiểm tra xem ảnh có hiện lên ngay lập tức cho người khác thấy không.
- **Thư viện**: Chia sẻ bệnh -> Kiểm tra hiển thị ảnh Base64 trên bảng tin.
- **Dung lượng**: Kiểm tra xem ảnh có bị mờ không sau khi nén 80%.
