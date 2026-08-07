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
    public Integer parentCommentId; // SQL Server ID of parent comment
    public Map<String, Boolean> likedBy = new HashMap<>();

    public CommentModel() {}

    public CommentModel(String authorName, String content, long timestamp, String authorUid, String authorPhotoUrl, Integer parentCommentId) {
        this.authorName = authorName;
        this.content = content;
        this.timestamp = timestamp;
        this.authorUid = authorUid;
        this.authorPhotoUrl = authorPhotoUrl;
        this.parentCommentId = parentCommentId;
    }
}
