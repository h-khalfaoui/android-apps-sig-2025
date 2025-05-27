package com.example.saydaliyati.Models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import java.io.Serializable;

@Entity(
        tableName = "guard_dates",
        foreignKeys = @ForeignKey(
                entity = Pharmacy.class,
                parentColumns = "id",
                childColumns = "pharmacyId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("pharmacyId")} // Ajout d'index pour de meilleures performances
)
public class GuardDate implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int pharmacyId; // Clé étrangère vers Pharmacy

    @NonNull
    private String guardDate; // Format: "YYYY-MM-DD"

    private String startTime; // Nouveau champ: format "HH:MM"
    private String endTime;   // Nouveau champ: format "HH:MM"

    // Constructeur principal que Room utilisera
    public GuardDate(int pharmacyId, @NonNull String guardDate, String startTime, String endTime) {
        this.pharmacyId = pharmacyId;
        this.guardDate = guardDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Constructeur secondaire - ignoré par Room
    @Ignore
    public GuardDate(int pharmacyId, @NonNull String guardDate) {
        this.pharmacyId = pharmacyId;
        this.guardDate = guardDate;
        this.startTime = null;
        this.endTime = null;
    }

    // Getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPharmacyId() { return pharmacyId; }
    public void setPharmacyId(int pharmacyId) { this.pharmacyId = pharmacyId; }

    @NonNull
    public String getGuardDate() { return guardDate; }
    public void setGuardDate(@NonNull String guardDate) { this.guardDate = guardDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}