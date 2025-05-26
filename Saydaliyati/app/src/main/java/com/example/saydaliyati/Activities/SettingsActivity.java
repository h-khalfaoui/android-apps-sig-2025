package com.example.saydaliyati.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.saydaliyati.R;
import com.example.saydaliyati.Utils.LanguageUtils;
import com.example.saydaliyati.Utils.ManualStrings;
import com.example.saydaliyati.Utils.NotificationUtils;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends BaseActivity {

    private RadioGroup languageRadioGroup;
    private RadioButton frenchRadioButton;
    private RadioButton arabicRadioButton;
    private SwitchMaterial notificationsSwitch;
    private SwitchMaterial dutyPharmacyNotificationsSwitch;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Appliquer la langue avant de charger le layout
        LanguageUtils.applyLanguage(this);

        setContentView(R.layout.activity_settings);

        // Apply manual translations to all text views
        applyManualTranslations();

        // Configurer la barre d'action
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings);

            // Also apply the Arabic title if in Arabic mode
            if (LanguageUtils.LANGUAGE_ARABIC.equals(LanguageUtils.getCurrentLanguage(this))) {
                getSupportActionBar().setTitle(ManualStrings.getArabicString("settings"));
            }
        }

        // Initialiser les vues
        initializeViews();

        // Apply Arabic texts manually if needed
        applyManualTexts();

        // Charger les préférences actuelles
        loadCurrentSettings();

        // Configurer les écouteurs d'événements
        setupEventListeners();
    }

    private void initializeViews() {
        languageRadioGroup = findViewById(R.id.languageRadioGroup);
        frenchRadioButton = findViewById(R.id.frenchRadioButton);
        arabicRadioButton = findViewById(R.id.arabicRadioButton);
        notificationsSwitch = findViewById(R.id.notificationsSwitch);
        dutyPharmacyNotificationsSwitch = findViewById(R.id.dutyPharmacyNotificationsSwitch);
        saveButton = findViewById(R.id.saveButton);
    }

    private void applyManualTexts() {
        // Apply Arabic texts manually if needed
        if (LanguageUtils.LANGUAGE_ARABIC.equals(LanguageUtils.getCurrentLanguage(this))) {
            // Find TextViews by their IDs - assuming these IDs exist in your layout
            // If these IDs don't match your layout, replace them with the correct ones

            // Try to find language section title
            int languageLabelId = getResources().getIdentifier("languageLabel", "id", getPackageName());
            if (languageLabelId != 0) {
                TextView languageLabel = findViewById(languageLabelId);
                if (languageLabel != null) {
                    languageLabel.setText(ManualStrings.getArabicString("language_settings"));
                }
            }

            // Radio buttons
            if (frenchRadioButton != null) {
                frenchRadioButton.setText(ManualStrings.getArabicString("french"));
            }

            if (arabicRadioButton != null) {
                arabicRadioButton.setText(ManualStrings.getArabicString("arabic"));
            }

            // Find notifications section title
            int notificationsLabelId = getResources().getIdentifier("notificationsLabel", "id", getPackageName());
            if (notificationsLabelId != 0) {
                TextView notificationsLabel = findViewById(notificationsLabelId);
                if (notificationsLabel != null) {
                    notificationsLabel.setText(ManualStrings.getArabicString("notifications"));
                }
            }

            // Notification switches
            if (notificationsSwitch != null) {
                notificationsSwitch.setText(ManualStrings.getArabicString("enable_notifications"));
            }

            if (dutyPharmacyNotificationsSwitch != null) {
                dutyPharmacyNotificationsSwitch.setText(ManualStrings.getArabicString("duty_pharmacy_notifications"));
            }

            // Save button
            if (saveButton != null) {
                saveButton.setText(ManualStrings.getArabicString("save"));
            }
        }
    }

    private void loadCurrentSettings() {
        // Charger la langue
        String currentLanguage = LanguageUtils.getCurrentLanguage(this);
        if (LanguageUtils.LANGUAGE_FRENCH.equals(currentLanguage)) {
            frenchRadioButton.setChecked(true);
        } else if (LanguageUtils.LANGUAGE_ARABIC.equals(currentLanguage)) {
            arabicRadioButton.setChecked(true);
        }

        // Charger les préférences de notification
        boolean notificationsEnabled = NotificationUtils.areNotificationsEnabled(this);
        notificationsSwitch.setChecked(notificationsEnabled);

        boolean dutyNotificationsEnabled = NotificationUtils.areDutyPharmacyNotificationsEnabled(this);
        dutyPharmacyNotificationsSwitch.setChecked(dutyNotificationsEnabled);
        dutyPharmacyNotificationsSwitch.setEnabled(notificationsEnabled);
    }

    private void setupEventListeners() {
        // État des notifications principales change l'état des notifications secondaires
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dutyPharmacyNotificationsSwitch.setEnabled(isChecked);
            if (!isChecked) {
                dutyPharmacyNotificationsSwitch.setChecked(false);
            }
        });

        // Bouton Enregistrer
        saveButton.setOnClickListener(v -> saveSettings());
    }

    // Dans la méthode saveSettings() de SettingsActivity.java
    private void saveSettings() {
        // Enregistrer la langue
        String languageCode;
        if (arabicRadioButton.isChecked()) {
            languageCode = LanguageUtils.LANGUAGE_ARABIC;
        } else {
            languageCode = LanguageUtils.LANGUAGE_FRENCH;
        }

        String oldLanguage = LanguageUtils.getCurrentLanguage(this);
        Log.d("LanguageDebug", "SettingsActivity.saveSettings: current language before change: " + oldLanguage);
        Log.d("LanguageDebug", "SettingsActivity.saveSettings: changing to language: " + languageCode);

        boolean languageChanged = !oldLanguage.equals(languageCode);

        // Enregistrer les paramètres
        LanguageUtils.setLanguage(this, languageCode);
        Log.d("LanguageDebug", "SettingsActivity.saveSettings: after setLanguage, new current language: " +
                LanguageUtils.getCurrentLanguage(this));
        NotificationUtils.setNotificationsEnabled(this, notificationsSwitch.isChecked());
        NotificationUtils.setDutyPharmacyNotificationsEnabled(this, dutyPharmacyNotificationsSwitch.isChecked());

        // Show a toast message in the correct language
        String toastMessage;
        if (LanguageUtils.LANGUAGE_ARABIC.equals(languageCode)) {
            toastMessage = ManualStrings.getArabicString("settings_saved");
        } else {
            toastMessage = getString(R.string.settings_saved);
        }
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();

        // Si la langue a changé, redémarrer complètement l'application
        if (languageChanged) {
            Log.d("LanguageDebug", "SettingsActivity.saveSettings: language changed, restarting app");
            // Approche alternative qui redémarre l'application sans tuer le processus
            Intent intent = getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        } else {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}