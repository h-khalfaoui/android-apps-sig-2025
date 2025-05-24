package com.example.medcare.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medcare.R;
import com.example.medcare.model.TimeSlot;
import com.example.medcare.viewmodel.PatientBookingViewModel;
import com.example.medcare.viewmodel.PatientBookingViewModelFactory;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PatientBookingFragment extends Fragment implements TimeSlotAdapter.OnSlotClickListener {

    private static final String TAG = "PatientBookingFragment";

    private CalendarView calendarView;
    private RecyclerView recyclerViewSlots;
    private CircularProgressIndicator loadingIndicator;
    private TextView labelAvailableSlots;
    private MaterialButton buttonViewMyAppointments;
    private LinearLayout layoutNoSlots;

    private PatientBookingViewModel viewModel;
    private TimeSlotAdapter adapter;

    private String professionalId;
    private String establishmentId;

    private static final String ARG_PROFESSIONAL_ID = "professionalId";
    private static final String ARG_ESTABLISHMENT_ID = "establishmentId";

    public static PatientBookingFragment newInstance(String professionalId, String establishmentId) {
        PatientBookingFragment fragment = new PatientBookingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROFESSIONAL_ID, professionalId);
        args.putString(ARG_ESTABLISHMENT_ID, establishmentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            professionalId = getArguments().getString(ARG_PROFESSIONAL_ID);
            establishmentId = getArguments().getString(ARG_ESTABLISHMENT_ID);
        } else {
            Log.e(TAG, "Error: Required IDs not provided to fragment.");
            if (getContext() != null) {
                Toast.makeText(getContext(), "Erreur: Informations manquantes.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_booking, container, false);

        calendarView = view.findViewById(R.id.calendar_view_booking);
        recyclerViewSlots = view.findViewById(R.id.recycler_view_slots);
        loadingIndicator = view.findViewById(R.id.loading_indicator_booking);
        labelAvailableSlots = view.findViewById(R.id.label_available_slots);
        buttonViewMyAppointments = view.findViewById(R.id.button_view_my_appointments);
        layoutNoSlots = view.findViewById(R.id.layout_no_slots);

        if(professionalId == null || establishmentId == null) {
            Log.e(TAG, "Professional ID or Establishment ID is null in onCreateView.");
            if (labelAvailableSlots != null) {
                labelAvailableSlots.setText("Erreur: Données de réservation manquantes.");
            }
            if (calendarView != null) calendarView.setVisibility(View.GONE);
            if (recyclerViewSlots != null) recyclerViewSlots.setVisibility(View.GONE);
            if (layoutNoSlots != null) layoutNoSlots.setVisibility(View.GONE);
            if (loadingIndicator != null) loadingIndicator.setVisibility(View.GONE);
            if (buttonViewMyAppointments != null) buttonViewMyAppointments.setEnabled(false);
        } else {
            PatientBookingViewModelFactory factory = new PatientBookingViewModelFactory();
            viewModel = new ViewModelProvider(this, factory).get(PatientBookingViewModel.class);

            setupRecyclerView();
            setupCalendarView();
            setupObservers();

            viewModel.setSelectedDate(LocalDate.now(), professionalId);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (buttonViewMyAppointments != null) {
            buttonViewMyAppointments.setOnClickListener(v -> {
                if (getActivity() != null) {

                    Fragment myAppointmentsFragment = new PatientAppointmentsFragment(); // Create instance

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, myAppointmentsFragment)
                            .addToBackStack(null)
                            .commit();
                } else {
                    Log.e(TAG, "Activity is null, cannot perform fragment transaction.");
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Erreur: Impossible de naviguer.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Log.e(TAG, "buttonViewMyAppointments is null in onViewCreated.");
        }
    }

    private void setupRecyclerView() {
        if (recyclerViewSlots == null || getContext() == null) return;
        adapter = new TimeSlotAdapter(requireContext(), this);
        recyclerViewSlots.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerViewSlots.setAdapter(adapter);
        recyclerViewSlots.setHasFixedSize(true);
    }

    private void setupCalendarView() {
        if (calendarView == null) return;
        calendarView.setOnDateChangeListener((cv, year, month, dayOfMonth) -> {
            if (viewModel != null && professionalId != null) {
                LocalDate selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                viewModel.setSelectedDate(selectedDate, professionalId);
            }
        });
    }

    private void setupObservers() {
        if (viewModel == null) return;

        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (loadingIndicator != null) {
                loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getSelectedDate().observe(getViewLifecycleOwner(), date -> {
            if (labelAvailableSlots != null && date != null) {
                labelAvailableSlots.setText("Créneaux pour: " + date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
            }
        });

        viewModel.getAvailableSlots().observe(getViewLifecycleOwner(), slots -> {
            boolean hasSlots = slots != null && !slots.isEmpty();
            if (recyclerViewSlots != null) {
                recyclerViewSlots.setVisibility(hasSlots ? View.VISIBLE : View.GONE);
            }
            if (layoutNoSlots != null) {
                layoutNoSlots.setVisibility(hasSlots ? View.GONE : View.VISIBLE);
            }

            if (adapter != null) {
                adapter.submitList(slots);
            }
        });

        viewModel.getBookingResult().observe(getViewLifecycleOwner(), resultMessage -> {
            if (resultMessage != null && !resultMessage.isEmpty() && getContext() != null) {
                Toast.makeText(getContext(), resultMessage, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty() && getContext() != null) {
                Toast.makeText(getContext(), "Erreur: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onSlotClick(TimeSlot slot) {
        if (viewModel == null || getContext() == null) {
            if (getContext() != null) Toast.makeText(getContext(), "Erreur: Impossible de traiter la demande.", Toast.LENGTH_SHORT).show();
            return;
        }
        LocalDate selectedDate = viewModel.getSelectedDate().getValue();
        if (slot == null || selectedDate == null || professionalId == null || establishmentId == null) {
            Toast.makeText(getContext(), "Informations manquantes pour la réservation.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmer Rendez-vous")
                .setMessage("Réserver le créneau de " + slot.time + " le " + selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "?")
                .setPositiveButton("Confirmer", (dialog, which) -> {
                    viewModel.bookAppointment(slot.time, selectedDate, professionalId, establishmentId);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}