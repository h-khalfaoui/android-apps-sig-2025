package com.example.medcare.model;

public class Disponibilite {

    private String etablissementId;
    private String professionalId;
    private String jour;
    private String heureDebut;
    private String heureFin;
    private boolean ouvert;

    public Disponibilite() {
    }

    public Disponibilite(String etablissementId, String professionalId, String jour, String heureDebut,
                         String heureFin, boolean ouvert) {
        this.etablissementId = etablissementId;
        this.professionalId = professionalId;
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.ouvert = ouvert;
    }

    public String getEtablissementId() {
        return etablissementId;
    }

    public void setEtablissementId(String etablissementId) {
        this.etablissementId = etablissementId;
    }

    public String getProfessionalId() {
        return professionalId;
    }

    public void setProfessionalId(String professionalId) {
        this.professionalId = professionalId;
    }

    public String getJour() {
        return jour;
    }

    public void setJour(String jour) {
        this.jour = jour;
    }

    public String getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(String heureDebut) {
        this.heureDebut = heureDebut;
    }

    public String getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(String heureFin) {
        this.heureFin = heureFin;
    }

    public boolean isOuvert() {
        return ouvert;
    }

    public void setOuvert(boolean ouvert) {
        this.ouvert = ouvert;
    }
}
