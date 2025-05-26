package com.example.sigsignalement.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Signalement {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String type;
    public double latitude;
    public double longitude;
    public String date;
    public byte[] photo;
    public String utilisateurEmail;

    public Signalement(String type, double latitude, double longitude, String date, byte[] photo, String utilisateurEmail) {
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
        this.photo = photo;
        this.utilisateurEmail = utilisateurEmail;
    }
}
