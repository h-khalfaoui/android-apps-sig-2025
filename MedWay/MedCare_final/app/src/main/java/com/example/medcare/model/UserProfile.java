package com.example.medcare.model;


import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

@IgnoreExtraProperties
public class UserProfile {

    private String uid;
    private String name;
    private String email;
    private String role;
    private String status;
    private String licenseNumber;
    private String specialty;

    @ServerTimestamp
    private Date createdAt;
    public UserProfile() {

    }

    // --- Getters  ---
    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public String getLicenseNumber() { return licenseNumber; }
    public String getSpecialty() { return specialty; }
    public Date getCreatedAt() { return createdAt; }

    // --- Setters  ---
    public void setUid(String uid) { this.uid = uid; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setStatus(String status) { this.status = status; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }


    @Override
    public String toString() {
        return "UserProfile{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", status='" + status + '\'' +
                ", licenseNumber='" + licenseNumber + '\'' +
                ", specialty='" + specialty + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}