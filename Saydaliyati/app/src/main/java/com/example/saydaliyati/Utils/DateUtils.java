package com.example.saydaliyati.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Classe utilitaire pour les opérations de date et d'heure
 */
public class DateUtils {

    // Format pour le stockage en base de données (format ISO-8601 pour les dates)
    private static final String DB_DATE_FORMAT = "yyyy-MM-dd";

    // Format pour l'affichage
    private static final String DISPLAY_DATE_FORMAT = "dd MMMM yyyy";

    /**
     * Obtient la date actuelle au format de base de données
     * @return Date actuelle au format yyyy-MM-dd
     */
    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Formate la date pour l'affichage
     * @param dbDate Chaîne de date au format de base de données (yyyy-MM-dd)
     * @return Chaîne de date formatée pour l'affichage (par ex., "25 avril 2025")
     */
    public static String formatForDisplay(String dbDate) {
        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault());
            Date date = dbFormat.parse(dbDate);

            if (date != null) {
                SimpleDateFormat displayFormat = new SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault());
                return displayFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dbDate; // Retourner l'original si l'analyse échoue
    }

    /**
     * Obtient une liste de dates pour les N prochains jours
     * @param days Nombre de jours à générer
     * @return Tableau de chaînes de date au format de base de données
     */
    public static String[] getNextNDays(int days) {
        String[] result = new String[days];
        SimpleDateFormat sdf = new SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < days; i++) {
            result[i] = sdf.format(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return result;
    }

    /**
     * Vérifie si une date est dans le passé
     * @param dateStr Chaîne de date au format de base de données (yyyy-MM-dd)
     * @return true si la date est dans le passé, false sinon
     */
    public static boolean isDateInPast(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault());
            Date date = sdf.parse(dateStr);
            Date today = Calendar.getInstance().getTime();

            if (date != null) {
                // Supprimer le composant temporel pour la comparaison
                Calendar cal1 = Calendar.getInstance();
                Calendar cal2 = Calendar.getInstance();
                cal1.setTime(date);
                cal2.setTime(today);
                cal1.set(Calendar.HOUR_OF_DAY, 0);
                cal1.set(Calendar.MINUTE, 0);
                cal1.set(Calendar.SECOND, 0);
                cal1.set(Calendar.MILLISECOND, 0);
                cal2.set(Calendar.HOUR_OF_DAY, 0);
                cal2.set(Calendar.MINUTE, 0);
                cal2.set(Calendar.SECOND, 0);
                cal2.set(Calendar.MILLISECOND, 0);

                return cal1.before(cal2);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Obtient les dates pour la semaine en cours
     * @return Liste de chaînes de date pour la semaine en cours
     */
    public static List<String> getCurrentWeekDates() {
        List<String> weekDates = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault());

        Calendar calendar = Calendar.getInstance();
        // Définir au premier jour de la semaine (dimanche ou lundi selon la locale)
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

        for (int i = 0; i < 7; i++) {
            weekDates.add(sdf.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return weekDates;
    }

    /**
     * Obtient les dates pour un mois spécifié
     * @param year Année (ex. 2025)
     * @param month Mois (1-12)
     * @return Liste de chaînes de date pour le mois spécifié
     */
    public static List<String> getMonthDates(int year, int month) {
        List<String> monthDates = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault());

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1); // Le mois est 0-based dans Calendar

        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < daysInMonth; i++) {
            monthDates.add(sdf.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return monthDates;
    }

    /**
     * Formate l'heure pour l'affichage
     * @param time Chaîne d'heure au format 24 heures (HH:mm)
     * @param use12HourFormat S'il faut convertir en format 12 heures
     * @return Chaîne d'heure formatée
     */
    public static String formatTime(String time, boolean use12HourFormat) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date timeDate = input.parse(time);

            if (timeDate != null) {
                SimpleDateFormat output = new SimpleDateFormat(
                        use12HourFormat ? "hh:mm a" : "HH:mm",
                        Locale.getDefault());
                return output.format(timeDate);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return time; // Retourner l'original si l'analyse échoue
    }
}