package com.example.quietspaceeee.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reservations")
public class Reservation {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String userName;
    private String date;
    private String cafeName;
    private int numberOfPeople;
    private int estimatedDuration;

    private String userEmail;

    public Reservation(String userName, String date, String cafeName, int numberOfPeople, int estimatedDuration, String userEmail) {
        this.userName = userName;
        this.date = date;
        this.cafeName = cafeName;
        this.numberOfPeople = numberOfPeople;
        this.estimatedDuration = estimatedDuration;
        this.userEmail = userEmail;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    // Getters
    public int getId() { return id; }
    public String getUserName() { return userName; }
    public String getDate() { return date; }
    public String getCafeName() { return cafeName; }
    public int getNumberOfPeople() { return numberOfPeople; }
    public int getEstimatedDuration() { return estimatedDuration; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setDate(String date) { this.date = date; }
    public void setCafeName(String cafeName) { this.cafeName = cafeName; }
    public void setNumberOfPeople(int numberOfPeople) { this.numberOfPeople = numberOfPeople; }
    public void setEstimatedDuration(int estimatedDuration) { this.estimatedDuration = estimatedDuration; }
}
