package com.example.geotourisme.model;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "sites")
public class Site {

    @PrimaryKey(autoGenerate = true)
    private int id_site;

    @ColumnInfo(name = "nom_site")
    private String nom_site;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "type_site")
    private String type_site;


    @ColumnInfo(name = "localisation")
    private String localisation;

    @ColumnInfo(name = "latitude")
    private double latitude;

    @ColumnInfo(name = "longitude")
    private double longitude;

    @ColumnInfo(name = "nature_relief")
    private String nature_relief;
    private String imageUrl;

    @ColumnInfo(name = "visites", defaultValue = "0")
    public int visites;
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }


    public int getVisites() {
        return visites;
    }

    public void setVisites(int visites) {
        this.visites = visites;
    }


    public Site(String nom_site, String description, String type_site, String localisation, double latitude, double longitude, String nature_relief, String imageUrl) {
        this.nom_site = nom_site;
        this.description = description;
        this.type_site = type_site;
        this.localisation = localisation;
        this.latitude = latitude;
        this.longitude = longitude;
        this.nature_relief = nature_relief;
        this.imageUrl = imageUrl;
        this.visites=0;
    }

    public int getId_site() {
        return id_site;
    }

    public String getNom_site() {
        return nom_site;
    }

    public String getDescription() {
        return description;
    }

    public String getType_site() {
        return type_site;
    }

    public String getLocalisation() {
        return localisation;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getNature_relief() {
        return nature_relief;
    }



    public void setId_site(int id_site) {
        this.id_site = id_site;
    }

    public void setNom_site(String nom_site) {
        this.nom_site = nom_site;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType_site(String type_site) {
        this.type_site = type_site;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setNature_relief(String nature_relief) {
        this.nature_relief = nature_relief;
    }

}
