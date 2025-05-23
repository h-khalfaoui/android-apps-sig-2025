package com.example.medcare.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.medcare.repository.FileAttenteRepository;

public class FileAttenteViewModelFactory implements ViewModelProvider.Factory {

    public FileAttenteViewModelFactory() {
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(FileAttenteViewModel.class)) {
            FileAttenteRepository repository = new FileAttenteRepository();
            return (T) new FileAttenteViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}