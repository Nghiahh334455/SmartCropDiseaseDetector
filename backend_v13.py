from fastapi import FastAPI, UploadFile, File, Form, Query
from typing import Optional, List, Any
import pyodbc
from pydantic import BaseModel
import time
from datetime import datetime

app = FastAPI()

# Cấu hình kết nối SQL Server của bạn
# Server: TRONGNGHIA\KKK
CONN_STR = (
    "Driver={SQL Server};"
    "Server=TRONGNGHIA\\KKK;"
    "Database=ThanNongAI;"
    "UID=sa;"
    "PWD=12345;"
)

def get_db():
    return pyodbc.connect(CONN_STR)

@app.post("/posts")
async def create_post(
    uid: str = Form(...),
    author: str = Form(...),
    question: str = Form(...),
    userPhotoUrl: Optional[str] = Form(None),
    imageUrl: Optional[str] = Form(None),
    disease: str = Form("Chia sẻ từ cộng đồng")
):
    db = get_db()
    cursor = db.cursor()
    cursor.execute(
        "INSERT INTO Posts (uid, author, userPhotoUrl, question, imageUrl, disease) VALUES (?, ?, ?, ?, ?, ?)",
        (uid, author, userPhotoUrl, question, imageUrl, disease)
    )
    db.commit()
    return {"status": "success"}

@app.get("/posts")
async def get_posts():
    db = get_db()
    cursor = db.cursor()
    cursor.execute("SELECT id, uid, author, userPhotoUrl, question, imageUrl, disease, timestamp, likesCount, commentsCount FROM Posts ORDER BY timestamp DESC")
    rows = cursor.fetchall()

    posts = []
    for row in rows:
        posts.append({
            "id": row[0],
            "uid": row[1],
            "author": row[2],
            "userPhotoUrl": row[3],
            "question": row[4],
            "imageUrl": row[5],
            "disease": row[6],
            "timestamp": int(row[7].timestamp() * 1000),
            "likesCount": row[8],
            "commentsCount": row[9]
        })
    return posts

@app.post("/comments")
async def add_comment(
    postId: int = Form(...),
    authorName: str = Form(...),
    authorUid: str = Form(...),
    content: str = Form(...),
    authorPhotoUrl: Optional[str] = Form(None),
    parentCommentId: Optional[int] = Form(None)
):
    db = get_db()
    cursor = db.cursor()
    cursor.execute(
        "INSERT INTO Comments (postId, authorName, authorUid, content, authorPhotoUrl, parentCommentId) VALUES (?, ?, ?, ?, ?, ?)",
        (postId, authorName, authorUid, content, authorPhotoUrl, parentCommentId)
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

    comments = []
    for row in rows:
        comments.append({
            "id": row[0],
            "authorName": row[1],
            "content": row[2],
            "timestamp": int(row[3].timestamp() * 1000),
            "authorUid": row[4],
            "authorPhotoUrl": row[5],
            "parentCommentId": row[6]
        })
    return comments

@app.post("/likes")
async def toggle_like(uid: str = Form(...), postId: int = Form(...)):
    db = get_db()
    cursor = db.cursor()
    cursor.execute("SELECT 1 FROM Likes WHERE uid = ? AND postId = ?", (uid, postId))
    exists = cursor.fetchone()
    if exists:
        cursor.execute("DELETE FROM Likes WHERE uid = ? AND postId = ?", (uid, postId))
    else:
        cursor.execute("INSERT INTO Likes (uid, postId) VALUES (?, ?)", (uid, postId))
    # Sync count
    cursor.execute("UPDATE Posts SET likesCount = (SELECT COUNT(*) FROM Likes WHERE postId = ?) WHERE id = ?", (postId, postId))
    db.commit()
    return {"status": "success"}

@app.get("/likes/{post_id}")
async def get_likes(post_id: int):
    db = get_db()
    cursor = db.cursor()
    cursor.execute("SELECT uid FROM Likes WHERE postId = ?", (post_id,))
    rows = cursor.fetchall()
    return [row[0] for row in rows]

@app.post("/notifications")
async def add_notification(
    targetUid: str = Form(...),
    senderName: str = Form(...),
    senderAvatar: str = Form(...),
    type: str = Form(...),
    postId: int = Form(...),
    postContent: str = Form(...)
):
    db = get_db()
    cursor = db.cursor()
    cursor.execute(
        "INSERT INTO Notifications (targetUid, senderName, senderAvatar, type, postId, postContent) VALUES (?, ?, ?, ?, ?, ?)",
        (targetUid, senderName, senderAvatar, type, postId, postContent)
    )
    db.commit()
    return {"status": "success"}

@app.get("/notifications/{uid}")
async def get_notifications(uid: str):
    db = get_db()
    cursor = db.cursor()
    cursor.execute("SELECT id, targetUid, senderName, senderAvatar, type, postId, postContent, timestamp, isRead FROM Notifications WHERE targetUid = ? ORDER BY timestamp DESC", (uid,))
    rows = cursor.fetchall()

    notifs = []
    for row in rows:
        notifs.append({
            "id": row[0],
            "targetUid": row[1],
            "senderName": row[2],
            "senderAvatar": row[3],
            "type": row[4],
            "postId": row[5],
            "postContent": row[6],
            "timestamp": int(row[7].timestamp() * 1000),
            "read": bool(row[8])
        })
    return notifs

@app.post("/notifications/read/{notif_id}")
async def mark_as_read(notif_id: int):
    db = get_db()
    cursor = db.cursor()
    cursor.execute("UPDATE Notifications SET isRead = 1 WHERE id = ?", (notif_id,))
    db.commit()
    return {"status": "success"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8001)
