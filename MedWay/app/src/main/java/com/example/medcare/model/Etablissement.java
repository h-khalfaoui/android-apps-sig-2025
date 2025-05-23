package com.example.medcare.model;

public class Etablissement {

    private String documentId;

    private String nom;
    private String adresse;
    private String telephone;
    private String email;
    private String responsable;
    private String type;
    private String siteWeb;
    private String imageUrl;
    private String professionalOwnerId;

    public Etablissement() {}

    public Etablissement(String nom, String adresse, String telephone, String email,
                         String responsable, String type, String siteWeb, String imageUrl, String professionalOwnerId) {
        this.nom = nom;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.responsable = responsable;
        this.type = type;
        this.siteWeb = siteWeb;
        this.imageUrl = imageUrl;
        this.professionalOwnerId = professionalOwnerId;
    }

    public Etablissement(String nom, String adresse, String telephone, String email, String responsable, String type, String siteWeb, String imageUrl) {
    }

    // Getters
    public String getDocumentId() { return documentId; }
    public String getNom() { return nom; }
    public String getAdresse() { return adresse; }
    public String getTelephone() { return telephone; }
    public String getEmail() { return email; }
    public String getResponsable() { return responsable; }
    public String getType() { return type; }
    public String getSiteWeb() { return siteWeb; }
    public String getImageUrl() { return imageUrl; }
    public String getProfessionalOwnerId() { return professionalOwnerId; }


    // Setters
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public void setNom(String nom) { this.nom = nom; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public void setEmail(String email) { this.email = email; }
    public void setResponsable(String responsable) { this.responsable = responsable; }
    public void setType(String type) { this.type = type; }
    public void setSiteWeb(String siteWeb) { this.siteWeb = siteWeb; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setProfessionalOwnerId(String professionalOwnerId) { this.professionalOwnerId = professionalOwnerId; }
}