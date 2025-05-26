package com.example.geotourisme.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Review {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;
    public String siteName;
    public float rating;
}
