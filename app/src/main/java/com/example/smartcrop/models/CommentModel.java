package com.example.smartcrop.models;

import java.util.HashMap;
import java.util.Map;

public class CommentModel {
    public int id;
    public String authorName;
    public String content;
    public long timestamp;
    public String authorUid;
    public String authorPhotoUrl;
    public String parentCommentId; // Nếu != null thì là phản hồi của bình luận này
    public Map<String, Boolean> likedBy = new HashMap<>(); // UID -> true

    public CommentModel() {} // Required for Firestore

    public CommentModel(String authorName, String content, long timestamp, String authorUid, String authorPhotoUrl, String parentCommentId) {
        this.authorName = authorName;
        this.content = content;
        this.timestamp = timestamp;
        this.authorUid = authorUid;
        this.authorPhotoUrl = authorPhotoUrl;
        this.parentCommentId = parentCommentId;
    }
}
