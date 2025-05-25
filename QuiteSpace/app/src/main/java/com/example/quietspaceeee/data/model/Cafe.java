package com.example.quietspaceeee.data.model;

public class Cafe {
    private int id;
    private String name;
    private String location;
    private String noiseLevel;
    private double averageCost;
    private double latitude;
    private double longitude;
    private String imageUrl;

    private String description;
    private String city;

    private String type;
    // Ajoute dans le modèle Cafe
    private String availability; // horaires
    private String equipments;   // ex: "wifi, prises, snacks"

    public Cafe(int id, String name,double latitude,double longitude, String description,String type, String availability, String equipments,String city, String location, String noiseLevel,
                double averageCost, String imageUrl) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.description = description;
        this.type = type;
        this.location = location;
        this.city = city;
        this.noiseLevel = noiseLevel;
        this.averageCost = averageCost;
        this.imageUrl = imageUrl;
        this.availability = availability;
        this.equipments = equipments;
    }

    public Cafe() {

    }


    public String getAvailability() { return availability; }
    public String getEquipments() { return equipments; }

    public double getLatitude() {return latitude; }

    public double getLongitude() {return longitude; }

    public int getId() { return id; }

    public String getCity() { return city; }


    public String getType() { return type; }


    public String getName() { return name; }

    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public String getNoiseLevel() { return noiseLevel; }
    public double getAverageCost() { return averageCost; }
    public String getImageUrl() { return imageUrl; }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setNoiseLevel(String noiseLevel) {
        this.noiseLevel = noiseLevel;
    }

    public void setAverageCost(double averageCost) {
        this.averageCost = averageCost;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public void setEquipments(String equipments) {
        this.equipments = equipments;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
