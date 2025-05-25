package com.example.quietspaceeee;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.example.quietspaceeee.data.view.CafeDetailActivity1;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.quietspaceeee.data.db.CafeRepository;
import com.example.quietspaceeee.data.model.Cafe;
import com.example.quietspaceeee.data.view.CafeDetailActivity;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

public class MapSearchFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private RequestQueue requestQueue;
    private CafeRepository cafeRepository;
    private Uri selectedImageUri = null;
    private ImageView selectedImageViewGlobal;
    ActivityResultLauncher<String[]> pickImageLauncher;

    private static final int REQUEST_IMAGE_PICK = 1001;

    private TextInputEditText searchInput;
    private ChipGroup chipGroupFilters;

    private Map<Marker, Cafe> markerCafeMap = new HashMap<>();
    private final String API_KEY = "AIzaSyDXYvvB5Up1uqvQRNaHbUOuxbO6tIxCUH0"; // Replace with your real API key

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        cafeRepository = new CafeRepository(context);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;

                        // Donne la permission persistante d'accès au fichier
                        requireContext().getContentResolver().takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );

                        if (selectedImageViewGlobal != null) {
                            try {
                                Bitmap bitmap;
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                                    ImageDecoder.Source source = ImageDecoder.createSource(requireContext().getContentResolver(), uri);
                                    bitmap = ImageDecoder.decodeBitmap(source);
                                } else {
                                    bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), uri);
                                }

                                // Évite les images trop lourdes
                                int targetSize = 800;
                                int width = bitmap.getWidth();
                                int height = bitmap.getHeight();
                                float scale = Math.min((float) targetSize / width, (float) targetSize / height);

                                Bitmap resizedBitmap = Bitmap.createScaledBitmap(
                                        bitmap,
                                        Math.round(scale * width),
                                        Math.round(scale * height),
                                        true
                                );

                                selectedImageViewGlobal.setImageBitmap(resizedBitmap);

                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }


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
        else if (checkedChipId == R.id.chip_map_coworking) selectedType = "Espace Coworking";
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
                Intent intent = new Intent(getContext(), CafeDetailActivity1.class);
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
        googleMap = map;

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));
                            findNearbyCafes(location);
                        } else {
                            Toast.makeText(getContext(), "Impossible d'obtenir votre position", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }


        // Charger cafés depuis la base
        loadLocalCafes();

        // Ajouter un café par appui long
        googleMap.setOnMapLongClickListener(this::showAddCafeDialog);
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
                Intent intent = new Intent(getContext(), CafeDetailActivity1.class);
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
        int radius = 4000;

        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + lat + "," + lng +
                "&radius=" + radius +
                "&type=cafe" +
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

                            Cafe existing = cafeRepository.getCafeByNameAndLocation(name, placeLat, placeLng);
                            Cafe cafe;
                            if (existing == null) {
                                cafe = new Cafe();
                                cafe.setName(name);
                                cafe.setLatitude(placeLat);
                                cafe.setLongitude(placeLng);
                                cafe.setCity("Inconnue");
                                cafe.setEquipments("Wi-Fi , Prises");
                                cafe.setNoiseLevel("");
                                cafe.setAvailability("");
                                cafe.setType(" ");

                                long id = cafeRepository.insertCafe(cafe);
                                cafe.setId((int) id);
                            } else {
                                cafe = existing;
                            }

                            LatLng cafeLatLng = new LatLng(placeLat, placeLng);
                            Marker marker = googleMap.addMarker(new MarkerOptions().position(cafeLatLng).title(name));
                            markerCafeMap.put(marker, cafe);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()
        );

        requestQueue.add(request);
    }

    private void showAddCafeDialog(LatLng latLng) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_cafe, null);

        EditText nameInput = dialogView.findViewById(R.id.editCafeName);
        EditText descInput = dialogView.findViewById(R.id.editDescription);
        EditText locationInput = dialogView.findViewById(R.id.editCafeLocation);
        EditText cityInput = dialogView.findViewById(R.id.editCafeCity);

        Spinner noiseSpinner = dialogView.findViewById(R.id.spinnerCafeNoise);
        Spinner availabilitySpinner = dialogView.findViewById(R.id.spinnerCafeDisponibilite);
        Spinner typeSpinner = dialogView.findViewById(R.id.spinnerType);



        // Adapter pour le type
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.type_array,
                android.R.layout.simple_spinner_item
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        // Adapter pour la disponibilité
        ArrayAdapter<CharSequence> availabilityAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.disponibilite_array,
                android.R.layout.simple_spinner_item
        );
        availabilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        availabilitySpinner.setAdapter(availabilityAdapter);

        // Adapter pour le niveau sonore
        ArrayAdapter<CharSequence> noiseAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.bruit_array,
                android.R.layout.simple_spinner_item
        );
        noiseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        noiseSpinner.setAdapter(noiseAdapter);

        // Récupération des CheckBox pour les équipements
        CheckBox wifiCheckBox = dialogView.findViewById(R.id.checkbox_wifi);
        CheckBox prisesCheckBox = dialogView.findViewById(R.id.checkbox_prises);
        CheckBox snacksCheckBox = dialogView.findViewById(R.id.checkbox_snacks);
        CheckBox boissonCheckBox = dialogView.findViewById(R.id.checkbox_boisson);

        Button buttonSelectImage = dialogView.findViewById(R.id.buttonSelectImage);
        selectedImageViewGlobal = dialogView.findViewById(R.id.selectedImageView);

        buttonSelectImage.setOnClickListener(v -> pickImageLauncher.launch(new String[]{"image/*"}));

        new AlertDialog.Builder(requireContext())
                .setTitle("Ajouter un café")
                .setView(dialogView)
                .setPositiveButton("Ajouter", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String description = descInput.getText().toString().trim();
                    String location = locationInput.getText().toString().trim();
                    String city = cityInput.getText().toString().trim();
                    String noiseLevel = noiseSpinner.getSelectedItem().toString();
                    String availability = availabilitySpinner.getSelectedItem().toString();
                    String type = typeSpinner.getSelectedItem().toString();


                    // Construction de la chaîne des équipements cochés
                    List<String> selectedEquipments = new ArrayList<>();
                    if (wifiCheckBox.isChecked()) selectedEquipments.add("Wi-Fi");
                    if (prisesCheckBox.isChecked()) selectedEquipments.add("Prises");
                    if (snacksCheckBox.isChecked()) selectedEquipments.add("Snacks");
                    if (boissonCheckBox.isChecked()) selectedEquipments.add("Boissons");

                    String equipments = TextUtils.join(", ", selectedEquipments); // ex: "Wi-Fi, Snacks"

                    if (!name.isEmpty() && !description.isEmpty()) {
                        Cafe newCafe = new Cafe();
                        newCafe.setName(name);
                        newCafe.setDescription(description);
                        newCafe.setLatitude(latLng.latitude);
                        newCafe.setLongitude(latLng.longitude);
                        newCafe.setCity(city);
                        newCafe.setType(type);
                        newCafe.setType(type);
                        newCafe.setAvailability(availability);
                        newCafe.setEquipments(equipments);
                        newCafe.setLocation(location);
                        newCafe.setNoiseLevel(noiseLevel);
                        newCafe.setAverageCost(0.0);
                        newCafe.setImageUrl(selectedImageUri != null ? selectedImageUri.toString() : "");

                        long id = cafeRepository.insertCafe(newCafe);
                        newCafe.setId((int) id);

                        Marker marker = googleMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(name));
                        markerCafeMap.put(marker, newCafe);

                        Toast.makeText(requireContext(), "Café ajouté !", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData(); // Stocker l'image
            if (selectedImageViewGlobal != null) {
                selectedImageViewGlobal.setImageURI(selectedImageUri); // Afficher dans l'ImageView
            }
        }
    }

}
