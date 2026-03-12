package com.example.personal_refelection;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Notification Settings screen — lets the user toggle each notification type.
 * On Android 13+ we request POST_NOTIFICATIONS permission before enabling any toggle.
 * Each toggle schedules / cancels its corresponding AlarmManager alarm via NotificationHelper.
 */
public class NotificationSettingsActivity extends AppCompatActivity {

    private static final String PREF_GOAL_REMINDERS     = "notif_goal_reminders";
    private static final String PREF_REFLECTION_PROMPTS = "notif_reflection_prompts";
    private static final String PREF_ACHIEVEMENTS       = "notif_achievements";
    private static final String PREF_WEEKLY_SUMMARY     = "notif_weekly_summary";

    private SwitchMaterial switchGoalReminders;
    private SwitchMaterial switchReflectionPrompts;
    private SwitchMaterial switchAchievements;
    private SwitchMaterial switchWeeklySummary;

    private SharedPreferences sharedPreferences;

    // ── Runtime permission launcher (Android 13+) ─────────────────────────
    private final ActivityResultLauncher<String> notifPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) {
                            // Permission just granted — activate all enabled toggles
                            applyAllSchedules();
                            Toast.makeText(this,
                                    getString(R.string.lbl_notif_permission_granted),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // User denied — turn all switches off and inform them
                            disableAllSwitches();
                            showPermissionDeniedDialog();
                        }
                    });

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification_settings);

        View toolbar = findViewById(R.id.notificationToolbar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.notificationRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, 0, bars.right, 0);
            if (toolbar != null) toolbar.setPadding(4, bars.top, 16, 0);
            return insets;
        });

        NotificationHelper.createChannels(this);

        sharedPreferences = getSharedPreferences("GoalReflectPrefs", MODE_PRIVATE);

        bindViews();
        restorePreferences();
        setupListeners();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    // ── View binding ──────────────────────────────────────────────────────────

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

    // ── Toggle listeners ──────────────────────────────────────────────────────

    private void setupListeners() {
        switchGoalReminders.setOnCheckedChangeListener((btn, checked) -> {
            if (checked && isNotifPermissionMissing()) {
                btn.setChecked(false);
                requestNotifPermission();
                return;
            }
            savePreference(PREF_GOAL_REMINDERS, checked);
            if (checked) NotificationHelper.scheduleGoalReminder(this);
            else         NotificationHelper.cancelGoalReminder(this);
            showSavedToast();
        });

        switchReflectionPrompts.setOnCheckedChangeListener((btn, checked) -> {
            if (checked && isNotifPermissionMissing()) {
                btn.setChecked(false);
                requestNotifPermission();
                return;
            }
            savePreference(PREF_REFLECTION_PROMPTS, checked);
            if (checked) NotificationHelper.scheduleReflectionPrompt(this);
            else         NotificationHelper.cancelReflectionPrompt(this);
            showSavedToast();
        });

        switchAchievements.setOnCheckedChangeListener((btn, checked) -> {
            if (checked && isNotifPermissionMissing()) {
                btn.setChecked(false);
                requestNotifPermission();
                return;
            }
            savePreference(PREF_ACHIEVEMENTS, checked);
            showSavedToast();
        });

        switchWeeklySummary.setOnCheckedChangeListener((btn, checked) -> {
            if (checked && isNotifPermissionMissing()) {
                btn.setChecked(false);
                requestNotifPermission();
                return;
            }
            savePreference(PREF_WEEKLY_SUMMARY, checked);
            if (checked) NotificationHelper.scheduleWeeklySummary(this);
            else         NotificationHelper.cancelWeeklySummary(this);
            showSavedToast();
        });
    }

    // ── Permission helpers ────────────────────────────────────────────────────

    /** Returns true if POST_NOTIFICATIONS permission is NOT yet granted. */
    private boolean isNotifPermissionMissing() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false; // not needed
        return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED;
    }

    private void requestNotifPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.lbl_notif_permission_title))
                .setMessage(getString(R.string.lbl_notif_permission_rationale))
                .setPositiveButton(getString(R.string.lbl_permission_settings), (d, w) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", getPackageName(), null));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Called after permission is granted — activate all currently-enabled toggles. */
    private void applyAllSchedules() {
        if (switchGoalReminders.isChecked())     NotificationHelper.scheduleGoalReminder(this);
        if (switchReflectionPrompts.isChecked()) NotificationHelper.scheduleReflectionPrompt(this);
        if (switchWeeklySummary.isChecked())     NotificationHelper.scheduleWeeklySummary(this);
    }

    private void disableAllSwitches() {
        switchGoalReminders.setChecked(false);
        switchReflectionPrompts.setChecked(false);
        switchAchievements.setChecked(false);
        switchWeeklySummary.setChecked(false);
        savePreference(PREF_GOAL_REMINDERS, false);
        savePreference(PREF_REFLECTION_PROMPTS, false);
        savePreference(PREF_ACHIEVEMENTS, false);
        savePreference(PREF_WEEKLY_SUMMARY, false);
    }

    private void savePreference(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    private void showSavedToast() {
        Toast.makeText(this, getString(R.string.lbl_notif_saved), Toast.LENGTH_SHORT).show();
    }
}

