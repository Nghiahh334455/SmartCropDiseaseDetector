package com.example.smartcrop.models;

import com.google.gson.annotations.SerializedName;

public class PredictResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("disease_name")
    private String diseaseName;

    @SerializedName("confidence")
    private double confidence;

    @SerializedName("treatment")
    private String treatment;

    @SerializedName("warning_level")
    private String warningLevel;

    @SerializedName("image_result_base64")
    private String imageResultBase64;

    // Getters and Setters
    public String getStatus() { return status; }
    public String getDiseaseName() { return diseaseName; }
    public double getConfidence() { return confidence; }
    public String getTreatment() { return treatment; }
    public String getWarningLevel() { return warningLevel; }
    public String getImageResultBase64() { return imageResultBase64; }
}
