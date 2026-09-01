import pyodbc
from r2_storage import upload_base64_to_r2
from backend_sql import get_db

def migrate_database_to_r2():
    print("🚀 [Cloudflare R2 Migration] Bắt đầu chuyển đổi dữ liệu từ Base64 sang Cloudflare R2 CDN...")
    try:
        db = get_db()
        cursor = db.cursor()

        # 1. Chuyển đổi ảnh Users (Avatars)
        cursor.execute("SELECT uid, photoBase64 FROM Users WHERE photoBase64 IS NOT NULL AND LEN(photoBase64) > 50")
        users = cursor.fetchall()
        print(f"📸 Đang xử lý {len(users)} tài khoản Users...")
        for uid, photo in users:
            if photo and (photo.startswith("BASE64:") or len(photo) > 200) and not photo.startswith("http"):
                new_url = upload_base64_to_r2(photo, folder="avatars")
                cursor.execute("UPDATE Users SET photoBase64 = ? WHERE uid = ?", (new_url, uid))

        # 2. Chuyển đổi ảnh Posts
        cursor.execute("SELECT id, userPhotoUrl, imageUrl FROM Posts")
        posts = cursor.fetchall()
        print(f"📝 Đang xử lý {len(posts)} bài viết Posts...")
        for p_id, user_photo, img in posts:
            new_uphoto = user_photo
            new_img = img
            updated = False

            if user_photo and (user_photo.startswith("BASE64:") or len(user_photo) > 200) and not user_photo.startswith("http"):
                new_uphoto = upload_base64_to_r2(user_photo, folder="avatars")
                updated = True

            if img and (img.startswith("BASE64:") or len(img) > 200) and not img.startswith("http"):
                new_img = upload_base64_to_r2(img, folder="posts")
                updated = True

            if updated:
                cursor.execute("UPDATE Posts SET userPhotoUrl = ?, imageUrl = ? WHERE id = ?", (new_uphoto, new_img, p_id))

        # 3. Chuyển đổi ảnh History
        cursor.execute("SELECT id, imageBase64 FROM History WHERE imageBase64 IS NOT NULL AND LEN(imageBase64) > 50")
        histories = cursor.fetchall()
        print(f"📜 Đang xử lý {len(histories)} lịch sử chẩn đoán History...")
        for h_id, img in histories:
            if img and (img.startswith("BASE64:") or len(img) > 200) and not img.startswith("http"):
                new_url = upload_base64_to_r2(img, folder="history")
                cursor.execute("UPDATE History SET imageBase64 = ? WHERE id = ?", (new_url, h_id))

        # 4. Chuyển đổi ảnh Diseases
        cursor.execute("SELECT id, imageResource FROM Diseases WHERE imageResource IS NOT NULL AND LEN(imageResource) > 50")
        diseases = cursor.fetchall()
        print(f"🌿 Đang xử lý {len(diseases)} thư viện bệnh Diseases...")
        for d_id, img in diseases:
            if img and (img.startswith("BASE64:") or len(img) > 200) and not img.startswith("http"):
                new_url = upload_base64_to_r2(img, folder="diseases")
                cursor.execute("UPDATE Diseases SET imageResource = ? WHERE id = ?", (new_url, d_id))

        # 5. Chuyển đổi ảnh Tips
        cursor.execute("SELECT id, imageResource FROM Tips WHERE imageResource IS NOT NULL AND LEN(imageResource) > 50")
        tips = cursor.fetchall()
        print(f"💡 Đang xử lý {len(tips)} mẹo chăm sóc Tips...")
        for t_id, img in tips:
            if img and (img.startswith("BASE64:") or len(img) > 200) and not img.startswith("http"):
                new_url = upload_base64_to_r2(img, folder="tips")
                cursor.execute("UPDATE Tips SET imageResource = ? WHERE id = ?", (new_url, t_id))

        db.commit()
        print("🎉 [Cloudflare R2 Migration Complete] Đã chuyển đổi thành công 100% CSDL sang Cloudflare R2 CDN!")

    except Exception as e:
        print(f"❌ Lỗi Migration: {e}")

if __name__ == "__main__":
    migrate_database_to_r2()
