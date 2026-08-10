from fastapi import FastAPI, UploadFile, File, Form, Query
from typing import Optional, List, Any
import pyodbc
from pydantic import BaseModel
import time
from datetime import datetime

app = FastAPI(title="Thần Nông AI - SQL Database Server (Cổng 8001)")

# Cấu hình SQL Server của bạn
CONN_STR = (
    "Driver={SQL Server};"
    "Server=TRONGNGHIA\\KKK;"
    "Database=ThanNongAI;"
    "UID=sa;"
    "PWD=12345;"
)

def get_db():
    return pyodbc.connect(CONN_STR)

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
    cursor.execute("SELECT id, uid, author, userPhotoUrl, question, imageUrl, disease, timestamp, likesCount, commentsCount FROM Posts ORDER BY timestamp DESC")
    rows = cursor.fetchall()

    posts = []
    for row in rows:
        p_id = row[0]
        # Fetch liked UIDs for this post
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
    p_id = int(parentCommentId) if (parentCommentId and parentCommentId != "null") else None
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
    cursor.execute("SELECT id, authorName, content, timestamp, authorUid, authorPhotoUrl, parentCommentId FROM Comments WHERE postId = ? ORDER BY timestamp ASC", (post_id,))
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
    else:
        cursor.execute("INSERT INTO Likes (uid, postId) VALUES (?, ?)", (uid, postId))
    # Sync count
    cursor.execute("UPDATE Posts SET likesCount = (SELECT COUNT(*) FROM Likes WHERE postId = ?) WHERE id = ?", (postId, postId))
    db.commit()
    return {"status": "success"}

# --- THỐNG KÊ (STATS) ---
@app.post("/disease_stats/increment")
async def inc_stats(name: str = Form(...), imageUrl: Optional[str] = Form("")):
    db = get_db()
    cursor = db.cursor()
    cursor.execute("SELECT 1 FROM DiseaseStats WHERE diseaseName = ?", (name,))
    if cursor.fetchone():
        cursor.execute("UPDATE DiseaseStats SET count = count + 1, lastImageUrl = ? WHERE diseaseName = ?", (imageUrl, name))
    else:
        cursor.execute("INSERT INTO DiseaseStats (diseaseName, count, lastImageUrl) VALUES (?, 1, ?)", (name, imageUrl))
    db.commit()
    return {"status": "success"}

@app.get("/disease_stats/top")
async def get_top():
    db = get_db()
    cursor = db.cursor()
    cursor.execute("SELECT TOP 5 diseaseName, count, lastImageUrl FROM DiseaseStats ORDER BY count DESC")
    return [{"name": r[0], "count": r[1], "imageUrl": r[2]} for r in cursor.fetchall()]

# --- THÔNG BÁO (NOTIFICATIONS) ---
@app.get("/notifications/{uid}")
async def get_notifs(uid: str):
    db = get_db()
    cursor = db.cursor()
    cursor.execute("SELECT id, targetUid, senderName, senderAvatar, type, postId, postContent, timestamp, isRead FROM Notifications WHERE targetUid = ? ORDER BY timestamp DESC", (uid,))
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
    db = get_db()
    cursor = db.cursor()
    cursor.execute(
        "INSERT INTO Notifications (targetUid, senderName, senderAvatar, senderUid, type, postId, postContent) VALUES (?, ?, ?, ?, ?, ?, ?)",
        (targetUid, senderName, senderAvatar, senderUid, type, postId, postContent)
    )
    db.commit()
    return {"status": "success"}

# --- PROFILE PHOTO SYNC ---
@app.post("/users/update_photo")
async def update_profile_photo(uid: str = Form(...), photoBase64: str = Form(...)):
    # Cập nhật ảnh đại diện của người dùng này trên toàn bộ các bài viết và bình luận cũ trong SQL
    db = get_db()
    cursor = db.cursor()
    cursor.execute("UPDATE Posts SET userPhotoUrl = ? WHERE uid = ?", (photoBase64, uid))
    cursor.execute("UPDATE Comments SET authorPhotoUrl = ? WHERE authorUid = ?", (photoBase64, uid))
    cursor.execute("UPDATE Notifications SET senderAvatar = ? WHERE senderUid = ?", (photoBase64, uid))
    db.commit()
    return {"status": "success"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8001)
