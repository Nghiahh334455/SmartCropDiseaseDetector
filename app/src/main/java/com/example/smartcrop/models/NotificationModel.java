package com.example.smartcrop.models;

public class NotificationModel {
    private String id;
    private String type; // "LIKE", "COMMENT", "SHARE"
    private String senderName;
    private String senderAvatar;
    private String senderUid;
    private String postId;
    private String postContent;
    private long timestamp;
    private boolean read;

    public NotificationModel() {}

    public NotificationModel(String id, String type, String senderName, String senderAvatar, String senderUid, String postId, String postContent, long timestamp, boolean read) {
        this.id = id;
        this.type = type;
        this.senderName = senderName;
        this.senderAvatar = senderAvatar;
        this.senderUid = senderUid;
        this.postId = postId;
        this.postContent = postContent;
        this.timestamp = timestamp;
        this.read = read;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getSenderAvatar() { return senderAvatar; }
    public void setSenderAvatar(String senderAvatar) { this.senderAvatar = senderAvatar; }
    public String getSenderUid() { return senderUid; }
    public void setSenderUid(String senderUid) { this.senderUid = senderUid; }
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }
    public String getPostContent() { return postContent; }
    public void setPostContent(String postContent) { this.postContent = postContent; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
