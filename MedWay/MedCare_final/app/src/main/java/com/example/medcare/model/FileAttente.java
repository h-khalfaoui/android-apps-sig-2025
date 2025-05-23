package com.example.medcare.model;

import com.google.firebase.Timestamp;

public class FileAttente {

    private String documentId;

    private String etablissementId;
    private String nomPersonne;
    private String prenomPersonne;
    private String heureArrivee;
    private String motif;
    private String statut;

    public FileAttente() {}

    public FileAttente(String etablissementId, String nomPersonne, String prenomPersonne,
                       String heureArrivee, String motif, String statut) {
        this.etablissementId = etablissementId;
        this.nomPersonne = nomPersonne;
        this.prenomPersonne = prenomPersonne;
        this.heureArrivee = heureArrivee;
        this.motif = motif;
        this.statut = statut;
    }

    // Getters
    public String getDocumentId() { return documentId; }
    public String getEtablissementId() { return etablissementId; }
    public String getNomPersonne() { return nomPersonne; }
    public String getPrenomPersonne() { return prenomPersonne; }
    public String getHeureArrivee() { return heureArrivee; }
    public String getMotif() { return motif; }
    public String getStatut() { return statut; }


    // Setters
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public void setEtablissementId(String etablissementId) { this.etablissementId = etablissementId; }
    public void setNomPersonne(String nomPersonne) { this.nomPersonne = nomPersonne; }
    public void setPrenomPersonne(String prenomPersonne) { this.prenomPersonne = prenomPersonne; }
    public void setHeureArrivee(String heureArrivee) { this.heureArrivee = heureArrivee; }
    public void setMotif(String motif) { this.motif = motif; }
    public void setStatut(String statut) { this.statut = statut; }
}