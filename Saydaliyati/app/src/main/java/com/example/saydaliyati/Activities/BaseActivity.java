package com.example.saydaliyati.Activities;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.saydaliyati.R;
import com.example.saydaliyati.Utils.LanguageUtils;
import com.example.saydaliyati.Utils.ManualStrings;

import java.lang.reflect.Field;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        String languageCode = LanguageUtils.getCurrentLanguage(newBase);
        Context context = LanguageUtils.forceLocaleResources(newBase, languageCode);
        super.attachBaseContext(context);

        // Log pour le débogage
        String testString = context.getString(R.string.get_started);
        Log.d("LanguageTest", "attachBaseContext test: " + testString);
    }
    protected void applyManualTranslations() {
        if (LanguageUtils.LANGUAGE_ARABIC.equals(LanguageUtils.getCurrentLanguage(this))) {
            // Get the root view of the activity
            View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
            // Apply translations to all text views recursively
            applyArabicToTextViews(rootView);
            Log.d("LanguageDebug", "Applied manual translations to all text views");
        }}
    private void applyArabicToTextViews(View view) {
        // Skip if view is null
        if (view == null) return;

        // Log for debugging
        String viewId = "unknown";
        try {
            if (view.getId() != View.NO_ID) {
                viewId = getResources().getResourceEntryName(view.getId());
            }
        } catch (Exception e) {
            Log.e("LanguageDebug", "Error getting resource name", e);
        }

        Log.d("LanguageDebug", "Processing view: " + viewId + " of type " + view.getClass().getSimpleName());

        // Handle ViewGroup (layout containers)
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            // Process all children
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                applyArabicToTextViews(viewGroup.getChildAt(i));
            }
        }
        // Handle all text-based views
        else if (view instanceof TextView) {
            TextView textView = (TextView) view;
            CharSequence text = textView.getText();

            if (text != null && !text.toString().isEmpty()) {
                Log.d("LanguageDebug", "Found TextView: " + viewId + " with text: " + text);

                // Method 1: Try to match by view ID
                if (view.getId() != View.NO_ID) {
                    String resourceName = getResources().getResourceEntryName(view.getId());
                    if (ManualStrings.hasArabicString(resourceName)) {
                        textView.setText(ManualStrings.getArabicString(resourceName));
                        Log.d("LanguageDebug", "Translated by ID: " + resourceName);
                        return;
                    }
                }

                // Method 2: Try to find matching string in resources
                try {
                    String textStr = text.toString();
                    // Find resource ID by comparing with known strings
                    for (Field field : R.string.class.getFields()) {
                        int id = field.getInt(null);
                        String resName = field.getName();
                        String resValue = getString(id);

                        if (textStr.equals(resValue) && ManualStrings.hasArabicString(resName)) {
                            textView.setText(ManualStrings.getArabicString(resName));
                            Log.d("LanguageDebug", "Translated by matching: " + resName);
                            return;
                        }
                    }
                } catch (Exception e) {
                    Log.e("LanguageDebug", "Error finding resource match", e);
                }

                // Method 3: Try for common patterns
                if (view instanceof Button) {
                    String textStr = text.toString().toLowerCase();
                    // Common button texts
                    if (textStr.contains("démarrer") || textStr.contains("commencer")) {
                        textView.setText(ManualStrings.getArabicString("get_started"));
                    } else if (textStr.contains("login") || textStr.contains("connexion")) {
                        textView.setText(ManualStrings.getArabicString("authority_login"));
                    } else if (textStr.contains("paramètres") || textStr.contains("réglages")) {
                        textView.setText(ManualStrings.getArabicString("settings"));
                    }
                }

                // Special handling for app description text
                if (viewId.equals("appDescriptionText") ||
                        (text.toString().contains("pharmacies") && text.toString().contains("garde"))) {
                    textView.setText(ManualStrings.getArabicString("app_slogan"));
                    Log.d("LanguageDebug", "Applied app_slogan translation");
                }
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("LanguageDebug", "BaseActivity.onCreate: current locale: " +
                getResources().getConfiguration().locale.getLanguage());

        LanguageUtils.updateResources(this);

        Log.d("LanguageDebug", "BaseActivity.onCreate: after updateResources, locale: " +
                getResources().getConfiguration().locale.getLanguage());

        // Une vérification simple: tester si une chaîne arabe est chargée
        if(LanguageUtils.LANGUAGE_ARABIC.equals(LanguageUtils.getCurrentLanguage(this))) {
            String testString = getString(R.string.app_name);
            Log.d("LanguageDebug", "Arabic test string (app_name): " + testString);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // S'assurer que la langue est appliquée à chaque reprise de l'activité
        LanguageUtils.updateResources(this);
    }
}