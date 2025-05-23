package com.example.medcare.viewmodel;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import com.example.medcare.model.Disponibilite;
import com.example.medcare.repository.DisponibiliteRepository;

public class DisponibiliteViewModel extends ViewModel {

    private static final String TAG = "DisponibiliteViewModel";
    private final DisponibiliteRepository repository;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> _operationSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();

    public LiveData<Boolean> isLoading() { return _isLoading; }
    public LiveData<Boolean> getOperationSuccess() { return _operationSuccess; }
    public LiveData<String> getErrorMessage() { return _errorMessage; }

    public DisponibiliteViewModel(DisponibiliteRepository disponibiliteRepository) {
        this.repository = disponibiliteRepository;
    }

    public LiveData<List<Disponibilite>> getDisponibilitesForProfessionalStream(String professionalId) {
        return repository.getDisponibilitesForProfessionalStream(professionalId);
    }

    public void upsert(Disponibilite disponibilite, String documentId) {
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        _operationSuccess.setValue(false);

        repository.upsertDisponibilite(disponibilite, documentId)
                .addOnSuccessListener(aVoid -> {
                    Log.i(TAG, "Upsert successful for " + documentId);
                    _isLoading.setValue(false);
                    _operationSuccess.setValue(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Upsert failed for " + documentId, e);
                    _isLoading.setValue(false);
                    _errorMessage.setValue("Failed to save availability: " + e.getMessage());
                });
    }

    public void delete(String documentId) {
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        _operationSuccess.setValue(false);

        repository.deleteDisponibilite(documentId)
                .addOnSuccessListener(aVoid -> {
                    Log.i(TAG, "Delete successful for " + documentId);
                    _isLoading.setValue(false);
                    _operationSuccess.setValue(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Delete failed for " + documentId, e);
                    _isLoading.setValue(false);
                    _errorMessage.setValue("Failed to delete availability: " + e.getMessage());
                });
    }
}