package com.example.personal_refelection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String PREF_GOAL_REMINDERS     = "notif_goal_reminders";
    private static final String PREF_REFLECTION_PROMPTS = "notif_reflection_prompts";
    private static final String PREF_ACHIEVEMENTS       = "notif_achievements";
    private static final String PREF_WEEKLY_SUMMARY     = "notif_weekly_summary";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        SharedPreferences prefs =
                context.getSharedPreferences("GoalReflectPrefs", Context.MODE_PRIVATE);
        String userName = prefs.getString("user_name", "there");

        switch (intent.getAction()) {
            case NotificationHelper.ACTION_GOAL_REMINDER:
                if (prefs.getBoolean(PREF_GOAL_REMINDERS, true))
                    NotificationHelper.postGoalReminder(context, userName);
                // Reschedule for next day (self-rescheduling pattern)
                NotificationHelper.scheduleGoalReminder(context);
                break;

            case NotificationHelper.ACTION_REFLECTION_PROMPT:
                if (prefs.getBoolean(PREF_REFLECTION_PROMPTS, true))
                    NotificationHelper.postReflectionPrompt(context, userName);
                // Reschedule for next day
                NotificationHelper.scheduleReflectionPrompt(context);
                break;

            case NotificationHelper.ACTION_ACHIEVEMENT_ALERT:
                if (prefs.getBoolean(PREF_ACHIEVEMENTS, true)) {
                    String goalTitle = intent.getStringExtra("goal_title");
                    NotificationHelper.postAchievementAlert(context,
                            goalTitle != null ? goalTitle : "your goal");
                }
                break;

            case NotificationHelper.ACTION_WEEKLY_SUMMARY:
                if (prefs.getBoolean(PREF_WEEKLY_SUMMARY, false)) {
                    int active      = prefs.getInt("stat_active_goals", 0);
                    int achieved    = prefs.getInt("stat_achieved_goals", 0);
                    int reflections = prefs.getInt("stat_total_reflections", 0);
                    NotificationHelper.postWeeklySummary(context, active, achieved, reflections);
                }
                // Reschedule for next week
                NotificationHelper.scheduleWeeklySummary(context);
                break;

            case NotificationHelper.ACTION_GOAL_DEADLINE:
                // Always fire deadline alerts (they were explicitly set per goal)
                String deadlineGoalTitle = intent.getStringExtra(NotificationHelper.EXTRA_GOAL_TITLE);
                NotificationHelper.postGoalDeadline(context,
                        deadlineGoalTitle != null ? deadlineGoalTitle : "Your goal");
                break;

            case NotificationHelper.ACTION_GOAL_DEADLINE_WARNING:
                // 5-minute warning before the goal deadline — always fire this alert
                String warningGoalTitle = intent.getStringExtra(NotificationHelper.EXTRA_GOAL_TITLE);
                NotificationHelper.postGoalDeadlineWarning(context,
                        warningGoalTitle != null ? warningGoalTitle : "Your goal");
                break;

            case Intent.ACTION_BOOT_COMPLETED:
                // Device rebooted — reschedule all active alarms
                if (prefs.getBoolean(PREF_GOAL_REMINDERS, true))
                    NotificationHelper.scheduleGoalReminder(context);
                if (prefs.getBoolean(PREF_REFLECTION_PROMPTS, true))
                    NotificationHelper.scheduleReflectionPrompt(context);
                if (prefs.getBoolean(PREF_WEEKLY_SUMMARY, false))
                    NotificationHelper.scheduleWeeklySummary(context);
                break;
        }
    }
}

