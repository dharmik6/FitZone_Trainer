package com.example.fitzonetrainer;

public class BookingItemList {
    private String name;
    private String email;
    private String status;
    private String id;

    public BookingItemList(String name, String email, String status, String id) {
        this.name = name;
        this.email = email;
        this.status = status;
        this.id = id;
    }

    // Getters and setters for the properties
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
