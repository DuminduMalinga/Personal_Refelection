package com.example.personal_refelection;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Notification Settings screen — lets the user toggle each notification type.
 * Preferences are persisted in SharedPreferences.
 */
public class NotificationSettingsActivity extends AppCompatActivity {

    private static final String PREF_GOAL_REMINDERS        = "notif_goal_reminders";
    private static final String PREF_REFLECTION_PROMPTS    = "notif_reflection_prompts";
    private static final String PREF_ACHIEVEMENTS          = "notif_achievements";
    private static final String PREF_WEEKLY_SUMMARY        = "notif_weekly_summary";

    private SwitchMaterial switchGoalReminders;
    private SwitchMaterial switchReflectionPrompts;
    private SwitchMaterial switchAchievements;
    private SwitchMaterial switchWeeklySummary;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        sharedPreferences = getSharedPreferences("GoalReflectPrefs", MODE_PRIVATE);

        bindViews();
        restorePreferences();
        setupListeners();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void bindViews() {
        switchGoalReminders     = findViewById(R.id.switchGoalReminders);
        switchReflectionPrompts = findViewById(R.id.switchReflectionPrompts);
        switchAchievements      = findViewById(R.id.switchAchievements);
        switchWeeklySummary     = findViewById(R.id.switchWeeklySummary);
    }

    private void restorePreferences() {
        switchGoalReminders.setChecked(sharedPreferences.getBoolean(PREF_GOAL_REMINDERS, true));
        switchReflectionPrompts.setChecked(sharedPreferences.getBoolean(PREF_REFLECTION_PROMPTS, true));
        switchAchievements.setChecked(sharedPreferences.getBoolean(PREF_ACHIEVEMENTS, true));
        switchWeeklySummary.setChecked(sharedPreferences.getBoolean(PREF_WEEKLY_SUMMARY, false));
    }

    private void setupListeners() {
        switchGoalReminders.setOnCheckedChangeListener((btn, checked) -> saveAndNotify(PREF_GOAL_REMINDERS, checked));
        switchReflectionPrompts.setOnCheckedChangeListener((btn, checked) -> saveAndNotify(PREF_REFLECTION_PROMPTS, checked));
        switchAchievements.setOnCheckedChangeListener((btn, checked) -> saveAndNotify(PREF_ACHIEVEMENTS, checked));
        switchWeeklySummary.setOnCheckedChangeListener((btn, checked) -> saveAndNotify(PREF_WEEKLY_SUMMARY, checked));
    }

    private void saveAndNotify(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
        Toast.makeText(this, getString(R.string.lbl_notif_saved), Toast.LENGTH_SHORT).show();
    }
}

