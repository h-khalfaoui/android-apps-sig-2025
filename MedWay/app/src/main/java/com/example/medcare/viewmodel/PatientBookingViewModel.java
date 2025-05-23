package com.example.medcare.viewmodel;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.medcare.model.Disponibilite;
import com.example.medcare.model.RendezVous;
import com.example.medcare.model.TimeSlot;
import com.example.medcare.repository.DisponibiliteRepository;
import com.example.medcare.repository.RendezVousRepository;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PatientBookingViewModel extends ViewModel {

    private static final String TAG = "PatientBookingVM";
    private static final int SLOT_DURATION_MINUTES = 30;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final DisponibiliteRepository dispoRepository;
    private final RendezVousRepository rdvRepository;

    private final MutableLiveData<LocalDate> _selectedDate = new MutableLiveData<>();
    private final MutableLiveData<List<TimeSlot>> _availableSlots = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> _bookingResult = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();

    public LiveData<LocalDate> getSelectedDate() { return _selectedDate; }
    public LiveData<List<TimeSlot>> getAvailableSlots() { return _availableSlots; }
    public LiveData<Boolean> isLoading() { return _isLoading; }
    public LiveData<String> getBookingResult() { return _bookingResult; }
    public LiveData<String> getErrorMessage() { return _errorMessage; }


    public PatientBookingViewModel(DisponibiliteRepository dispoRepo, RendezVousRepository rdvRepo) {
        this.dispoRepository = dispoRepo;
        this.rdvRepository = rdvRepo;
    }

    public void setSelectedDate(LocalDate date, String professionalId) {
        if (date == null || professionalId == null || professionalId.isEmpty()) {
            _errorMessage.setValue("Date or Professional ID is missing.");
            _availableSlots.setValue(Collections.emptyList());
            return;
        }
        _selectedDate.setValue(date);
        loadAvailability(date, professionalId);
    }

    private void loadAvailability(LocalDate date, String professionalId) {
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        _availableSlots.setValue(Collections.emptyList());

        DayOfWeek day = date.getDayOfWeek();
        String dayOfWeekString = day.getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH).toUpperCase();
        String availabilityDocId = professionalId + "_" + dayOfWeekString;

        com.google.android.gms.tasks.Task<com.google.firebase.firestore.DocumentSnapshot> availabilityTask =
                dispoRepository.getDisponibiliteByDocId(availabilityDocId);

        String dateString = date.format(DATE_FORMATTER);
        com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot> bookedTask =
                rdvRepository.getBookedAppointmentsForDateTask(professionalId, dateString);


        Tasks.whenAllSuccess(availabilityTask, bookedTask).addOnSuccessListener(results -> {
            Disponibilite dailyAvailability = null;
            List<RendezVous> bookedAppointments = new ArrayList<>();

            com.google.firebase.firestore.DocumentSnapshot availSnapshot = (com.google.firebase.firestore.DocumentSnapshot) results.get(0);
            if (availSnapshot != null && availSnapshot.exists()) {
                try {
                    dailyAvailability = availSnapshot.toObject(Disponibilite.class);
                } catch (Exception e){
                    Log.e(TAG, "Error converting availability snapshot", e);
                }
            } else {
                Log.d(TAG, "No availability document found for " + availabilityDocId);
            }

            com.google.firebase.firestore.QuerySnapshot bookedSnapshot = (com.google.firebase.firestore.QuerySnapshot) results.get(1);
            if(bookedSnapshot != null && !bookedSnapshot.isEmpty()) {
                for (com.google.firebase.firestore.DocumentSnapshot doc : bookedSnapshot.getDocuments()){
                    try {
                        RendezVous rdv = doc.toObject(RendezVous.class);
                        if(rdv != null) {
                            bookedAppointments.add(rdv);
                        }
                    } catch(Exception e){
                        Log.e(TAG, "Error converting booked appointment snapshot " + doc.getId(), e);
                    }
                }
            }

            calculateAvailableSlots(date, dailyAvailability, bookedAppointments);
            _isLoading.setValue(false);

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading availability/appointments", e);
            _errorMessage.setValue("Erreur de chargement des créneaux: " + e.getMessage());
            _availableSlots.setValue(Collections.emptyList());
            _isLoading.setValue(false);
        });
    }


    private void calculateAvailableSlots(LocalDate selectedDate, @Nullable Disponibilite dailyAvailability, @NonNull List<RendezVous> bookedAppointments) {
        List<TimeSlot> calculatedSlots = new ArrayList<>();

        if (dailyAvailability == null || !dailyAvailability.isOuvert()) {
            _availableSlots.postValue(calculatedSlots);
            return;
        }

        try {
            LocalTime startTime = LocalTime.parse(dailyAvailability.getHeureDebut(), TIME_FORMATTER);
            LocalTime endTime = LocalTime.parse(dailyAvailability.getHeureFin(), TIME_FORMATTER);

            Set<LocalTime> bookedTimes = new HashSet<>();
            for (RendezVous rdv : bookedAppointments) {
                if (!"CANCELLED".equalsIgnoreCase(rdv.getStatus())) {
                    try {
                        bookedTimes.add(LocalTime.parse(rdv.getAppointmentTime(), TIME_FORMATTER));
                    } catch (Exception ignored) {}
                }
            }

            LocalTime currentSlotTime = startTime;
            while (currentSlotTime.isBefore(endTime)) {
                boolean isAlreadyBooked = bookedTimes.contains(currentSlotTime);
                boolean isPast = selectedDate.isEqual(LocalDate.now()) && currentSlotTime.isBefore(LocalTime.now());
                boolean isAvailable = !isAlreadyBooked && !isPast;

                calculatedSlots.add(new TimeSlot(currentSlotTime.format(TIME_FORMATTER), isAvailable));
                currentSlotTime = currentSlotTime.plusMinutes(SLOT_DURATION_MINUTES);
            }
        } catch (DateTimeParseException e) {
            Log.e(TAG, "Error parsing start/end times from availability: " + dailyAvailability.getHeureDebut() + " / " + dailyAvailability.getHeureFin(), e);
            _errorMessage.postValue("Erreur de format d'heure de disponibilité.");
        } catch(Exception e) {
            Log.e(TAG, "Error calculating slots", e);
            _errorMessage.postValue("Erreur de calcul des créneaux.");
        }

        _availableSlots.postValue(calculatedSlots);
    }

    public void bookAppointment(String timeSlot, LocalDate date, String professionalId, String establishmentId) {
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        _bookingResult.setValue(null);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            _errorMessage.setValue("Utilisateur non connecté.");
            _isLoading.setValue(false);
            return;
        }
        if (establishmentId == null || establishmentId.isEmpty() || professionalId == null || professionalId.isEmpty()) {
            _errorMessage.setValue("Information sur l'établissement ou le professionnel manquante.");
            _isLoading.setValue(false);
            return;
        }

        String patientId = currentUser.getUid();
        String patientName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Patient Invité";
        String dateString = date.format(DATE_FORMATTER);
        String status = "CONFIRMED";

        RendezVous newRdv = new RendezVous(patientId, patientName, establishmentId,dateString, timeSlot, status);
        Log.e(TAG, "--- CHECKING IDS FOR BOOKING ---"); // Use Log.e to make it stand out
        Log.e(TAG, "request.auth.uid (User Logged In) = '" + patientId + "'");
        Log.e(TAG, "newRdv.getPatientId() (Data Sent)  = '" + newRdv.getPatientId() + "'");
        Log.e(TAG, "Are they equal? " + patientId.equals(newRdv.getPatientId()));
        rdvRepository.bookAppointment(newRdv)
                .addOnSuccessListener(docRef -> {
                    Log.i(TAG, "Booking successful. Document ID: " + docRef.getId());
                    _bookingResult.setValue("Rendez-vous confirmé pour " + timeSlot + " le " + dateString);
                    _isLoading.setValue(false);
                    loadAvailability(date, professionalId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Booking failed", e);
                    _bookingResult.setValue("Échec de la réservation: " + e.getMessage());
                    _isLoading.setValue(false);
                });
    }
}