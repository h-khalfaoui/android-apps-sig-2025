
package com.example.medcare.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.util.Log;

import com.example.medcare.model.RendezVous;
import com.example.medcare.repository.RendezVousRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class RendezVousViewModel extends ViewModel {

    private static final String TAG = "RendezVousViewModel";
    private final RendezVousRepository repository;

    // LiveData for states exposed to the UI
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() { return _isLoading; }

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() { return _errorMessage; }

    private final MutableLiveData<String> _operationResult = new MutableLiveData<>();
    public LiveData<String> getOperationResult() { return _operationResult; } // For cancel status etc.


    // Constructor for injection
    public RendezVousViewModel(RendezVousRepository rendezVousRepository) {
        this.repository = rendezVousRepository;
    }

    // Fetches the stream of appointments for a specific patient
    public LiveData<List<RendezVous>> getAppointmentsForPatient(String patientId) {

        if(patientId == null || patientId.isEmpty()){
            MutableLiveData<List<RendezVous>> errorData = new MutableLiveData<>();
            errorData.setValue(null); // Indicate error state or empty
            _errorMessage.setValue("Patient ID missing.");
            return errorData;
        }
        return repository.getAppointmentsForPatientStream(patientId);
    }

    // Example: Method to handle cancelling an appointment
    public void cancelAppointment(String appointmentId) {
        if (appointmentId == null || appointmentId.isEmpty()) {
            _errorMessage.setValue("Cannot cancel: Appointment ID missing.");
            return;
        }
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        _operationResult.setValue(null);

        repository.updateAppointmentStatus(appointmentId, "CANCELLED") // Use repository method
                .addOnSuccessListener(aVoid -> {
                    Log.i(TAG, "Appointment cancelled successfully: " + appointmentId);
                    _operationResult.setValue("Rendez-vous annulé.");
                    _isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to cancel appointment: " + appointmentId, e);
                    _errorMessage.setValue("Échec de l'annulation: " + e.getMessage());
                    _isLoading.setValue(false);
                });
    }

    public void clearOperationResult() {
        _operationResult.setValue(null);
    }
    public void clearErrorMessage() {
        _errorMessage.setValue(null);
    }
}