package com.example.medcare.viewmodel;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentReference;

import java.util.List;

import com.example.medcare.model.FileAttente;
import com.example.medcare.repository.FileAttenteRepository;

public class FileAttenteViewModel extends ViewModel {

    private static final String TAG = "FileAttenteViewModel";
    private final FileAttenteRepository repository;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> _operationSuccessId = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();

    // Public LiveData for UI observation
    public LiveData<Boolean> isLoading() { return _isLoading; }
    public LiveData<String> getOperationSuccessId() { return _operationSuccessId; }
    public LiveData<String> getErrorMessage() { return _errorMessage; }


    public FileAttenteViewModel(FileAttenteRepository fileAttenteRepository) {
        this.repository = fileAttenteRepository;
    }

    public LiveData<List<FileAttente>> getFileAttenteForEtablissementStream(String etablissementId) {
        // Call the repository method that returns a stream
        return repository.getFileAttenteForEtablissementStream(etablissementId);
    }

    public void insert(FileAttente fileAttente) {
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        _operationSuccessId.setValue(null); // Reset status

        repository.insert(fileAttente)
                .addOnSuccessListener((DocumentReference documentReference) -> {
                    String newId = documentReference.getId();
                    Log.i(TAG, "Waiting list entry added successfully! New ID: " + newId);
                    _isLoading.setValue(false);
                    _operationSuccessId.setValue(newId); // Signal success with new ID
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add waiting list entry", e);
                    _isLoading.setValue(false);
                    _errorMessage.setValue("Failed to add to waiting list: " + e.getMessage());
                });
    }

    public void update(FileAttente fileAttente, String documentId) {
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        _operationSuccessId.setValue(null);

        repository.update(fileAttente, documentId)
                .addOnSuccessListener(aVoid -> {
                    Log.i(TAG, "Update successful for ID: " + documentId);
                    _isLoading.setValue(false);
                    _operationSuccessId.setValue(documentId); // Indicate success for this ID
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Update failed for ID: " + documentId, e);
                    _isLoading.setValue(false);
                    _errorMessage.setValue("Failed to update waiting list entry: " + e.getMessage());
                });
    }

    public void delete(String documentId) {
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        _operationSuccessId.setValue(null);

        repository.delete(documentId)
                .addOnSuccessListener(aVoid -> {
                    Log.i(TAG, "Delete successful for ID: " + documentId);
                    _isLoading.setValue(false);
                    _operationSuccessId.setValue(documentId); // Indicate success for this ID
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Delete failed for ID: " + documentId, e);
                    _isLoading.setValue(false);
                    _errorMessage.setValue("Failed to delete waiting list entry: " + e.getMessage());
                });
    }
}