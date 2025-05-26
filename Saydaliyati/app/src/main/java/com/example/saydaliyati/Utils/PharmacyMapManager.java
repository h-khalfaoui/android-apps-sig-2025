package com.example.saydaliyati.Utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Handler;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.saydaliyati.Adapters.PharmacyAdapter;
import com.example.saydaliyati.Models.Pharmacy;
import com.example.saydaliyati.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PharmacyMapManager {

    private final Context context;
    private final MapView mapView;
    private MyLocationNewOverlay locationOverlay;
    private final Map<Marker, Pharmacy> markersMap = new HashMap<>();
    private Location currentLocation;
    private PharmacyAdapter.OnPharmacyClickListener pharmacyClickListener;
    private Marker locationMarker;
    private final Handler locationUpdateHandler = new Handler();
    private final Runnable locationUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updateLocationMarker();
            locationUpdateHandler.postDelayed(this, 2000); // Update every 2 seconds
        }
    };
    private boolean manualZoomInProgress = false;

    // Constants
    private static final double DEFAULT_ZOOM = 14.0;
    private static final double MY_LOCATION_ZOOM = 19.0; // Higher zoom level for my location
    private static final GeoPoint DEFAULT_LOCATION = new GeoPoint(35.7796, -5.8137); // Tangier
    private static final int MAP_PADDING = 100;

    public PharmacyMapManager(Context context, MapView mapView) {
        this.context = context;
        this.mapView = mapView;
    }

    // Add this method to allow PharmacyFinderActivity to control the manual zoom state
    public void setManualZoomInProgress(boolean inProgress) {
        this.manualZoomInProgress = inProgress;
    }

    // Also add a method to get the current state
    public boolean isManualZoomInProgress() {
        return manualZoomInProgress;
    }

    public void initializeMap() {
        // Configure the map
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        // Initialize location tracking
        GpsMyLocationProvider provider = new GpsMyLocationProvider(context);
        locationOverlay = new MyLocationNewOverlay(provider, mapView);
        locationOverlay.enableMyLocation();

        // We won't add the overlay to the map, since we'll use our own marker instead
        // mapView.getOverlays().add(locationOverlay);

        // Set default view
        IMapController mapController = mapView.getController();
        mapController.setZoom(17.0);
        mapController.setCenter(DEFAULT_LOCATION);

        // Check location permissions
        if (checkLocationPermission()) {
            enableMyLocation();
        }
    }

    public void setOnPharmacyMarkerClickListener(PharmacyAdapter.OnPharmacyClickListener listener) {
        this.pharmacyClickListener = listener;
    }

    public void setCurrentLocation(Location location) {
        this.currentLocation = location;
        if (locationMarker != null && location != null) {
            locationMarker.setPosition(new GeoPoint(location.getLatitude(), location.getLongitude()));
            mapView.invalidate();
        }
    }

    public void enableMyLocation() {
        if (checkLocationPermission() && locationOverlay != null) {
            locationOverlay.enableMyLocation();
            updateLocationMarker();

            // Start periodic updates
            locationUpdateHandler.postDelayed(locationUpdateRunnable, 2000);
        }
    }

    // Call this when the activity is paused/destroyed
    public void disableMyLocation() {
        if (locationOverlay != null) {
            locationOverlay.disableMyLocation();
        }
        // Stop periodic updates
        locationUpdateHandler.removeCallbacks(locationUpdateRunnable);
    }

    private void updateLocationMarker() {
        if (!checkLocationPermission()) return;

        try {
            // Get location from the overlay if available
            GeoPoint myLocation = locationOverlay.getMyLocation();

            // If not available from overlay, try to get from location services
            if (myLocation == null && currentLocation != null) {
                myLocation = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
            }

            // If still not available, try one last method
            if (myLocation == null) {
                android.location.LocationManager locationManager =
                        (android.location.LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

                Location lastLocation = locationManager.getLastKnownLocation(
                        android.location.LocationManager.GPS_PROVIDER);

                if (lastLocation != null) {
                    myLocation = new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude());
                    currentLocation = lastLocation;
                }
            }

            if (myLocation != null) {
                // Create or update the marker
                if (locationMarker == null) {
                    locationMarker = new Marker(mapView);
                    locationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);

                    // Set the person icon
                    Drawable personIcon = ContextCompat.getDrawable(context, R.drawable.ic_person_location);
                    if (personIcon != null) {
                        locationMarker.setIcon(personIcon);
                    }

                    mapView.getOverlays().add(locationMarker);
                }

                // Update position
                locationMarker.setPosition(myLocation);
                mapView.invalidate();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void animateToLocation(GeoPoint point) {
        if (mapView != null) {
            mapView.getController().animateTo(point);
            mapView.getController().setZoom(DEFAULT_ZOOM);
        }
    }

    public void moveCameraToCurrentLocation() {
        if (mapView == null || !checkLocationPermission()) {
            if (mapView != null) {
                mapView.getController().setZoom(MY_LOCATION_ZOOM);
            }
            return;
        }

        try {
            // Set flag to prevent automatic zoom changes
            manualZoomInProgress = true;

            android.location.LocationManager locationManager =
                    (android.location.LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            Location lastKnownLocation = locationManager.getLastKnownLocation(
                    android.location.LocationManager.GPS_PROVIDER);

            if (lastKnownLocation != null) {
                currentLocation = lastKnownLocation;
                GeoPoint currentGeoPoint = new GeoPoint(
                        lastKnownLocation.getLatitude(),
                        lastKnownLocation.getLongitude()
                );

                // Use a higher zoom level (19.0 instead of 17.0)
                mapView.getController().setCenter(currentGeoPoint);
                mapView.getController().setZoom(MY_LOCATION_ZOOM);

                // Update the marker
                updateLocationMarker();

                // Keep the flag active for 5 seconds to prevent other methods from changing the zoom
                new Handler().postDelayed(() -> {
                    manualZoomInProgress = false;
                }, 5000);
            } else {
                Toast.makeText(context, "Could not get current location", Toast.LENGTH_SHORT).show();
                manualZoomInProgress = false;
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show();
            manualZoomInProgress = false;
        }
    }

    public void updateMapMarkers(List<Pharmacy> pharmacies, boolean onDutyOnly, Location currentLocation) {
        // Clear all existing pharmacy markers but keep the location marker
        List<Marker> markersToRemove = new ArrayList<>(markersMap.keySet());
        for (Marker marker : markersToRemove) {
            mapView.getOverlays().remove(marker);
        }
        markersMap.clear();

        // Add markers for all displayed pharmacies
        for (Pharmacy pharmacy : pharmacies) {
            GeoPoint position = new GeoPoint(pharmacy.getLatitude(), pharmacy.getLongitude());

            Marker marker = new Marker(mapView);
            marker.setPosition(position);
            marker.setTitle(pharmacy.getName());
            marker.setSnippet(pharmacy.getAddress());
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            // Use different icon for pharmacies on duty
            if (onDutyOnly) {
                marker.setIcon(context.getResources().getDrawable(R.drawable.ic_pharmacy_on_duty));
            } else {
                marker.setIcon(context.getResources().getDrawable(R.drawable.ic_pharmacy_logo));
            }

            // Add marker click listener
            marker.setOnMarkerClickListener((marker1, mapView1) -> {
                Pharmacy clickedPharmacy = markersMap.get(marker1);
                if (clickedPharmacy != null && pharmacyClickListener != null) {
                    pharmacyClickListener.onPharmacyClick(clickedPharmacy);
                }
                return true;
            });

            mapView.getOverlays().add(marker);
            markersMap.put(marker, pharmacy);
        }

        // Make sure our location marker is on top
        if (locationMarker != null) {
            mapView.getOverlays().remove(locationMarker);
            mapView.getOverlays().add(locationMarker);
        }

        // Adjust camera to show all markers if there are any and no manual zoom is in progress
        if (!pharmacies.isEmpty() && !manualZoomInProgress) {
            zoomToShowAllMarkers(pharmacies);
        }

        // Update current location
        if (currentLocation != null) {
            this.currentLocation = currentLocation;
            updateLocationMarker();
        }

        mapView.invalidate();
    }

    public void highlightPharmacy(Pharmacy pharmacy) {
        // Set flag to indicate a manual zoom
        manualZoomInProgress = true;

        // Find the marker for this pharmacy
        for (Map.Entry<Marker, Pharmacy> entry : markersMap.entrySet()) {
            if (entry.getValue().getId() == pharmacy.getId()) {
                // Center map on this pharmacy
                mapView.getController().setCenter(
                        new GeoPoint(pharmacy.getLatitude(), pharmacy.getLongitude()));

                // Show marker info
                mapView.getController().setZoom(18.0);
                entry.getKey().showInfoWindow();
                break;
            }
        }

        // Clear the manual zoom flag after 5 seconds
        new Handler().postDelayed(() -> {
            manualZoomInProgress = false;
        }, 5000);
    }

    private void zoomToShowAllMarkers(List<Pharmacy> pharmacies) {
        if (pharmacies.isEmpty() || mapView == null || manualZoomInProgress) return;

        // If only one pharmacy, just zoom to it
        if (pharmacies.size() == 1) {
            Pharmacy pharmacy = pharmacies.get(0);
            GeoPoint position = new GeoPoint(pharmacy.getLatitude(), pharmacy.getLongitude());
            mapView.getController().animateTo(position);
            mapView.getController().setZoom(17.0);
            return;
        }

        // Calculate bounds to include all markers
        double north = -90, east = -180, south = 90, west = 180;

        // Include all pharmacy positions
        for (Pharmacy pharmacy : pharmacies) {
            double lat = pharmacy.getLatitude();
            double lon = pharmacy.getLongitude();

            north = Math.max(north, lat);
            south = Math.min(south, lat);
            east = Math.max(east, lon);
            west = Math.min(west, lon);
        }

        // Include current location if available
        if (currentLocation != null) {
            double lat = currentLocation.getLatitude();
            double lon = currentLocation.getLongitude();

            north = Math.max(north, lat);
            south = Math.min(south, lat);
            east = Math.max(east, lon);
            west = Math.min(west, lon);
        }

        // Add some padding
        double latPadding = (north - south) * 0.1;
        double lonPadding = (east - west) * 0.1;

        // Create the BoundingBox
        BoundingBox boundingBox = new BoundingBox(
                north + latPadding,
                east + lonPadding,
                south - latPadding,
                west - lonPadding
        );

        // Apply the BoundingBox to the map
        mapView.zoomToBoundingBox(boundingBox, true, MAP_PADDING);
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}