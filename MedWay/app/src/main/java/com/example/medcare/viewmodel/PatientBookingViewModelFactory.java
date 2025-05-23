package com.example.medcare.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.medcare.repository.DisponibiliteRepository;
import com.example.medcare.repository.RendezVousRepository;

public class PatientBookingViewModelFactory implements ViewModelProvider.Factory {

    public PatientBookingViewModelFactory() { }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(PatientBookingViewModel.class)) {
            // Instantiate repositories needed by the ViewModel
            DisponibiliteRepository dispoRepo = new DisponibiliteRepository();
            RendezVousRepository rdvRepo = new RendezVousRepository();
            return (T) new PatientBookingViewModel(dispoRepo, rdvRepo);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}