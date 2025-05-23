package com.example.medcare.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.example.medcare.R;
import com.example.medcare.model.RendezVous;
import com.example.medcare.viewmodel.RendezVousViewModel;
import com.example.medcare.viewmodel.RendezVousViewModelFactory;

import java.util.ArrayList;

public class PatientAppointmentsFragment extends Fragment implements AppointmentAdapter.OnAppointmentActionListener {

    private static final String TAG = "PatientApptsFragment";

    private RendezVousViewModel rendezVousViewModel;
    private RecyclerView recyclerViewMyAppointments;
    private AppointmentAdapter adapter;
    private ProgressBar loadingIndicatorMyAppts;

    private View layoutEmptyMyAppts;
    private TextView emptyTitle;
    private TextView emptySubtitle;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_appointments, container, false);

        recyclerViewMyAppointments = view.findViewById(R.id.recycler_view_my_appointments);
        loadingIndicatorMyAppts = view.findViewById(R.id.loading_indicator_my_appointments);
        layoutEmptyMyAppts = view.findViewById(R.id.layout_my_appointments_empty);
        emptyTitle = view.findViewById(R.id.text_view_my_appointments_empty_title);
        emptySubtitle = view.findViewById(R.id.text_view_my_appointments_empty_subtitle);

        RendezVousViewModelFactory factory = new RendezVousViewModelFactory();
        rendezVousViewModel = new ViewModelProvider(this, factory).get(RendezVousViewModel.class);

        setupRecyclerView();
        setupObservers();
        loadPatientAppointmentsIfUserAvailable();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new AppointmentAdapter(this);
        recyclerViewMyAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewMyAppointments.setAdapter(adapter);
        recyclerViewMyAppointments.setHasFixedSize(true);
    }

    private void loadPatientAppointmentsIfUserAvailable() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String patientId = currentUser.getUid();
            Log.d(TAG, "Setting up observer for patient appointments: " + patientId);
        } else {
            handleUserNotLoggedIn();
        }
    }

    private void handleUserNotLoggedIn() {
        Log.e(TAG, "No logged-in user found to load appointments.");
        Toast.makeText(getContext(), "Erreur: Utilisateur non connecté", Toast.LENGTH_SHORT).show();

        if (layoutEmptyMyAppts != null) {
            layoutEmptyMyAppts.setVisibility(View.VISIBLE);
            emptyTitle.setText("Utilisateur non connecté");
            emptySubtitle.setText("Veuillez vous reconnecter pour voir vos rendez-vous.");
        }

        if (recyclerViewMyAppointments != null) {
            recyclerViewMyAppointments.setVisibility(View.GONE);
        }

        if (adapter != null) adapter.submitList(new ArrayList<>());
    }

    private void setupObservers() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String patientId = currentUser.getUid();

        rendezVousViewModel.getAppointmentsForPatient(patientId).observe(getViewLifecycleOwner(), appointments -> {
            if (appointments != null) {
                Log.d(TAG, "Observer received " + appointments.size() + " appointments.");
                adapter.submitList(appointments);

                boolean isEmpty = appointments.isEmpty();
                layoutEmptyMyAppts.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                recyclerViewMyAppointments.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

                if (isEmpty) {
                    emptyTitle.setText("Aucun rendez-vous programmé");
                    emptySubtitle.setText("Vos rendez-vous s'afficheront ici une fois planifiés.");
                }
            } else {
                Log.w(TAG, "Observer received null appointment list");
                adapter.submitList(new ArrayList<>());
                layoutEmptyMyAppts.setVisibility(View.VISIBLE);
                recyclerViewMyAppointments.setVisibility(View.GONE);
                emptyTitle.setText("Erreur");
                emptySubtitle.setText("Erreur de chargement des rendez-vous.");
            }
        });

        rendezVousViewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (loadingIndicatorMyAppts != null) {
                loadingIndicatorMyAppts.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        rendezVousViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Erreur: " + error, Toast.LENGTH_LONG).show();
                if (adapter == null || adapter.getItemCount() == 0) {
                    layoutEmptyMyAppts.setVisibility(View.VISIBLE);
                    recyclerViewMyAppointments.setVisibility(View.GONE);
                    emptyTitle.setText("Erreur");
                    emptySubtitle.setText(error);
                }
                rendezVousViewModel.clearErrorMessage();
            }
        });

        rendezVousViewModel.getOperationResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null && !result.isEmpty()) {
                Toast.makeText(getContext(), result, Toast.LENGTH_SHORT).show();
                rendezVousViewModel.clearOperationResult();
            }
        });
    }

    @Override
    public void onCancelClick(RendezVous appointment) {
        if (appointment != null && appointment.getDocumentId() != null) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Confirmer Annulation")
                    .setMessage("Êtes-vous sûr de vouloir annuler ce rendez-vous le "
                            + appointment.getAppointmentDate() + " à " + appointment.getAppointmentTime() + "?")
                    .setPositiveButton("Oui, Annuler", (dialog, which) -> {
                        rendezVousViewModel.cancelAppointment(appointment.getDocumentId());
                    })
                    .setNegativeButton("Non", null)
                    .show();
        } else {
            Toast.makeText(getContext(), "Impossible d'annuler: informations manquantes.", Toast.LENGTH_SHORT).show();
        }
    }
}
