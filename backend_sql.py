from fastapi import FastAPI, UploadFile, File, Form, Query
from typing import Optional, List, Any, Dict
import pyodbc
from pydantic import BaseModel
import time
from datetime import datetime

app = FastAPI(title="Thần Nông AI - SQL Database Server (Cổng 8001)")

# Cấu hình SQL Server
CONN_STR = (
    "Driver={SQL Server};"
    "Server=TRONGNGHIA\\KKK;"
    "Database=ThanNongAI;"
    "UID=sa;"
    "PWD=12345;"
)

def get_db():
    return pyodbc.connect(CONN_STR)

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
        cursor.execute("SELECT displayName, email, photoBase64 FROM Users WHERE uid = ?", (uid,))
        existing = cursor.fetchone()

        if existing:
            # Chỉ cập nhật avatar nếu giá trị gửi lên không rỗng (tránh bị mất ảnh khi login máy mới chưa có cache)
            final_photo = photoBase64 if (photoBase64 and len(photoBase64) > 100) else existing[2]
            cursor.execute(
                "UPDATE Users SET displayName = ?, email = ?, photoBase64 = ? WHERE uid = ?",
                (displayName, email, final_photo, uid)
            )
        else:
            cursor.execute(
                "INSERT INTO Users (uid, displayName, email, photoBase64) VALUES (?, ?, ?, ?)",
                (uid, displayName, email, photoBase64)
            )
        db.commit()
        return {"status": "success"}
    except Exception as e:
        return {"status": "error", "message": str(e)}

@app.get("/users/{uid}")
async def get_user(uid: str):
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
        cursor.execute(
            "INSERT INTO Posts (uid, author, userPhotoUrl, question, imageUrl, disease) VALUES (?, ?, ?, ?, ?, ?)",
            (uid, author, userPhotoUrl, question, imageUrl, disease)
        )
        db.commit()
        return {"status": "success", "message": "Post created"}
    except Exception as e:
        print(f"ERROR Post: {str(e)}")
        return {"status": "error", "message": str(e)}

@app.get("/posts")
async def get_posts():
    db = get_db()
    cursor = db.cursor()
    # SỬ DỤNG JOIN ĐỂ LUÔN LẤY ẢNH ĐẠI DIỆN MỚI NHẤT TỪ BẢNG USERS
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
            "timestamp": int(row[7].timestamp() * 1000),
            "likesCount": row[8], "commentsCount": row[9],
            "likedBy": liked_uids
        })
    return posts

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

@app.get("/comments/{post_id}")
async def get_comments(post_id: int):
    db = get_db()
    cursor = db.cursor()
    # JOIN USERS ĐỂ LẤY AVATAR MỚI NHẤT
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
        "id": r[0], "authorName": r[1], "content": r[2], "timestamp": int(r[3].timestamp() * 1000),
        "authorUid": r[4], "authorPhotoUrl": r[5], "parentCommentId": r[6]
    } for r in rows]

# --- YÊU THÍCH (LIKES) ---
@app.post("/likes")
async def toggle_like(uid: str = Form(...), postId: int = Form(...)):
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
        db = get_db()
        cursor = db.cursor()
        cursor.execute(
            "INSERT INTO Notifications (targetUid, senderName, senderAvatar, senderUid, type, postId, postContent) VALUES (?, ?, ?, ?, ?, ?, ?)",
            (targetUid, senderName, senderAvatar, senderUid, type, postId, postContent)
        )
        db.commit()
        return {"status": "success"}
    except Exception as e:
        return {"status": "error", "message": str(e)}

@app.get("/notifications/{uid}")
async def get_notifs(uid: str):
    db = get_db()
    cursor = db.cursor()
    # JOIN USERS ĐỂ LẤY AVATAR NGƯỜI GỬI MỚI NHẤT
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
        "type": r[4], "postId": r[5], "postContent": r[6], "timestamp": int(r[7].timestamp() * 1000),
        "read": bool(r[8])
    } for r in rows]

@app.post("/notifications/read/{notif_id}")
async def mark_read(notif_id: int):
    db = get_db()
    cursor = db.cursor()
    cursor.execute("UPDATE Notifications SET isRead = 1 WHERE id = ?", (notif_id,))
    db.commit()
    return {"status": "success"}

# --- THƯ VIỆN & MẸO (DYNAMIC CONTENT) ---
@app.get("/diseases")
async def get_all_diseases():
    db = get_db()
    cursor = db.cursor()
    cursor.execute("SELECT name, description, treatment, imageResource FROM Diseases")
    return [{"name": r[0], "description": r[1], "treatment": r[2], "imageResource": r[3]} for r in cursor.fetchall()]

@app.post("/diseases")
async def add_disease(name: str = Form(...), description: str = Form(...), treatment: str = Form(...), imageResource: str = Form("")):
    db = get_db()
    cursor = db.cursor()
    cursor.execute("IF EXISTS (SELECT 1 FROM Diseases WHERE name = ?) UPDATE Diseases SET description=?, treatment=?, imageResource=? WHERE name=? ELSE INSERT INTO Diseases (name, description, treatment, imageResource) VALUES (?,?,?,?)",
                   (name, description, treatment, imageResource, name, name, description, treatment, imageResource))
    db.commit()
    return {"status": "success"}

@app.delete("/admin/diseases/{name}")
async def delete_disease(name: str):
    db = get_db()
    cursor = db.cursor()
    cursor.execute("DELETE FROM Diseases WHERE name = ?", (name,))
    db.commit()
    return {"status": "success"}

@app.get("/tips")
async def get_all_tips():
    db = get_db()
    cursor = db.cursor()
    cursor.execute("SELECT id, title, content, imageResource FROM Tips")
    return [{"id": r[0], "title": r[1], "content": r[2], "imageResource": r[3]} for r in cursor.fetchall()]

@app.post("/tips")
async def add_tip(title: str = Form(...), content: str = Form(...), imageResource: str = Form("")):
    db = get_db()
    cursor = db.cursor()
    cursor.execute("INSERT INTO Tips (title, content, imageResource) VALUES (?, ?, ?)", (title, content, imageResource))
    db.commit()
    return {"status": "success"}

@app.delete("/admin/tips/{tip_id}")
async def delete_tip(tip_id: int):
    db = get_db()
    cursor = db.cursor()
    cursor.execute("DELETE FROM Tips WHERE id = ?", (tip_id,))
    db.commit()
    return {"status": "success"}

# --- ADMIN APIs ---
@app.get("/admin/stats")
async def get_admin_stats():
    db = get_db()
    cursor = db.cursor()
    cursor.execute("SELECT diseaseName, count FROM DiseaseStats ORDER BY count DESC")
    stats = [{"name": r[0], "count": r[1]} for r in cursor.fetchall()]
    cursor.execute("SELECT COUNT(*) FROM Users")
    user_count = cursor.fetchone()[0]
    cursor.execute("SELECT COUNT(*) FROM Posts")
    post_count = cursor.fetchone()[0]
    return {"diseaseStats": stats, "totalUsers": user_count, "totalPosts": post_count}

@app.get("/admin/users")
async def get_admin_users():
    db = get_db()
    cursor = db.cursor()
    cursor.execute("SELECT uid, displayName, email, isAdmin FROM Users")
    return [{"uid": r[0], "displayName": r[1], "email": r[2], "isAdmin": bool(r[3])} for r in cursor.fetchall()]

@app.delete("/admin/users/{uid}")
async def delete_user(uid: str):
    db = get_db()
    cursor = db.cursor()
    cursor.execute("DELETE FROM Users WHERE uid = ?", (uid,))
    db.commit()
    return {"status": "success"}

@app.delete("/admin/posts/{post_id}")
async def delete_post_admin(post_id: int):
    db = get_db()
    cursor = db.cursor()
    cursor.execute("DELETE FROM Posts WHERE id = ?", (post_id,))
    db.commit()
    return {"status": "success"}

# --- COMMON UTILS ---
@app.get("/posts/{post_id}")
async def get_post_by_id(post_id: int):
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
        "timestamp": int(row[7].timestamp() * 1000),
        "likesCount": row[8], "commentsCount": row[9],
        "likedBy": liked_uids
    }

@app.post("/history")
async def add_history(uid: str = Form(...), diseaseName: str = Form(...), confidence: float = Form(...), treatment: str = Form(...), imageBase64: str = Form(...)):
    try:
        db = get_db()
        cursor = db.cursor()
        cursor.execute("INSERT INTO History (uid, diseaseName, confidence, treatment, imageBase64) VALUES (?, ?, ?, ?, ?)", (uid, diseaseName, confidence, treatment, imageBase64))
        db.commit()
        return {"status": "success"}
    except Exception as e: return {"status": "error", "message": str(e)}

@app.get("/history/{uid}")
async def get_history(uid: str):
    db = get_db()
    cursor = db.cursor()
    cursor.execute("SELECT id, diseaseName, confidence, treatment, imageBase64, timestamp FROM History WHERE uid = ? ORDER BY timestamp DESC", (uid,))
    return [{"id": r[0], "diseaseName": r[1], "confidence": r[2], "treatment": r[3], "imageBase64": r[4], "timestamp": int(r[5].timestamp() * 1000)} for r in cursor.fetchall()]

@app.post("/users/update_photo")
async def update_profile_photo(uid: str = Form(...), photoBase64: str = Form(...)):
    try:
        db = get_db()
        cursor = db.cursor()
        cursor.execute("UPDATE Users SET photoBase64 = ? WHERE uid = ?", (photoBase64, uid))
        # Với JOIN, các lệnh UPDATE dưới đây không còn bắt buộc nhưng vẫn giữ để đảm bảo tính tương thích nếu cache chưa reload
        cursor.execute("UPDATE Posts SET userPhotoUrl = ? WHERE uid = ?", (photoBase64, uid))
        cursor.execute("UPDATE Comments SET authorPhotoUrl = ? WHERE authorUid = ?", (photoBase64, uid))
        cursor.execute("UPDATE Notifications SET senderAvatar = ? WHERE senderUid = ?", (photoBase64, uid))
        db.commit()
        return {"status": "success"}
    except Exception as e: return {"status": "error", "message": str(e)}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8001)
