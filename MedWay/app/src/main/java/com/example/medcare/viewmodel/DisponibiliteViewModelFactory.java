package com.example.medcare.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.medcare.repository.DisponibiliteRepository;

public class DisponibiliteViewModelFactory implements ViewModelProvider.Factory {

    public DisponibiliteViewModelFactory() {
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DisponibiliteViewModel.class)) {
            // Create repository instance
            DisponibiliteRepository repository = new DisponibiliteRepository();
            // Construct the ViewModel
            return (T) new DisponibiliteViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}