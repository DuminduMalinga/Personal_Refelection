package com.example.personal_refelection;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;

@SuppressLint("MissingPermission")
public class NotificationHelper {

    public static final String CHANNEL_GOAL_REMINDERS     = "channel_goal_reminders";
    public static final String CHANNEL_REFLECTION_PROMPTS = "channel_reflection_prompts";
    public static final String CHANNEL_ACHIEVEMENTS       = "channel_achievements";
    public static final String CHANNEL_WEEKLY_SUMMARY     = "channel_weekly_summary";

    public static final int NOTIF_ID_GOAL_REMINDER     = 1001;
    public static final int NOTIF_ID_REFLECTION_PROMPT = 1002;
    public static final int NOTIF_ID_ACHIEVEMENT       = 1003;
    public static final int NOTIF_ID_WEEKLY_SUMMARY    = 1004;

    private static final int REQUEST_GOAL_REMINDER     = 2001;
    private static final int REQUEST_REFLECTION_PROMPT = 2002;
    private static final int REQUEST_WEEKLY_SUMMARY    = 2003;

    public static final String ACTION_GOAL_REMINDER     = "com.example.personal_refelection.GOAL_REMINDER";
    public static final String ACTION_REFLECTION_PROMPT = "com.example.personal_refelection.REFLECTION_PROMPT";
    public static final String ACTION_ACHIEVEMENT_ALERT = "com.example.personal_refelection.ACHIEVEMENT_ALERT";
    public static final String ACTION_WEEKLY_SUMMARY    = "com.example.personal_refelection.WEEKLY_SUMMARY";

    // ── Channels ──────────────────────────────────────────────────────────────

    public static void createChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        android.app.NotificationChannel ch1 = new android.app.NotificationChannel(
                CHANNEL_GOAL_REMINDERS, "Goal Reminders", NotificationManager.IMPORTANCE_DEFAULT);
        ch1.setDescription("Daily reminders to check on your active goals");
        ch1.enableVibration(true);
        ch1.setShowBadge(true);
        nm.createNotificationChannel(ch1);

        android.app.NotificationChannel ch2 = new android.app.NotificationChannel(
                CHANNEL_REFLECTION_PROMPTS, "Reflection Prompts", NotificationManager.IMPORTANCE_DEFAULT);
        ch2.setDescription("Evening prompts to write your daily reflection");
        ch2.enableVibration(true);
        ch2.setShowBadge(true);
        nm.createNotificationChannel(ch2);

        android.app.NotificationChannel ch3 = new android.app.NotificationChannel(
                CHANNEL_ACHIEVEMENTS, "Achievement Alerts", NotificationManager.IMPORTANCE_HIGH);
        ch3.setDescription("Celebrate when you complete a goal");
        ch3.enableVibration(true);
        ch3.setShowBadge(true);
        nm.createNotificationChannel(ch3);

        android.app.NotificationChannel ch4 = new android.app.NotificationChannel(
                CHANNEL_WEEKLY_SUMMARY, "Weekly Summary", NotificationManager.IMPORTANCE_LOW);
        ch4.setDescription("Your weekly growth summary every Sunday");
        ch4.setShowBadge(false);
        nm.createNotificationChannel(ch4);
    }

    // ── Post notifications ────────────────────────────────────────────────────

    public static void postGoalReminder(Context ctx, String userName) {
        post(ctx, CHANNEL_GOAL_REMINDERS, NOTIF_ID_GOAL_REMINDER,
                "⏰ Goal Check-In",
                "Hey " + userName + "! Don't forget to review your active goals today.");
    }

    public static void postReflectionPrompt(Context ctx, String userName) {
        post(ctx, CHANNEL_REFLECTION_PROMPTS, NOTIF_ID_REFLECTION_PROMPT,
                "📝 Time to Reflect",
                "Hey " + userName + "! Take a moment to journal your thoughts for today.");
    }

    public static void postAchievementAlert(Context ctx, String goalTitle) {
        post(ctx, CHANNEL_ACHIEVEMENTS, NOTIF_ID_ACHIEVEMENT,
                "🏆 Goal Achieved!",
                "You completed: \"" + goalTitle + "\". Amazing work!");
    }

    public static void postWeeklySummary(Context ctx, int active, int achieved, int reflections) {
        post(ctx, CHANNEL_WEEKLY_SUMMARY, NOTIF_ID_WEEKLY_SUMMARY,
                "📊 Your Weekly Summary",
                "This week: " + achieved + " goals achieved, " + active + " active, "
                        + reflections + " reflections written. Keep going! 🌿");
    }

    private static void post(Context ctx, String channelId, int notifId,
                              String title, String body) {
        Intent tap = new Intent(ctx, DashboardActivity.class);
        tap.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi = PendingIntent.getActivity(ctx, notifId, tap,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, channelId)
                .setSmallIcon(R.drawable.ic_logo_journal)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pi)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(ctx).notify(notifId, b.build());
        } catch (SecurityException e) {
            // POST_NOTIFICATIONS not granted — silent fail
        }
    }

    // ── Schedule / cancel alarms ──────────────────────────────────────────────

    public static void scheduleGoalReminder(Context ctx) {
        scheduleDailyAlarm(ctx, ACTION_GOAL_REMINDER, REQUEST_GOAL_REMINDER, 9);
    }

    public static void cancelGoalReminder(Context ctx) {
        cancelAlarm(ctx, ACTION_GOAL_REMINDER, REQUEST_GOAL_REMINDER);
    }

    public static void scheduleReflectionPrompt(Context ctx) {
        scheduleDailyAlarm(ctx, ACTION_REFLECTION_PROMPT, REQUEST_REFLECTION_PROMPT, 21);
    }

    public static void cancelReflectionPrompt(Context ctx) {
        cancelAlarm(ctx, ACTION_REFLECTION_PROMPT, REQUEST_REFLECTION_PROMPT);
    }

    public static void scheduleWeeklySummary(Context ctx) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        if (cal.getTimeInMillis() < System.currentTimeMillis()) {
            cal.add(Calendar.WEEK_OF_YEAR, 1);
        }
        PendingIntent pi = buildPendingIntent(ctx, ACTION_WEEKLY_SUMMARY, REQUEST_WEEKLY_SUMMARY);
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            try {
                am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY * 7, pi);
            } catch (SecurityException e) {
                am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
            }
        }
    }

    public static void cancelWeeklySummary(Context ctx) {
        cancelAlarm(ctx, ACTION_WEEKLY_SUMMARY, REQUEST_WEEKLY_SUMMARY);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void scheduleDailyAlarm(Context ctx, String action, int requestCode, int hour) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        if (cal.getTimeInMillis() < System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        PendingIntent pi = buildPendingIntent(ctx, action, requestCode);
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            try {
                am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, pi);
            } catch (SecurityException e) {
                am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
            }
        }
    }

    private static void cancelAlarm(Context ctx, String action, int requestCode) {
        PendingIntent pi = buildPendingIntent(ctx, action, requestCode);
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am != null) am.cancel(pi);
        pi.cancel();
    }

    private static PendingIntent buildPendingIntent(Context ctx, String action, int requestCode) {
        Intent intent = new Intent(action);
        intent.setClassName(ctx.getPackageName(),
                ctx.getPackageName() + ".NotificationReceiver");
        return PendingIntent.getBroadcast(ctx, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
