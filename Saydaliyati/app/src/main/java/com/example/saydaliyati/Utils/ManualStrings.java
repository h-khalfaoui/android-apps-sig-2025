package com.example.saydaliyati.Utils;

import java.util.HashMap;
import java.util.Map;

public class ManualStrings {
    private static final Map<String, String> arabicStrings = new HashMap<>();

    static {
        // Initialiser les chaînes arabes manuellement
        arabicStrings.put("app_name", "صيدليتي");
        arabicStrings.put("app_slogan", "ابحث عن الصيدليات القريبة منك، بما في ذلك الصيدليات المناوبة");
        arabicStrings.put("app_version", "الإصدار 1.0");
        arabicStrings.put("get_started", "ابدأ");
        arabicStrings.put("authority_login", "تسجيل دخول المسؤول");
        arabicStrings.put("admin_dashboard", "لوحة تحكم المسؤول");
        arabicStrings.put("app_logo_description", "شعار التطبيق");
        arabicStrings.put("pharmacy_admin_login", "تسجيل دخول مسؤول الصيدلية");

        // Titles
        arabicStrings.put("title_pharmacy_finder", "البحث عن الصيدليات");
        arabicStrings.put("title_admin_login", "تسجيل دخول المسؤول");
        arabicStrings.put("title_admin_dashboard", "لوحة التحكم");
        arabicStrings.put("title_add_pharmacy", "إضافة صيدلية");
        arabicStrings.put("title_assign_guard_date", "تعيين مواعيد المناوبة");

        // General
        arabicStrings.put("action_sign_in", "تسجيل الدخول");
        arabicStrings.put("prompt_username", "اسم المستخدم");
        arabicStrings.put("prompt_password", "كلمة المرور");
        arabicStrings.put("forgot_password", "نسيت كلمة المرور؟");
        arabicStrings.put("error_field_required", "هذا الحقل مطلوب");
        arabicStrings.put("error_invalid_password", "كلمة المرور قصيرة جدًا");
        arabicStrings.put("error_incorrect_credentials", "بيانات الاعتماد غير صحيحة");

        // Settings
        arabicStrings.put("settings", "الإعدادات");
        arabicStrings.put("language_settings", "إعدادات اللغة");
        arabicStrings.put("select_language", "اختر اللغة");
        arabicStrings.put("french", "الفرنسية");
        arabicStrings.put("arabic", "العربية");
        arabicStrings.put("save", "حفظ");
        arabicStrings.put("cancel", "إلغاء");
        arabicStrings.put("settings_saved", "تم حفظ الإعدادات");

        // Notifications
        arabicStrings.put("notifications", "الإشعارات");
        arabicStrings.put("enable_notifications", "تفعيل الإشعارات");
        arabicStrings.put("duty_pharmacy_notifications", "إشعارات الصيدليات المناوبة");
        arabicStrings.put("notify_before_hours", "إشعار قبل ساعات من المناوبة");
        arabicStrings.put("notify_on_change", "إشعار عند تغيير الصيدليات المناوبة");
        arabicStrings.put("pharmacy_on_duty", "صيدلية مناوبة");
        arabicStrings.put("tomorrow", "غدا");
        arabicStrings.put("in_one_hour", "خلال ساعة");
        arabicStrings.put("pharmacy_duty_notification", "%1$s ستكون مناوبة %2$s");
        arabicStrings.put("new_duty_pharmacy", "صيدلية مناوبة جديدة");
        arabicStrings.put("new_duty_pharmacy_notification", "تم تعيين %1$s كصيدلية مناوبة ليوم %2$s");

        arabicStrings.put("opening_map", "جاري فتح الخريطة...");

        // Pour le test
        arabicStrings.put("test_language", "هذا هو النص العربي");
    }

    /**
     * Récupère une chaîne en arabe par sa clé
     * @param key La clé de la chaîne (même nom que dans strings.xml)
     * @return La chaîne en arabe ou null si non trouvée
     */
    public static String getArabicString(String key) {
        return arabicStrings.getOrDefault(key, key + "_ar");
    }

    /**
     * Vérifie si une clé existe dans les chaînes arabes
     */
    public static boolean hasArabicString(String key) {
        return arabicStrings.containsKey(key);
    }
}