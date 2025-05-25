package com.example.quietspaceeee;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import com.example.quietspaceeee.data.db.CafeRepository;
import com.example.quietspaceeee.data.model.Cafe;
import com.example.quietspaceeee.data.view.CafeDetailActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import android.text.Editable;
import android.text.TextWatcher;

public class MapSearchFragment1 extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private RequestQueue requestQueue;
    private CafeRepository cafeRepository;
    private Map<Marker, Cafe> markerCafeMap = new HashMap<>();

    private TextInputEditText searchInput;
    private ChipGroup chipGroupFilters;
    private String currentFilterType = ""; // café, bibliothèque, coworking ou vide

    private final String API_KEY = "AIzaSyDXYvvB5Up1uqvQRNaHbUOuxbO6tIxCUH0"; // Replace with your real API key

    public MapSearchFragment1() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map_search, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Window window = getActivity().getWindow();
        window.setStatusBarColor(ContextCompat.getColor(getContext(), R.color.chip_selected_background_color));

        Toolbar toolbar = view.findViewById(R.id.toolbar_main);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> activity.onBackPressed());
        }


        searchInput = view.findViewById(R.id.searchInput_map);
        chipGroupFilters = view.findViewById(R.id.chipGroup_filters_map);

        // Écoute de la barre de recherche
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Écoute des changements dans le ChipGroup
        chipGroupFilters.setOnCheckedChangeListener((group, checkedId) -> applyFilters());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        requestQueue = Volley.newRequestQueue(requireContext());
        cafeRepository = new CafeRepository(requireContext());

        cafeRepository = new CafeRepository(requireContext());
        markerCafeMap = new HashMap<>();

        SupportMapFragment mapFragment = new SupportMapFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.map_container, mapFragment)
                .commit();

        mapFragment.getMapAsync(this);
    }


    private void applyFilters() {
        if (googleMap == null) return;

        // Récupère texte et type sélectionné
        String searchText = searchInput.getText() != null ? searchInput.getText().toString().toLowerCase().trim() : "";

        int checkedChipId = chipGroupFilters.getCheckedChipId();
        String selectedType = null;

        if (checkedChipId == R.id.chip_map_cafes) selectedType = "Café";
        else if (checkedChipId == R.id.chip_map_bibliotheques) selectedType = "Bibliothèque";
        else if (checkedChipId == R.id.chip_map_coworking) selectedType = "espace Coworking";
        // Tous sélectionné ou aucun : selectedType = null (aucun filtre de type)

        // Enlève tous les marqueurs existants
        googleMap.clear();
        markerCafeMap.clear();

        // Recréé les marqueurs filtrés
        for (Cafe cafe : cafeRepository.getAllCafes()) {
            boolean matchesSearch = cafe.getName().toLowerCase().contains(searchText);
            boolean matchesType = (selectedType == null || selectedType.equals("Tous")) ||
                    cafe.getType().equalsIgnoreCase(selectedType);

            if (matchesSearch && matchesType) {
                LatLng position = new LatLng(cafe.getLatitude(), cafe.getLongitude());
                Marker marker = googleMap.addMarker(new MarkerOptions().position(position).title(cafe.getName()));
                markerCafeMap.put(marker, cafe);
            }
        }

        // Remet l'écouteur sur les marqueurs
        googleMap.setOnMarkerClickListener(marker -> {
            Cafe cafe = markerCafeMap.get(marker);
            if (cafe != null) {
                Intent intent = new Intent(getContext(), CafeDetailActivity.class);
                intent.putExtra("cafeId", cafe.getId());
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Café non enregistré localement", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        googleMap.setMyLocationEnabled(true);

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));
                        findNearbyCafes(location);
                        loadLocalCafes();
                    }
                });

    }
    private void loadLocalCafes() {
        for (Cafe cafe : cafeRepository.getAllCafes()) {
            LatLng position = new LatLng(cafe.getLatitude(), cafe.getLongitude());
            Marker marker = googleMap.addMarker(new MarkerOptions().position(position).title(cafe.getName()));
            markerCafeMap.put(marker, cafe);
        }

        googleMap.setOnMarkerClickListener(marker -> {
            Cafe cafe = markerCafeMap.get(marker);
            if (cafe != null) {
                Intent intent = new Intent(getContext(), CafeDetailActivity.class);
                intent.putExtra("cafeId", cafe.getId());
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Café non enregistré localement", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }
    private void findNearbyCafes(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        int radius = 4000; // meters
        String type = "cafe";

        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + lat + "," + lng +
                "&radius=" + radius +
                "&type=" + type +
                "&key=" + API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject place = results.getJSONObject(i);
                            JSONObject loc = place.getJSONObject("geometry").getJSONObject("location");
                            String name = place.getString("name");
                            double placeLat = loc.getDouble("lat");
                            double placeLng = loc.getDouble("lng");

                            // Vérifie si le café existe déjà dans la base
                            Cafe existing = cafeRepository.getCafeByNameAndLocation(name, placeLat, placeLng);
                            Cafe cafe;
                            if (existing == null) {
                                cafe = new Cafe();
                                cafe.setName(name);
                                cafe.setLatitude(placeLat);
                                cafe.setLongitude(placeLng);
                                cafe.setCity("Inconnue");
                                cafe.setEquipments("Wi-Fi , Snacks , Prises");
                                cafe.setNoiseLevel("Modéré");
                                cafe.setAvailability("");
                                cafe.setType("Café");

                                long id = cafeRepository.insertCafe(cafe);
                                cafe.setId((int) id);
                            } else {
                                cafe = existing;
                            }

                            // Ajoute le marqueur
                            LatLng cafeLatLng = new LatLng(placeLat, placeLng);
                            Marker marker = googleMap.addMarker(new MarkerOptions().position(cafeLatLng).title(name));
                            markerCafeMap.put(marker, cafe);
                        }

                        // Définir le comportement au clic
                        googleMap.setOnMarkerClickListener(marker -> {
                            Cafe cafe = markerCafeMap.get(marker);
                            if (cafe != null) {
                                Intent intent = new Intent(getContext(), CafeDetailActivity.class);
                                intent.putExtra("cafeId", cafe.getId());
                                startActivity(intent);
                            } else {
                                Toast.makeText(getContext(), "Café non enregistré localement", Toast.LENGTH_SHORT).show();
                            }
                            return true;
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()
        );

        requestQueue.add(request);
    }
}