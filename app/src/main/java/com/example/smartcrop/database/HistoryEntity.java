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
    public String imagePath; // Path to local image
    public long timestamp;

    public HistoryEntity(String diseaseName, double confidence, String treatment, String imagePath, long timestamp) {
        this.diseaseName = diseaseName;
        this.confidence = confidence;
        this.treatment = treatment;
        this.imagePath = imagePath;
        this.timestamp = timestamp;
    }
}
