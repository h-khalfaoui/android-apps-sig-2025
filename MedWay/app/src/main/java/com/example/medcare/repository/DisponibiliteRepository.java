package com.example.medcare.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.medcare.model.Disponibilite;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.List;

public class DisponibiliteRepository {

    private static final String TAG = "DisponibiliteRepo";
    private final String COLLECTION_NAME = "availabilities";

    private final FirebaseFirestore db;
    private final CollectionReference dispoCollection;

    public DisponibiliteRepository() {
        db = FirebaseFirestore.getInstance();
        dispoCollection = db.collection(COLLECTION_NAME);
    }

    public LiveData<List<Disponibilite>> getDisponibilitesForProfessionalStream(String professionalId) {
        MutableLiveData<List<Disponibilite>> liveData = new MutableLiveData<>();

        Query query = dispoCollection.whereEqualTo("professionalId", professionalId);

        /* ListenerRegistration registration = */ query.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Availability listen error for professional: " + professionalId, e);
                liveData.postValue(new ArrayList<>());
                return;
            }

            ArrayList<Disponibilite> disponibilites = new ArrayList<>();
            if (snapshots != null) {
                for (QueryDocumentSnapshot doc : snapshots) {
                    try {
                        Disponibilite dispo = doc.toObject(Disponibilite.class);
                        disponibilites.add(dispo);
                    } catch (Exception parseError) {
                        Log.e(TAG, "Error parsing availability document: " + doc.getId(), parseError);
                    }
                }
                Log.d(TAG, "Received " + disponibilites.size() + " availability records for " + professionalId);
                liveData.postValue(disponibilites);
            } else {
                Log.w(TAG, "Received null snapshot for " + professionalId);
                liveData.postValue(new ArrayList<>());
            }
        });

        return liveData;
    }

    public Task<Void> upsertDisponibilite(@NonNull Disponibilite disponibilite, @NonNull String documentId) {
        if (documentId.isEmpty()) {
            Log.e(TAG, "Document ID cannot be empty for upsert");
            return Tasks.forException(
                    new IllegalArgumentException("Document ID cannot be empty for upserting availability"));
        }
        Log.d(TAG, "Upserting availability with ID: " + documentId);
        return dispoCollection.document(documentId).set(disponibilite, SetOptions.merge());
    }

    public Task<Void> deleteDisponibilite(@NonNull String documentId) {
        if (documentId.isEmpty()) {
            Log.e(TAG, "Document ID cannot be empty for delete");
            return Tasks.forException(
                    new IllegalArgumentException("Document ID cannot be empty for deleting availability"));
        }
        Log.d(TAG, "Deleting availability with ID: " + documentId);
        return dispoCollection.document(documentId).delete();
    }

    public Task<DocumentSnapshot> getDisponibiliteByDocId(String documentId) {
        if (documentId == null || documentId.isEmpty()) {
            Log.w(TAG, "getDisponibiliteByDocId called with null or empty ID.");
            return Tasks.forException(new IllegalArgumentException("Document ID cannot be null or empty"));
        }
        Log.d(TAG, "Fetching Disponibilite document: " + documentId);
        return dispoCollection.document(documentId).get();
    }


}