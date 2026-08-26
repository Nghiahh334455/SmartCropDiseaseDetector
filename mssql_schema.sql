-- SCRIPT CẬP NHẬT CƠ SỞ DỮ LIỆU THẦN NÔNG AI (v16 - ADMIN & DYNAMIC CONTENT)

IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'ThanNongAI')
BEGIN
    CREATE DATABASE ThanNongAI;
END
GO

USE ThanNongAI;
GO

-- 0. Bảng Người dùng (Users) - Lưu thông tin tập trung để đồng bộ
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
    -- Thêm tài khoản Admin mặc định
    INSERT INTO Users (uid, displayName, email, isAdmin)
    VALUES ('12345N', 'Quản trị viên', 'admin@thannongai.vn', 1);
END
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
        senderUid NVARCHAR(128),
        type NVARCHAR(50), -- LIKE, COMMENT, SHARE, SYSTEM
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

-- 6. Bảng lịch sử chẩn đoán (History)
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
GO

-- 7. Bảng Thư viện bệnh (Diseases) - Để Admin quản lý
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Diseases')
BEGIN
    CREATE TABLE Diseases (
        id INT PRIMARY KEY IDENTITY(1,1),
        name NVARCHAR(255) UNIQUE,
        description NVARCHAR(MAX),
        treatment NVARCHAR(MAX),
        imageResource NVARCHAR(MAX) -- Có thể là tên ảnh res hoặc Base64
    );
END
GO

-- 8. Bảng Mẹo chăm sóc (Tips) - Để Admin quản lý
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Tips')
BEGIN
    CREATE TABLE Tips (
        id INT PRIMARY KEY IDENTITY(1,1),
        title NVARCHAR(255),
        content NVARCHAR(MAX),
        imageResource NVARCHAR(255)
    );
END
GO
