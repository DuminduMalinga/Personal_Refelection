package com.example.personal_refelection;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Centralized theme manager.
 * Stores and applies the user's chosen theme (Light / Dark / Follow System).
 */
public class ThemeManager {

    public static final String PREF_NAME    = "GoalReflectPrefs";
    public static final String PREF_THEME   = "app_theme";

    // Values stored in SharedPreferences
    public static final int THEME_LIGHT  = 0;
    public static final int THEME_DARK   = 1;
    public static final int THEME_SYSTEM = 2;   // default

    /** Apply the saved theme. Call this in Application.onCreate and in every Activity.onCreate (before super). */
    public static void applyTheme(Context context) {
        int theme = getSavedTheme(context);
        applyMode(theme);
    }

    /** Persist the chosen theme and apply immediately. */
    public static void saveAndApplyTheme(Context context, int themeChoice) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(PREF_THEME, themeChoice).apply();
        applyMode(themeChoice);
    }

    public static int getSavedTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(PREF_THEME, THEME_SYSTEM);
    }

    private static void applyMode(int themeChoice) {
        switch (themeChoice) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}

