package com.example.medcare.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.medcare.repository.EtablissementRepository;

public class EtablissementViewModelFactory implements ViewModelProvider.Factory {

    public EtablissementViewModelFactory() {
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(EtablissementViewModel.class)) {
            // Create repository instance (or get from DI framework)
            EtablissementRepository repository = new EtablissementRepository();
            // Construct the ViewModel
            return (T) new EtablissementViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}