package com.example.saydaliyati.Activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.saydaliyati.Database.AppDatabase;
import com.example.saydaliyati.Database.PharmacyDAO;
import com.example.saydaliyati.Models.Pharmacy;
import com.example.saydaliyati.R;
import com.example.saydaliyati.Utils.SecurityUtils;
import com.google.android.material.textfield.TextInputLayout;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class AddPharmacyActivity extends BaseActivity implements MapEventsReceiver {

    // UI Components
    private TextInputLayout nameLayout, addressLayout, phoneLayout, hoursLayout;
    private EditText nameEditText, addressEditText, phoneEditText, hoursEditText,
            latitudeEditText, longitudeEditText;
    private CheckBox hasParkingCheckBox;
    private Button savePharmacyButton, useCurrentLocationButton;
    private ProgressBar progressBar;

    // OSMDroid Map
    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;
    private Marker currentMarker;
    private static final double DEFAULT_ZOOM = 15.0;
    private static final int REQUEST_LOCATION_PERMISSION = 1001;

    // Database
    private PharmacyDAO pharmacyDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize OSMDroid
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        setContentView(R.layout.activity_add_pharmacy);

        // Check if user is authenticated, redirect to login if not
        if (!SecurityUtils.isAuthenticated(this)) {
            Toast.makeText(this, "Authentication required", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up action bar with back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Add New Pharmacy");
        }

        // Initialize views
        initializeViews();

        // Setup database connection
        pharmacyDAO = AppDatabase.getInstance(this).pharmacyDAO();

        // Initialize the map
        initializeMap();

        // Setup event listeners
        setupEventListeners();
    }

    private void initializeViews() {
        nameLayout = findViewById(R.id.nameLayout);
        addressLayout = findViewById(R.id.addressLayout);
        phoneLayout = findViewById(R.id.phoneLayout);
        hoursLayout = findViewById(R.id.hoursLayout);

        nameEditText = findViewById(R.id.nameEditText);
        addressEditText = findViewById(R.id.addressEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        hoursEditText = findViewById(R.id.hoursEditText);
        latitudeEditText = findViewById(R.id.latitudeEditText);
        longitudeEditText = findViewById(R.id.longitudeEditText);

        hasParkingCheckBox = findViewById(R.id.hasParkingCheckBox);

        savePharmacyButton = findViewById(R.id.savePharmacyButton);
        useCurrentLocationButton = findViewById(R.id.useCurrentLocationButton);

        progressBar = findViewById(R.id.progressBar);

        mapView = findViewById(R.id.mapFragment);
    }

    private void initializeMap() {
        // Configure the map
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        // Initialize location overlay
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        locationOverlay.enableMyLocation();
        mapView.getOverlays().add(locationOverlay);

        // Add map click events
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this);
        mapView.getOverlays().add(0, mapEventsOverlay); // Add at bottom of overlay stack

        // Set default view
        IMapController mapController = mapView.getController();
        mapController.setZoom(DEFAULT_ZOOM);

        // Default location (Tangier)
        GeoPoint defaultLocation = new GeoPoint(35.7796, -5.8137);
        mapController.setCenter(defaultLocation);

        // Check location permissions
        if (checkLocationPermission()) {
            enableMyLocation();
        } else {
            requestLocationPermission();
        }
    }

    private void setupEventListeners() {
        // Save pharmacy button
        savePharmacyButton.setOnClickListener(v -> validateAndSavePharmacy());

        // Use current location button
        useCurrentLocationButton.setOnClickListener(v -> {
            if (checkLocationPermission()) {
                getCurrentLocation();
            } else {
                requestLocationPermission();
            }
        });
    }

    private void validateAndSavePharmacy() {
        // Reset errors
        nameLayout.setError(null);
        addressLayout.setError(null);
        phoneLayout.setError(null);
        hoursLayout.setError(null);

        // Get input values
        String name = nameEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String hours = hoursEditText.getText().toString().trim();
        String latStr = latitudeEditText.getText().toString().trim();
        String lonStr = longitudeEditText.getText().toString().trim();
        boolean hasParking = hasParkingCheckBox.isChecked();

        // Validate inputs
        boolean cancel = false;
        View focusView = null;

        // Validate name
        if (TextUtils.isEmpty(name)) {
            nameLayout.setError("Please enter pharmacy name");
            focusView = nameEditText;
            cancel = true;
        }

        // Validate address
        if (TextUtils.isEmpty(address)) {
            addressLayout.setError("Please enter pharmacy address");
            focusView = addressEditText;
            cancel = true;
        }

        // Validate coordinates
        if (TextUtils.isEmpty(latStr)) {
            focusView = latitudeEditText;
            cancel = true;
            Toast.makeText(this, "Please set latitude", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(lonStr)) {
            focusView = longitudeEditText;
            cancel = true;
            Toast.makeText(this, "Please set longitude", Toast.LENGTH_SHORT).show();
        }

        if (cancel) {
            // There was an error; focus the first form field with an error
            if (focusView != null) {
                focusView.requestFocus();
            }
        } else {
            // Show progress and save pharmacy
            showProgress(true);

            try {
                double latitude = Double.parseDouble(latStr);
                double longitude = Double.parseDouble(lonStr);

                // Create pharmacy object
                Pharmacy pharmacy = new Pharmacy(name, address, latitude, longitude,
                        phone, hours, hasParking);

                // Save to database in background
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    long pharmacyId = pharmacyDAO.insert(pharmacy);

                    // Handle result on UI thread
                    runOnUiThread(() -> {
                        showProgress(false);
                        if (pharmacyId > 0) {
                            // Success
                            Toast.makeText(AddPharmacyActivity.this,
                                    "Pharmacy added successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            // Error
                            Toast.makeText(AddPharmacyActivity.this,
                                    "Error adding pharmacy", Toast.LENGTH_SHORT).show();
                        }
                    });
                });

            } catch (NumberFormatException e) {
                showProgress(false);
                Toast.makeText(this, "Invalid coordinates!", Toast.LENGTH_SHORT).show();
                latitudeEditText.requestFocus();
            }
        }
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        savePharmacyButton.setEnabled(!show);
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint point) {
        // Update marker
        updateMarker(point);

        // Update EditText fields
        latitudeEditText.setText(String.valueOf(point.getLatitude()));
        longitudeEditText.setText(String.valueOf(point.getLongitude()));

        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint point) {
        // Same behavior as single tap for long press
        return singleTapConfirmedHelper(point);
    }

    private void updateMarker(GeoPoint point) {
        // Remove existing marker if any
        if (currentMarker != null) {
            mapView.getOverlays().remove(currentMarker);
        }

        // Create new marker
        currentMarker = new Marker(mapView);
        currentMarker.setPosition(point);
        currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        currentMarker.setTitle("Selected Location");

        // Add marker to map
        mapView.getOverlays().add(currentMarker);

        // Refresh map
        mapView.invalidate();
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

    private void enableMyLocation() {
        if (checkLocationPermission() && locationOverlay != null) {
            locationOverlay.enableMyLocation();
        }
    }

    private void getCurrentLocation() {
        if (!checkLocationPermission()) {
            requestLocationPermission();
            return;
        }

        try {
            // Try to get current location from overlay
            if (locationOverlay != null && locationOverlay.getMyLocation() != null) {
                GeoPoint myLocation = locationOverlay.getMyLocation();

                // Update marker
                updateMarker(myLocation);

                // Update EditText fields
                latitudeEditText.setText(String.valueOf(myLocation.getLatitude()));
                longitudeEditText.setText(String.valueOf(myLocation.getLongitude()));

                // Move map to location
                mapView.getController().animateTo(myLocation);

                Toast.makeText(this, "Current location set", Toast.LENGTH_SHORT).show();
            } else {
                // Fallback to Android location manager
                android.location.LocationManager locationManager =
                        (android.location.LocationManager) getSystemService(LOCATION_SERVICE);

                Location lastKnownLocation = locationManager.getLastKnownLocation(
                        android.location.LocationManager.GPS_PROVIDER);

                if (lastKnownLocation != null) {
                    GeoPoint currentGeoPoint = new GeoPoint(
                            lastKnownLocation.getLatitude(),
                            lastKnownLocation.getLongitude());

                    // Update marker
                    updateMarker(currentGeoPoint);

                    // Update EditText fields
                    latitudeEditText.setText(String.valueOf(lastKnownLocation.getLatitude()));
                    longitudeEditText.setText(String.valueOf(lastKnownLocation.getLongitude()));

                    // Move map to location
                    mapView.getController().animateTo(currentGeoPoint);

                    Toast.makeText(this, "Current location set", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Could not get current location", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
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

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDetach();
    }
}