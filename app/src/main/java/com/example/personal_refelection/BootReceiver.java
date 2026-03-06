package com.example.personal_refelection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * BootReceiver — listens for BOOT_COMPLETED and re-schedules all enabled
 * notification alarms, because AlarmManager alarms are wiped on device reboot.
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String PREF_GOAL_REMINDERS     = "notif_goal_reminders";
    private static final String PREF_REFLECTION_PROMPTS = "notif_reflection_prompts";
    private static final String PREF_WEEKLY_SUMMARY     = "notif_weekly_summary";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action)
                && !"android.intent.action.QUICKBOOT_POWERON".equals(action)) {
            return;
        }

        SharedPreferences prefs =
                context.getSharedPreferences("GoalReflectPrefs", Context.MODE_PRIVATE);

        // Re-create notification channels first
        NotificationHelper.createChannels(context);

        // Re-schedule each alarm that was previously enabled
        if (prefs.getBoolean(PREF_GOAL_REMINDERS, true)) {
            NotificationHelper.scheduleGoalReminder(context);
        }

        if (prefs.getBoolean(PREF_REFLECTION_PROMPTS, true)) {
            NotificationHelper.scheduleReflectionPrompt(context);
        }

        if (prefs.getBoolean(PREF_WEEKLY_SUMMARY, false)) {
            NotificationHelper.scheduleWeeklySummary(context);
        }
    }
}

