package com.example.saydaliyati.Models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
import androidx.annotation.NonNull;

import java.io.Serializable;

@Entity(tableName = "pharmacies")
public class Pharmacy implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String name;

    @NonNull
    private String address;

    private double latitude;
    private double longitude;
    private String phone; // Champ ajouté
    private String hours; // Champ ajouté
    private boolean hasParking; // Champ ajouté

    // Non stocké dans la base de données : utilisé pour le tri/affichage
    private transient Double distance;

    // Constructeur principal que Room utilisera
    public Pharmacy(@NonNull String name, @NonNull String address, double latitude, double longitude,
                    String phone, String hours, boolean hasParking) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone = phone;
        this.hours = hours;
        this.hasParking = hasParking;
    }

    // Constructeur secondaire - ignoré par Room
    @Ignore
    public Pharmacy(@NonNull String name, @NonNull String address, double latitude, double longitude) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone = null;
        this.hours = null;
        this.hasParking = false;
    }

    // Getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @NonNull
    public String getName() { return name; }
    public void setName(@NonNull String name) { this.name = name; }

    @NonNull
    public String getAddress() { return address; }
    public void setAddress(@NonNull String address) { this.address = address; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getHours() { return hours; }
    public void setHours(String hours) { this.hours = hours; }

    public boolean isHasParking() { return hasParking; }
    public void setHasParking(boolean hasParking) { this.hasParking = hasParking; }
}