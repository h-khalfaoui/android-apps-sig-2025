
package com.example.medcare.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.medcare.repository.RendezVousRepository;

public class RendezVousViewModelFactory implements ViewModelProvider.Factory {

    public RendezVousViewModelFactory() {
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(RendezVousViewModel.class)) {
            // Create repository instance here (or get from DI framework)
            RendezVousRepository repository = new RendezVousRepository();
            // Construct the ViewModel with the repository
            return (T) new RendezVousViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}