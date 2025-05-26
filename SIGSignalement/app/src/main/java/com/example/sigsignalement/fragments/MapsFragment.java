package com.example.sigsignalement.fragments;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.sigsignalement.R;
import com.example.sigsignalement.model.AppDatabase;
import com.example.sigsignalement.model.Signalement;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.sigsignalement.model.AppDatabaseSingleton;



import java.util.List;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    @Override
    public void onViewCreated(@NonNull android.view.View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater,
                                          android.view.ViewGroup container,
                                          Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Permission GPS manquante", Toast.LENGTH_SHORT).show();
            return;
        }

        mMap.setMyLocationEnabled(true);

        // Centrer la carte sur la dernière position connue
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
            }
        });

        // Charger les anciens signalements
        chargerAnciensSignalements();
    }

    private void chargerAnciensSignalements() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE);
        String email = prefs.getString("email", "anonyme");

        AppDatabase db = AppDatabaseSingleton.getInstance(requireContext());

        List<Signalement> signalements = db.signalementDao().getByUser(email);

        if (signalements.isEmpty()) {
            Toast.makeText(getContext(), "Aucun signalement trouvé", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Signalement s : signalements) {
            LatLng pos = new LatLng(s.latitude, s.longitude);
            mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(s.type)
                    .snippet("Signalé le " + s.date));
        }
    }
}
