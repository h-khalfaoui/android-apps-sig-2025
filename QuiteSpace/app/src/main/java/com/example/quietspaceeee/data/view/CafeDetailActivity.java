package com.example.quietspaceeee.data.view;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.quietspaceeee.R;

import java.io.File;

import com.example.quietspaceeee.data.db.CafeRepository;
import com.example.quietspaceeee.data.model.Cafe;

public class CafeDetailActivity extends Activity {

    ImageView cafeImage;
    TextView cafeName, cafeCity, cafeLocation, cafeDescription, cafeType, cafeEquipments, cafeNoise, cafeAvailability, cafeCost;
    Button btnReserve;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cafe_detail);



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
        btnReserve = findViewById(R.id.btnReserve);

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

                // Affichage de l'image (si disponible)
                Glide.with(this)
                        .load(cafe.getImageUrl())
                        .placeholder(R.drawable.ic_cafe) // image par défaut si l'URL est nulle ou vide
                        .into(cafeImage);
            }
        }

        btnReserve = findViewById(R.id.btnReserve);
        btnReserve.setOnClickListener(v -> {
            Intent intent = new Intent(CafeDetailActivity.this, CafeReservationActivity.class);
            intent.putExtra("cafeName", cafeName.getText().toString());
            SharedPreferences prefs = getSharedPreferences("QuietSpacePrefs", MODE_PRIVATE);
            String userEmail = prefs.getString("userEmail", null);

            startActivity(intent);
        });




    }
}