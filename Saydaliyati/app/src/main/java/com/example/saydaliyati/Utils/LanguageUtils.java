package com.example.saydaliyati.Utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import java.util.Locale;

public class LanguageUtils {

    private static final String PREF_NAME = "LanguagePref";
    private static final String KEY_LANGUAGE = "app_language";

    // Codes de langue disponibles
    public static final String LANGUAGE_FRENCH = "fr";
    public static final String LANGUAGE_ARABIC = "ar";

    // Enregistre la langue sélectionnée
    public static void setLanguage(Context context, String languageCode) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_LANGUAGE, languageCode);
        editor.apply();
    }

    public static String getCurrentLanguage(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String language = preferences.getString(KEY_LANGUAGE, LANGUAGE_FRENCH);
        Log.d("LanguageDebug", "getCurrentLanguage: requested language is " + language);
        return language;
    }

    public static Context applyLanguage(Context context) {
        String languageCode = getCurrentLanguage(context);
        Log.d("LanguageDebug", "applyLanguage: applying language " + languageCode);
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Log.d("LanguageDebug", "Using newer API to set locale");
            config.setLocale(locale);
            return context.createConfigurationContext(config);
        } else {
            Log.d("LanguageDebug", "Using legacy API to set locale");
            config.locale = locale;
            resources.updateConfiguration(config, resources.getDisplayMetrics());
            return context;
        }
    }

    public static void updateResources(Context context) {
        String languageCode = getCurrentLanguage(context);
        Log.d("LanguageDebug", "updateResources: updating resources with language " + languageCode);
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }

        resources.updateConfiguration(config, resources.getDisplayMetrics());

        // Vérifiez si les ressources ont été correctement mises à jour
        Configuration updatedConfig = resources.getConfiguration();
        Locale updatedLocale = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updatedLocale = updatedConfig.getLocales().get(0);
        } else {
            updatedLocale = updatedConfig.locale;
        }
        Log.d("LanguageDebug", "After updateResources: resources locale is " + updatedLocale.getLanguage());
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResourcesLocale(Context context, Locale locale) {
        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale); // Important pour RTL
        return context.createConfigurationContext(configuration);
    }

    @SuppressWarnings("deprecation")
    private static Context updateResourcesLocaleLegacy(Context context, Locale locale) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale); // Important pour RTL
        }

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        return context;
    }
    /**
     * Force le chargement des ressources pour la langue spécifiée
     */
    public static Context forceLocaleResources(Context context, String languageCode) {
        // Créer la configuration de locale
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        // Obtenir les ressources actuelles
        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());

        // Configuration explicite
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }

        // Forcer la mise à jour des ressources
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        // Créer un contexte avec cette configuration
        return context.createConfigurationContext(config);
    }
    public static String getString(Context context, int resourceId) {
        String key = context.getResources().getResourceEntryName(resourceId);
        String lang = getCurrentLanguage(context);

        if (LANGUAGE_ARABIC.equals(lang) && ManualStrings.hasArabicString(key)) {
            return ManualStrings.getArabicString(key);
        } else {
            return context.getString(resourceId);
        }
    }

}