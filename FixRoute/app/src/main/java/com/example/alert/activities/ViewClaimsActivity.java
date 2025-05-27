package com.example.alert.activities;

import android.app.AlertDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.alert.R;
import com.example.alert.database.SQLiteHelper;

public class ViewClaimsActivity extends AppCompatActivity {

    private LinearLayout claimsContainer;
    private SQLiteHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_claims);

        claimsContainer = findViewById(R.id.claimsContainer);
        dbHelper = new SQLiteHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Réclamations");
        toolbar.setNavigationIcon(R.drawable.ic_back);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> finish());

        loadClaims();
    }

    private void loadClaims() {
        claimsContainer.removeAllViews();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, location, description, image, status FROM claims", null);

        if (cursor.moveToFirst()) {
            do {
                int claimId = cursor.getInt(0);
                String location = cursor.getString(1);
                String description = cursor.getString(2);
                byte[] image = cursor.getBlob(3);
                String status = cursor.getString(4);

                LinearLayout claimView = new LinearLayout(this);
                claimView.setOrientation(LinearLayout.VERTICAL);
                claimView.setPadding(16, 16, 16, 16);
                claimView.setBackgroundResource(R.drawable.bg_claim_item);
                claimView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                TextView locationText = new TextView(this);
                locationText.setText("📍 Localisation : " + location);
                locationText.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                locationText.setTypeface(null, android.graphics.Typeface.BOLD);
                locationText.setPadding(0, 0, 0, 8);

                TextView descriptionText = new TextView(this);
                descriptionText.setText("📝 Description : " + description);
                descriptionText.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                descriptionText.setPadding(0, 0, 0, 8);

                TextView statusText = new TextView(this);
                statusText.setText("📌 Statut : " + status);
                statusText.setTypeface(null, android.graphics.Typeface.BOLD);
                statusText.setTextColor(getStatusColor(status));
                statusText.setPadding(0, 0, 0, 8);

                ImageView claimImage = new ImageView(this);
                if (image != null && image.length > 0) {
                    claimImage.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
                } else {
                    claimImage.setImageResource(android.R.drawable.ic_menu_report_image);
                }
                claimImage.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        400));  // Limite la hauteur de l'image

                LinearLayout buttonsLayout = new LinearLayout(this);
                buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
                buttonsLayout.setWeightSum(2);

                Button acceptButton = new Button(this);
                acceptButton.setText("Accepter");
                acceptButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
                acceptButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                acceptButton.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                acceptButton.setOnClickListener(v -> showConfirmationDialog(claimId, "acceptée"));

                Button rejectButton = new Button(this);
                rejectButton.setText("Refuser");
                rejectButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
                rejectButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                rejectButton.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                rejectButton.setOnClickListener(v -> showConfirmationDialog(claimId, "refusée"));

                buttonsLayout.addView(acceptButton);
                buttonsLayout.addView(rejectButton);

                claimView.addView(locationText);
                claimView.addView(descriptionText);
                claimView.addView(statusText);
                claimView.addView(claimImage);
                claimView.addView(buttonsLayout);

                claimsContainer.addView(claimView);

            } while (cursor.moveToNext());
        } else {
            Toast.makeText(this, "Aucune réclamation à afficher", Toast.LENGTH_SHORT).show();
        }

        cursor.close();
        db.close();
    }

    private int getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "acceptée":
                return ContextCompat.getColor(this, android.R.color.holo_green_dark);
            case "refusée":
                return ContextCompat.getColor(this, android.R.color.holo_red_dark);
            default:
                return ContextCompat.getColor(this, android.R.color.holo_orange_light);
        }
    }

    private void showConfirmationDialog(int claimId, String status) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmer l'action")
                .setMessage("Voulez-vous vraiment marquer cette réclamation comme " + status + " ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    changeClaimStatus(claimId, status);
                })
                .setNegativeButton("Non", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void changeClaimStatus(int claimId, String status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "UPDATE claims SET status = ? WHERE id = ?";
        db.execSQL(sql, new Object[]{status, claimId});
        db.close();

        Toast.makeText(this, "Réclamation " + status, Toast.LENGTH_SHORT).show();

        // Rafraîchit les réclamations après mise à jour
        loadClaims();
    }
}
