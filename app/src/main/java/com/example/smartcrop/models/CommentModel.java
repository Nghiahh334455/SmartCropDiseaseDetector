package com.example.smartcrop.models;

import java.util.HashMap;
import java.util.Map;

public class CommentModel {
    public String authorName;
    public String content;
    public long timestamp;
    public String authorUid;
    public String authorPhotoUrl;
    public Map<String, Boolean> likedBy = new HashMap<>(); // UID -> true
    public Map<String, Boolean> sadBy = new HashMap<>();   // UID -> true

    public CommentModel() {} // Required for Firestore

    public CommentModel(String authorName, String content, long timestamp, String authorUid, String authorPhotoUrl) {
        this.authorName = authorName;
        this.content = content;
        this.timestamp = timestamp;
        this.authorUid = authorUid;
        this.authorPhotoUrl = authorPhotoUrl;
    }
}
