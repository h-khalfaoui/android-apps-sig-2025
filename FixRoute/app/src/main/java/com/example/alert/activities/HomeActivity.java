package com.example.alert.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.alert.R;
import com.example.alert.database.SQLiteHelper;
import com.example.alert.models.AlertData;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SQLiteHelper dbHelper;
    private Map<Marker, AlertData> markerDataMap = new HashMap<>();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Marker searchMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbHelper = new SQLiteHelper(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> logout());

        SearchView searchView = findViewById(R.id.mapSearch);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query == null || query.isEmpty()) return false;

                Geocoder geocoder = new Geocoder(HomeActivity.this);
                List<Address> addressList = null;
                try {
                    addressList = geocoder.getFromLocationName(query, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (addressList != null && !addressList.isEmpty()) {
                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                    if (searchMarker != null) searchMarker.remove();

                    searchMarker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(query));

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                } else {
                    Toast.makeText(HomeActivity.this, "Lieu non trouvé", Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);

        LatLng initialLocation = new LatLng(35.7806, -5.8136);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 12));

        loadAcceptedClaimsOnMap();

        mMap.setOnMapClickListener(latLng -> {
            String location = latLng.latitude + "," + latLng.longitude;

            Intent intent = new Intent(HomeActivity.this, ClaimActivity.class);
            intent.putExtra("location", location);
            startActivity(intent);
        });
    }


    private void loadAcceptedClaimsOnMap() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT location, description, image, status FROM claims WHERE status = 'acceptée'", null);

        if (cursor.moveToFirst()) {
            do {
                String location = cursor.getString(0);
                String description = cursor.getString(1);
                byte[] image = cursor.getBlob(2);

                try {
                    String[] parts = location.split(",");
                    if (parts.length == 2) {
                        double lat = Double.parseDouble(parts[0].trim());
                        double lng = Double.parseDouble(parts[1].trim());
                        LatLng latLng = new LatLng(lat, lng);

                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title("Réclamation Acceptée")
                                .snippet(description));

                        markerDataMap.put(marker, new AlertData(location, description, image));

                        if (cursor.isFirst()) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                        }
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
    }

    private void logout() {
        Intent loginIntent = new Intent(HomeActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        Toast.makeText(this, "Déconnecté avec succès", Toast.LENGTH_SHORT).show();
    }
}
