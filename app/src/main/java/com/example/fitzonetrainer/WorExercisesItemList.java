package com.example.fitzonetrainer;

public class WorExercisesItemList {
    private String name;
    private String id;
    private String body;
    private String imageUrl;
    private String wid;

    public WorExercisesItemList() {
        // Empty constructor needed for Firestore
    }

    public WorExercisesItemList(String name, String body, String imageUrl, String id, String wid) {
        this.name = name;
        this.body = body;
        this.imageUrl = imageUrl;
        this.id = id;
        this.wid = wid;
    }

    // Getters and setters for the properties
    public String getName() {
        return name;
    }

    public String getWid() {
        return wid;
    }

    public void setWid(String wid) {
        this.wid = wid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}

