package com.example.fitzonetrainer;

public class ExercisesItemList {
    private String name;
    private String id;
    private String body;
    private String imageUrl;

    public ExercisesItemList() {
        // Empty constructor needed for Firestore
    }

    public ExercisesItemList(String name, String body, String imageUrl, String id) {
        this.name = name;
        this.body = body;
        this.imageUrl = imageUrl;
        this.id = id;
    }

    // Getters and setters for the properties
    public String getName() {
        return name;
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
