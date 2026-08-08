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

    // Getters
    public String getStatus() { return status; }
    public String getDiseaseName() { return diseaseName; }
    public double getConfidence() { return confidence; }
    public String getAiExpertAdvice() { return aiExpertAdvice; }
    public Map<String, String> getTreatmentDetails() { return treatmentDetails; }
    public Map<String, Integer> getBbox() { return bbox; }
}
