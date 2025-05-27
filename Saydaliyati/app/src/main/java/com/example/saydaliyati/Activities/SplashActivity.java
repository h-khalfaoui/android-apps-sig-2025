package com.example.saydaliyati.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.saydaliyati.Database.AppDatabase;
import com.example.saydaliyati.Database.PharmacyDAO;
import com.example.saydaliyati.Models.Pharmacy;
import com.example.saydaliyati.R;

import org.osmdroid.config.Configuration;

/**
 * Splash screen activity shown on app startup
 */
public class SplashActivity extends AppCompatActivity {

    // UI components
    private ImageView logoImageView;
    private TextView appNameTextView;
    private TextView appDescriptionTextView;

    // Animation duration
    private static final int SPLASH_DURATION = 2500; // milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize OSMDroid configuration (important to do this early)
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        setContentView(R.layout.activity_splash);

        // Initialize views
        logoImageView = findViewById(R.id.splashLogo);
        appNameTextView = findViewById(R.id.appNameText);
        appDescriptionTextView = findViewById(R.id.appDescriptionText);

        // Load animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        // Apply animations
        logoImageView.startAnimation(fadeIn);
        appNameTextView.startAnimation(slideUp);
        appDescriptionTextView.startAnimation(slideUp);

        // Preload database
        preloadDatabaseData();

        // Navigate to main activity after delay
        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToMainActivity, SPLASH_DURATION);
    }

    /**
     * Preload initial database data if needed
     */
    private void preloadDatabaseData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            PharmacyDAO dao = AppDatabase.getInstance(this).pharmacyDAO();

            // Check if data already exists
            if (dao.getAllPharmacies().isEmpty()) {
                // Original pharmacies
                dao.insert(new Pharmacy("Pharmacie Centrale", "Boulevard Mohammed V", 35.7796, -5.8137,
                        "+212 539 931 099", "08:00 - 23:00", true));
                dao.insert(new Pharmacy("Pharmacie Diamant", "Avenue Hassan II", 35.7741, -5.7995,
                        "+212 539 321 456", "24/7", false));
                dao.insert(new Pharmacy("Pharmacie Principale", "Rue de Fès", 35.7694, -5.7962,
                        "+212 539 654 321", "09:00 - 21:00", true));
                dao.insert(new Pharmacy("Pharmacie Ibn Khaldoun", "Avenue Mohammed VI", 35.7791, -5.8081,
                        "+212 539 123 456", "08:00 - 22:00", false));
                dao.insert(new Pharmacy("Pharmacie Al Andalus", "Rue Larache", 35.7699, -5.8224,
                        "+212 539 876 543", "09:00 - 20:00", true));

                // City center pharmacies
                dao.insert(new Pharmacy("Pharmacie Place des Nations", "Place des Nations", 35.7784, -5.8112,
                        "+212 539 334 455", "08:00 - 23:00", true));
                dao.insert(new Pharmacy("Pharmacie Acima", "Centre Commercial Acima", 35.7735, -5.8096,
                        "+212 539 940 505", "09:00 - 22:00", false));
                dao.insert(new Pharmacy("Pharmacie Pasteur", "Boulevard Pasteur", 35.7807, -5.8127,
                        "+212 539 936 025", "24/7", true));
                dao.insert(new Pharmacy("Pharmacie Mabrouk", "Avenue Hassan I", 35.7770, -5.8079,
                        "+212 539 372 873", "08:30 - 21:30", false));
                dao.insert(new Pharmacy("Pharmacie du Port", "Avenue Mohammed VI", 35.7850, -5.8073,
                        "+212 539 325 967", "08:00 - 23:00", true));

                // Malabata area
                dao.insert(new Pharmacy("Pharmacie Malabata", "Avenue Malabata", 35.7938, -5.7989,
                        "+212 539 947 111", "08:00 - 22:00", false));
                dao.insert(new Pharmacy("Pharmacie Tanja Bay", "Complexe Tanja Bay", 35.7953, -5.7962,
                        "+212 539 309 875", "09:00 - 21:00", true));
                dao.insert(new Pharmacy("Pharmacie de la Plage", "Boulevard de la Plage", 35.7918, -5.8003,
                        "+212 539 954 678", "08:00 - 20:00", false));

                // Iberia / Branes
                dao.insert(new Pharmacy("Pharmacie Iberia", "Quartier Iberia", 35.7677, -5.8058,
                        "+212 539 943 265", "08:30 - 22:30", true));
                dao.insert(new Pharmacy("Pharmacie Branes", "Avenue des FAR, Branes", 35.7589, -5.8114,
                        "+212 539 386 554", "09:00 - 21:00", false));
                dao.insert(new Pharmacy("Pharmacie Al Wafa", "Rue Al Wafa, Branes", 35.7602, -5.8092,
                        "+212 539 351 741", "08:00 - 22:00", true));

                // Souani area
                dao.insert(new Pharmacy("Pharmacie Souani", "Avenue Souani", 35.7694, -5.7962,
                        "+212 539 940 120", "08:00 - 23:00", false));
                dao.insert(new Pharmacy("Pharmacie Ibn Batouta", "Rue Ibn Batouta, Souani", 35.7683, -5.7978,
                        "+212 539 941 234", "24/7", true));
                dao.insert(new Pharmacy("Pharmacie Socco Alto", "Centre Commercial Socco Alto", 35.7604, -5.8108,
                        "+212 539 342 987", "10:00 - 22:00", false));

                // Near hospitals
                dao.insert(new Pharmacy("Pharmacie Hôpital Mohammed V", "Av. Mohamed V, près de l'Hôpital", 35.7733, -5.8042,
                        "+212 539 333 741", "24/7", true));
                dao.insert(new Pharmacy("Pharmacie Clinique Assaada", "En face de Clinique Assaada", 35.7668, -5.7954,
                        "+212 539 365 489", "08:00 - 23:00", false));
                dao.insert(new Pharmacy("Pharmacie Centre Hospitalier", "Près du CH Mohammed VI", 35.7600, -5.7899,
                        "+212 539 375 821", "24/7", true));

                // Mesnana area
                dao.insert(new Pharmacy("Pharmacie Mesnana", "Quartier Mesnana", 35.7409, -5.8464,
                        "+212 539 381 943", "08:30 - 21:30", false));
                dao.insert(new Pharmacy("Pharmacie Al Amal", "Route de Rabat, Mesnana", 35.7412, -5.8401,
                        "+212 539 364 529", "09:00 - 22:00", true));

                // Boukhalef area
                dao.insert(new Pharmacy("Pharmacie Boukhalef", "Zone Boukhalef", 35.7325, -5.9043,
                        "+212 539 393 827", "09:00 - 21:00", false));
                dao.insert(new Pharmacy("Pharmacie Aéroport", "Près de l'Aéroport Ibn Batouta", 35.7264, -5.9161,
                        "+212 539 354 217", "07:00 - 23:00", true));

                // Marjane area
                dao.insert(new Pharmacy("Pharmacie Marjane", "Centre Commercial Marjane", 35.7534, -5.7883,
                        "+212 539 309 416", "09:00 - 22:00", false));
                dao.insert(new Pharmacy("Pharmacie Ibn Sina", "Avenue Ibn Sina, près de Marjane", 35.7552, -5.7891,
                        "+212 539 371 528", "08:30 - 22:30", true));

                // Medina
                dao.insert(new Pharmacy("Pharmacie Médina", "Grand Socco, Médina", 35.7882, -5.8123,
                        "+212 539 935 143", "08:00 - 22:00", false));
                dao.insert(new Pharmacy("Pharmacie Bab El Fahs", "Bab El Fahs, Médina", 35.7871, -5.8106,
                        "+212 539 938 652", "09:00 - 21:00", true));
                dao.insert(new Pharmacy("Pharmacie Kasbah", "Près de la Kasbah", 35.7901, -5.8136,
                        "+212 539 934 189", "08:30 - 21:30", false));

                // Luxury areas
                dao.insert(new Pharmacy("Pharmacie Marina Bay", "Marina Bay Tanger", 35.7884, -5.8099,
                        "+212 539 302 145", "09:00 - 23:00", true));
                dao.insert(new Pharmacy("Pharmacie Royal", "Boulevard Mohammed VI", 35.7770, -5.8053,
                        "+212 539 307 963", "24/7", false));

                // University area
                dao.insert(new Pharmacy("Pharmacie Campus", "Près de l'Université Abdelmalek", 35.7636, -5.8548,
                        "+212 539 362 471", "08:00 - 22:00", true));
                dao.insert(new Pharmacy("Pharmacie des Étudiants", "Avenue des FAR, Zone Universitaire", 35.7636, -5.8537,
                        "+212 539 371 459", "09:00 - 21:00", false));
            }
        });
    }
    /**
     * Navigate to the main activity
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Close the splash activity
    }
}