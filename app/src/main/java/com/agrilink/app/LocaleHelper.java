package com.agrilink.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;

public class LocaleHelper {

    public static final String KEY_LANGUAGE = "app_lang";
    public static final String LANG_EN = "en";
    public static final String LANG_HI = "hi";

    public static Context onAttach(Context context) {
        String lang = getPersistedLanguage(context);
        return setLocale(context, lang);
    }

    public static String getPersistedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(KrishiSetuApplication.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, LANG_EN);
    }

    public static Context setLocale(Context context, String language) {
        persist(context, language);

        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            configuration.setLocales(localeList);
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            return context.createConfigurationContext(configuration);
        } else {
            configuration.setLocale(locale);
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            return context;
        }
    }

    public static void applyAppLanguage(String language) {
        try {
            LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(language);
            AppCompatDelegate.setApplicationLocales(appLocale);
        } catch (Exception ignored) {
        }
    }

    private static void persist(Context context, String language) {
        SharedPreferences prefs = context.getSharedPreferences(KrishiSetuApplication.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, language).apply();
    }

    public static boolean isHindi(Context context) {
        return LANG_HI.equalsIgnoreCase(getPersistedLanguage(context));
    }

    public static String getLanguageToggleText(Context context) {
        return isHindi(context) ? "🌐 English" : "🌐 हिंदी";
    }

    public static void toggleLanguage(android.app.Activity activity) {
        if (activity == null) return;
        String current = getPersistedLanguage(activity);
        String next = LANG_HI.equalsIgnoreCase(current) ? LANG_EN : LANG_HI;
        setLocale(activity, next);
        applyAppLanguage(next);
        
        String toastMsg = LANG_HI.equalsIgnoreCase(next) ? "भाषा बदलकर हिंदी कर दी गई" : "Language switched to English";
        android.widget.Toast.makeText(activity, toastMsg, android.widget.Toast.LENGTH_SHORT).show();
        
        activity.recreate();
    }
}
