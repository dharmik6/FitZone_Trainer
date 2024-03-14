package com.example.fitzonetrainer;

public class WorkoutPlansItemList {
    private String name;
    private String goal;
    private String image;
    private String id;


    public WorkoutPlansItemList() {
        // Empty constructor needed for Firestore
    }

    public WorkoutPlansItemList(String name, String goal, String image,String id) {
        this.name = name;
        this.goal = goal;
        this.image = image;
        this.id = id;
    }

    // Getters and setters for the properties
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }
}

