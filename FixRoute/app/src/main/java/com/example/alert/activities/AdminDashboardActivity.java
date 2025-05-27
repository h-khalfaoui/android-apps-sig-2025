package com.example.alert.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.alert.R;
import com.example.alert.database.SQLiteHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class AdminDashboardActivity extends AppCompatActivity implements OnMapReadyCallback {

    private LinearLayout claimsContainer;
    private SQLiteHelper dbHelper;
    private GoogleMap mMap;

    @Override
    protected void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);


        claimsContainer = findViewById(R.id.claimsContainer);
        dbHelper = new SQLiteHelper(this);

        loadPendingClaims();

        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> showLogoutConfirmationDialog());

        Button viewClaimsButton = findViewById(R.id.viewClaimsButton);
        viewClaimsButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ViewClaimsActivity.class);
            startActivity(intent);
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.adminMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }



    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Définir une position initiale (exemple : Tanger)
        LatLng initialLocation = new LatLng(35.7806, -5.8136);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 10));

        // Charger les réclamations sur la carte
        loadClaimsOnMap();
    }

    private void loadPendingClaims() {
        // Vide le conteneur avant de le remplir à nouveau
        claimsContainer.removeAllViews();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT location, description, image, status FROM claims WHERE status = 'en attente'", null);

        if (cursor.moveToFirst()) {
            do {
                String location = cursor.getString(0);
                String description = cursor.getString(1);
                byte[] image = cursor.getBlob(2);
                String status = cursor.getString(3);

                // Création de la vue pour chaque réclamation
                LinearLayout claimView = new LinearLayout(this);
                claimView.setOrientation(LinearLayout.VERTICAL);
                claimView.setPadding(16, 16, 16, 16);
                claimView.setBackgroundResource(R.drawable.bg_claim_item);
                claimView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                // Affichage des informations de la réclamation
                TextView locationText = new TextView(this);
                locationText.setText(getString(R.string.location_label) + " " + location);
                locationText.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                locationText.setTypeface(null, android.graphics.Typeface.BOLD);
                locationText.setPadding(0, 0, 0, 8);

                TextView descriptionText = new TextView(this);
                descriptionText.setText(getString(R.string.description_label) + " " + description);
                descriptionText.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                descriptionText.setPadding(0, 0, 0, 8);

                // Affichage de l'image
                ImageView claimImage = new ImageView(this);
                if (image != null && image.length > 0) {
                    claimImage.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
                } else {
                    claimImage.setImageResource(android.R.drawable.ic_menu_report_image);
                }
                claimImage.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        300));  // Limite la hauteur de l'image

                // Ajout des composants à la vue de réclamation
                claimView.addView(locationText);
                claimView.addView(descriptionText);
                claimView.addView(claimImage);

                // Ajout de la réclamation au conteneur
                claimsContainer.addView(claimView);

            } while (cursor.moveToNext());
        } else {
            Toast.makeText(this, "Aucune réclamation en attente", Toast.LENGTH_SHORT).show();
        }

        cursor.close();
        db.close();
    }



    private void changeClaimStatus(int claimId, String newStatus) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "UPDATE claims SET status = ? WHERE id = ?";
        db.execSQL(sql, new Object[]{newStatus, claimId});
        db.close();

        // Rafraîchir l'affichage des réclamations après la mise à jour
        loadPendingClaims();

        Toast.makeText(this, "Réclamation " + newStatus, Toast.LENGTH_SHORT).show();
    }

    private void loadClaimsOnMap() {
        if (mMap == null) {
            Log.e("AdminDashboardActivity", "Map is not ready yet");
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT location, description FROM claims", null);

        if (cursor.moveToFirst()) {
            do {
                String location = cursor.getString(0);
                String description = cursor.getString(1);

                String[] parts = location.split(",");
                if (parts.length == 2) {
                    try {
                        double lat = Double.parseDouble(parts[0].trim());
                        double lng = Double.parseDouble(parts[1].trim());
                        LatLng latLng = new LatLng(lat, lng);
                        mMap.addMarker(new MarkerOptions().position(latLng).title(description));
                    } catch (NumberFormatException e) {
                        Log.e("AdminDashboardActivity", "Error parsing location", e);
                    }
                }

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Déconnexion")
                .setMessage("Voulez-vous vraiment vous déconnecter ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Non", (dialog, which) -> dialog.dismiss())
                .setIcon(R.drawable.ic_logout)
                .show();
    }
}
