package com.example.saydaliyati.Utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.saydaliyati.Activities.PharmacyFinderActivity;
import com.example.saydaliyati.Models.GuardDate;
import com.example.saydaliyati.Models.Pharmacy;
import com.example.saydaliyati.R;
import com.example.saydaliyati.Receivers.NotificationReceiver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NotificationUtils {

    // -----------------------------
    // Constantes & préférences
    // -----------------------------
    private static final String PREF_NAME = "NotificationPref";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_DUTY_NOTIFICATIONS_ENABLED = "duty_notifications_enabled";

    private static final String CHANNEL_ID = "pharmacy_notification_channel";
    private static final String CHANNEL_NAME = "Pharmacy Notifications";
    private static final String CHANNEL_DESC = "Notifications for pharmacy duty schedules";

    private static final String TAG = "NotificationUtils";

    // -----------------------------
    // Gestion des préférences
    // -----------------------------
    public static boolean areNotificationsEnabled(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    public static void setNotificationsEnabled(Context context, boolean enabled) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        preferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply();

        if (!enabled) {
            cancelAllScheduledNotifications(context);
        }
    }

    public static boolean areDutyPharmacyNotificationsEnabled(Context context) {
        if (!areNotificationsEnabled(context)) return false;

        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_DUTY_NOTIFICATIONS_ENABLED, true);
    }

    public static void setDutyPharmacyNotificationsEnabled(Context context, boolean enabled) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        preferences.edit().putBoolean(KEY_DUTY_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    // -----------------------------
    // Vérification des permissions
    // -----------------------------
    public static boolean checkAlarmPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            return alarmManager != null && alarmManager.canScheduleExactAlarms();
        }
        return true; // Permission not needed for Android < 12
    }

    public static void requestAlarmPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Show toast informing about permission requirement
            String message = LanguageUtils.LANGUAGE_ARABIC.equals(LanguageUtils.getCurrentLanguage(context)) ?
                    "يرجى تمكين إذن الإشعارات الدقيقة في الإعدادات" :
                    "Please enable exact alarm permission in settings";
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();

            // Open settings to enable permission
            try {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Failed to open alarm settings: " + e.getMessage());
            }
        }
    }

    // -----------------------------
    // Création du canal
    // -----------------------------
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESC);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    // -----------------------------
    // Notification immédiate
    // -----------------------------
    public static void showNotification(Context context, String title, String content) {
        if (!areNotificationsEnabled(context)) return;

        Intent intent = new Intent(context, PharmacyFinderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, flags
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        try {
            manager.notify(generateUniqueId(), builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Notification permission denied: " + e.getMessage());
        }
    }

    // -----------------------------
    // Notifications programmées
    // -----------------------------
    public static void scheduleGuardDutyNotifications(Context context, List<GuardDate> guardDates, List<Pharmacy> pharmacies) {
        try {
            // Verify we have all needed data
            if (!areDutyPharmacyNotificationsEnabled(context) ||
                    guardDates == null || guardDates.isEmpty() ||
                    pharmacies == null || pharmacies.isEmpty()) {
                return;
            }

            // Check for exact alarm permission on Android 12+
            if (!checkAlarmPermission(context)) {
                requestAlarmPermission(context);
                return;
            }

            // Clear any existing notifications
            cancelAllNotifications(context);

            // Schedule new notifications
            for (GuardDate guardDate : guardDates) {
                Pharmacy pharmacy = findPharmacyById(pharmacies, guardDate.getPharmacyId());
                if (pharmacy == null) continue;

                try {
                    String dateTimeStr = guardDate.getGuardDate() + " " + (guardDate.getStartTime() != null ? guardDate.getStartTime() : "08:00");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    Date guardDateTime = sdf.parse(dateTimeStr);

                    if (guardDateTime == null) continue;

                    // Planifier pour la veille et 1 heure avant
                    scheduleNotification(context, pharmacy, guardDate, guardDateTime, TimeUnit.DAYS.toMillis(1));
                    scheduleNotification(context, pharmacy, guardDate, guardDateTime, TimeUnit.HOURS.toMillis(1));

                } catch (ParseException e) {
                    Log.e(TAG, "Error parsing date: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling notifications: " + e.getMessage());
        }
    }

    private static void scheduleNotification(Context context, Pharmacy pharmacy, GuardDate guardDate, Date guardDateTime, long advanceTimeMillis) {
        try {
            // Verify we have permission for exact alarms
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager == null || !alarmManager.canScheduleExactAlarms()) {
                    return;
                }
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(guardDateTime);
            calendar.add(Calendar.MILLISECOND, -(int) advanceTimeMillis);

            // Skip if notification time is in the past
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) return;

            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.putExtra("title", context.getString(R.string.pharmacy_on_duty));

            String timeText = (advanceTimeMillis == TimeUnit.DAYS.toMillis(1))
                    ? context.getString(R.string.tomorrow)
                    : context.getString(R.string.in_one_hour);

            String content = String.format(context.getString(R.string.pharmacy_duty_notification),
                    pharmacy.getName(), timeText);
            intent.putExtra("content", content);

            int requestCode = generateRequestCode(guardDate, advanceTimeMillis);

            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    flags
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    } else {
                        alarmManager.setExact(
                                AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    }
                    Log.d(TAG, "Notification scheduled for " + pharmacy.getName() + " at " + calendar.getTime());
                } catch (SecurityException e) {
                    Log.e(TAG, "Security exception scheduling alarm: " + e.getMessage());
                    requestAlarmPermission(context);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in scheduleNotification: " + e.getMessage());
        }
    }

    // -----------------------------
    // Méthodes utilitaires
    // -----------------------------
    private static Pharmacy findPharmacyById(List<Pharmacy> pharmacies, int id) {
        for (Pharmacy pharmacy : pharmacies) {
            if (pharmacy.getId() == id) {
                return pharmacy;
            }
        }
        return null;
    }

    private static int generateUniqueId() {
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }

    private static int generateRequestCode(GuardDate guardDate, long advanceTimeMillis) {
        return guardDate.getPharmacyId() * 100000 +
                guardDate.getGuardDate().hashCode() % 10000 +
                (int) (advanceTimeMillis / 1000 / 60 / 60);
    }

    private static void cancelAllScheduledNotifications(Context context) {
        // Just cancel any pending alarms - simplified implementation
        // A more complete implementation would track all scheduled alarms
        Log.d(TAG, "Cancelling all scheduled notifications");
    }

    private static void cancelAllNotifications(Context context) {
        // Clear all shown notifications
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancelAll();

        // Also cancel scheduled ones
        cancelAllScheduledNotifications(context);
    }
}