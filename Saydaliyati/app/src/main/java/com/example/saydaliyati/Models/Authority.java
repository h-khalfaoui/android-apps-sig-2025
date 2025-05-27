package com.example.saydaliyati.Models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "authorities")
public class Authority {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String username;

    @NonNull
    private String passwordHash; // Stockage du hash du mot de passe, pas le texte brut

    private String role; // Ajout : ADMIN, MODERATOR
    private String lastLoginDate; // Champ ajouté

    // Constructeur principal que Room utilisera
    public Authority(@NonNull String username, @NonNull String passwordHash, String role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // Constructeur secondaire - ignoré par Room
    @Ignore
    public Authority(@NonNull String username, @NonNull String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = "ADMIN"; // Rôle par défaut
    }

    // Getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @NonNull
    public String getUsername() { return username; }
    public void setUsername(@NonNull String username) { this.username = username; }

    @NonNull
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(@NonNull String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getLastLoginDate() { return lastLoginDate; }
    public void setLastLoginDate(String lastLoginDate) { this.lastLoginDate = lastLoginDate; }
}