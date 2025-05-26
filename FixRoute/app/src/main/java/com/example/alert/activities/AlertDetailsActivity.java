package com.example.alert.activities;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.alert.R;

public class AlertDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Détails de la réclamation");
        toolbar.setNavigationIcon(R.drawable.ic_back);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String location = getIntent().getStringExtra("location");
        String description = getIntent().getStringExtra("description");
        byte[] imageBytes = getIntent().getByteArrayExtra("image");

        TextView locationTextView = findViewById(R.id.locationTextView);
        locationTextView.setText("📍 Localisation : " + location);

        TextView descriptionTextView = findViewById(R.id.descriptionTextView);
        descriptionTextView.setText("📝 Description : " + description);

        ImageView imageView = findViewById(R.id.alertImageView);
        if (imageBytes != null) {
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_report_image);  // Image par défaut si pas d'image
        }
    }
}
