package com.example.smartcrop.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "diagnosis_history")
public class HistoryEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String diseaseName;
    public double confidence;
    public String treatment;
    public String imageBase64; // Lưu trực tiếp chuỗi ảnh Base64
    public long timestamp;

    public HistoryEntity(String diseaseName, double confidence, String treatment, String imageBase64, long timestamp) {
        this.diseaseName = diseaseName;
        this.confidence = confidence;
        this.treatment = treatment;
        this.imageBase64 = imageBase64;
        this.timestamp = timestamp;
    }
}
