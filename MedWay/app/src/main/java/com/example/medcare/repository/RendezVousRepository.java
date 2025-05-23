package com.example.medcare.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.medcare.model.RendezVous;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class RendezVousRepository {

    private static final String TAG = "RendezVousRepository";
    private final FirebaseFirestore db;
    private final CollectionReference rendezvousCollection;

    public RendezVousRepository() {
        db = FirebaseFirestore.getInstance();
        rendezvousCollection = db.collection("rendezvous");
    }

    public LiveData<List<RendezVous>> getAppointmentsForDateStream(String etablissementId, String date) {
        MutableLiveData<List<RendezVous>> liveData = new MutableLiveData<>();

        Query query = rendezvousCollection
                .whereEqualTo("etablissementId", etablissementId)
                .whereEqualTo("appointmentDate", date);

        query.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen error for date " + date, e);
                liveData.postValue(new ArrayList<>());
                return;
            }

            ArrayList<RendezVous> appointments = new ArrayList<>();
            if (snapshots != null) {
                for (QueryDocumentSnapshot doc : snapshots) {
                    try {
                        RendezVous rendezVous = doc.toObject(RendezVous.class);
                        appointments.add(rendezVous);
                    } catch (Exception parseError) {
                        Log.e(TAG, "Error converting document " + doc.getId(), parseError);
                    }
                }
                Log.d(TAG, "Received " + appointments.size() + " appointments for " + date);
                liveData.postValue(appointments);
            } else {
                Log.d(TAG, "Received null snapshot for date " + date);
                liveData.postValue(new ArrayList<>());
            }
        });

        return liveData;
    }

    public LiveData<List<RendezVous>> getAppointmentsForPatientStream(String patientId) {
        MutableLiveData<List<RendezVous>> liveData = new MutableLiveData<>();

        Query query = rendezvousCollection
                .whereEqualTo("patientId", patientId);

        query.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Patient listen error for " + patientId, e);
                liveData.postValue(new ArrayList<>());
                return;
            }

            ArrayList<RendezVous> appointments = new ArrayList<>();
            if (snapshots != null) {
                for (QueryDocumentSnapshot doc : snapshots) {
                    try {
                        RendezVous rendezVous = doc.toObject(RendezVous.class);
                        appointments.add(rendezVous);
                    } catch (Exception parseError) {
                        Log.e(TAG, "Error converting patient document " + doc.getId(), parseError);
                    }
                }
                liveData.postValue(appointments);
            } else {
                liveData.postValue(new ArrayList<>());
            }
        });

        return liveData;
    }

    public Task<DocumentReference> bookAppointment(@NonNull RendezVous rendezVous) {
        Log.d(TAG, "Attempting to add RendezVous document...");
        return rendezvousCollection.add(rendezVous);
    }

    public Task<Void> updateAppointmentStatus(@NonNull String appointmentId, @NonNull String newStatus) {
        if (appointmentId.isEmpty()) {
            Log.e(TAG, "Cannot update appointment with empty ID");
            return com.google.android.gms.tasks.Tasks.forException(
                    new IllegalArgumentException("Appointment ID cannot be empty"));
        }
        Log.d(TAG, "Attempting to update status for " + appointmentId + " to " + newStatus);
        return rendezvousCollection.document(appointmentId).update("status", newStatus);
    }

    public Task<Void> deleteAppointment(@NonNull String appointmentId) {
        if (appointmentId.isEmpty()) {
            Log.e(TAG, "Cannot delete appointment with empty ID");
            return com.google.android.gms.tasks.Tasks.forException(
                    new IllegalArgumentException("Appointment ID cannot be empty"));
        }
        Log.d(TAG, "Attempting to delete appointment " + appointmentId);
        return rendezvousCollection.document(appointmentId).delete();
    }
    public Task<QuerySnapshot> getBookedAppointmentsForDateTask(String professionalId, String dateString) {
        if (professionalId == null || professionalId.isEmpty() || dateString == null || dateString.isEmpty()) {
            Log.w(TAG, "getBookedAppointmentsForDateTask called with null or empty parameters.");
            // Return a failed task
            return Tasks.forException(new IllegalArgumentException("Professional ID and Date String cannot be null or empty"));
        }

        Log.d(TAG, "Fetching booked appointments for Prof ID: " + professionalId + " on Date: " + dateString);

        return rendezvousCollection
                .whereEqualTo("professionalId", professionalId)
                .whereEqualTo("appointmentDate", dateString)

                .get();
    }
}
