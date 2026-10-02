package com.agrilink.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public class KrishiSetuApplication extends Application {

    public static final String PREFS_NAME = "agrilink_prefs";
    public static final String KEY_THEME_MODE = "theme_mode"; // "system", "light", "dark"

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        applyStoredTheme(this);
        applyStoredLanguage(this);
    }

    public static void applyStoredTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String themeMode = prefs.getString(KEY_THEME_MODE, "system");

        switch (themeMode) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "system":
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    public static void applyStoredLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lang = prefs.getString(LocaleHelper.KEY_LANGUAGE, LocaleHelper.LANG_EN);
        LocaleHelper.applyAppLanguage(lang);
    }
}
