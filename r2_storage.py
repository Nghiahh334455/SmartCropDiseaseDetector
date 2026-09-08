import os
import uuid
import base64
from dotenv import load_dotenv
import boto3
from botocore.config import Config

load_dotenv()

# Load Cloudflare R2 configuration from environment variables.
# This keeps secrets out of source control and still supports local development.
R2_ACCOUNT_ID = os.getenv("R2_ACCOUNT_ID", "")
R2_ACCESS_KEY_ID = os.getenv("R2_ACCESS_KEY_ID", "")
R2_SECRET_ACCESS_KEY = os.getenv("R2_SECRET_ACCESS_KEY", "")
R2_BUCKET_NAME = os.getenv("R2_BUCKET_NAME", "smartcrop")
R2_PUBLIC_DOMAIN = os.getenv("R2_PUBLIC_DOMAIN", "")

# Initialize the S3 client only when credentials are provided.
s3_client = None
if R2_ACCOUNT_ID and R2_ACCESS_KEY_ID and R2_SECRET_ACCESS_KEY:
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
        if clean_b64.startswith("BASE64:"):
            clean_b64 = clean_b64[7:]

        # Nếu không phải chuỗi mã hóa dài thì trả về nguyên bản (vd: tên drawable)
        if len(clean_b64) < 30:
            return base64_str

        if s3_client is None or not R2_PUBLIC_DOMAIN:
            print("[Cloudflare R2] Missing AWS/R2 credentials or public URL. Skipping upload.")
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
        print("[Cloudflare R2 Success] " + filename + " -> " + full_url)
        return full_url

    except Exception as e:
        print("[Cloudflare R2 Upload Error] " + str(e))
        return base64_str


if __name__ == "__main__":
    # Test upload 1 ảnh mẫu lên R2
    sample_b64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
    test_url = upload_base64_to_r2(sample_b64, "test")
    print("Test Cloudflare R2 URL: " + str(test_url))
