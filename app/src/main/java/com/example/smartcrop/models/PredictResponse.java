package com.example.smartcrop.models;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class PredictResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("disease_name_vi")
    private String diseaseName;

    @SerializedName("confidence")
    private double confidence;

    @SerializedName("ai_expert_advice")
    private String aiExpertAdvice;

    @SerializedName("treatment_details")
    private Map<String, String> treatmentDetails;

    @SerializedName("bbox")
    private Map<String, Integer> bbox;

    @SerializedName("email_sent")
    private boolean emailSent;

    @SerializedName("target_email")
    private String targetEmail;

    // Getters
    public String getStatus() { return status; }
    public String getDiseaseName() { return diseaseName; }
    public double getConfidence() { return confidence; }
    public String getAiExpertAdvice() { return aiExpertAdvice; }
    public Map<String, String> getTreatmentDetails() { return treatmentDetails; }
    public Map<String, Integer> getBbox() { return bbox; }
    public boolean isEmailSent() { return emailSent; }
    public String getTargetEmail() { return targetEmail; }

    // Setters
    public void setStatus(String status) { this.status = status; }
    public void setDiseaseName(String diseaseName) { this.diseaseName = diseaseName; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public void setAiExpertAdvice(String aiExpertAdvice) { this.aiExpertAdvice = aiExpertAdvice; }
    public void setTreatmentDetails(Map<String, String> treatmentDetails) { this.treatmentDetails = treatmentDetails; }
    public void setBbox(Map<String, Integer> bbox) { this.bbox = bbox; }
    public void setEmailSent(boolean emailSent) { this.emailSent = emailSent; }
    public void setTargetEmail(String targetEmail) { this.targetEmail = targetEmail; }
}
