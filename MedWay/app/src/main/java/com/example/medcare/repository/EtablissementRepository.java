package com.example.medcare.repository;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.medcare.model.Etablissement;

public class EtablissementRepository {

    private static final String TAG = "EtablissementRepo";
    private final String COLLECTION_NAME = "establishments";

    private final FirebaseFirestore db;
    private final CollectionReference establishmentCollection;


    public EtablissementRepository() {
        db = FirebaseFirestore.getInstance();
        establishmentCollection = db.collection(COLLECTION_NAME);
    }

    public LiveData<List<Etablissement>> getAllEtablissementsStream() {
        MutableLiveData<List<Etablissement>> liveData = new MutableLiveData<>();
        ListenerRegistration registration = establishmentCollection.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen error for all establishments", e);
                liveData.postValue(new ArrayList<>());
                return;
            }
            ArrayList<Etablissement> etablissements = new ArrayList<>();
            if (snapshots != null) {
                for (QueryDocumentSnapshot doc : snapshots) {
                    try {
                        Etablissement etab = doc.toObject(Etablissement.class);
                        etab.setDocumentId(doc.getId());
                        etablissements.add(etab);
                    } catch (Exception parseError){
                        Log.e(TAG, "Error parsing establishment document: " + doc.getId(), parseError);
                    }
                }
                liveData.postValue(etablissements);
            } else {
                liveData.postValue(new ArrayList<>());
            }
        });
        return liveData;
    }

    public LiveData<Etablissement> getEtablissementByIdStream(String documentId) {
        if (documentId == null || documentId.isEmpty()) {
            MutableLiveData<Etablissement> errorData = new MutableLiveData<>();
            errorData.setValue(null);
            return errorData;
        }
        MutableLiveData<Etablissement> liveData = new MutableLiveData<>();
        ListenerRegistration registration = establishmentCollection.document(documentId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen error for establishment " + documentId, e);
                        liveData.postValue(null);
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        try {
                            Etablissement etab = snapshot.toObject(Etablissement.class);
                            if (etab != null) {
                                etab.setDocumentId(snapshot.getId());
                                liveData.postValue(etab);
                            } else {
                                liveData.postValue(null);
                            }
                        } catch (Exception parseError) {
                            liveData.postValue(null);
                        }
                    } else {
                        liveData.postValue(null);
                    }
                });
        return liveData;
    }

    public Task<DocumentReference> insert(Etablissement etablissement) {
        return establishmentCollection.add(etablissement);
    }

    public Task<Void> update(@NonNull Etablissement etablissement, @NonNull String documentId) {
        if (documentId.isEmpty()) {
            return com.google.android.gms.tasks.Tasks.forException(
                    new IllegalArgumentException("Document ID cannot be empty for update"));
        }
        return establishmentCollection.document(documentId).set(etablissement, SetOptions.merge());
    }

    public Task<Void> delete(@NonNull String documentId) {
        if (documentId.isEmpty()) {
            return com.google.android.gms.tasks.Tasks.forException(
                    new IllegalArgumentException("Document ID cannot be empty for delete"));
        }
        return establishmentCollection.document(documentId).delete();
    }
}