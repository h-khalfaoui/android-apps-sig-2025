package com.example.alert.models;

public class AlertData {
    String location;
    String description;
    byte[] image;
    String status;

    public AlertData(String location, String description, byte[] image, String status) {
        this.location = location;
        this.description = description;
        this.image = image;
        this.status = status;
    }

    public AlertData(String location, String description, byte[] image) {
        this(location, description, image, "acceptée"); // Défaut à "acceptée"
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public byte[] getImage() {
        return image;
    }

    public String getStatus() {
        return status;
    }
}
