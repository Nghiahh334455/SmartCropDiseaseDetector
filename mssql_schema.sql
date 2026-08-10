-- SCRIPT CẬP NHẬT CƠ SỞ DỮ LIỆU THẦN NÔNG AI (v15 - LƯU TRỮ ẢNH TRỰC TIẾP)

IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'ThanNongAI')
BEGIN
    CREATE DATABASE ThanNongAI;
END
GO

USE ThanNongAI;
GO

-- 1. Bảng bài viết (Posts)
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
GO

-- 2. Bảng bình luận (Comments)
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
GO

-- 3. Bảng yêu thích (Likes)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Likes')
BEGIN
    CREATE TABLE Likes (
        uid NVARCHAR(128),
        postId INT FOREIGN KEY REFERENCES Posts(id) ON DELETE CASCADE,
        PRIMARY KEY (uid, postId)
    );
END
GO

-- 4. Bảng thông báo (Notifications)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Notifications')
BEGIN
    CREATE TABLE Notifications (
        id INT PRIMARY KEY IDENTITY(1,1),
        targetUid NVARCHAR(128) NOT NULL,
        senderName NVARCHAR(255),
        senderAvatar NVARCHAR(MAX),
        senderUid NVARCHAR(128), -- Firebase UID của người gửi
        type NVARCHAR(50),
        postId INT,
        postContent NVARCHAR(MAX),
        timestamp DATETIME DEFAULT GETDATE(),
        isRead BIT DEFAULT 0
    );
END
GO

-- 5. Bảng thống kê bệnh (DiseaseStats)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'DiseaseStats')
BEGIN
    CREATE TABLE DiseaseStats (
        diseaseName NVARCHAR(255) PRIMARY KEY,
        count INT DEFAULT 1,
        lastImageUrl NVARCHAR(MAX)
    );
END
GO
