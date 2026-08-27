package com.example.smartcrop.ui.home;

public class TipModel {
    public int id;
    public String title;
    public String content;
    public String imageUrl;

    public TipModel(String title, String content, String imageUrl) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    public TipModel(int id, String title, String content, String imageUrl) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
    }
}
