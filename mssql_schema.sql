-- SCRIPT TẠO CƠ SỞ DỮ LIỆU THẦN NÔNG AI (MSSQL)

CREATE DATABASE ThanNongAI;
GO

USE ThanNongAI;
GO

-- 1. Bảng bài viết (Posts)
CREATE TABLE Posts (
    id INT PRIMARY KEY IDENTITY(1,1),
    uid NVARCHAR(128) NOT NULL, -- Firebase UID
    author NVARCHAR(255),
    userPhotoUrl NVARCHAR(MAX),
    question NVARCHAR(MAX),
    imageUrl NVARCHAR(MAX),
    disease NVARCHAR(255),
    timestamp DATETIME DEFAULT GETDATE(),
    likesCount INT DEFAULT 0,
    commentsCount INT DEFAULT 0
);

-- 2. Bảng bình luận (Comments)
CREATE TABLE Comments (
    id INT PRIMARY KEY IDENTITY(1,1),
    postId INT FOREIGN KEY REFERENCES Posts(id) ON DELETE CASCADE,
    authorName NVARCHAR(255),
    authorUid NVARCHAR(128),
    authorPhotoUrl NVARCHAR(MAX),
    content NVARCHAR(MAX),
    timestamp DATETIME DEFAULT GETDATE(),
    parentCommentId INT DEFAULT NULL -- NULL nếu là bình luận chính, có ID nếu là trả lời
);

-- 3. Bảng yêu thích (Likes)
CREATE TABLE Likes (
    uid NVARCHAR(128),
    postId INT FOREIGN KEY REFERENCES Posts(id) ON DELETE CASCADE,
    PRIMARY KEY (uid, postId)
);

-- 4. Bảng thông báo (Notifications)
CREATE TABLE Notifications (
    id INT PRIMARY KEY IDENTITY(1,1),
    targetUid NVARCHAR(128) NOT NULL,
    senderName NVARCHAR(255),
    senderAvatar NVARCHAR(MAX),
    type NVARCHAR(50), -- LIKE, COMMENT, SHARE
    postId INT,
    postContent NVARCHAR(MAX),
    timestamp DATETIME DEFAULT GETDATE(),
-- 5. Bảng thống kê bệnh (Disease Stats)
CREATE TABLE DiseaseStats (
    diseaseName NVARCHAR(255) PRIMARY KEY,
    count INT DEFAULT 1,
    lastImageUrl NVARCHAR(MAX) -- Link ảnh thực tế gần nhất
);
