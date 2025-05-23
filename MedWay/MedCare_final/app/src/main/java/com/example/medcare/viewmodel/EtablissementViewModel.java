package com.example.medcare.viewmodel;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.Transformations;

import com.google.firebase.firestore.DocumentReference;

import java.util.List;

import com.example.medcare.model.Etablissement;
import com.example.medcare.repository.EtablissementRepository;

public class EtablissementViewModel extends ViewModel {

    private static final String TAG = "EtablissementViewModel";
    private final EtablissementRepository repository;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> _operationSuccessId = new MutableLiveData<>(); // Holds ID on success
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();

    // Public LiveData for UI observation
    public LiveData<Boolean> isLoading() { return _isLoading; }
    public LiveData<String> getOperationSuccessId() { return _operationSuccessId; } // Event: new ID after insert
    public LiveData<String> getErrorMessage() { return _errorMessage; }

    private LiveData<List<Etablissement>> allEtablissements; // Lazily initialized stream

    public EtablissementViewModel(EtablissementRepository etablissementRepository) {
        this.repository = etablissementRepository;
        // Removed initialization from constructor - let getter handle it
    }

    public LiveData<List<Etablissement>> getAllEtablissementsStream() {
        if (allEtablissements == null) {
            allEtablissements = repository.getAllEtablissementsStream();
        }
        return allEtablissements;
    }

    public LiveData<Etablissement> getEtablissementByIdStream(String documentId) {
        // Pass String ID now
        return repository.getEtablissementByIdStream(documentId);
    }

    public void insert(Etablissement etablissement) {
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        _operationSuccessId.setValue(null);

        repository.insert(etablissement)
                .addOnSuccessListener((DocumentReference documentReference) -> {
                    String newId = documentReference.getId();
                    Log.i(TAG, "Insert successful! New ID: " + newId);
                    _isLoading.setValue(false);
                    _operationSuccessId.setValue(newId); // Send new ID as success signal
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Insert failed", e);
                    _isLoading.setValue(false);
                    _errorMessage.setValue("Failed to add establishment: " + e.getMessage());
                });
    }

    public void update(Etablissement etablissement, String documentId) {
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        _operationSuccessId.setValue(null); // Use generic success or specific for update if needed

        repository.update(etablissement, documentId)
                .addOnSuccessListener(aVoid -> {
                    Log.i(TAG, "Update successful for ID: " + documentId);
                    _isLoading.setValue(false);
                    _operationSuccessId.setValue(documentId); // Indicate success for this ID
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Update failed for ID: " + documentId, e);
                    _isLoading.setValue(false);
                    _errorMessage.setValue("Failed to update establishment: " + e.getMessage());
                });
    }

    public void delete(String documentId) {
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        _operationSuccessId.setValue(null); // Use generic success or specific for delete if needed

        repository.delete(documentId)
                .addOnSuccessListener(aVoid -> {
                    Log.i(TAG, "Delete successful for ID: " + documentId);
                    _isLoading.setValue(false);
                    _operationSuccessId.setValue(documentId); // Indicate success for this ID
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Delete failed for ID: " + documentId, e);
                    _isLoading.setValue(false);
                    _errorMessage.setValue("Failed to delete establishment: " + e.getMessage());
                });
    }
}