package com.example.medcare.model;

public class TimeSlot {
    public final String time;
    public final boolean isAvailable;

    public TimeSlot(String time, boolean isAvailable) {
        this.time = time;
        this.isAvailable = isAvailable;
    }

}