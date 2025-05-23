package com.example.medcare.repository;

import com.example.medcare.model.MedicalRecord;
import com.google.firebase.firestore.FirebaseFirestore;

public class MedicalRecordRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface SaveCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface GetCallback {
        void onSuccess(MedicalRecord medicalRecord);
        void onFailure(Exception e);
    }

    public void saveMedicalRecord(MedicalRecord record, SaveCallback callback) {
        db.collection("medical_records")
                .document(record.getId())
                .set(record)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public void getMedicalRecord(String recordId, GetCallback callback) {
        db.collection("medical_records")
                .document(recordId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        MedicalRecord medicalRecord = documentSnapshot.toObject(MedicalRecord.class);
                        callback.onSuccess(medicalRecord);
                    } else {
                        callback.onFailure(new Exception("Dossier médical non trouvé"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void deleteMedicalRecord(String recordId, SaveCallback callback) {
        db.collection("medical_records")
                .document(recordId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }
}
