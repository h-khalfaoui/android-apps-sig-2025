package com.example.geotourisme.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.mindrot.jbcrypt.BCrypt;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id_user;

    public String nom;
    public String email;
    @ColumnInfo(name = "password")
    public String password;
    public int visites;
    public User() { }

    // Constructeur existant
    public User(String nom, String email, String password) {
        this.nom = nom;
        this.email = email;
        setPassword(password); // Hachage sécurisé
        this.visites = 0;
    }
    public String getPassword() {
        return password;
    }
    // Méthode de hachage corrigée
    public void setPassword(String password) {
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public boolean verifyPassword(String password) {
        return BCrypt.checkpw(password, this.password);
    }

    // Getters/Setters existants conservés
    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMot_de_passe(String mot_de_passe) {
        setPassword(mot_de_passe);
    }

    public int getId_user() {
        return id_user;
    }

    public String getNom() {
        return nom;
    }

    public String getEmail() {
        return email;
    }

    public String getMot_de_passe() {
        return password;
    }

    public int getVisites() {
        return visites;
    }

    public void setVisites(int visites) {
        this.visites = visites;
    }
}