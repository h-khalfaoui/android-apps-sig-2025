package com.example.quietspaceeee.data.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;

import com.example.quietspaceeee.data.db.CafeRepository;
import com.example.quietspaceeee.data.model.Cafe;

public class CafeViewModel extends AndroidViewModel {

    private final CafeRepository repository;
    private final MutableLiveData<ArrayList<Cafe>> cafesLiveData = new MutableLiveData<>();

    public CafeViewModel(@NonNull Application application) {
        super(application);
        repository = new CafeRepository(application);
        loadCafes(); // tu peux charger dès le début
    }

    private void loadCafes() {
        ArrayList<Cafe> cafes = repository.getAllCafes(); // pas async ici, mais tu peux le faire plus tard
        cafesLiveData.setValue(cafes);
    }

    public LiveData<ArrayList<Cafe>> getCafes() {
        return cafesLiveData;
    }

    public Cafe getCafeById(int id) {
        return repository.getCafeById(id);
    }
}
