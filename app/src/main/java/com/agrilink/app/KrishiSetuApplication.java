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
        prefs.edit().putString(KEY_THEME_MODE, "light").apply();
        // Keep exclusively Light version - remove dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    public static void applyStoredLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lang = prefs.getString(LocaleHelper.KEY_LANGUAGE, LocaleHelper.LANG_EN);
        LocaleHelper.applyAppLanguage(lang);
    }
}
