

package com.example.geotourisme;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.geotourisme.ViewModel.SiteViewModel;
import com.example.geotourisme.adapter.SiteListAdapter;
import com.example.geotourisme.databinding.ActivityMainBinding;
import com.example.geotourisme.model.Site;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private SiteViewModel siteViewModel;

    private RecyclerView recyclerView;
    private SiteListAdapter siteAdapter;

    private ImageView imagePreview;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private boolean isSelectingLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(getApplicationContext(), getSharedPreferences("osmdroid", MODE_PRIVATE));
        // Demande de permission de localisation
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        siteAdapter = new SiteListAdapter();
        // Ajout du listener pour le FloatingActionButton qui permet d'accéder à l'activité de la liste des sites
        FloatingActionButton fabViewSites = findViewById(R.id.fab_view_sites);
        fabViewSites.setOnClickListener(view -> {
            // Démarrer l'activité pour voir la liste des sites
            Intent intent = new Intent(MainActivity.this, SitesListActivity.class);
            startActivity(intent);
        });



        FloatingActionButton fabAddSite = findViewById(R.id.fab_add_site);
        fabAddSite.setOnClickListener(view -> showAddSiteDialog());

        siteViewModel = new ViewModelProvider(this).get(SiteViewModel.class);
        siteViewModel.getAllSites().observe(this, sites -> {
            siteAdapter.setSites(sites);
            // Ajouter les sites dans le HorizontalScrollView
            LinearLayout galleryContainer = findViewById(R.id.galleryContainer);
            galleryContainer.removeAllViews(); // Vider le conteneur avant d'ajouter de nouveaux sites
            Set<String> coordSet = new HashSet<>();

            for (Site site : sites) {
                // Arrondir latitude et longitude à 3 chiffres après la virgule
                double roundedLat = Math.round(site.getLatitude() * 1000.0) / 1000.0;
                double roundedLon = Math.round(site.getLongitude() * 1000.0) / 1000.0;
                String coordKey = roundedLat + "," + roundedLon;

                // Vérifier si les coordonnées existent déjà
                if (coordSet.contains(coordKey)) {
                    continue; // Ne pas ajouter de doublon
                }

                coordSet.add(coordKey); // Ajouter les coordonnées uniques

                // Ensuite, créer et ajouter la CardView normalement...
                CardView cardView = new CardView(MainActivity.this);
                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
                cardParams.setMargins(16, 0, 16, 0);
                cardView.setLayoutParams(cardParams);
                cardView.setRadius(12);
                cardView.setCardElevation(6);
                //cardView.setCardBackgroundColor(getResources().getColor(android.R.color.white)); // Arrière-plan blanc
                cardView.setOnClickListener(v -> {
                    // Récupérer l'ImageView existant dans la CardView
                    ImageView imageView = (ImageView) ((ViewGroup) cardView.getChildAt(0)).getChildAt(0);

                    Drawable drawable = imageView.getDrawable();
                    if (drawable instanceof BitmapDrawable) {
                        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                        // Convertir le Bitmap en byte array
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] byteArray = stream.toByteArray();

                        // Lancer l'activité de détail
                        Intent intent = new Intent(MainActivity.this, SiteDetailActivity.class);
                        intent.putExtra("nom", site.getNom_site());
                        intent.putExtra("description", site.getDescription());
                        intent.putExtra("localisation", site.getLocalisation());
                        intent.putExtra("latitude", site.getLatitude());
                        intent.putExtra("longitude", site.getLongitude());
                        intent.putExtra("imageBitmap", byteArray);
                        intent.putExtra("id",site.getId_site());
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "Image non disponible", Toast.LENGTH_SHORT).show();
                    }
                });


                LinearLayout cardContent = new LinearLayout(MainActivity.this);
                cardContent.setOrientation(LinearLayout.VERTICAL);
                cardContent.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));

                ImageView imageView = new ImageView(MainActivity.this);
                imageView.setLayoutParams(new ViewGroup.LayoutParams(450, 400));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                Glide.with(MainActivity.this)
                        .load(site.getImageUrl())
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(imageView);

                TextView descriptionTextView = new TextView(MainActivity.this);
                descriptionTextView.setText(site.getDescription());
                descriptionTextView.setPadding(8, 8, 8, 8);
                descriptionTextView.setTextColor(getResources().getColor(android.R.color.black));
                descriptionTextView.setTextSize(16);

                cardContent.addView(imageView);
                cardContent.addView(descriptionTextView);
                cardView.addView(cardContent);
                galleryContainer.addView(cardView);
            }


        });
        TextInputEditText searchInput = findViewById(R.id.searchInput);


        siteViewModel.getAllSites().observe(this, sites -> {

            searchInput.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String searchQuery = searchInput.getText().toString().trim();
                    if (!searchQuery.isEmpty()) {
                        boolean found = false;
                        for (Site site : sites) {
                            if (site.getNom_site().equalsIgnoreCase(searchQuery)) {
                                Intent intent = new Intent(MainActivity.this, SiteDetailActivity.class);
                                intent.putExtra("nom", site.getNom_site());
                                intent.putExtra("description", site.getDescription());
                                intent.putExtra("localisation", site.getLocalisation());
                                intent.putExtra("latitude", site.getLatitude());
                                intent.putExtra("longitude", site.getLongitude());
                                intent.putExtra("imageBitmap", site.getImageUrl());
                                intent.putExtra("id", site.getId_site());
                                startActivity(intent);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            Toast.makeText(MainActivity.this, "Aucun site trouvé avec ce nom.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Veuillez entrer un nom de site.", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            });
        });

        setSupportActionBar(binding.appBarMain.toolbar);


        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_sites)
//                .setOpenableLayout(drawer)
                .build();
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);


        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

    }





    private void showAddSiteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_site_dialog, null);
        builder.setView(dialogView);
        builder.setTitle("Ajouter un site");

        // Initialize all views
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

        // Configure the map
        MapView mapInDialog = new MapView(this);
        mapInDialog.setTileSource(TileSourceFactory.MAPNIK);
        mapInDialog.setMultiTouchControls(true);
        mapInDialog.getController().setZoom(12.0);
        mapInDialog.getController().setCenter(new GeoPoint(35.774, -5.819)); // Tangier
        mapContainer.addView(mapInDialog);
        mapContainer.setVisibility(View.GONE);

        // Setup site type dropdown
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.site_types,
                android.R.layout.simple_dropdown_item_1line
        );
        typeSiteInput.setAdapter(adapter);

        // Gesture Detector for long press
        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                if (isSelectingLocation) {
                    Projection proj = mapInDialog.getProjection();
                    GeoPoint loc = (GeoPoint) proj.fromPixels((int) e.getX(), (int) e.getY());

                    // Update coordinates
                    latitudeInput.setText(String.valueOf(loc.getLatitude()));
                    longitudeInput.setText(String.valueOf(loc.getLongitude()));

                    // Clear previous markers and add new one
                    mapInDialog.getOverlays().clear();
                    Marker marker = new Marker(mapInDialog);
                    marker.setPosition(loc);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    mapInDialog.getOverlays().add(marker);
                    mapInDialog.invalidate();

                    Toast.makeText(MainActivity.this, "Emplacement sélectionné", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Map touch listener
        mapInDialog.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false; // Allow normal map interaction
        });

        // Image picker handler
        btnChooseImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // Current location button
        btnLocaliser.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

        // Map selection button - Modified for reselection
        btnSelectLocation.setOnClickListener(v -> {
            mapContainer.setVisibility(View.VISIBLE);
            isSelectingLocation = true;

            // Clear previous selection when choosing new location
            mapInDialog.getOverlays().clear();
            latitudeInput.getText().clear();
            longitudeInput.getText().clear();

            Toast.makeText(this, "Appuyez longuement pour sélectionner\n(Cliquez à nouveau pour changer)", Toast.LENGTH_LONG).show();
        });

        // Dialog buttons
        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            // Validation and save logic
            String type = typeSiteInput.getText().toString().trim();
            String nom = nomSiteInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            String localisation = localisationInput.getText().toString().trim();
            double latitude = Double.parseDouble(latitudeInput.getText().toString());
            double longitude = Double.parseDouble(longitudeInput.getText().toString());
            String nature = natureReliefInput.getText().toString().trim();
            String imageUriString = (selectedImageUri != null) ? selectedImageUri.toString() : null;

            Site nouveauSite = new Site(nom, description, type, localisation, latitude, longitude, nature, imageUriString);
            siteViewModel.insert(nouveauSite);
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());

        // Show dialog and manage map lifecycle
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(dialogInterface -> {
            mapInDialog.onPause();
            mapContainer.removeAllViews();
        });
        dialog.show();
        mapInDialog.onResume();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            performLogout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void performLogout() {
        // Clear authentication state
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
        startActivity(new Intent(this, signUpActivity.class));
        finish();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }
}
