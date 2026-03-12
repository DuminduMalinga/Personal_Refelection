package com.example.personal_refelection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personal_refelection.database.AppDatabase;
import com.example.personal_refelection.database.Goal;
import com.example.personal_refelection.database.GoalDao;
import com.example.personal_refelection.database.Reflection;
import com.example.personal_refelection.database.ReflectionDao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * WeeklyReportActivity — shows the user's weekly growth summary.
 *
 * Displays:
 *  - Active goal count
 *  - Goals achieved this week
 *  - New goals started this week
 *  - Reflections written this week
 *  - List of achieved goals this week
 *  - Recent reflections this week
 *
 * Triggered every Sunday at 9:00 PM by AlarmManager via NotificationHelper.
 */
public class WeeklyReportActivity extends BaseActivity {

    private TextView tvWeekRange;
    private TextView tvWeekLabel;
    private TextView tvMotivation;
    private TextView tvActiveCount;
    private TextView tvAchievedWeekCount;
    private TextView tvReflectionsWeekCount;
    private TextView tvNewGoalsWeekCount;

    private ImageButton btnPrevWeek;
    private ImageButton btnNextWeek;

    private RecyclerView recyclerWeekGoals;
    private RecyclerView recyclerWeekReflections;
    private LinearLayout layoutNoAchievedThisWeek;
    private LinearLayout layoutNoReflectionsThisWeek;

    private GoalDao goalDao;
    private ReflectionDao reflectionDao;
    private int userId = -1;

    /** 0 = this week, -1 = last week, -2 = 2 weeks ago, etc. */
    private int weekOffset = 0;

    private final ExecutorService executor    = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

    private static final SimpleDateFormat DATE_DISPLAY =
            new SimpleDateFormat("MMM dd", Locale.ENGLISH);
    private static final SimpleDateFormat DATE_FULL =
            new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
    private static final SimpleDateFormat TIME_FMT =
            new SimpleDateFormat("MMM dd, yyyy  hh:mm a", Locale.ENGLISH);

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_weekly_report);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.weeklyReportRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, 0, bars.right, 0);
            View topNav = v.findViewById(R.id.topNavInclude);
            if (topNav != null) topNav.setPadding(
                    topNav.getPaddingLeft(), bars.top + 8,
                    topNav.getPaddingRight(), topNav.getPaddingBottom());
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("GoalReflectPrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        goalDao       = AppDatabase.getInstance(this).goalDao();
        reflectionDao = AppDatabase.getInstance(this).reflectionDao();

        bindViews();
        setupTopNav("WEEKLY REPORT");
        setupBottomNav(-1);   // no tab highlighted for this screen
        weekOffset = 0;
        setupWeekRange();
        loadWeeklyData();

        // View Goals button
        findViewById(R.id.btnViewGoals).setOnClickListener(v -> {
            startActivity(new Intent(this, GoalsActivity.class));
            overridePendingTransition(0, 0);
        });

        // Week navigation
        if (btnPrevWeek != null) {
            btnPrevWeek.setOnClickListener(v -> {
                weekOffset--;
                setupWeekRange();
                loadWeeklyData();
            });
        }
        if (btnNextWeek != null) {
            btnNextWeek.setOnClickListener(v -> {
                if (weekOffset < 0) {
                    weekOffset++;
                    setupWeekRange();
                    loadWeeklyData();
                }
            });
        }
    }

    // ── View binding ──────────────────────────────────────────────────────────

    private void bindViews() {
        tvWeekRange              = findViewById(R.id.tvWeekRange);
        tvWeekLabel              = findViewById(R.id.tvWeekLabel);
        tvMotivation             = findViewById(R.id.tvMotivation);
        tvActiveCount            = findViewById(R.id.tvActiveCount);
        tvAchievedWeekCount      = findViewById(R.id.tvAchievedWeekCount);
        tvReflectionsWeekCount   = findViewById(R.id.tvReflectionsWeekCount);
        tvNewGoalsWeekCount      = findViewById(R.id.tvNewGoalsWeekCount);
        recyclerWeekGoals        = findViewById(R.id.recyclerWeekGoals);
        recyclerWeekReflections  = findViewById(R.id.recyclerWeekReflections);
        layoutNoAchievedThisWeek = findViewById(R.id.layoutNoAchievedThisWeek);
        layoutNoReflectionsThisWeek = findViewById(R.id.layoutNoReflectionsThisWeek);
        btnPrevWeek              = findViewById(R.id.btnPrevWeek);
        btnNextWeek              = findViewById(R.id.btnNextWeek);

        recyclerWeekGoals.setLayoutManager(new LinearLayoutManager(this));
        recyclerWeekGoals.setNestedScrollingEnabled(false);
        recyclerWeekReflections.setLayoutManager(new LinearLayoutManager(this));
        recyclerWeekReflections.setNestedScrollingEnabled(false);
    }

    // ── Week range label ──────────────────────────────────────────────────────

    private void setupWeekRange() {
        long[] range = getWeekRange(weekOffset);
        String label = DATE_DISPLAY.format(new Date(range[0]))
                + " – " + DATE_DISPLAY.format(new Date(range[1]));
        if (tvWeekRange != null) tvWeekRange.setText(label);

        // Update week label
        if (tvWeekLabel != null) {
            if (weekOffset == 0) {
                tvWeekLabel.setText("This Week");
            } else if (weekOffset == -1) {
                tvWeekLabel.setText("Last Week");
            } else {
                tvWeekLabel.setText(Math.abs(weekOffset) + " Weeks Ago");
            }
        }

        // Hide next arrow when viewing current week
        if (btnNextWeek != null) {
            btnNextWeek.setAlpha(weekOffset == 0 ? 0.35f : 1.0f);
            btnNextWeek.setEnabled(weekOffset < 0);
        }
    }

    /** Returns {weekStartMillis, weekEndMillis} for the week at offset (0=current, -1=last, etc.). */
    private long[] getWeekRange(int offset) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.WEEK_OF_YEAR, offset);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long weekStart = cal.getTimeInMillis();

        cal.add(Calendar.DAY_OF_YEAR, 6);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        long weekEnd = cal.getTimeInMillis();

        return new long[]{ weekStart, weekEnd };
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    private void loadWeeklyData() {
        if (userId == -1) return;
        long[] range = getWeekRange(weekOffset);
        long weekStart = range[0];
        long weekEnd   = range[1];

        executor.execute(() -> {
            int active       = goalDao.countActiveGoals(userId);
            int totalAchieved = goalDao.countAchievedGoals(userId);

            List<Goal> achievedThisWeek = goalDao.getGoalsAchievedThisWeek(userId, weekStart, weekEnd);
            int newGoalsThisWeek        = goalDao.countGoalsCreatedThisWeek(userId, weekStart, weekEnd);
            int reflectionsThisWeek     = reflectionDao.countReflectionsThisWeek(userId, weekStart, weekEnd);
            List<Reflection> recentReflections =
                    reflectionDao.getRecentReflectionsThisWeek(userId, weekStart, weekEnd);

            // Persist stats to SharedPreferences so the notification can show them
            // even before the activity is opened
            getSharedPreferences("GoalReflectPrefs", MODE_PRIVATE).edit()
                    .putInt("stat_active_goals", active)
                    .putInt("stat_achieved_goals", totalAchieved)
                    .putInt("stat_total_reflections", reflectionDao.countTotalReflections(userId))
                    .apply();

            mainHandler.post(() -> {
                tvActiveCount.setText(String.valueOf(active));
                tvAchievedWeekCount.setText(String.valueOf(achievedThisWeek.size()));
                tvReflectionsWeekCount.setText(String.valueOf(reflectionsThisWeek));
                tvNewGoalsWeekCount.setText(String.valueOf(newGoalsThisWeek));

                applyMotivation(achievedThisWeek.size(), reflectionsThisWeek);

                // Achieved goals list
                if (achievedThisWeek.isEmpty()) {
                    layoutNoAchievedThisWeek.setVisibility(View.VISIBLE);
                    recyclerWeekGoals.setVisibility(View.GONE);
                } else {
                    layoutNoAchievedThisWeek.setVisibility(View.GONE);
                    recyclerWeekGoals.setVisibility(View.VISIBLE);
                    recyclerWeekGoals.setAdapter(new WeekGoalAdapter(achievedThisWeek));
                }

                // Reflections list
                if (recentReflections.isEmpty()) {
                    layoutNoReflectionsThisWeek.setVisibility(View.VISIBLE);
                    recyclerWeekReflections.setVisibility(View.GONE);
                } else {
                    layoutNoReflectionsThisWeek.setVisibility(View.GONE);
                    recyclerWeekReflections.setVisibility(View.VISIBLE);
                    recyclerWeekReflections.setAdapter(new WeekReflectionAdapter(recentReflections));
                }
            });
        });
    }

    // ── Motivational message ──────────────────────────────────────────────────

    private void applyMotivation(int achievedCount, int reflectionsCount) {
        if (tvMotivation == null) return;
        String msg;
        if (achievedCount >= 3) {
            msg = "Incredible week! 🔥 You crushed " + achievedCount + " goals! Keep the momentum going!";
        } else if (achievedCount >= 1) {
            msg = "Great work this week! 🌟 You achieved " + achievedCount
                    + " goal" + (achievedCount > 1 ? "s" : "") + ". Keep pushing forward!";
        } else if (reflectionsCount >= 3) {
            msg = "Reflection is the key to growth! 📝 You wrote " + reflectionsCount
                    + " reflections this week. Stay consistent!";
        } else {
            msg = "Every week is a fresh start. 🌱 Keep working on your goals — you've got this!";
        }
        tvMotivation.setText(msg);
    }

    // ── Inner adapters ────────────────────────────────────────────────────────

    /** Compact adapter for achieved goals in the weekly report. */
    private class WeekGoalAdapter extends RecyclerView.Adapter<WeekGoalAdapter.VH> {

        private final List<Goal> goals;

        WeekGoalAdapter(List<Goal> goals) {
            this.goals = goals;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_achieved_goal, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            Goal g = goals.get(position);
            if (h.tvTitle   != null) h.tvTitle.setText(g.title != null ? g.title : "");
            if (h.tvDate    != null) h.tvDate.setText(
                    g.targetDate != null && !g.targetDate.isEmpty()
                    ? g.targetDate
                    : DATE_FULL.format(new Date(g.createdAt)));
            if (h.tvDesc    != null) {
                if (g.description != null && !g.description.isEmpty()) {
                    h.tvDesc.setText(g.description);
                    h.tvDesc.setVisibility(View.VISIBLE);
                } else {
                    h.tvDesc.setVisibility(View.GONE);
                }
            }
            if (h.tvCategory != null) h.tvCategory.setText(getCategoryLabel(g));
        }

        @Override
        public int getItemCount() { return goals.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDate, tvDesc, tvCategory;
            VH(View v) {
                super(v);
                tvTitle    = v.findViewById(R.id.tvAchievedTitle);
                tvDate     = v.findViewById(R.id.tvAchievedDate);
                tvDesc     = v.findViewById(R.id.tvAchievedDesc);
                tvCategory = v.findViewById(R.id.tvAchievedCategory);
            }
        }

        private String getCategoryLabel(Goal goal) {
            if (goal.title == null) return "✨ Personal";
            String t = goal.title.toLowerCase(Locale.getDefault());
            if (t.contains("health") || t.contains("gym") || t.contains("exercise") || t.contains("run"))
                return "💪 Health";
            if (t.contains("study") || t.contains("learn") || t.contains("course") || t.contains("read"))
                return "📚 Study";
            if (t.contains("work") || t.contains("job") || t.contains("career") || t.contains("project"))
                return "💼 Career";
            if (t.contains("finance") || t.contains("save") || t.contains("money") || t.contains("invest"))
                return "💰 Finance";
            return "✨ Personal";
        }
    }

    /** Compact adapter for reflections in the weekly report. */
    private class WeekReflectionAdapter extends RecyclerView.Adapter<WeekReflectionAdapter.VH> {

        private final List<Reflection> reflections;

        WeekReflectionAdapter(List<Reflection> reflections) {
            this.reflections = reflections;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_reflection, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            Reflection r = reflections.get(position);
            if (h.tvContent != null) h.tvContent.setText(r.content != null ? r.content : "");
            if (h.tvDate    != null) h.tvDate.setText(TIME_FMT.format(new Date(r.createdAt)));
        }

        @Override
        public int getItemCount() { return reflections.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvContent, tvDate;
            VH(View v) {
                super(v);
                tvContent = v.findViewById(R.id.tvReflectionContent);
                tvDate    = v.findViewById(R.id.tvReflectionDate);
            }
        }
    }
}

