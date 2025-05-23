package com.example.medcare.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MedicalRecord implements Serializable {

    private String id;
    private String nom;
    private String prenom;
    private String dateNaissance;
    private String sexe;
    private String groupeSanguin;
    private String allergies;
    private String maladiesChroniques;
    private String chirurgies;
    private String hospitalisations;
    private String medicaments;
    private String dosage;
    private String analyses;
    private String imageries;

    public MedicalRecord() {
    }

    public MedicalRecord(String id, String nom, String prenom, String dateNaissance, String sexe, String groupeSanguin,
                         String allergies, String maladiesChroniques, String chirurgies, String hospitalisations,
                         String medicaments, String dosage, String analyses, String imageries) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.sexe = sexe;
        this.groupeSanguin = groupeSanguin;
        this.allergies = allergies;
        this.maladiesChroniques = maladiesChroniques;
        this.chirurgies = chirurgies;
        this.hospitalisations = hospitalisations;
        this.medicaments = medicaments;
        this.dosage = dosage;
        this.analyses = analyses;
        this.imageries = imageries;
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(String dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public String getGroupeSanguin() {
        return groupeSanguin;
    }

    public void setGroupeSanguin(String groupeSanguin) {
        this.groupeSanguin = groupeSanguin;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getMaladiesChroniques() {
        return maladiesChroniques;
    }

    public void setMaladiesChroniques(String maladiesChroniques) {
        this.maladiesChroniques = maladiesChroniques;
    }

    public String getChirurgies() {
        return chirurgies;
    }

    public void setChirurgies(String chirurgies) {
        this.chirurgies = chirurgies;
    }

    public String getHospitalisations() {
        return hospitalisations;
    }

    public void setHospitalisations(String hospitalisations) {
        this.hospitalisations = hospitalisations;
    }

    public String getMedicaments() {
        return medicaments;
    }

    public void setMedicaments(String medicaments) {
        this.medicaments = medicaments;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getAnalyses() {
        return analyses;
    }

    public void setAnalyses(String analyses) {
        this.analyses = analyses;
    }

    public String getImageries() {
        return imageries;
    }

    public void setImageries(String imageries) {
        this.imageries = imageries;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("nom", nom);
        map.put("prenom", prenom);
        map.put("dateNaissance", dateNaissance);
        map.put("sexe", sexe);
        map.put("groupeSanguin", groupeSanguin);
        map.put("allergies", allergies);
        map.put("maladiesChroniques", maladiesChroniques);
        map.put("chirurgies", chirurgies);
        map.put("hospitalisations", hospitalisations);
        map.put("medicaments", medicaments);
        map.put("dosage", dosage);
        map.put("analyses", analyses);
        map.put("imageries", imageries);
        return map;
    }
}
