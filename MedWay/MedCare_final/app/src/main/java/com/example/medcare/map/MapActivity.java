package com.example.medcare.map;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.medcare.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapActivity extends AppCompatActivity {
    private MapView map;
    private List<Facility> facilities;
    private GeoPoint userPoint;
    private double userLat = 35.76, userLon = -5.8; // default location

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(R.layout.activity_main_map);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        facilities = JsonUtils.loadFacilities(this, "facilities.json");
        if (facilities == null) {
            Log.d("HealthMapDebug", "Facilities list is NULL.");
        } else {
            Log.d("HealthMapDebug", "Facilities loaded: " + facilities.size());
        }

        requestLocationPermission();

        // SearchView setup
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterFacilities(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterFacilities(newText);
                return true;
            }
        });

        // Filter button setup
        Button btnFilter = findViewById(R.id.btnFilter);
        btnFilter.setOnClickListener(v -> showFilterDialog());
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            showUserLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showUserLocation();
        }
    }

    private void showUserLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location != null) {
            userLat = location.getLatitude();
            userLon = location.getLongitude();
        }

        userPoint = new GeoPoint(userLat, userLon);
        map.getController().setZoom(14.0);
        map.getController().setCenter(userPoint);

        Marker userMarker = new Marker(map);
        userMarker.setPosition(userPoint);
        userMarker.setTitle("Vous êtes ici=");
        map.getOverlays().add(userMarker);

        displayNearbyFacilities(userLat, userLon);
    }

    private void displayNearbyFacilities(double userLat, double userLon) {
        map.getOverlays().clear();

        Marker userMarker = new Marker(map);
        userMarker.setPosition(new GeoPoint(userLat, userLon));
        userMarker.setTitle("Vous êtes ici");
        map.getOverlays().add(userMarker);

        for (Facility f : facilities) {
            double dist = FacilityUtils.distance(userLat, userLon, f.latitude, f.longitude);
            if (dist <= 2.0) { // 2 km
                Marker marker = new Marker(map);
                marker.setPosition(new GeoPoint(f.latitude, f.longitude));
                marker.setTitle(f.name + " (" + f.type + ")");
                map.getOverlays().add(marker);
            }
        }

        map.invalidate();
    }

    private void displayNearbyFacilities(double userLat, double userLon, List<Facility> filteredList) {
        map.getOverlays().clear();

        Marker userMarker = new Marker(map);
        userMarker.setPosition(new GeoPoint(userLat, userLon));
        userMarker.setTitle("Vous êtes ici");
        map.getOverlays().add(userMarker);

        for (Facility f : filteredList) {
            Marker marker = new Marker(map);
            marker.setPosition(new GeoPoint(f.latitude, f.longitude));
            marker.setTitle(f.name + " (" + f.type + ")");
            map.getOverlays().add(marker);
        }

        map.invalidate();
    }

    private void filterFacilities(String query) {
        map.getOverlays().clear();

        Marker userMarker = new Marker(map);
        userMarker.setPosition(userPoint);
        userMarker.setTitle("Vous êtes ici");
        map.getOverlays().add(userMarker);

        for (Facility f : facilities) {
            if (f.name.toLowerCase().contains(query.toLowerCase()) ||
                    f.type.toLowerCase().contains(query.toLowerCase())) {
                Marker marker = new Marker(map);
                marker.setPosition(new GeoPoint(f.latitude, f.longitude));
                marker.setTitle(f.name + " (" + f.type + ")");
                map.getOverlays().add(marker);
            }
        }

        map.invalidate();
    }

    private void showFilterDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter, null);
        Spinner spinnerDistance = dialogView.findViewById(R.id.spinnerDistance);
        Spinner spinnerType = dialogView.findViewById(R.id.spinnerType);

        String[] distances = {"1", "2", "5", "10"};
        ArrayAdapter<String> distanceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, distances);
        distanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistance.setAdapter(distanceAdapter);

        Set<String> types = new HashSet<>();
        for (Facility f : facilities) types.add(f.type);
        List<String> typeList = new ArrayList<>(types);
        typeList.add(0, "Tous");

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typeList);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Options de Filtrage")
                .setView(dialogView)
                .setPositiveButton("Appliquer", (dialog, which) -> {
                    double selectedDistance = Double.parseDouble(spinnerDistance.getSelectedItem().toString());
                    String selectedType = spinnerType.getSelectedItem().toString();
                    applyFilters(selectedDistance, selectedType);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void applyFilters(double distance, String type) {
        List<Facility> filteredList = new ArrayList<>();

        for (Facility f : facilities) {
            double dist = FacilityUtils.distance(userPoint.getLatitude(), userPoint.getLongitude(), f.latitude, f.longitude);
            if (dist <= distance && (type.equals("All") || f.type.equalsIgnoreCase(type))) {
                filteredList.add(f);
            }
        }

        displayNearbyFacilities(userPoint.getLatitude(), userPoint.getLongitude(), filteredList);
    }
}
