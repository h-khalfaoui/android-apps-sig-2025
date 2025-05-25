package com.example.quietspaceeee.data.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.quietspaceeee.R;
import com.example.quietspaceeee.data.db.CafeRepository;
import com.example.quietspaceeee.data.model.Cafe;
import com.example.quietspaceeee.data.view.CafeEditActivity;

import java.io.File;


public class CafeDetailActivity1 extends Activity {

    ImageView cafeImage;
    TextView cafeName, cafeCity, cafeLocation, cafeDescription, cafeType, cafeEquipments, cafeNoise, cafeAvailability, cafeCost;
    Button btnReserve;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cafe_detail1);



        // Initialisation des vues
        cafeImage = findViewById(R.id.cafeImage);
        cafeName = findViewById(R.id.cafeName);
        cafeCity = findViewById(R.id.cafeCity);
        cafeLocation = findViewById(R.id.cafeLocation);
        cafeDescription = findViewById(R.id.cafeDescription);
        cafeType = findViewById(R.id.cafeType);
        cafeEquipments = findViewById(R.id.cafeEquipments);
        cafeNoise = findViewById(R.id.cafeNoise);
        cafeAvailability = findViewById(R.id.cafeAvailability);
        cafeCost = findViewById(R.id.cafeCost);
        Button btnEdit = findViewById(R.id.btnEdit);
        Button btnDelete = findViewById(R.id.btnDelete);




        // Récupérer l'ID du café passé par l'intent
        int cafeId = getIntent().getIntExtra("cafeId", -1);
        if (cafeId != -1) {
            CafeRepository repository = new CafeRepository(this);
            Cafe cafe = repository.getCafeById(cafeId);
            if (cafe != null) {
                // Affichage des données
                cafeName.setText(cafe.getName());
                cafeCity.setText("Ville : " + cafe.getCity());
                cafeLocation.setText("Adresse : " + cafe.getLocation());
                cafeDescription.setText(cafe.getDescription());
                cafeType.setText("Type : " + cafe.getType());
                cafeEquipments.setText("Équipements : " + cafe.getEquipments());
                cafeNoise.setText("Niveau sonore : " + cafe.getNoiseLevel());
                cafeAvailability.setText("Disponibilité : " + cafe.getAvailability());

                Glide.with(this)
                        .load(cafe.getImageUrl())
                        .placeholder(R.drawable.ic_cafe) // image par défaut si l'URL est nulle ou vide
                        .into(cafeImage);

            }
        }
// Modifier
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(CafeDetailActivity1.this, CafeEditActivity.class);
            intent.putExtra("cafeId", cafeId);
            startActivityForResult(intent, 1);
        });

// Supprimer
        btnDelete.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Confirmer la suppression")
                    .setMessage("Êtes-vous sûr de vouloir supprimer ce café ?")
                    .setPositiveButton("Oui", (dialog, which) -> {
                        CafeRepository repo = new CafeRepository(this);
                        repo.deleteCafeById(cafeId);
                        setResult(RESULT_OK);  // Signale qu'il faut mettre à jour la liste
                        finish(); // Fermer l'activité et revenir à la liste
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Recharger les données du café
            int cafeId = getIntent().getIntExtra("cafeId", -1);
            if (cafeId != -1) {
                CafeRepository repository = new CafeRepository(this);
                Cafe cafe = repository.getCafeById(cafeId);
                if (cafe != null) {
                    cafeName.setText(cafe.getName());
                    cafeCity.setText("Ville : " + cafe.getCity());
                    cafeLocation.setText("Adresse : " + cafe.getLocation());
                    cafeDescription.setText(cafe.getDescription());
                    cafeType.setText("Type : " + cafe.getType());
                    cafeEquipments.setText("Équipements : " + cafe.getEquipments());
                    cafeNoise.setText("Niveau sonore : " + cafe.getNoiseLevel());
                    cafeAvailability.setText("Disponibilité : " + cafe.getAvailability());
                }
            }
        }
    }
}