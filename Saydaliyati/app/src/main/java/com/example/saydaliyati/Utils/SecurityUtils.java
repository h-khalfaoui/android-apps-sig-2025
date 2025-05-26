package com.example.saydaliyati.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Classe utilitaire pour les opérations de sécurité.
 */
public class SecurityUtils {
    private static final String TAG = "SecurityUtils";
    private static final String PREFS_FILE = "auth_prefs";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EXPIRY = "token_expiry";

    /**
     * Génère un hash SHA-256 d'un mot de passe
     * @param password Le mot de passe en texte brut à hacher
     * @return Hash encodé en Base64
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Erreur de hachage du mot de passe", e);
            return null;
        }
    }

    /**
     * Vérifie si un mot de passe correspond au hash stocké
     * @param password Le mot de passe en texte brut à vérifier
     * @param storedHash Le hash stocké pour comparaison
     * @return true si le mot de passe correspond au hash
     */
    public static boolean verifyPassword(String password, String storedHash) {
        String passwordHash = hashPassword(password);
        return passwordHash != null && passwordHash.equals(storedHash);
    }

    /**
     * Génère un jeton aléatoire sécurisé pour l'authentification
     * @return Un jeton encodé en Base64
     */
    public static String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    /**
     * Sauvegarde les informations d'authentification
     * @param context Contexte de l'application
     * @param userId ID de l'utilisateur
     * @param username Nom d'utilisateur
     * @param authToken Jeton d'authentification
     * @return true si sauvegardé avec succès
     */
    public static boolean saveAuthInfo(Context context, int userId, String username, String authToken) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);

        // Définir l'expiration à 7 jours à partir de maintenant
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 7);
        Date expiryDate = cal.getTime();
        String expiryString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(expiryDate);

        // Sauvegarder dans les préférences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_AUTH_TOKEN, authToken);
        editor.putString(KEY_EXPIRY, expiryString);
        return editor.commit(); // Utiliser commit() au lieu de apply() pour obtenir un résultat immédiat
    }

    /**
     * Vérifie si l'utilisateur est authentifié
     * @param context Contexte de l'application
     * @return true si l'authentification est valide
     */
    public static boolean isAuthenticated(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);

        // Vérifier si le jeton existe
        String token = prefs.getString(KEY_AUTH_TOKEN, null);
        if (token == null) {
            return false;
        }

        // Vérifier si le jeton a expiré
        try {
            String expiryString = prefs.getString(KEY_EXPIRY, null);
            if (expiryString == null) {
                return false;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            Date expiryDate = sdf.parse(expiryString);
            Date now = new Date();

            return expiryDate != null && expiryDate.after(now);
        } catch (Exception e) {
            Log.e(TAG, "Erreur de vérification de l'expiration du jeton", e);
            return false;
        }
    }

    /**
     * Récupère l'ID de l'utilisateur actuellement authentifié
     * @param context Contexte de l'application
     * @return ID de l'utilisateur ou -1 si non authentifié
     */
    public static int getCurrentUserId(Context context) {
        if (!isAuthenticated(context)) {
            return -1;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_USER_ID, -1);
    }

    /**
     * Récupère le nom d'utilisateur de l'utilisateur actuellement authentifié
     * @param context Contexte de l'application
     * @return Nom d'utilisateur ou null si non authentifié
     */
    public static String getCurrentUsername(Context context) {
        if (!isAuthenticated(context)) {
            return null;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USERNAME, null);
    }

    /**
     * Efface toutes les informations d'authentification (déconnexion)
     * @param context Contexte de l'application
     * @return true si effacé avec succès
     */
    public static boolean clearAuthInfo(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        return editor.commit();
    }
}