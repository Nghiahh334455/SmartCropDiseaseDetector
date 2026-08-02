package com.example.smartcrop.models;

public class DiseaseModel {
    public String name;
    public String description;
    public String imageUrl;
    public String treatment;

    public DiseaseModel(String name, String description, String imageUrl, String treatment) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.treatment = treatment;
    }
}
