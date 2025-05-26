package com.example.geotourisme.model;


import android.support.annotation.Nullable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "commentaires",
        foreignKeys = {
                @ForeignKey(entity = User.class,
                        parentColumns = "id_user",
                        childColumns = "id_user",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Site.class,
                        parentColumns = "id_site",
                        childColumns = "id_site",
                        onDelete = ForeignKey.CASCADE)
        }
)
public class Commentaire {
    @PrimaryKey(autoGenerate = true)
    public int id_commentaire;
    @Nullable
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    public byte[] image;

    public String contenu;
    public String date_commentaire;

    public int id_user;
    public int id_site;
    private String imageUri;

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public void setId_commentaire(int id_commentaire) {
        this.id_commentaire = id_commentaire;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public void setDate_commentaire(String date_commentaire) {
        this.date_commentaire = date_commentaire;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public void setId_site(int id_site) {
        this.id_site = id_site;
    }

    public int getId_commentaire() {
        return id_commentaire;
    }

    public String getContenu() {
        return contenu;
    }

    public String getDate_commentaire() {
        return date_commentaire;
    }

    public int getId_user() {
        return id_user;
    }

    public int getId_site() {
        return id_site;
    }

    public Commentaire(int id_commentaire, String contenu, String date_commentaire, int id_user, int id_site) {
        this.id_commentaire = id_commentaire;
        this.contenu = contenu;
        this.date_commentaire = date_commentaire;
        this.id_user = id_user;
        this.id_site = id_site;
    }
    public Commentaire() {


    }
}
