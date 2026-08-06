package com.example.smartcrop.models;

import com.google.gson.annotations.SerializedName;

public class ChatResponse {
    @SerializedName("response")
    private String response;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
