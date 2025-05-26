package com.example.saydaliyati.Activities;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.saydaliyati.Database.AppDatabase;
import com.example.saydaliyati.Database.PharmacyDAO;
import com.example.saydaliyati.Models.Pharmacy;
import com.example.saydaliyati.R;
import com.example.saydaliyati.Utils.LanguageUtils;
import com.example.saydaliyati.Utils.ManualStrings;
import com.example.saydaliyati.Utils.NotificationUtils;
import com.example.saydaliyati.Utils.SecurityUtils;

import org.osmdroid.config.Configuration;

public class MainActivity extends BaseActivity {

    private Button startButton;
    private Button authorityLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("LanguageDebug", "MainActivity.onCreate: before super.onCreate");
        super.onCreate(savedInstanceState);
        Log.d("LanguageDebug", "MainActivity.onCreate: after super.onCreate, locale: " +
                getResources().getConfiguration().locale.getLanguage());

        // Initialiser OSMDroid
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        // Layout de bienvenue
        setContentView(R.layout.activity_main);


        applyManualTranslations();

        // Vérifier si la langue actuelle est l'arabe
        boolean isArabic = LanguageUtils.LANGUAGE_ARABIC.equals(
                LanguageUtils.getCurrentLanguage(this));

        // Initialiser les boutons
        startButton = findViewById(R.id.startButton);
        authorityLoginButton = findViewById(R.id.authorityLoginButton);
        Button settingsButton = findViewById(R.id.settingsButton);
        Button quickAdminButton = findViewById(R.id.quickAdminButton);

        // Appliquer les textes arabes manuellement si nécessaire
        if (isArabic) {
            startButton.setText(ManualStrings.getArabicString("get_started"));
            authorityLoginButton.setText(ManualStrings.getArabicString("authority_login"));
            settingsButton.setText(ManualStrings.getArabicString("settings"));

            // Titre de l'application
            setTitle(ManualStrings.getArabicString("app_name"));

            // Autres éléments d'interface
            TextView appNameTextView = findViewById(R.id.appNameText);
            if (appNameTextView != null) {
                appNameTextView.setText(ManualStrings.getArabicString("app_name"));
            }

            TextView appDescTextView = findViewById(R.id.appDescriptionText);
            if (appDescTextView != null) {
                appDescTextView.setText(ManualStrings.getArabicString("app_slogan"));
            }

            // Suppression de la référence à versionText qui n'existe pas
        }

        // Log pour débogage
        String startButtonText = isArabic ?
                ManualStrings.getArabicString("get_started") :
                getString(R.string.get_started);
        Log.d("LanguageTest", "Texte du bouton démarrer: " + startButtonText);

        // Lancer carte → PharmacyFinderActivity
        startButton.setOnClickListener(v -> {
            String toastMessage = isArabic ?
                    ManualStrings.getArabicString("opening_map") :
                    getString(R.string.opening_map);
            Toast.makeText(MainActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, PharmacyFinderActivity.class));
        });

        // Lancer login autorité
        authorityLoginButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AuthorityLoginActivity.class));
        });

        // Lancer paramètres
        settingsButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });

        // Affichage rapide vers dashboard si déjà connecté
        if (SecurityUtils.isAuthenticated(this)) {
            quickAdminButton.setVisibility(View.VISIBLE);
            if (isArabic) {
                quickAdminButton.setText(ManualStrings.getArabicString("admin_dashboard"));
            }
            quickAdminButton.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, AuthorityDashboardActivity.class));
            });
        } else {
            quickAdminButton.setVisibility(View.GONE);
        }

        // Préremplir la base si vide (thread)
        AppDatabase.databaseWriteExecutor.execute(() -> {
            PharmacyDAO dao = AppDatabase.getInstance(this).pharmacyDAO();
            if (dao.getAllPharmacies().isEmpty()) {
                dao.insert(new Pharmacy("Pharmacie Tanger Centre", "Boulevard Pasteur", 35.7796, -5.8137,
                        "+212 539 931 099", "08:00 - 23:00", true));
                dao.insert(new Pharmacy("Pharmacie Ibn Batouta", "Avenue Mohamed V", 35.7741, -5.7995,
                        "+212 539 321 456", "24/7", false));
                dao.insert(new Pharmacy("Pharmacie Souani", "Rue de Fès", 35.7694, -5.7962,
                        "+212 539 654 321", "09:00 - 21:00", true));
                dao.insert(new Pharmacy("Pharmacie Corniche", "Avenue Mohammed VI", 35.7791, -5.8081,
                        "+212 539 123 456", "08:00 - 22:00", false));
                dao.insert(new Pharmacy("Pharmacie Drissia", "Route de Rabat", 35.7699, -5.8224,
                        "+212 539 876 543", "09:00 - 20:00", true));
            }
        });

        // Créer le canal de notifications
        NotificationUtils.createNotificationChannel(this);
    }

    // ------------------------------
    // Menu Options (langue + réglages)
    // ------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        if (id == R.id.action_language) {
            showLanguageDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showLanguageDialog() {
        boolean isArabic = LanguageUtils.LANGUAGE_ARABIC.equals(
                LanguageUtils.getCurrentLanguage(this));

        String[] languages = {
                isArabic ? ManualStrings.getArabicString("french") : getString(R.string.french),
                isArabic ? ManualStrings.getArabicString("arabic") : getString(R.string.arabic)
        };

        String dialogTitle = isArabic ?
                ManualStrings.getArabicString("select_language") :
                getString(R.string.select_language);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(dialogTitle)
                .setItems(languages, (dialog, which) -> {
                    String languageCode = (which == 0)
                            ? LanguageUtils.LANGUAGE_FRENCH
                            : LanguageUtils.LANGUAGE_ARABIC;

                    String currentLanguage = LanguageUtils.getCurrentLanguage(this);
                    if (!currentLanguage.equals(languageCode)) {
                        LanguageUtils.setLanguage(this, languageCode);

                        // Redémarrer pour appliquer le changement
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });

        builder.create().show();
    }
}