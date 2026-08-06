package com.example.smartcrop.models;

import java.util.ArrayList;
import java.util.List;

public class DiseaseModel {
    public String name;
    public String description;
    public List<String> imageResources; // Danh sách tên file trong drawable
    public String treatment;

    public DiseaseModel(String name, String description, List<String> imageResources, String treatment) {
        this.name = name;
        this.description = description;
        this.imageResources = imageResources;
        this.treatment = treatment;
    }

    // Constructor cũ để tránh lỗi trong quá trình chuyển đổi nếu cần
    public DiseaseModel(String name, String description, String singleImage, String treatment) {
        this.name = name;
        this.description = description;
        this.imageResources = new ArrayList<>();
        this.imageResources.add(singleImage);
        this.treatment = treatment;
    }
}
