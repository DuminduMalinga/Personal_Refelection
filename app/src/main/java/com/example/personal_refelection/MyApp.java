package com.example.personal_refelection;

import android.app.Application;

/**
 * Custom Application class — applies the user's saved theme before any activity starts.
 */
public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Apply the user's chosen theme globally
        ThemeManager.applyTheme(this);
    }
}

