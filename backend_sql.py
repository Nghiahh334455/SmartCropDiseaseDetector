import os
from dotenv import load_dotenv
from fastapi import FastAPI, UploadFile, File, Form, Query
from fastapi.middleware.cors import CORSMiddleware
from typing import Optional, List, Any, Dict
import pyodbc
from pydantic import BaseModel
import time
from datetime import datetime
from r2_storage import upload_base64_to_r2

load_dotenv()

app = FastAPI(title="Thần Nông AI - SQL Database Server (Cổng 8001)")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

def get_db():
    """Tự động phát hiện ODBC Driver và thử danh sách Server name khả thi để kết nối SQL Server."""
    drivers = pyodbc.drivers()
    preferred_drivers = [
        "ODBC Driver 17 for SQL Server",
        "ODBC Driver 18 for SQL Server",
        "SQL Server",
        "SQL Server Native Client 11.0"
    ]

    driver = os.getenv("SQL_DRIVER")
    if not driver:
        for candidate in preferred_drivers:
            if candidate in drivers:
                driver = candidate
                break
    driver = driver or "SQL Server"

    server = os.getenv("SQL_SERVER", "localhost")
    database = os.getenv("SQL_DATABASE", "ThanNongAI")
    uid = os.getenv("SQL_UID", "sa")
    password = os.getenv("SQL_PWD", "Admin123")

    servers = [
        server,
        r"TRONGNGHIA\KKK",
        r"localhost\KKK",
        r"127.0.0.1\KKK",
        r"localhost",
        r"127.0.0.1"
    ]

    unique_servers = []
    seen = set()
    for candidate in servers:
        if candidate and candidate not in seen:
            seen.add(candidate)
            unique_servers.append(candidate)

    for current_server in unique_servers:
        conn_str = (
            f"Driver={{{driver}}};"
            f"Server={current_server};"
            f"Database={database};"
            f"UID={uid};"
            f"PWD={password};"
            f"TrustServerCertificate=yes;"
        )
        try:
            return pyodbc.connect(conn_str, timeout=3)
        except Exception:
            continue

    default_conn_str = (
        f"Driver={{{driver}}};"
        f"Server={server};"
        f"Database={database};"
        f"UID={uid};"
        f"PWD={password};"
        f"TrustServerCertificate=yes;"
    )
    return pyodbc.connect(default_conn_str)


def init_db():
    """Tự động kiểm tra và khởi tạo các bảng còn thiếu trong SQL Server"""
    try:
        db = get_db()
        cursor = db.cursor()
        cursor.execute("""
            IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Users')
            BEGIN
                CREATE TABLE Users (
                    uid NVARCHAR(128) PRIMARY KEY,
                    displayName NVARCHAR(255),
                    email NVARCHAR(255),
                    photoBase64 NVARCHAR(MAX),
                    isAdmin BIT DEFAULT 0,
                    createdAt DATETIME DEFAULT GETDATE()
                );
                INSERT INTO Users (uid, displayName, email, isAdmin) VALUES ('12345N', 'Quản trị viên', 'admin@thannongai.vn', 1);
            END

            IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Posts')
            BEGIN
                CREATE TABLE Posts (
                    id INT PRIMARY KEY IDENTITY(1,1),
                    uid NVARCHAR(128) NOT NULL,
                    author NVARCHAR(255),
                    userPhotoUrl NVARCHAR(MAX),
                    question NVARCHAR(MAX),
                    imageUrl NVARCHAR(MAX),
                    disease NVARCHAR(255),
                    timestamp DATETIME DEFAULT GETDATE(),
                    likesCount INT DEFAULT 0,
                    commentsCount INT DEFAULT 0
                );
            END

            IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Comments')
            BEGIN
                CREATE TABLE Comments (
                    id INT PRIMARY KEY IDENTITY(1,1),
                    postId INT FOREIGN KEY REFERENCES Posts(id) ON DELETE CASCADE,
                    authorName NVARCHAR(255),
                    authorUid NVARCHAR(128),
                    authorPhotoUrl NVARCHAR(MAX),
                    content NVARCHAR(MAX),
                    timestamp DATETIME DEFAULT GETDATE(),
                    parentCommentId INT DEFAULT NULL
                );
            END

            IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Likes')
            BEGIN
                CREATE TABLE Likes (
                    uid NVARCHAR(128),
                    postId INT FOREIGN KEY REFERENCES Posts(id) ON DELETE CASCADE,
                    PRIMARY KEY (uid, postId)
                );
            END

            IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Notifications')
            BEGIN
                CREATE TABLE Notifications (
                    id INT PRIMARY KEY IDENTITY(1,1),
                    targetUid NVARCHAR(128) NOT NULL,
                    senderName NVARCHAR(255),
                    senderAvatar NVARCHAR(MAX),
                    senderUid NVARCHAR(128),
                    type NVARCHAR(50),
                    postId INT,
                    postContent NVARCHAR(MAX),
                    timestamp DATETIME DEFAULT GETDATE(),
                    isRead BIT DEFAULT 0
                );
            END

            IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'DiseaseStats')
            BEGIN
                CREATE TABLE DiseaseStats (
                    diseaseName NVARCHAR(255) PRIMARY KEY,
                    count INT DEFAULT 1,
                    lastImageUrl NVARCHAR(MAX)
                );
            END

            IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Diseases')
            BEGIN
                CREATE TABLE Diseases (
                    id INT PRIMARY KEY IDENTITY(1,1),
                    name NVARCHAR(255) UNIQUE,
                    description NVARCHAR(MAX),
                    treatment NVARCHAR(MAX),
                    imageResource NVARCHAR(MAX)
                );
            END

            IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Tips')
            BEGIN
                CREATE TABLE Tips (
                    id INT PRIMARY KEY IDENTITY(1,1),
                    title NVARCHAR(255),
                    content NVARCHAR(MAX),
                    imageResource NVARCHAR(255)
                );
            END

            IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'History')
            BEGIN
                CREATE TABLE History (
                    id INT PRIMARY KEY IDENTITY(1,1),
                    uid NVARCHAR(128) NOT NULL,
                    diseaseName NVARCHAR(255),
                    confidence FLOAT,
                    treatment NVARCHAR(MAX),
                    imageBase64 NVARCHAR(MAX),
                    timestamp DATETIME DEFAULT GETDATE()
                );
            END
        """)
        db.commit()
        print("[SQL Server] Da tu dong kiem tra va khoi tao cac bang CSDL thanh cong!")
        seed_real_world_data()
    except Exception as e:
        print("[SQL Server Init Warning] " + str(e))


def seed_real_world_data():
    """Tự động bơm dữ liệu nông nghiệp thực tế chuẩn Việt Nam nếu CSDL chưa có dữ liệu"""
    try:
        db = get_db()
        cursor = db.cursor()

        # 1. Bơm Thư viện Bệnh Cây Trồng (Diseases)
        cursor.execute("SELECT COUNT(*) FROM Diseases")
        if cursor.fetchone()[0] == 0:
            diseases = [
                ("Bệnh Sương Mai (Late Blight)", "Úng nước nâu đen trên lá cà chua/khoai tây, có màng nấm trắng xám dưới lá khi ẩm ướt.", "Phun Ridomil Gold 68WG, Daconil 75WP hoặc vi sinh Trichoderma.", "img_tomato_late_blight_1"),
                ("Bệnh Đốm Vòng (Early Blight)", "Đốm nâu đen có các đường vòng đồng tâm đặc trưng như hình bia bắn trên lá.", "Sử dụng Score 250EC, Anvil 5SC hoặc vi sinh Bio-Clean.", "img_tomato_early_blight_1"),
                ("Bệnh Đạo Ôn Lúa (Rice Blast)", "Vết bệnh hình thoi mắt én màu xám nâu trên lá lúa và cổ bông làm lép hạt.", "Phun ngay Beam 75WP, Fuji-One 40EC, tạm ngưng bón đạm.", "img_rice_blast_1"),
                ("Bệnh Bạc Lá Lúa (Bacterial Leaf Blight)", "Vết cháy sọc dài bìa lá màu vàng xám khô xơ do vi khuẩn Xanthomonas.", "Sử dụng Starner 20WP, Physan 20L kết hợp ngắt nước.", "img_rice_bacterial_blight_1"),
                ("Bệnh Rỉ Sắt (Coffee Rust)", "Bột phấn màu cam rỉ sắt phủ dày ở mặt dưới lá cà phê làm rụng lá hàng loạt.", "Phun Tilt Super 300EC, Anvil 5SC, tỉa cành thông thoáng.", "img_coffee_rust_1"),
                ("Bệnh Khảm Lá Virus (Mosaic Virus)", "Lá đu đủ, dưa hấu biến dạng loang lổ xanh vàng, xoăn ngọn do bọ trĩ truyền virus.", "Diệt côn trùng truyền bệnh bằng bẫy dính vàng, phun dầu neem.", "img_papaya_mosaic_1"),
                ("Bệnh Phấn Trắng (Powdery Mildew)", "Lớp bột màu trắng như phấn bao phủ mặt trên lá dưa leo, nho làm khô héo.", "Phun Microthiol Special 80WG (Lưu huỳnh), Anvil 5SC.", "img_cucumber_powdery_mildew_1"),
                ("Bệnh Thán Thư (Anthracnose)", "Đốm đen lõm sâu trên quả xoài, ớt và lá, làm thối rụng quả nhanh chóng.", "Phun Amistar Top 325SC, Antracol 70WP.", "img_chili_anthracnose_1"),
                ("Bệnh Thối Rễ Thối Thân (Foot Rot)", "Nấm Phytophthora làm thối cổ rễ hồ tiêu, bưởi làm lá vàng rụng chết nhanh.", "Tưới gốc vi sinh Trichoderma, tưới Agrifos 400 phòng trừ.", "img_pepper_foot_rot_1"),
                ("Bệnh Thối Nhũn (Soft Rot)", "Mô cây bắp cải, hoa lan nhũn nước có mùi hôi nồng đặc trưng do vi khuẩn.", "Cắt bỏ phần nhũn, rắc vôi bột, phun Poner 40TB.", "img_cabbage_soft_rot_1")
            ]
            cursor.executemany("INSERT INTO Diseases (name, description, treatment, imageResource) VALUES (?, ?, ?, ?)", diseases)

        # 2. Bơm Mẹo Chăm Sóc Nông Nghiệp (Tips)
        cursor.execute("SELECT COUNT(*) FROM Tips")
        if cursor.fetchone()[0] == 0:
            tips = [
                ("Kỹ thuật tưới nước nhỏ giọt tiết kiệm & phòng nấm", "Tưới nước vào gốc cây lúc 6-8h sáng, tránh tưới phun mưa lên lá chiều tối để hạn chế nấm bệnh phát triển.", "img_tip_water"),
                ("Bón phân cân đối N-P-K theo từng giai đoạn", "Giảm bón Đạm (N) khi cây ra hoa đậu quả, tăng cường Kali (K) giúp thân cây cứng cáp chống rụng quả.", "img_tip_fertilizer"),
                ("Sử dụng vi sinh Trichoderma phòng thối rễ", "Trộn nấm đối kháng Trichoderma với phân hữu cơ ủ hoai mục bón lót giúp tiêu diệt nấm hại trong đất.", "img_tip_organic"),
                ("Luân canh cây trồng họ Đậu cải tạo đất", "Trồng luân canh cây họ Đậu giúp cố định Đạm tự nhiên vào đất và cắt đứt vòng đời sâu bệnh hại cây họ Cà.", "img_tip_rotation"),
                ("Cắt tỉa cành lá sát gốc thông thoáng", "Cắt tỉa bớt lá dịch sát gốc giúp tán cây thông thoáng, tăng ánh sáng, giảm độ ẩm nội tán hạn chế rệp sáp.", "img_tip_pruning"),
                ("Bổ sung Canxi & Bo chống nứt quả", "Phun phân bón lá Canxi-Bo định kỳ giai đoạn nuôi trái giúp vỏ trái dẻo dai, chống nứt thối đít quả.", "img_tip_foliar"),
                ("Kỹ thuật ủ phân hữu cơ vi sinh tại nhà", "Ủ phế phẩm nông nghiệp (rơm rạ, vỏ cà phê) với men vi sinh 30-45 ngày tạo phân bón sạch giàu dinh dưỡng.", "img_tip_compost"),
                ("Sử dụng bẫy dính vàng diệt bọ trĩ & rệp sáp", "Treo bẫy dính màu vàng xung quanh vườn để thu hút và tiêu diệt rệp sáp, bọ trĩ không cần xịt thuốc hóa học.", "img_tip_trap")
            ]
            cursor.executemany("INSERT INTO Tips (title, content, imageResource) VALUES (?, ?, ?)", tips)

        # 3. Bơm Thống Kê Bệnh Hại (DiseaseStats)
        # Không bơm dữ liệu mẫu để thống kê chỉ lấy từ lượt quét thực tế của người dùng
        pass

        # 4. Dọn dẹp tài khoản giả lập để Admin chỉ hiển thị 100% tài khoản thực tế từ Firebase
        cursor.execute("DELETE FROM Users WHERE uid LIKE 'user_0%'")

        db.commit()
        print("[SQL Server] Da tu dong nap thanh cong bo du lieu nong nghiep thuc te!")
    except Exception as e:
        print("[SQL Seed Warning] " + str(e))


@app.on_event("startup")
async def startup_event():
    init_db()


@app.get("/")
async def root_healthcheck():
    try:
        db = get_db()
        cursor = db.cursor()
        cursor.execute("SELECT 1")
        return {"status": "online", "message": "Kết nối SQL Server thành công!"}
    except Exception as e:
        return {"status": "error", "message": f"Lỗi kết nối SQL Server: {str(e)}"}


# --- QUẢN LÝ NGƯỜI DÙNG (USERS) ---
@app.post("/users")
async def sync_user(
    uid: str = Form(...),
    displayName: Optional[str] = Form("Người dùng"),
    email: Optional[str] = Form(""),
    photoBase64: Optional[str] = Form("")
):
    try:
        db = get_db()
        cursor = db.cursor()
        r2_photo = upload_base64_to_r2(photoBase64, folder="avatars") if photoBase64 else ""
        cursor.execute("SELECT displayName, email, photoBase64 FROM Users WHERE uid = ?", (uid,))
        existing = cursor.fetchone()

        if existing:
            final_photo = r2_photo if (r2_photo and len(r2_photo) > 5) else existing[2]
            cursor.execute(
                "UPDATE Users SET displayName = ?, email = ?, photoBase64 = ? WHERE uid = ?",
                (displayName, email, final_photo, uid)
            )
        else:
            cursor.execute(
                "INSERT INTO Users (uid, displayName, email, photoBase64) VALUES (?, ?, ?, ?)",
                (uid, displayName, email, r2_photo)
            )
        db.commit()
        return {"status": "success"}
    except Exception as e:
        return {"status": "error", "message": str(e)}

@app.get("/users/{uid}")
async def get_user(uid: str):
    try:
        db = get_db()
        cursor = db.cursor()
        cursor.execute("SELECT uid, displayName, email, photoBase64, isAdmin FROM Users WHERE uid = ?", (uid,))
        row = cursor.fetchone()
        if row:
            return {
                "uid": row[0], "displayName": row[1], "email": row[2],
                "photoBase64": row[3], "isAdmin": bool(row[4])
            }
        return {"status": "error", "message": "User not found"}
    except Exception as e:
        return {"status": "error", "message": str(e)}

@app.get("/users/email/{email}")
async def get_user_by_email(email: str):
    try:
        db = get_db()
        cursor = db.cursor()
        cursor.execute("SELECT uid, displayName, email, photoBase64, isAdmin FROM Users WHERE LOWER(email) = LOWER(?)", (email.strip(),))
        row = cursor.fetchone()
        if row:
            return {
                "status": "success",
                "uid": row[0], "displayName": row[1], "email": row[2],
                "photoBase64": row[3], "isAdmin": bool(row[4])
            }
        return {"status": "error", "message": "User not found"}
    except Exception as e:
        return {"status": "error", "message": str(e)}


# --- DIỄN ĐÀN (FORUM) ---
@app.post("/posts")
async def create_post(
    uid: str = Form(...),
    author: str = Form("Người dùng Thần Nông AI"),
    question: Optional[str] = Form(""),
    userPhotoUrl: Optional[str] = Form(""),
    imageUrl: Optional[str] = Form(""),
    disease: Optional[str] = Form("Chia sẻ từ cộng đồng")
):
    try:
        db = get_db()
        cursor = db.cursor()
        r2_user_photo = upload_base64_to_r2(userPhotoUrl, folder="avatars") if userPhotoUrl else ""
        r2_image_url = upload_base64_to_r2(imageUrl, folder="posts") if imageUrl else ""
        cursor.execute(
            "INSERT INTO Posts (uid, author, userPhotoUrl, question, imageUrl, disease) VALUES (?, ?, ?, ?, ?, ?)",
            (uid, author, r2_user_photo, question, r2_image_url, disease)
        )
        db.commit()
        return {"status": "success", "message": "Post created"}
    except Exception as e:
        print(f"ERROR Post: {str(e)}")
        return {"status": "error", "message": str(e)}

@app.get("/posts")
async def get_posts():
    try:
        db = get_db()
        cursor = db.cursor()
        cursor.execute("""
            SELECT p.id, p.uid, p.author,
                   COALESCE(u.photoBase64, p.userPhotoUrl) as userPhotoUrl,
                   p.question, p.imageUrl, p.disease, p.timestamp, p.likesCount, p.commentsCount
            FROM Posts p
            LEFT JOIN Users u ON p.uid = u.uid
            ORDER BY p.timestamp DESC
        """)
        rows = cursor.fetchall()

        posts = []
        for row in rows:
            p_id = row[0]
            cursor.execute("SELECT uid FROM Likes WHERE postId = ?", (p_id,))
            liked_uids = [r[0] for r in cursor.fetchall()]

            posts.append({
                "id": p_id, "uid": row[1], "author": row[2], "userPhotoUrl": row[3],
                "question": row[4], "imageUrl": row[5], "disease": row[6],
                "timestamp": int(row[7].timestamp() * 1000) if row[7] else 0,
                "likesCount": row[8], "commentsCount": row[9],
                "likedBy": liked_uids
            })
        return posts
    except Exception as e:
        print(f"❌ Lỗi get_posts: {e}")
        return []

# --- BÌNH LUẬN (COMMENTS) ---
@app.post("/comments")
async def add_comment(
    postId: int = Form(...),
    authorName: str = Form("Thần nông"),
    authorUid: str = Form(...),
    content: str = Form(...),
    authorPhotoUrl: Optional[str] = Form(""),
    parentCommentId: Optional[str] = Form(None)
):
    try:
        p_id = int(parentCommentId) if (parentCommentId and parentCommentId != "null" and parentCommentId != "None") else None
        db = get_db()
        cursor = db.cursor()
        cursor.execute(
            "INSERT INTO Comments (postId, authorName, authorUid, content, authorPhotoUrl, parentCommentId) VALUES (?, ?, ?, ?, ?, ?)",
            (postId, authorName, authorUid, content, authorPhotoUrl, p_id)
        )
        cursor.execute("UPDATE Posts SET commentsCount = (SELECT COUNT(*) FROM Comments WHERE postId = ?) WHERE id = ?", (postId, postId))
        db.commit()
        return {"status": "success"}
    except Exception as e:
        return {"status": "error", "message": str(e)}

@app.get("/comments/{post_id}")
async def get_comments(post_id: int):
    try:
        db = get_db()
        cursor = db.cursor()
        cursor.execute("""
            SELECT c.id, c.authorName, c.content, c.timestamp, c.authorUid,
                   COALESCE(u.photoBase64, c.authorPhotoUrl) as authorPhotoUrl,
                   c.parentCommentId
            FROM Comments c
            LEFT JOIN Users u ON c.authorUid = u.uid
            WHERE c.postId = ?
            ORDER BY c.timestamp ASC
        """, (post_id,))
        rows = cursor.fetchall()
        return [{
            "id": r[0], "authorName": r[1], "content": r[2], "timestamp": int(r[3].timestamp() * 1000) if r[3] else 0,
            "authorUid": r[4], "authorPhotoUrl": r[5], "parentCommentId": r[6]
        } for r in rows]
    except Exception as e:
        return []

# --- YÊU THÍCH (LIKES) ---
@app.post("/likes")
async def toggle_like(uid: str = Form(...), postId: int = Form(...)):
    try:
        db = get_db()
        cursor = db.cursor()
        cursor.execute("SELECT 1 FROM Likes WHERE uid = ? AND postId = ?", (uid, postId))
        if cursor.fetchone():
            cursor.execute("DELETE FROM Likes WHERE uid = ? AND postId = ?", (uid, postId))
            action = "unliked"
        else:
            cursor.execute("INSERT INTO Likes (uid, postId) VALUES (?, ?)", (uid, postId))
            action = "liked"
        cursor.execute("UPDATE Posts SET likesCount = (SELECT COUNT(*) FROM Likes WHERE postId = ?) WHERE id = ?", (postId, postId))
        db.commit()
        return {"status": "success", "action": action}
    except Exception as e:
        return {"status": "error", "message": str(e)}

# --- THÔNG BÁO (NOTIFICATIONS) ---
@app.post("/notifications")
async def add_notif(
    targetUid: str = Form(...),
    senderName: str = Form(...),
    senderAvatar: str = Form(...),
    senderUid: str = Form(...),
    type: str = Form(...),
    postId: int = Form(...),
    postContent: str = Form(...)
):
    try:
        print(f"🔔 [New Notification] {senderName} ({type}) -> {targetUid}")
        db = get_db()
        cursor = db.cursor()
        cursor.execute(
            "INSERT INTO Notifications (targetUid, senderName, senderAvatar, senderUid, type, postId, postContent) VALUES (?, ?, ?, ?, ?, ?, ?)",
            (targetUid, senderName, senderAvatar, senderUid, type, postId, postContent)
        )
        db.commit()
        return {"status": "success"}
    except Exception as e:
        print(f"❌ [Notification Error] {e}")
        return {"status": "error", "message": str(e)}

@app.get("/notifications/{uid}")
async def get_notifs(uid: str):
    try:
        db = get_db()
        cursor = db.cursor()
        cursor.execute("""
            SELECT n.id, n.targetUid, n.senderName,
                   COALESCE(u.photoBase64, n.senderAvatar) as senderAvatar,
                   n.type, n.postId, n.postContent, n.timestamp, n.isRead
            FROM Notifications n
            LEFT JOIN Users u ON n.senderUid = u.uid
            WHERE n.targetUid = ?
            ORDER BY n.timestamp DESC
        """, (uid,))
        rows = cursor.fetchall()
        return [{
            "id": r[0], "targetUid": r[1], "senderName": r[2], "senderAvatar": r[3],
            "type": r[4], "postId": r[5], "postContent": r[6], "timestamp": int(r[7].timestamp() * 1000) if r[7] else 0,
            "read": bool(r[8])
        } for r in rows]
    except Exception as e:
        return []

@app.post("/notifications/read/{notif_id}")
async def mark_read(notif_id: int):
    try:
        db = get_db()
        cursor = db.cursor()
        cursor.execute("UPDATE Notifications SET isRead = 1 WHERE id = ?", (notif_id,))
        db.commit()
        return {"status": "success"}
    except Exception as e:
        return {"status": "error", "message": str(e)}

# --- THƯ VIỆN & MẸO (DYNAMIC CONTENT) ---
@app.get("/diseases")
async def get_all_diseases():
    try:
        db = get_db()
        cursor = db.cursor()
        cursor.execute("SELECT name, description, treatment, imageResource FROM Diseases")
        return [{"name": r[0], "description": r[1], "treatment": r[2], "imageResource": r[3]} for r in cursor.fetchall()]
    except Exception as e:
        return []

@app.post("/diseases")
async def add_disease(name: str = Form(...), description: str = Form(...), treatment: str = Form(...), imageResource: str = Form("")):
    try:
        db = get_db()
        cursor = db.cursor()
        r2_disease_img = upload_base64_to_r2(imageResource, folder="diseases") if imageResource else ""
        cursor.execute("IF EXISTS (SELECT 1 FROM Diseases WHERE name = ?) UPDATE Diseases SET description=?, treatment=?, imageResource=? WHERE name=? ELSE INSERT INTO Diseases (name, description, treatment, imageResource) VALUES (?,?,?,?)",
                       (name, description, treatment, r2_disease_img, name, name, description, treatment, r2_disease_img))
        db.commit()
        return {"status": "success"}
    except Exception as e:
        return {"status": "error", "message": str(e)}

@app.delete("/admin/diseases/{name}")
async def delete_disease(name: str):
    try:
        db = get_db()
        cursor = db.cursor()
        cursor.execute("DELETE FROM Diseases WHERE name = ?", (name,))
        db.commit()
        return {"status": "success"}
    except Exception as e:
        return {"status": "error", "message": str(e)}

@app.get("/tips")
async def get_all_tips():
    try:
        db = get_db()
        cursor = db.cursor()
        cursor.execute("SELECT id, title, content, imageResource FROM Tips")
        return [{"id": r[0], "title": r[1], "content": r[2], "imageResource": r[3]} for r in cursor.fetchall()]
    except Exception as e:
        return []

@app.post("/tips")
async def add_tip(title: str = Form(...), content: str = Form(...), imageResource: str = Form("")):
    try:
        db = get_db()
        cursor = db.cursor()
        r2_tip_img = upload_base64_to_r2(imageResource, folder="tips") if imageResource else ""
        cursor.execute("INSERT INTO Tips (title, content, imageResource) VALUES (?, ?, ?)", (title, content, r2_tip_img))
        db.commit()
        return {"status": "success"}
    except Exception as e:
        return {"status": "error", "message": str(e)}

@app.delete("/admin/tips/{tip_id}")
async def delete_tip(tip_id: int):
    try:
        db = get_db()
        cursor = db.cursor()
        cursor.execute("DELETE FROM Tips WHERE id = ?", (tip_id,))
        db.commit()
        return {"status": "success"}
    except Exception as e:
        return {"status": "error", "message": str(e)}

# --- ADMIN APIs ---
@app.get("/admin/stats")
async def get_admin_stats():
    try:
        db = get_db()
        cursor = db.cursor()
        cursor.execute("SELECT diseaseName, count FROM DiseaseStats ORDER BY count DESC")
        stats = [{"name": r[0], "count": r[1]} for r in cursor.fetchall()]
        cursor.execute("SELECT COUNT(*) FROM Users")
        user_count = cursor.fetchone()[0]
        cursor.execute("SELECT COUNT(*) FROM Posts")
        post_count = cursor.fetchone()[0]
        return {"diseaseStats": stats, "totalUsers": user_count, "totalPosts": post_count}
    except Exception as e:
        return {"diseaseStats": [], "totalUsers": 0, "totalPosts": 0}

@app.get("/admin/users")
async def get_admin_users():
    try:
        db = get_db()
        cursor = db.cursor()
        cursor.execute("SELECT uid, displayName, email, isAdmin FROM Users")
        return [{"uid": r[0], "displayName": r[1], "email": r[2], "isAdmin": bool(r[3])} for r in cursor.fetchall()]
    except Exception as e:
        return []

@app.delete("/admin/users/{uid}")
async def delete_user(uid: str):
    try:
        db = get_db()
        cursor = db.cursor()
        cursor.execute("DELETE FROM Users WHERE uid = ?", (uid,))
        db.commit()
        return {"status": "success"}
    except Exception as e:
        return {"status": "error", "message": str(e)}

@app.delete("/admin/posts/{post_id}")
async def delete_post_admin(post_id: int):
    try:
        db = get_db()
        cursor = db.cursor()
        cursor.execute("DELETE FROM Posts WHERE id = ?", (post_id,))
        db.commit()
        return {"status": "success"}
    except Exception as e:
        return {"status": "error", "message": str(e)}

# --- DISEASE STATS ---
@app.get("/disease_stats/top")
async def get_top_diseases():
    try:
        db = get_db()
        cursor = db.cursor()
        # Lọc bỏ các mục Cây Khỏe Mạnh và sắp xếp theo lượt quét giảm dần
        cursor.execute("""
            SELECT diseaseName, count, lastImageUrl
            FROM DiseaseStats
            WHERE diseaseName NOT LIKE '%Khỏe mạnh%'
              AND diseaseName NOT LIKE '%Healthy%'
              AND count > 0
            ORDER BY count DESC
        """)
        return [{"name": r[0], "count": r[1], "imageUrl": r[2]} for r in cursor.fetchall()]
    except Exception as e:
        print(f"❌ Lỗi get_top_diseases: {e}")
        return []

@app.post("/disease_stats/increment")
async def increment_disease_count(name: str = Form(...), imageUrl: str = Form("")):
    try:
        db = get_db()
        cursor = db.cursor()
        r2_stat_img = upload_base64_to_r2(imageUrl, folder="stats") if imageUrl else ""
        cursor.execute("IF EXISTS (SELECT 1 FROM DiseaseStats WHERE diseaseName = ?) UPDATE DiseaseStats SET count = count + 1, lastImageUrl = ? WHERE diseaseName = ? ELSE INSERT INTO DiseaseStats (diseaseName, count, lastImageUrl) VALUES (?, 1, ?)",
                       (name, r2_stat_img, name, name, r2_stat_img))
        db.commit()
        return {"status": "success"}
    except Exception as e:
        return {"status": "error", "message": str(e)}

# --- COMMON UTILS ---
@app.get("/posts/{post_id}")
async def get_post_by_id(post_id: int):
    try:
        db = get_db()
        cursor = db.cursor()
        cursor.execute("""
            SELECT p.id, p.uid, p.author,
                   COALESCE(u.photoBase64, p.userPhotoUrl) as userPhotoUrl,
                   p.question, p.imageUrl, p.disease, p.timestamp, p.likesCount, p.commentsCount
            FROM Posts p
            LEFT JOIN Users u ON p.uid = u.uid
            WHERE p.id = ?
        """, (post_id,))
        row = cursor.fetchone()
        if not row: return {"status": "error", "message": "Post not found"}
        cursor.execute("SELECT uid FROM Likes WHERE postId = ?", (post_id,))
        liked_uids = [r[0] for r in cursor.fetchall()]
        return {
            "id": row[0], "uid": row[1], "author": row[2], "userPhotoUrl": row[3],
            "question": row[4], "imageUrl": row[5], "disease": row[6],
            "timestamp": int(row[7].timestamp() * 1000) if row[7] else 0,
            "likesCount": row[8], "commentsCount": row[9],
            "likedBy": liked_uids
        }
    except Exception as e:
        return {"status": "error", "message": str(e)}

@app.post("/history")
async def add_history(uid: str = Form(...), diseaseName: str = Form(...), confidence: float = Form(...), treatment: str = Form(...), imageBase64: str = Form(...)):
    try:
        db = get_db()
        cursor = db.cursor()
        r2_history_img = upload_base64_to_r2(imageBase64, folder="history") if imageBase64 else ""
        cursor.execute("INSERT INTO History (uid, diseaseName, confidence, treatment, imageBase64) VALUES (?, ?, ?, ?, ?)", (uid, diseaseName, confidence, treatment, r2_history_img))
        db.commit()
        return {"status": "success"}
    except Exception as e: return {"status": "error", "message": str(e)}

@app.get("/history/{uid}")
async def get_history(uid: str):
    try:
        db = get_db()
        cursor = db.cursor()
        cursor.execute("SELECT id, diseaseName, confidence, treatment, imageBase64, timestamp FROM History WHERE uid = ? ORDER BY timestamp DESC", (uid,))
        return [{"id": r[0], "diseaseName": r[1], "confidence": r[2], "treatment": r[3], "imageBase64": r[4], "timestamp": int(r[5].timestamp() * 1000)} for r in cursor.fetchall()]
    except Exception as e:
        return []

@app.post("/users/update_photo")
async def update_profile_photo(uid: str = Form(...), photoBase64: str = Form(...)):
    try:
        db = get_db()
        cursor = db.cursor()
        r2_photo = upload_base64_to_r2(photoBase64, folder="avatars") if photoBase64 else ""
        cursor.execute("UPDATE Users SET photoBase64 = ? WHERE uid = ?", (r2_photo, uid))
        cursor.execute("UPDATE Posts SET userPhotoUrl = ? WHERE uid = ?", (r2_photo, uid))
        cursor.execute("UPDATE Comments SET authorPhotoUrl = ? WHERE authorUid = ?", (r2_photo, uid))
        cursor.execute("UPDATE Notifications SET senderAvatar = ? WHERE senderUid = ?", (r2_photo, uid))
        db.commit()
        return {"status": "success"}
    except Exception as e: return {"status": "error", "message": str(e)}

# --- CHUYÊN GIA AI (CHAT AI API) ---
def build_dynamic_chat_response(question: str) -> str:
    q = question.lower().strip()

    if "lúa" in q or "đạo ôn" in q or "bạc lá" in q or "cổ bông" in q:
        return (
            f"👨‍🌾 **Chuyên gia AI tư vấn cây lúa:**\n\n"
            f"📌 **Thắc mắc:** '{question}'\n"
            f"✅ **Khuyến nghị:** Giữ đồng ruộng luôn thông thoáng, ngưng bón ngay phân đạm nếu thấy vết bệnh. Phun thuốc đặc trị như **Beam 75WP**, **Fuji-One 40EC** đối với đạo ôn hoặc **Starner** đối với bạc lá vi khuẩn."
        )

    if "cà phê" in q or "rỉ sắt" in q or "tiêu" in q or "chết nhanh" in q or "cao su" in q:
        return (
            f"👨‍🌾 **Chuyên gia AI tư vấn cây công nghiệp:**\n\n"
            f"📌 **Thắc mắc:** '{question}'\n"
            f"✅ **Khuyến nghị:** Cắt tỉa cành rậm rạp, vệ sinh vườn sạch sẽ. Đối với rỉ sắt cà phê nên phun **Anvil 5SC** hoặc **Tilt Super**. Với hồ tiêu bị chết nhanh, cần tưới gốc bằng **Agrifos 400** kết hợp **Ridomil Gold**."
        )

    if "ớt" in q or "xoài" in q or "thán thư" in q or "thối quả" in q or "thối trái" in q:
        return (
            f"👨‍🌾 **Chuyên gia AI tư vấn bệnh thán thư:**\n\n"
            f"📌 **Thắc mắc:** '{question}'\n"
            f"✅ **Khuyến nghị:** Dọn sạch quả thối đem tiêu hủy xa vườn. Phun thuốc có hoạt chất Azoxystrobin như **Amistar Top 325SC** hoặc **Score 250EC**. Bổ sung thêm phân bón lá **Canxi-Bo** để vỏ trái chắc khỏe hơn."
        )

    if "cà chua" in q or "dưa" in q or "sương mai" in q or "phấn trắng" in q or "khảm" in q or "bọ trĩ" in q:
        return (
            f"👨‍🌾 **Chuyên gia AI tư vấn rau màu:**\n\n"
            f"📌 **Thắc mắc:** '{question}'\n"
            f"✅ **Khuyến nghị:** Cắt tỉa lá già sát gốc, tránh tưới phun mưa vào chiều tối. Phun **Ridomil Gold 68WG** hoặc **Daconil 75WP** đối với sương mai. Với phấn trắng, có thể dùng các thuốc gốc Lưu huỳnh hoặc **Anvil**."
        )

    return (
        f"👨‍🌾 **Chuyên gia AI tư vấn nông nghiệp:**\n\n"
        f"📌 **Giải đáp:** '{question}'\n\n"
        f"✅ **Khuyến nghị:** Vệ sinh vườn sạch sẽ, giữ khoảng cách trồng hợp lý và theo dõi cây thường xuyên. Khi có dấu hiệu bệnh, hãy sử dụng các chế phẩm vi sinh như **Trichoderma** hoặc thuốc bảo vệ thực vật phù hợp với từng giai đoạn sinh trưởng."
    )

@app.post("/chat")
async def chat_ai(question: str = Form(...)):
    try:
        reply = build_dynamic_chat_response(question)
        return {"response": reply}
    except Exception as e:
        return {"response": f"🤖 **Chuyên gia AI trả lời:**\n\nĐối với thắc mắc '{question}': Nên giữ cây thông thoáng, giảm độ ẩm và chăm sóc theo hướng dẫn phòng bệnh để duy trì sức khỏe cho cây trồng."}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8001)
