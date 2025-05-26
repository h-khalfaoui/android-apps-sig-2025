package com.example.geotourisme;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geotourisme.ViewModel.SiteViewModel;
import com.example.geotourisme.adapter.SiteListAdapter;
import com.example.geotourisme.model.Site;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;

import java.util.List;

public class SitesListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SiteListAdapter siteAdapter;
    private SiteViewModel siteViewModel;
    private ImageView imagePreview;
    private Uri selectedImageUri;
    private boolean isSelectingLocation = false;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    // private OnImagePickedListener imagePickedListener;
    interface OnImagePickedListener {
        void onImagePicked(Uri uri);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sites_list); // Assure-toi d'avoir créé ce layout
        // Initialisation de la Toolbar

        recyclerView = findViewById(R.id.siteRecyclerView);
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (imagePreview != null) {
                            imagePreview.setImageURI(selectedImageUri);
                        }
                    }
                });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        siteAdapter = new SiteListAdapter(); // Crée un adapter qui prendra en charge l'affichage des sites
        recyclerView.setAdapter(siteAdapter);

        siteViewModel = new ViewModelProvider(this).get(SiteViewModel.class);

        siteViewModel.getAllSites().observe(this, new Observer<List<Site>>() {
            @Override
            public void onChanged(List<Site> sites) {
                siteAdapter.setSites(sites); // Met à jour la liste des sites dans l'adapter
            }
        });

        siteAdapter.setOnItemClickListener(new SiteListAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Site site) {
                // ➔ Modifier (exemple: augmenter salaire de 1000)
//                employee.setSalary(employee.getSalary() + 1000);
//                employeeViewModel.update(employee);
                showEditSiteDialog(site);

            }

            @Override
            public void onDeleteClick(Site site) {
                // ➔ Supprimer
                siteViewModel.delete(site);
            }
        });

    }
//    @Override
//    public boolean onCreateOptionsMenu(android.view.Menu menu) {
//        // Ne pas afficher de menu ici
//        return false;
//    }

    private void showEditSiteDialog(Site site) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_site_dialog, null);
        builder.setView(dialogView);
        builder.setTitle("Modifier le site");
        final Uri[] currentImageUri = {null};

        // Initialisation des vues
        imagePreview = dialogView.findViewById(R.id.image_preview);
        Button btnChooseImage = dialogView.findViewById(R.id.btn_choisir_image);
        EditText nomSiteInput = dialogView.findViewById(R.id.edit_nom_site);
        EditText descriptionInput = dialogView.findViewById(R.id.edit_description);
        MaterialAutoCompleteTextView typeSiteInput = dialogView.findViewById(R.id.auto_complete_type_site);
        EditText localisationInput = dialogView.findViewById(R.id.edit_localisation);
        EditText latitudeInput = dialogView.findViewById(R.id.edit_latitude);
        EditText longitudeInput = dialogView.findViewById(R.id.edit_longitude);
        EditText natureReliefInput = dialogView.findViewById(R.id.edit_nature_relief);
        Button btnLocaliser = dialogView.findViewById(R.id.btnLocaliser);
        Button btnSelectLocation = dialogView.findViewById(R.id.btn_select_location);
        FrameLayout mapContainer = dialogView.findViewById(R.id.map_container);

        // Pré-remplir les champs avec les données du site
        nomSiteInput.setText(site.getNom_site());
        descriptionInput.setText(site.getDescription());
        typeSiteInput.setText(site.getType_site());
        localisationInput.setText(site.getLocalisation());
        latitudeInput.setText(String.valueOf(site.getLatitude()));
        longitudeInput.setText(String.valueOf(site.getLongitude()));
        natureReliefInput.setText(site.getNature_relief());

        // Charger l'image si elle existe
        if (site.getImageUrl() != null) {
            selectedImageUri = Uri.parse(site.getImageUrl());
            currentImageUri[0] = Uri.parse(site.getImageUrl());
            imagePreview.setImageURI(selectedImageUri);
        }

        // Configuration de la carte
        MapView mapInDialog = new MapView(this);
        mapInDialog.setTileSource(TileSourceFactory.MAPNIK);
        mapInDialog.setMultiTouchControls(true);
        GeoPoint initialPoint = new GeoPoint(site.getLatitude(), site.getLongitude());
        mapInDialog.getController().setZoom(12.0);
        mapInDialog.getController().setCenter(initialPoint);
        mapContainer.addView(mapInDialog);
        mapContainer.setVisibility(View.GONE);

        // Ajouter le marqueur existant
        Marker marker = new Marker(mapInDialog);
        marker.setPosition(initialPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapInDialog.getOverlays().add(marker);

        // Dropdown type
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.site_types,
                android.R.layout.simple_dropdown_item_1line
        );
        typeSiteInput.setAdapter(adapter);

        // Geste long clic pour sélection de localisation
        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                if (isSelectingLocation) {
                    Projection proj = mapInDialog.getProjection();
                    GeoPoint loc = (GeoPoint) proj.fromPixels((int) e.getX(), (int) e.getY());
                    latitudeInput.setText(String.valueOf(loc.getLatitude()));
                    longitudeInput.setText(String.valueOf(loc.getLongitude()));
                    mapInDialog.getOverlays().clear();
                    Marker newMarker = new Marker(mapInDialog);
                    newMarker.setPosition(loc);
                    newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    mapInDialog.getOverlays().add(newMarker);
                    mapInDialog.invalidate();

                    Toast.makeText(SitesListActivity.this, "Emplacement sélectionné", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mapInDialog.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false;
        });

        btnChooseImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        btnLocaliser.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            } else {
                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        latitudeInput.setText(String.valueOf(location.getLatitude()));
                        longitudeInput.setText(String.valueOf(location.getLongitude()));
                    } else {
                        Toast.makeText(this, "Localisation introuvable", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btnSelectLocation.setOnClickListener(v -> {
            mapContainer.setVisibility(View.VISIBLE);
            isSelectingLocation = true;
            mapInDialog.getOverlays().clear();
            latitudeInput.getText().clear();
            longitudeInput.getText().clear();
            Toast.makeText(this, "Appuyez longuement pour sélectionner\n(Cliquez à nouveau pour changer)", Toast.LENGTH_LONG).show();
        });

        builder.setPositiveButton("Enregistrer", (dialog, which) -> {
            String nom = nomSiteInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            String type = typeSiteInput.getText().toString().trim();
            String localisation = localisationInput.getText().toString().trim();
            double latitude = Double.parseDouble(latitudeInput.getText().toString());
            double longitude = Double.parseDouble(longitudeInput.getText().toString());
            String nature = natureReliefInput.getText().toString().trim();
            String imageUriString = (selectedImageUri != null) ? selectedImageUri.toString() : null;

            // Mise à jour de l'objet site
            site.setNom_site(nom);
            site.setDescription(description);
            site.setType_site(type);
            site.setLocalisation(localisation);
            site.setLatitude(latitude);
            site.setLongitude(longitude);
            site.setNature_relief(nature);
            site.setImageUrl(imageUriString);

            siteViewModel.update(site);
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(dialogInterface -> {
            mapInDialog.onPause();
            mapContainer.removeAllViews();
        });
        dialog.show();
        mapInDialog.onResume();
    }
//    private void performLogout() {
//        // Clear authentication state
//        getSharedPreferences("app_prefs", MODE_PRIVATE)
//                .edit()
//                .clear()
//                .apply();
//        startActivity(new Intent(this, signUpActivity.class));
//        finish();
//    }
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
//        if (id == R.id.action_logout) {
//            performLogout();
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

}



//public class SitesListActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_sites_list);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//    }
//}