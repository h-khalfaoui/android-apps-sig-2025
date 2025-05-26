package com.example.sigsignalement;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import java.util.List;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.example.sigsignalement.model.AppDatabase;
import com.example.sigsignalement.model.Signalement;

import androidx.room.Room;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    LatLng selectedLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap; //  Initialise mMap d'abord

        // Zoom sur une position par défaut
        LatLng maroc = new LatLng(31.7917, -7.0926);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(maroc, 6));

        // Activer la localisation
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        mMap.setMyLocationEnabled(true);

        // Récupération et affichage des signalements depuis la base de données
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "signalements_db") // Assure-toi que le nom est bien "signalements_db"
                .allowMainThreadQueries()
                .build();

        List<Signalement> signalements = db.signalementDao().getAll();

        for (Signalement s : signalements) {
            LatLng position = new LatLng(s.latitude, s.longitude);
            mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(s.type));
        }

        // Clic sur la carte pour sélectionner une position
        mMap.setOnMapClickListener(latLng -> {
            mMap.clear(); // Supprimer anciens marqueurs
            mMap.addMarker(new MarkerOptions().position(latLng).title("Position choisie"));
            selectedLocation = latLng;

            // Retourner la position à SignalementActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("latitude", latLng.latitude);
            resultIntent.putExtra("longitude", latLng.longitude);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }
}
