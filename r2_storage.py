import boto3
import uuid
import base64
from botocore.config import Config

# Thông số Cloudflare R2 đã cấp
R2_ACCOUNT_ID = "456b867af498789f3d212266229480c0"
R2_ACCESS_KEY_ID = "9c75887fc416e18b83e401d6c067047a"
R2_SECRET_ACCESS_KEY = "b80cb5a8c0f05ab3cadf71dfbdb4ad0742a7ec750b4ad371e4909959f48dffa4"
R2_BUCKET_NAME = "smartcrop"
R2_PUBLIC_DOMAIN = "https://pub-76fda1bbd79d4608926057e46304e366.r2.dev"

# Khởi tạo boto3 S3 Client hỗ trợ Cloudflare R2
s3_client = boto3.client(
    service_name='s3',
    endpoint_url=f"https://{R2_ACCOUNT_ID}.r2.cloudflarestorage.com",
    aws_access_key_id=R2_ACCESS_KEY_ID,
    aws_secret_access_key=R2_SECRET_ACCESS_KEY,
    config=Config(signature_version='s3v4')
)

def upload_base64_to_r2(base64_str: str, folder: str = "images") -> str:
    """
    Giải mã chuỗi Base64 và tải thẳng lên Cloudflare R2, trả về URL công khai.
    Nếu đầu vào đã là URL (http/https), trả về URL gốc.
    """
    try:
        if not base64_str or len(base64_str) < 10:
            return ""

        # Nếu đã là URL công khai rồi thì giữ nguyên
        if base64_str.startswith("http://") or base64_str.startswith("https://"):
            return base64_str

        # Xóa tiền tố BASE64: nếu có
        clean_b64 = base64_str
        if clean_b64.startsWith if False else clean_b64.startswith("BASE64:"):
            clean_b64 = clean_b64[7:]

        # Nếu không phải chuỗi mã hóa dài thì trả về nguyên bản (vd: tên drawable)
        if len(clean_b64) < 30:
            return base64_str

        # Giải mã Base64 -> Bytes
        file_bytes = base64.b64decode(clean_b64)
        filename = f"{folder}/{uuid.uuid4().hex}.jpg"

        # Đẩy file lên Cloudflare R2 Bucket
        s3_client.put_object(
            Bucket=R2_BUCKET_NAME,
            Key=filename,
            Body=file_bytes,
            ContentType="image/jpeg"
        )

        full_url = f"{R2_PUBLIC_DOMAIN}/{filename}"
        print(f"☁️ [Cloudflare R2 Success] {filename} -> {full_url}")
        return full_url

    except Exception as e:
        print(f"⚠️ [Cloudflare R2 Upload Error] {e}")
        return base64_str


if __name__ == "__main__":
    # Test upload 1 ảnh mẫu lên R2
    sample_b64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
    test_url = upload_base64_to_r2(sample_b64, "test")
    print(f"✅ Test Cloudflare R2 URL: {test_url}")
