package com.example.saydaliyati.Activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saydaliyati.Adapters.PharmacyAdapter;
import com.example.saydaliyati.Adapters.SimplePlacesAdapter;
import com.example.saydaliyati.Database.AppDatabase;
import com.example.saydaliyati.Database.GuardDateDAO;
import com.example.saydaliyati.Database.PharmacyDAO;
import com.example.saydaliyati.Models.GuardDate;
import com.example.saydaliyati.Models.Pharmacy;
import com.example.saydaliyati.R;
import com.example.saydaliyati.Utils.DateUtils;
import com.example.saydaliyati.Utils.LanguageUtils;
import com.example.saydaliyati.Utils.LocationUtils;
import com.example.saydaliyati.Utils.ManualStrings;
import com.example.saydaliyati.Utils.NotificationUtils;
import com.example.saydaliyati.Utils.PharmacyMapManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;

public class PharmacyFinderActivity extends BaseActivity implements PharmacyAdapter.OnPharmacyClickListener {

    // UI Components
    private AutoCompleteTextView locationEditText;
    private SimplePlacesAdapter placesAdapter;
    private SimplePlacesAdapter.TangierLocation selectedLocation;
    private SwitchMaterial dutySwitch;
    private Button datePickerButton;
    private RecyclerView pharmacyRecyclerView;
    private FloatingActionButton myLocationButton;
    private FloatingActionButton expandMapButton;
    private View bottomSheet;
    private TextView noResultsTextView;
    private MapView mapView;

    // Map Manager
    private PharmacyMapManager mapManager;

    // Data
    private PharmacyAdapter adapter;
    private List<Pharmacy> allPharmacies = new ArrayList<>();
    private List<Pharmacy> displayedPharmacies = new ArrayList<>();
    private PharmacyDAO pharmacyDAO;
    private GuardDateDAO guardDateDAO;
    private String selectedDate;
    private Location currentLocation;
    private FloatingActionButton zoomInButton;
    private FloatingActionButton zoomOutButton;
    private Handler zoomHandler = new Handler();

    // Constants
    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private BottomSheetBehavior<View> bottomSheetBehavior;

    private void zoomToLocation(double latitude, double longitude) {
        // Tell map manager that we're doing a manual zoom
        mapManager.setManualZoomInProgress(true);

        // Create a runnable that will execute after everything else
        Runnable zoomRunnable = () -> {
            GeoPoint point = new GeoPoint(latitude, longitude);


            mapView.getController().setCenter(point);


            mapView.getController().setZoom(18.0);

            // Force refresh the map
            mapView.invalidate();

            // Log for debugging
            Log.d("MapZoom", "Zoomed to lat: " + latitude + ", lon: " + longitude + " with zoom level 18.0");
        };

        // Clear any pending operations
        zoomHandler.removeCallbacksAndMessages(null);

        // Execute right now
        zoomRunnable.run();


        zoomHandler.postDelayed(zoomRunnable, 300);
        zoomHandler.postDelayed(zoomRunnable, 600);

        // Keep manual zoom active for 5 seconds to prevent other methods from changing it
        zoomHandler.postDelayed(() -> {
            mapManager.setManualZoomInProgress(false);
        }, 5000);
    }
    // Modify your preloadDatabaseData method to include logging:
    private void preloadDatabaseData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            PharmacyDAO dao = AppDatabase.getInstance(this).pharmacyDAO();

            // Now that we've added the method, we can use it
            // Force reset the database for testing
            dao.deleteAllPharmacies();
                dao.insert(new Pharmacy("Pharmacie Centrale", "Boulevard Mohammed V", 35.7796, -5.8137,
                        "+212 539 931 099", "08:00 - 23:00", true));
                dao.insert(new Pharmacy("Pharmacie Diamant", "Avenue Hassan II", 35.7741, -5.7995,
                        "+212 539 321 456", "24/7", false));
                dao.insert(new Pharmacy("Pharmacie Principale", "Rue de Fès", 35.7694, -5.7962,
                        "+212 539 654 321", "09:00 - 21:00", true));
                dao.insert(new Pharmacy("Pharmacie Ibn Khaldoun", "Avenue Mohammed VI", 35.7791, -5.8081,
                        "+212 539 123 456", "08:00 - 22:00", false));
                dao.insert(new Pharmacy("Pharmacie Al Andalus", "Rue Larache", 35.7699, -5.8224,
                        "+212 539 876 543", "09:00 - 20:00", true));

                // City center pharmacies
                dao.insert(new Pharmacy("Pharmacie Place des Nations", "Place des Nations", 35.7784, -5.8112,
                        "+212 539 334 455", "08:00 - 23:00", true));
                dao.insert(new Pharmacy("Pharmacie Acima", "Centre Commercial Acima", 35.7735, -5.8096,
                        "+212 539 940 505", "09:00 - 22:00", false));
                dao.insert(new Pharmacy("Pharmacie Pasteur", "Boulevard Pasteur", 35.7807, -5.8127,
                        "+212 539 936 025", "24/7", true));
                dao.insert(new Pharmacy("Pharmacie Mabrouk", "Avenue Hassan I", 35.7770, -5.8079,
                        "+212 539 372 873", "08:30 - 21:30", false));
                dao.insert(new Pharmacy("Pharmacie du Port", "Avenue Mohammed VI", 35.7850, -5.8073,
                        "+212 539 325 967", "08:00 - 23:00", true));

                // Malabata area
                dao.insert(new Pharmacy("Pharmacie Malabata", "Avenue Malabata", 35.7938, -5.7989,
                        "+212 539 947 111", "08:00 - 22:00", false));
                dao.insert(new Pharmacy("Pharmacie Tanja Bay", "Complexe Tanja Bay", 35.7953, -5.7962,
                        "+212 539 309 875", "09:00 - 21:00", true));
                dao.insert(new Pharmacy("Pharmacie de la Plage", "Boulevard de la Plage", 35.7918, -5.8003,
                        "+212 539 954 678", "08:00 - 20:00", false));

                // Iberia / Branes
                dao.insert(new Pharmacy("Pharmacie Iberia", "Quartier Iberia", 35.7677, -5.8058,
                        "+212 539 943 265", "08:30 - 22:30", true));
                dao.insert(new Pharmacy("Pharmacie Branes", "Avenue des FAR, Branes", 35.7589, -5.8114,
                        "+212 539 386 554", "09:00 - 21:00", false));
                dao.insert(new Pharmacy("Pharmacie Al Wafa", "Rue Al Wafa, Branes", 35.7602, -5.8092,
                        "+212 539 351 741", "08:00 - 22:00", true));

                // Souani area
                dao.insert(new Pharmacy("Pharmacie Souani", "Avenue Souani", 35.7694, -5.7962,
                        "+212 539 940 120", "08:00 - 23:00", false));
                dao.insert(new Pharmacy("Pharmacie Ibn Batouta", "Rue Ibn Batouta, Souani", 35.7683, -5.7978,
                        "+212 539 941 234", "24/7", true));
                dao.insert(new Pharmacy("Pharmacie Socco Alto", "Centre Commercial Socco Alto", 35.7604, -5.8108,
                        "+212 539 342 987", "10:00 - 22:00", false));

                // Near hospitals
                dao.insert(new Pharmacy("Pharmacie Hôpital Mohammed V", "Av. Mohamed V, près de l'Hôpital", 35.7733, -5.8042,
                        "+212 539 333 741", "24/7", true));
                dao.insert(new Pharmacy("Pharmacie Clinique Assaada", "En face de Clinique Assaada", 35.7668, -5.7954,
                        "+212 539 365 489", "08:00 - 23:00", false));
                dao.insert(new Pharmacy("Pharmacie Centre Hospitalier", "Près du CH Mohammed VI", 35.7600, -5.7899,
                        "+212 539 375 821", "24/7", true));

                // Mesnana area
                dao.insert(new Pharmacy("Pharmacie Mesnana", "Quartier Mesnana", 35.7409, -5.8464,
                        "+212 539 381 943", "08:30 - 21:30", false));
                dao.insert(new Pharmacy("Pharmacie Al Amal", "Route de Rabat, Mesnana", 35.7412, -5.8401,
                        "+212 539 364 529", "09:00 - 22:00", true));

                // Boukhalef area
                dao.insert(new Pharmacy("Pharmacie Boukhalef", "Zone Boukhalef", 35.7325, -5.9043,
                        "+212 539 393 827", "09:00 - 21:00", false));
                dao.insert(new Pharmacy("Pharmacie Aéroport", "Près de l'Aéroport Ibn Batouta", 35.7264, -5.9161,
                        "+212 539 354 217", "07:00 - 23:00", true));

                // Marjane area
                dao.insert(new Pharmacy("Pharmacie Marjane", "Centre Commercial Marjane", 35.7534, -5.7883,
                        "+212 539 309 416", "09:00 - 22:00", false));
                dao.insert(new Pharmacy("Pharmacie Ibn Sina", "Avenue Ibn Sina, près de Marjane", 35.7552, -5.7891,
                        "+212 539 371 528", "08:30 - 22:30", true));

                // Medina
                dao.insert(new Pharmacy("Pharmacie Médina", "Grand Socco, Médina", 35.7882, -5.8123,
                        "+212 539 935 143", "08:00 - 22:00", false));
                dao.insert(new Pharmacy("Pharmacie Bab El Fahs", "Bab El Fahs, Médina", 35.7871, -5.8106,
                        "+212 539 938 652", "09:00 - 21:00", true));
                dao.insert(new Pharmacy("Pharmacie Kasbah", "Près de la Kasbah", 35.7901, -5.8136,
                        "+212 539 934 189", "08:30 - 21:30", false));

                // Luxury areas
                dao.insert(new Pharmacy("Pharmacie Marina Bay", "Marina Bay Tanger", 35.7884, -5.8099,
                        "+212 539 302 145", "09:00 - 23:00", true));
                dao.insert(new Pharmacy("Pharmacie Royal", "Boulevard Mohammed VI", 35.7770, -5.8053,
                        "+212 539 307 963", "24/7", false));

                // University area
                dao.insert(new Pharmacy("Pharmacie Campus", "Près de l'Université Abdelmalek", 35.7636, -5.8548,
                        "+212 539 362 471", "08:00 - 22:00", true));
                dao.insert(new Pharmacy("Pharmacie des Étudiants", "Avenue des FAR, Zone Universitaire", 35.7636, -5.8537,
                        "+212 539 371 459", "09:00 - 21:00", false));
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                loadPharmacyData();
            }, 500);
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configure OSMDroid
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        // Set current date as default BEFORE setting content view
        selectedDate = DateUtils.getCurrentDate();

        setContentView(R.layout.activity_pharmacy_finder);

        // Apply manual translations to all text views
        applyManualTranslations();

        // Set Arabic title if needed
        if (LanguageUtils.LANGUAGE_ARABIC.equals(LanguageUtils.getCurrentLanguage(this))) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(ManualStrings.getArabicString("title_pharmacy_finder"));
            }
        }


        // Initialize UI components
        initializeViews();

        // Apply Arabic texts manually to specific views
        applyManualTexts();

        // Setup database connections
        pharmacyDAO = AppDatabase.getInstance(this).pharmacyDAO();
        guardDateDAO = AppDatabase.getInstance(this).guardDateDAO();
        preloadDatabaseData();

        // Initialize the map manager
        mapManager = new PharmacyMapManager(this, mapView);
        mapManager.initializeMap();
        mapManager.setOnPharmacyMarkerClickListener(this::onPharmacyClick);

        // Setup event listeners
        setupEventListeners();

        // Initialize the RecyclerView
        setupRecyclerView();

        // Load pharmacy data
        new Handler().postDelayed(() -> {
            loadPharmacyData();
        }, 500);
    }

    private void initializeViews() {
        locationEditText = findViewById(R.id.locationEditText);
        dutySwitch = findViewById(R.id.dutySwitch);
        datePickerButton = findViewById(R.id.datePickerButton);
        pharmacyRecyclerView = findViewById(R.id.pharmacyRecyclerView);
        myLocationButton = findViewById(R.id.myLocationButton);
        expandMapButton = findViewById(R.id.expandMapFab);
        mapView = findViewById(R.id.map);
        bottomSheet = findViewById(R.id.bottomSheet);
        noResultsTextView = findViewById(R.id.noResultsTextView);
        zoomInButton = findViewById(R.id.zoomInButton);
        zoomOutButton = findViewById(R.id.zoomOutButton);

        // Set up the bottom sheet behavior
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // Immediately set the date button text - this is safer than in applyManualTexts
        updateDateButtonText();
    }

    private void applyManualTexts() {
        // Apply Arabic texts manually if needed
        if (LanguageUtils.LANGUAGE_ARABIC.equals(LanguageUtils.getCurrentLanguage(this))) {
            // Translate duty switch
            if (dutySwitch != null) {
                if (ManualStrings.hasArabicString("pharmacy_on_duty")) {
                    dutySwitch.setText(ManualStrings.getArabicString("pharmacy_on_duty"));
                }
            }

            // Translate "No results" message
            if (noResultsTextView != null) {
                noResultsTextView.setText("لا توجد نتائج");  // Hardcoded as fallback
            }

            // Translate location hint
            if (locationEditText != null) {
                locationEditText.setHint("ابحث عن موقع");  // Hardcoded as fallback
            }


        }
    }

    private void updateDateButtonText() {
        // Check if datePickerButton is initialized and selectedDate is not null
        if (datePickerButton != null && selectedDate != null) {
            String displayText;

            if (LanguageUtils.LANGUAGE_ARABIC.equals(LanguageUtils.getCurrentLanguage(this))) {
                try {

                    String formattedDate = selectedDate; // Just use the date as is or format it simply
                    displayText = "التاريخ: " + formattedDate;
                } catch (Exception e) {
                    // In case of any error, use a default text
                    displayText = "اختر التاريخ";
                }
            } else {
                try {
                    // For French, use DateUtils if it works
                    String formattedDate = DateUtils.formatForDisplay(selectedDate);
                    displayText = formattedDate;
                } catch (Exception e) {
                    // Fallback
                    displayText = "Sélectionner une date";
                }
            }

            datePickerButton.setText(displayText);
        } else if (datePickerButton != null) {
            // If selectedDate is null but button exists, set a default text
            datePickerButton.setText(LanguageUtils.LANGUAGE_ARABIC.equals(LanguageUtils.getCurrentLanguage(this)) ?
                    "اختر التاريخ" : "Sélectionner une date");
        }
    }

    private void setupRecyclerView() {
        pharmacyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        displayedPharmacies = new ArrayList<>();
        adapter = new PharmacyAdapter(displayedPharmacies, this, this);
        pharmacyRecyclerView.setAdapter(adapter);
    }

    private void setupEventListeners() {
        // Date picker button
        datePickerButton.setOnClickListener(v -> showDatePicker());

        // Duty switch
        dutySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> filterPharmacies());

        // Initialize autocomplete
        placesAdapter = new SimplePlacesAdapter(this);
        locationEditText.setAdapter(placesAdapter);
        placesAdapter.refreshPharmacies();

        // Zoom button handlers
        zoomInButton.setOnClickListener(v -> {
            // Increase zoom level
            mapView.getController().zoomIn();
            // Set manual zoom flag to prevent auto-zoom
            mapManager.setManualZoomInProgress(true);
            // Clear it after a delay
            zoomHandler.removeCallbacksAndMessages(null);
            zoomHandler.postDelayed(() -> mapManager.setManualZoomInProgress(false), 3000);
        });

        zoomOutButton.setOnClickListener(v -> {
            // Decrease zoom level
            mapView.getController().zoomOut();
            // Set manual zoom flag to prevent auto-zoom
            mapManager.setManualZoomInProgress(true);
            // Clear it after a delay
            zoomHandler.removeCallbacksAndMessages(null);
            zoomHandler.postDelayed(() -> mapManager.setManualZoomInProgress(false), 3000);
        });

        // Setup text changed listener
        locationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                placesAdapter.getAutoComplete(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Setup item click listener for location search
        // In PharmacyFinderActivity.java - update the locationEditText.setOnItemClickListener method

        locationEditText.setOnItemClickListener((parent, view, position, id) -> {
            String locationName = placesAdapter.getItem(position);
            selectedLocation = placesAdapter.getLocationByName(locationName);

            if (selectedLocation != null) {
                // Check if this is a pharmacy (by the prefix we added)
                boolean isPharmacy = locationName.startsWith("🏥 ");
                String displayName = isPharmacy ? locationName.substring(3) :
                        locationName.startsWith("📍 ") ? locationName.substring(3) :
                                locationName;

                // Update location for sorting pharmacies
                Location mockLocation = new Location("selectedLocation");
                mockLocation.setLatitude(selectedLocation.latitude);
                mockLocation.setLongitude(selectedLocation.longitude);
                currentLocation = mockLocation;

                // Filter pharmacies with new location
                filterPharmacies();

                // Now zoom to the location using our dedicated method
                zoomToLocation(selectedLocation.latitude, selectedLocation.longitude);

                // If this is a pharmacy, also highlight it in the list and show its details
                if (isPharmacy) {
                    // Find the pharmacy in our list
                    for (Pharmacy pharmacy : displayedPharmacies) {
                        if (pharmacy.getName().equals(displayName)) {
                            // Highlight and show details
                            onPharmacyClick(pharmacy);
                            break;
                        }
                    }
                }

                // Show toast message
                String toastMsg;
                if (LanguageUtils.LANGUAGE_ARABIC.equals(LanguageUtils.getCurrentLanguage(this))) {
                    toastMsg = isPharmacy ?
                            "تم العثور على الصيدلية: " + displayName :
                            "الصيدليات مرتبة حسب القرب من " + displayName;
                } else {
                    toastMsg = isPharmacy ?
                            "Pharmacie trouvée: " + displayName :
                            "Pharmacies triées par proximité à " + displayName;
                }
                Toast.makeText(PharmacyFinderActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
            }
        });
        // Editor action listener for search button on keyboard
        // In PharmacyFinderActivity.java - update the locationEditText.setOnEditorActionListener method

        locationEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = locationEditText.getText().toString();
                if (!query.isEmpty()) {
                    // Try to find the location in our predefined list
                    selectedLocation = placesAdapter.getLocationByName(query);
                    if (selectedLocation != null) {
                        // Check if this is a pharmacy (by the prefix we added)
                        boolean isPharmacy = query.startsWith("🏥 ");
                        String displayName = isPharmacy ? query.substring(3) :
                                query.startsWith("📍 ") ? query.substring(3) :
                                        query;

                        // Update location for sorting
                        Location mockLocation = new Location("selectedLocation");
                        mockLocation.setLatitude(selectedLocation.latitude);
                        mockLocation.setLongitude(selectedLocation.longitude);
                        currentLocation = mockLocation;

                        // Filter pharmacies
                        filterPharmacies();

                        // Use our dedicated zoom method
                        zoomToLocation(selectedLocation.latitude, selectedLocation.longitude);

                        // If this is a pharmacy, also highlight it
                        if (isPharmacy) {
                            // Find the pharmacy in our list
                            for (Pharmacy pharmacy : displayedPharmacies) {
                                if (pharmacy.getName().equals(displayName)) {
                                    // Highlight and show details
                                    onPharmacyClick(pharmacy);
                                    break;
                                }
                            }
                        }

                        // Show appropriate toast message
                        String toastMsg;
                        if (LanguageUtils.LANGUAGE_ARABIC.equals(LanguageUtils.getCurrentLanguage(PharmacyFinderActivity.this))) {
                            toastMsg = isPharmacy ?
                                    "تم العثور على الصيدلية: " + displayName :
                                    "الصيدليات مرتبة حسب القرب من " + displayName;
                        } else {
                            toastMsg = isPharmacy ?
                                    "Pharmacie trouvée: " + displayName :
                                    "Pharmacies triées par proximité à " + displayName;
                        }
                        Toast.makeText(PharmacyFinderActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                    } else {
                        // Location not found, show message
                        String notFoundMsg = LanguageUtils.LANGUAGE_ARABIC.equals(LanguageUtils.getCurrentLanguage(PharmacyFinderActivity.this)) ?
                                "المكان أو الصيدلية غير موجودة" :
                                "Lieu ou pharmacie non trouvé";
                        Toast.makeText(PharmacyFinderActivity.this, notFoundMsg, Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            }
            return false;
        });       // My location button
        myLocationButton.setOnClickListener(v -> {
            if (checkLocationPermission()) {
                mapManager.setManualZoomInProgress(true);
                mapManager.moveCameraToCurrentLocation();
                updateCurrentLocation();
            } else {
                requestLocationPermission();
            }
        });

        // Expand map button
        expandMapButton.setOnClickListener(v -> {
            if (mapView.getLayoutParams().height == 300) {
                // If currently in small mode, expand to full screen
                mapView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                // Keep current zoom level when expanding
            } else {
                // If currently in full screen mode, shrink back and zoom to cover Tangier
                mapView.getLayoutParams().height = 300;

                // Set manual zoom to true to prevent conflicts
                mapManager.setManualZoomInProgress(true);

                // Center on Tangier with appropriate zoom level
                GeoPoint tangierCenter = new GeoPoint(35.7796, -5.8137); // Center of Tangier
                mapView.getController().setCenter(tangierCenter);
                mapView.getController().setZoom(14.0); // This zoom level should show most of Tangier

                // Alternatively, use a bounding box to ensure the whole city is visible
                BoundingBox tangierBounds = new BoundingBox(
                        35.8100, // North latitude
                        -5.7700, // East longitude
                        35.7500, // South latitude
                        -5.8600  // West longitude
                );
                mapView.zoomToBoundingBox(tangierBounds, true, 50);

                // Clear the manual zoom flag after a delay
                zoomHandler.removeCallbacksAndMessages(null);
                zoomHandler.postDelayed(() -> mapManager.setManualZoomInProgress(false), 3000);
            }
            mapView.requestLayout();
        });

        // Bottom sheet callback
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // Adjust UI based on bottom sheet state
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Optional: Implement animations during slide
            }
        });
    }

    private void loadPharmacyData() {
        // Use the executor service to load data in background
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Get all pharmacies from the database
            allPharmacies = pharmacyDAO.getAllPharmacies();

            // Log the number of pharmacies for debugging
            Log.d("PharmacyFinder", "Loaded " + allPharmacies.size() + " pharmacies from database");

            // Update UI on main thread
            runOnUiThread(() -> {
                if (allPharmacies.isEmpty()) {
                    String noPharmaciesMsg;
                    if (LanguageUtils.LANGUAGE_ARABIC.equals(LanguageUtils.getCurrentLanguage(this))) {
                        noPharmaciesMsg = "لم يتم العثور على صيدليات في قاعدة البيانات";
                    } else {
                        noPharmaciesMsg = "No pharmacies found in database";
                    }
                    Toast.makeText(this, noPharmaciesMsg, Toast.LENGTH_SHORT).show();
                } else {
                    filterPharmacies();
                }
            });
        });
    }
    private void filterPharmacies() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Pharmacy> filteredList = new ArrayList<>();
            boolean onDutyOnly = dutySwitch.isChecked();

            if (onDutyOnly) {
                // Get pharmacies on duty for selected date
                List<GuardDate> guardDates = guardDateDAO.getByDate(selectedDate);
                List<Integer> pharmacyIds = new ArrayList<>();

                for (GuardDate guardDate : guardDates) {
                    pharmacyIds.add(guardDate.getPharmacyId());
                }

                if (!pharmacyIds.isEmpty()) {
                    filteredList = pharmacyDAO.getPharmaciesByIds(pharmacyIds);
                }
            } else {
                // Include all pharmacies
                filteredList.addAll(allPharmacies);
            }

            // Sort by distance if location is available
            if (currentLocation != null) {
                LocationUtils.sortPharmaciesByDistance(
                        filteredList,
                        currentLocation.getLatitude(),
                        currentLocation.getLongitude()
                );
            }

            // Log the number of filtered pharmacies
            Log.d("PharmacyFinder", "Filtered to " + filteredList.size() + " pharmacies");

            // Update UI on main thread
            List<Pharmacy> finalFilteredList = filteredList;
            runOnUiThread(() -> {
                displayedPharmacies.clear();
                displayedPharmacies.addAll(finalFilteredList);
                adapter.notifyDataSetChanged();

                // Update map markers
                mapManager.updateMapMarkers(displayedPharmacies, dutySwitch.isChecked(), currentLocation);

                // Show/hide no results message
                if (displayedPharmacies.isEmpty()) {
                    noResultsTextView.setVisibility(View.VISIBLE);
                } else {
                    noResultsTextView.setVisibility(View.GONE);
                }
            });
        });
    }
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        // Parse existing date if available
        try {
            if (selectedDate != null) {
                String[] parts = selectedDate.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]) - 1; // Month is 0-based in Calendar
                int day = Integer.parseInt(parts[2]);

                calendar.set(year, month, day);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create DatePickerDialog with appropriate language title
        String title = LanguageUtils.LANGUAGE_ARABIC.equals(LanguageUtils.getCurrentLanguage(this)) ?
                "اختر التاريخ" : // Hardcoded Arabic text since string resource is missing
                "Sélectionner une date"; // Hardcoded French text

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    updateDateButtonText();
                    filterPharmacies();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.setTitle(title);
        datePickerDialog.show();
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION_PERMISSION);
    }

    private void updateCurrentLocation() {
        if (!checkLocationPermission()) {
            requestLocationPermission();
            return;
        }

        try {
            android.location.LocationManager locationManager =
                    (android.location.LocationManager) getSystemService(LOCATION_SERVICE);

            Location lastKnownLocation = locationManager.getLastKnownLocation(
                    android.location.LocationManager.GPS_PROVIDER);

            if (lastKnownLocation != null) {
                currentLocation = lastKnownLocation;
                mapManager.setCurrentLocation(currentLocation);

                // Re-sort pharmacies by distance
                if (!displayedPharmacies.isEmpty()) {
                    filterPharmacies();
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mapManager.enableMyLocation();
                mapManager.moveCameraToCurrentLocation();
                updateCurrentLocation();
            } else {
                // Show permission denied message in the appropriate language
                String message = LanguageUtils.LANGUAGE_ARABIC.equals(LanguageUtils.getCurrentLanguage(this)) ?
                        "تم رفض إذن الموقع" :
                        "Location permission denied";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPharmacyClick(Pharmacy pharmacy) {
        // Center map on this pharmacy
        mapManager.highlightPharmacy(pharmacy);
        showSinglePharmacyDetails(pharmacy);
    }

    private void showSinglePharmacyDetails(Pharmacy pharmacy) {
        // Create a dialog to show just this pharmacy's details
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate a custom layout for the pharmacy details
        View detailView = getLayoutInflater().inflate(R.layout.pharmacy_detail_dialog, null);

        // Set up the pharmacy details in the view
        TextView nameTextView = detailView.findViewById(R.id.detailPharmacyName);
        TextView addressTextView = detailView.findViewById(R.id.detailPharmacyAddress);
        TextView phoneTextView = detailView.findViewById(R.id.detailPharmacyPhone);
        TextView hoursTextView = detailView.findViewById(R.id.detailPharmacyHours);
        Button callButton = detailView.findViewById(R.id.detailCallButton);
        Button directionsButton = detailView.findViewById(R.id.detailDirectionsButton);

        // Populate the view with pharmacy data
        nameTextView.setText(pharmacy.getName());
        addressTextView.setText(pharmacy.getAddress());

        // Set phone if available
        if (pharmacy.getPhone() != null && !pharmacy.getPhone().isEmpty()) {
            phoneTextView.setText(pharmacy.getPhone());
            phoneTextView.setVisibility(View.VISIBLE);
            callButton.setVisibility(View.VISIBLE);

            // Set up call button
            callButton.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + pharmacy.getPhone()));
                startActivity(intent);
            });
        } else {
            phoneTextView.setVisibility(View.GONE);
            callButton.setVisibility(View.GONE);
        }

        // Set hours if available
        if (pharmacy.getHours() != null && !pharmacy.getHours().isEmpty()) {
            hoursTextView.setText(pharmacy.getHours());
            hoursTextView.setVisibility(View.VISIBLE);
        } else {
            hoursTextView.setVisibility(View.GONE);
        }

        // Set up directions button
        directionsButton.setOnClickListener(v -> {
            double lat = pharmacy.getLatitude();
            double lon = pharmacy.getLongitude();

            // Open Google Maps with direction intent
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lon);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            // If Google Maps is not installed, use browser instead
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // Fallback to web URL
                String uri = "http://maps.google.com/maps?daddr=" + lat + "," + lon;
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
            }
        });

        // Set the custom view to the dialog
        builder.setView(detailView);

        // Add a close button
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        mapManager.disableMyLocation();
        zoomHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDetach();
        zoomHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        updateCurrentLocation();

        // Si les notifications de pharmacies de garde sont activées, les planifier
        if (NotificationUtils.areDutyPharmacyNotificationsEnabled(this)) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                List<GuardDate> guardDates = guardDateDAO.getAllGuardDates();
                List<Pharmacy> pharmacies = pharmacyDAO.getAllPharmacies();

                // Planifier les notifications sur le thread principal
                runOnUiThread(() -> {
                    try {
                        NotificationUtils.scheduleGuardDutyNotifications(this, guardDates, pharmacies);
                    } catch (Exception e) {
                        Log.e("Notifications", "Error scheduling notifications: " + e.getMessage());
                    }
                });
            });
        }
    }
}