package com.example.medcare.repository;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.List;

import com.example.medcare.model.FileAttente;

public class FileAttenteRepository {

    private static final String TAG = "FileAttenteRepo";
    private final String COLLECTION_NAME = "waitingList";

    private final FirebaseFirestore db;
    private final CollectionReference waitingListCollection;

    public FileAttenteRepository() {
        db = FirebaseFirestore.getInstance();
        waitingListCollection = db.collection(COLLECTION_NAME);
    }

    public LiveData<List<FileAttente>> getFileAttenteForEtablissementStream(String etablissementId) {
        MutableLiveData<List<FileAttente>> liveData = new MutableLiveData<>();

        Query query = waitingListCollection.whereEqualTo("etablissementId", etablissementId);

        ListenerRegistration registration = query.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen error for waiting list, establishment: " + etablissementId, e);
                liveData.postValue(new ArrayList<>());
                return;
            }
            ArrayList<FileAttente> fileAttentes = new ArrayList<>();
            if (snapshots != null) {
                for (QueryDocumentSnapshot doc : snapshots) {
                    try {
                        FileAttente file = doc.toObject(FileAttente.class);
                        file.setDocumentId(doc.getId());
                        fileAttentes.add(file);
                    } catch (Exception parseError){
                        Log.e(TAG, "Error parsing waiting list doc: " + doc.getId(), parseError);
                    }
                }
                liveData.postValue(fileAttentes);
            } else {
                liveData.postValue(new ArrayList<>());
            }
        });
        return liveData;
    }

    public Task<DocumentReference> insert(FileAttente fileAttente) {
        return waitingListCollection.add(fileAttente);
    }

    public Task<Void> update(@NonNull FileAttente fileAttente, @NonNull String documentId) {
        if (documentId.isEmpty()) {
            return com.google.android.gms.tasks.Tasks.forException(
                    new IllegalArgumentException("Document ID cannot be empty for update"));
        }
        return waitingListCollection.document(documentId).set(fileAttente, SetOptions.merge());
    }

    public Task<Void> delete(@NonNull String documentId) {
        if (documentId.isEmpty()) {
            return com.google.android.gms.tasks.Tasks.forException(
                    new IllegalArgumentException("Document ID cannot be empty for delete"));
        }
        return waitingListCollection.document(documentId).delete();
    }
}