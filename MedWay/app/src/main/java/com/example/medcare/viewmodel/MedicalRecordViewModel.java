package com.example.medcare.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.medcare.model.MedicalRecord;
import com.example.medcare.repository.MedicalRecordRepository;

public class MedicalRecordViewModel extends ViewModel {

    private final MedicalRecordRepository repository;
    private final MutableLiveData<MedicalRecord> medicalRecordLiveData;
    private final MutableLiveData<String> errorMessageLiveData;

    public MedicalRecordViewModel() {
        repository = new MedicalRecordRepository();
        medicalRecordLiveData = new MutableLiveData<>();
        errorMessageLiveData = new MutableLiveData<>();
    }

    // Pour récupérer un dossier médical par son ID
    public LiveData<MedicalRecord> getMedicalRecord(String recordId) {
        repository.getMedicalRecord(recordId, new MedicalRecordRepository.GetCallback() {
            @Override
            public void onSuccess(MedicalRecord medicalRecord) {
                medicalRecordLiveData.setValue(medicalRecord);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessageLiveData.setValue("Erreur lors du chargement du dossier médical.");
            }
        });
        return medicalRecordLiveData;
    }

    // Sauvegarder un dossier médical
    public void saveMedicalRecord(MedicalRecord record) {
        repository.saveMedicalRecord(record, new MedicalRecordRepository.SaveCallback() {
            @Override
            public void onSuccess() {
                medicalRecordLiveData.setValue(record);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessageLiveData.setValue("Erreur lors de la sauvegarde du dossier médical.");
            }
        });
    }

    // Observer les erreurs pour afficher un message dans l'UI
    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }
}
