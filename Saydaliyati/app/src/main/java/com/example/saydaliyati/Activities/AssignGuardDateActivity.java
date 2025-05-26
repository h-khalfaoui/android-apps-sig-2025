package com.example.saydaliyati.Activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saydaliyati.Adapters.GuardDateAdapter;
import com.example.saydaliyati.Database.AppDatabase;
import com.example.saydaliyati.Database.GuardDateDAO;
import com.example.saydaliyati.Database.PharmacyDAO;
import com.example.saydaliyati.Models.GuardDate;
import com.example.saydaliyati.Models.Pharmacy;
import com.example.saydaliyati.R;
import com.example.saydaliyati.Utils.DateUtils;
import com.example.saydaliyati.Utils.NotificationUtils;
import com.example.saydaliyati.Utils.SecurityUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AssignGuardDateActivity extends BaseActivity implements GuardDateAdapter.OnGuardDateActionListener {

    private Spinner pharmacySpinner;
    private Button addGuardDateButton;
    private RecyclerView guardDatesRecyclerView;
    private ChipGroup selectedDatesChipGroup;
    private TextView noGuardDatesText;
    private ProgressBar progressBar;
    private FloatingActionButton saveAllFab;

    private List<Pharmacy> pharmacyList;
    private List<GuardDate> currentGuardDates;
    private List<GuardDate> selectedGuardDates;
    private GuardDateAdapter adapter;

    private PharmacyDAO pharmacyDAO;
    private GuardDateDAO guardDateDAO;

    private int selectedPharmacyPosition = 0;
    private String startTime = "08:00";
    private String endTime = "20:00";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_guard_date);

        // Crée le canal de notification
        NotificationUtils.createNotificationChannel(this);

        // Authentification
        if (!SecurityUtils.isAuthenticated(this)) {
            Toast.makeText(this, "Authentication required", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Toolbar avec bouton retour
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Assign Guard Dates");
        }

        initializeViews();

        pharmacyDAO = AppDatabase.getInstance(this).pharmacyDAO();
        guardDateDAO = AppDatabase.getInstance(this).guardDateDAO();

        currentGuardDates = new ArrayList<>();
        selectedGuardDates = new ArrayList<>();

        loadPharmacyData();
        setupEventListeners();
    }

    private void initializeViews() {
        pharmacySpinner = findViewById(R.id.pharmacySpinner);
        addGuardDateButton = findViewById(R.id.addGuardDateButton);
        guardDatesRecyclerView = findViewById(R.id.guardDatesRecyclerView);
        selectedDatesChipGroup = findViewById(R.id.selectedDatesChipGroup);
        noGuardDatesText = findViewById(R.id.noGuardDatesText);
        progressBar = findViewById(R.id.progressBar);
        saveAllFab = findViewById(R.id.saveAllFab);
        guardDatesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupEventListeners() {
        pharmacySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPharmacyPosition = position;
                loadGuardDatesForPharmacy();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        addGuardDateButton.setOnClickListener(v -> showDatePickerDialog());
        saveAllFab.setOnClickListener(v -> saveSelectedGuardDates());
    }

    private void loadPharmacyData() {
        showProgress(true);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            pharmacyList = pharmacyDAO.getAllPharmacies();
            runOnUiThread(() -> {
                showProgress(false);
                if (pharmacyList.isEmpty()) {
                    Toast.makeText(this, "No pharmacies found. Please add some first.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    setupPharmacySpinner();
                    loadGuardDatesForPharmacy();
                }
            });
        });
    }

    private void setupPharmacySpinner() {
        List<String> pharmacyNames = new ArrayList<>();
        for (Pharmacy pharmacy : pharmacyList) {
            pharmacyNames.add(pharmacy.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, pharmacyNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pharmacySpinner.setAdapter(adapter);
    }

    private void loadGuardDatesForPharmacy() {
        if (pharmacyList == null || pharmacyList.isEmpty()) return;
        showProgress(true);
        Pharmacy selectedPharmacy = pharmacyList.get(selectedPharmacyPosition);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            currentGuardDates = guardDateDAO.getGuardDatesByPharmacy(selectedPharmacy.getId());
            runOnUiThread(() -> {
                showProgress(false);
                updateGuardDatesList();
            });
        });
    }

    private void updateGuardDatesList() {
        if (currentGuardDates.isEmpty()) {
            noGuardDatesText.setVisibility(View.VISIBLE);
            guardDatesRecyclerView.setVisibility(View.GONE);
        } else {
            noGuardDatesText.setVisibility(View.GONE);
            guardDatesRecyclerView.setVisibility(View.VISIBLE);
            adapter = new GuardDateAdapter(currentGuardDates, this, this);
            guardDatesRecyclerView.setAdapter(adapter);
        }
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String dateStr = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    if (DateUtils.isDateInPast(dateStr)) {
                        Toast.makeText(this, "Cannot assign guard dates in the past", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    showTimePickerDialog(dateStr, true);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void showTimePickerDialog(String dateStr, boolean isStartTime) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minuteOfDay) -> {
                    String timeStr = String.format(Locale.US, "%02d:%02d", hourOfDay, minuteOfDay);
                    if (isStartTime) {
                        startTime = timeStr;
                        showTimePickerDialog(dateStr, false);
                    } else {
                        endTime = timeStr;
                        addGuardDate(dateStr);
                    }
                },
                hour, minute, true
        );

        timePickerDialog.setTitle(isStartTime ? "Select Start Time" : "Select End Time");
        timePickerDialog.show();
    }

    private void addGuardDate(String dateStr) {
        for (GuardDate gd : selectedGuardDates) {
            if (gd.getGuardDate().equals(dateStr)) {
                Toast.makeText(this, "This date is already selected", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Pharmacy selectedPharmacy = pharmacyList.get(selectedPharmacyPosition);
        for (GuardDate existing : currentGuardDates) {
            if (existing.getGuardDate().equals(dateStr)) {
                Toast.makeText(this, "This pharmacy is already on duty for this date", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        GuardDate guardDate = new GuardDate(selectedPharmacy.getId(), dateStr, startTime, endTime);
        selectedGuardDates.add(guardDate);
        addDateChip(guardDate);
    }

    private void addDateChip(GuardDate guardDate) {
        Chip chip = new Chip(this);
        String displayDate = DateUtils.formatForDisplay(guardDate.getGuardDate());
        chip.setText(displayDate + " (" + guardDate.getStartTime() + " - " + guardDate.getEndTime() + ")");
        chip.setCloseIconVisible(true);
        chip.setCheckable(false);
        chip.setOnCloseIconClickListener(v -> {
            selectedGuardDates.remove(guardDate);
            selectedDatesChipGroup.removeView(chip);
        });
        selectedDatesChipGroup.addView(chip);
    }

    private void saveSelectedGuardDates() {
        if (selectedGuardDates.isEmpty()) {
            Toast.makeText(this, "No guard dates selected", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            for (GuardDate guardDate : selectedGuardDates) {
                guardDateDAO.insert(guardDate);
            }

            // 🔔 Notifications (avant le clear)
            if (NotificationUtils.areDutyPharmacyNotificationsEnabled(this)) {
                for (GuardDate guardDate : selectedGuardDates) {
                    Pharmacy pharmacy = findPharmacyById(pharmacyList, guardDate.getPharmacyId());
                    if (pharmacy != null) {
                        String title = getString(R.string.new_duty_pharmacy);
                        String content = String.format(
                                getString(R.string.new_duty_pharmacy_notification),
                                pharmacy.getName(),
                                DateUtils.formatForDisplay(guardDate.getGuardDate())
                        );
                        NotificationUtils.showNotification(this, title, content);
                    }
                }
            }

            Pharmacy selectedPharmacy = pharmacyList.get(selectedPharmacyPosition);
            currentGuardDates = guardDateDAO.getGuardDatesByPharmacy(selectedPharmacy.getId());

            runOnUiThread(() -> {
                showProgress(false);
                selectedGuardDates.clear();
                selectedDatesChipGroup.removeAllViews();
                updateGuardDatesList();
                Toast.makeText(this, "Guard dates saved successfully!", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private Pharmacy findPharmacyById(List<Pharmacy> pharmacies, int id) {
        for (Pharmacy pharmacy : pharmacies) {
            if (pharmacy.getId() == id) {
                return pharmacy;
            }
        }
        return null;
    }

    @Override
    public void onDeleteGuardDate(GuardDate guardDate) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Guard Date")
                .setMessage("Are you sure you want to remove this guard duty date?")
                .setPositiveButton("Delete", (dialog, which) -> deleteGuardDate(guardDate))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteGuardDate(GuardDate guardDate) {
        showProgress(true);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            guardDateDAO.delete(guardDate);
            Pharmacy selectedPharmacy = pharmacyList.get(selectedPharmacyPosition);
            currentGuardDates = guardDateDAO.getGuardDatesByPharmacy(selectedPharmacy.getId());
            runOnUiThread(() -> {
                showProgress(false);
                updateGuardDatesList();
                Toast.makeText(this, "Guard date deleted", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            saveAllFab.hide();
        } else {
            saveAllFab.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
