package com.example.personal_refelection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {

    private static final String PREF_GOAL_REMINDERS     = "notif_goal_reminders";
    private static final String PREF_REFLECTION_PROMPTS = "notif_reflection_prompts";
    private static final String PREF_WEEKLY_SUMMARY     = "notif_weekly_summary";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action)
                && !"android.intent.action.QUICKBOOT_POWERON".equals(action)) return;

        SharedPreferences prefs =
                context.getSharedPreferences("GoalReflectPrefs", Context.MODE_PRIVATE);

        // Re-create channels (no-op below API 26)
        NotificationHelper.createChannels(context);

        // Re-schedule whichever alarms were enabled before reboot
        if (prefs.getBoolean(PREF_GOAL_REMINDERS, true))
            NotificationHelper.scheduleGoalReminder(context);

        if (prefs.getBoolean(PREF_REFLECTION_PROMPTS, true))
            NotificationHelper.scheduleReflectionPrompt(context);

        if (prefs.getBoolean(PREF_WEEKLY_SUMMARY, false))
            NotificationHelper.scheduleWeeklySummary(context);
    }
}

