package com.example.personal_refelection;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@SuppressLint("MissingPermission")
public class NotificationHelper {

    public static final String CHANNEL_GOAL_REMINDERS     = "channel_goal_reminders";
    public static final String CHANNEL_REFLECTION_PROMPTS = "channel_reflection_prompts";
    public static final String CHANNEL_ACHIEVEMENTS       = "channel_achievements";
    public static final String CHANNEL_WEEKLY_SUMMARY     = "channel_weekly_summary";
    public static final String CHANNEL_DEADLINE_WARNINGS  = "channel_deadline_warnings";

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
    public static final String ACTION_GOAL_DEADLINE         = "com.example.personal_refelection.GOAL_DEADLINE";
    public static final String ACTION_GOAL_DEADLINE_WARNING = "com.example.personal_refelection.GOAL_DEADLINE_WARNING";

    // Extra key for goal info passed to the receiver
    public static final String EXTRA_GOAL_TITLE = "goal_title";
    public static final String EXTRA_GOAL_ID    = "goal_id";

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

        android.app.NotificationChannel ch5 = new android.app.NotificationChannel(
                CHANNEL_DEADLINE_WARNINGS, "Deadline Warnings", NotificationManager.IMPORTANCE_HIGH);
        ch5.setDescription("Alert when a goal deadline is 5 minutes away");
        ch5.enableVibration(true);
        ch5.setShowBadge(true);
        nm.createNotificationChannel(ch5);
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

    public static void postGoalDeadline(Context ctx, String goalTitle) {
        post(ctx, CHANNEL_GOAL_REMINDERS, NOTIF_ID_GOAL_REMINDER + Math.abs(goalTitle.hashCode()) % 10000,
                "⏰ Goal Deadline Reached!",
                "\"" + goalTitle + "\" has reached its target date! Time to review your progress.");
    }

    public static void postGoalDeadlineWarning(Context ctx, String goalTitle) {
        // Use HIGH importance channel so the heads-up / alert pops up on screen
        int notifId = 50000 + Math.abs(goalTitle.hashCode()) % 10000;
        postWithPriority(ctx, CHANNEL_DEADLINE_WARNINGS, notifId,
                "⚠️ Goal Deadline in 5 Minutes!",
                "\"" + goalTitle + "\" deadline is almost here! Only 5 minutes left. Stay focused! 🎯",
                NotificationCompat.PRIORITY_HIGH);
    }

    public static void postWeeklySummary(Context ctx, int active, int achieved, int reflections) {
        post(ctx, CHANNEL_WEEKLY_SUMMARY, NOTIF_ID_WEEKLY_SUMMARY,
                "📊 Your Weekly Summary",
                "This week: " + achieved + " goals achieved, " + active + " active, "
                        + reflections + " reflections written. Keep going! 🌿");
    }

    private static void post(Context ctx, String channelId, int notifId,
                              String title, String body) {
        postWithPriority(ctx, channelId, notifId, title, body, NotificationCompat.PRIORITY_DEFAULT);
    }

    private static void postWithPriority(Context ctx, String channelId, int notifId,
                                         String title, String body, int priority) {
        Intent tap = new Intent(ctx, DashboardActivity.class);
        tap.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi = PendingIntent.getActivity(ctx, notifId, tap,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, channelId)
                .setSmallIcon(R.drawable.ic_logo_journal)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(priority)
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

    /**
     * Schedule an exact alarm to fire when the goal's target date/time arrives.
     * targetDateStr format: "yyyy-MM-dd HH:mm"
     * goalId is used as the unique request code so each goal has its own alarm.
     */
    public static void scheduleGoalDeadline(Context ctx, int goalId, String goalTitle, String targetDateStr) {
        if (targetDateStr == null || targetDateStr.isEmpty()) return;

        long triggerMillis;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
            Date date = sdf.parse(targetDateStr);
            if (date == null) return;
            triggerMillis = date.getTime();
        } catch (Exception e) {
            Log.e("NotificationHelper", "Failed to parse target date: " + targetDateStr, e);
            return;
        }

        // Don't schedule if the time is already in the past
        if (triggerMillis <= System.currentTimeMillis()) return;

        Intent intent = new Intent(ACTION_GOAL_DEADLINE);
        intent.setClassName(ctx.getPackageName(), ctx.getPackageName() + ".NotificationReceiver");
        intent.putExtra(EXTRA_GOAL_TITLE, goalTitle);
        intent.putExtra(EXTRA_GOAL_ID, goalId);

        // Use goalId + offset as unique request code (keep in range 10000–99999)
        int requestCode = 10000 + (goalId % 89999);

        PendingIntent pi = PendingIntent.getBroadcast(ctx, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (am.canScheduleExactAlarms()) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pi);
                } else {
                    // Fallback — less precise but functional
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pi);
                }
            } else {
                // API 23–30: setExactAndAllowWhileIdle works without runtime permission
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pi);
            }
            Log.d("NotificationHelper", "Goal deadline alarm set for goalId=" + goalId
                    + " at " + targetDateStr + " (millis=" + triggerMillis + ")");
        } catch (SecurityException e) {
            Log.e("NotificationHelper", "SecurityException scheduling alarm: " + e.getMessage());
            am.set(AlarmManager.RTC_WAKEUP, triggerMillis, pi);
        }
    }

    /**
     * Cancel a previously scheduled goal deadline alarm.
     */
    public static void cancelGoalDeadline(Context ctx, int goalId) {
        Intent intent = new Intent(ACTION_GOAL_DEADLINE);
        intent.setClassName(ctx.getPackageName(), ctx.getPackageName() + ".NotificationReceiver");
        int requestCode = 10000 + (goalId % 89999);
        PendingIntent pi = PendingIntent.getBroadcast(ctx, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am != null) am.cancel(pi);
        pi.cancel();
    }

    /**
     * Schedule an exact alarm 5 minutes BEFORE the goal's target date/time.
     * Fires ACTION_GOAL_DEADLINE_WARNING to alert the user with a heads-up notification.
     */
    public static void scheduleGoalDeadlineWarning(Context ctx, int goalId, String goalTitle, String targetDateStr) {
        if (targetDateStr == null || targetDateStr.isEmpty()) return;

        long deadlineMillis;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
            Date date = sdf.parse(targetDateStr);
            if (date == null) return;
            deadlineMillis = date.getTime();
        } catch (Exception e) {
            Log.e("NotificationHelper", "Failed to parse target date for warning: " + targetDateStr, e);
            return;
        }

        // Trigger 5 minutes before the deadline
        long triggerMillis = deadlineMillis - (5 * 60 * 1000L);

        // Don't schedule if that time is already in the past
        if (triggerMillis <= System.currentTimeMillis()) return;

        Intent intent = new Intent(ACTION_GOAL_DEADLINE_WARNING);
        intent.setClassName(ctx.getPackageName(), ctx.getPackageName() + ".NotificationReceiver");
        intent.putExtra(EXTRA_GOAL_TITLE, goalTitle);
        intent.putExtra(EXTRA_GOAL_ID, goalId);

        // Use goalId + offset in a different range from the deadline alarm (100000–189999)
        int requestCode = 100000 + (goalId % 89999);

        PendingIntent pi = PendingIntent.getBroadcast(ctx, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (am.canScheduleExactAlarms()) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pi);
                } else {
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pi);
                }
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pi);
            }
            Log.d("NotificationHelper", "5-min warning alarm set for goalId=" + goalId
                    + " firing 5 min before " + targetDateStr + " (millis=" + triggerMillis + ")");
        } catch (SecurityException e) {
            Log.e("NotificationHelper", "SecurityException scheduling warning alarm: " + e.getMessage());
            am.set(AlarmManager.RTC_WAKEUP, triggerMillis, pi);
        }
    }

    /**
     * Cancel a previously scheduled 5-minute deadline warning alarm.
     */
    public static void cancelGoalDeadlineWarning(Context ctx, int goalId) {
        Intent intent = new Intent(ACTION_GOAL_DEADLINE_WARNING);
        intent.setClassName(ctx.getPackageName(), ctx.getPackageName() + ".NotificationReceiver");
        int requestCode = 100000 + (goalId % 89999);
        PendingIntent pi = PendingIntent.getBroadcast(ctx, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am != null) am.cancel(pi);
        pi.cancel();
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
