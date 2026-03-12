package com.example.personal_refelection;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personal_refelection.database.AppDatabase;
import com.example.personal_refelection.database.Goal;
import com.example.personal_refelection.database.GoalDao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Goals screen — lists active goals with stats header, FAB to add,
 * edit, delete and mark-achieved. Filter chips removed.
 */
public class GoalsActivity extends BaseActivity {

    // ── Views ─────────────────────────────────────────────────────────────
    private RecyclerView recyclerGoals;
    private LinearLayout layoutEmptyState;
    private TextView tvHeaderActiveCount, tvHeaderAchievedCount;
    private LinearLayout fabAddGoal;

    // ── Data ──────────────────────────────────────────────────────────────
    private GoalAdapter adapter;
    private GoalDao goalDao;
    private int userId = -1;

    private List<Goal> activeGoals   = new ArrayList<>();
    private List<Goal> achievedGoals = new ArrayList<>();

    private final ExecutorService executor   = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

    // ── Launcher ──────────────────────────────────────────────────────────
    private final ActivityResultLauncher<Intent> addGoalLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) loadGoals();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.goal_activity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.goalsRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, 0, bars.right, 0);
            View topNav = v.findViewById(R.id.topNavInclude);
            if (topNav != null) topNav.setPadding(
                    topNav.getPaddingLeft(), bars.top + 8,
                    topNav.getPaddingRight(), topNav.getPaddingBottom());
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("GoalReflectPrefs", MODE_PRIVATE);
        userId  = prefs.getInt("user_id", -1);
        goalDao = AppDatabase.getInstance(this).goalDao();

        bindViews();
        setupTopNav("MY GOALS");
        setupRecycler();
        setupFab();
        loadGoals();
        setupBottomNav(R.id.navGoals);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGoals();
        setupTopNav("MY GOALS");
    }

    // ── Bind ──────────────────────────────────────────────────────────────

    private void bindViews() {
        recyclerGoals         = findViewById(R.id.recyclerGoals);
        layoutEmptyState      = findViewById(R.id.layoutEmptyState);
        tvHeaderActiveCount   = findViewById(R.id.tvHeaderActiveCount);
        tvHeaderAchievedCount = findViewById(R.id.tvHeaderAchievedCount);
        fabAddGoal            = findViewById(R.id.fabAddGoal);

        View btnAddEmpty = findViewById(R.id.btnAddGoalEmpty);
        if (btnAddEmpty != null) btnAddEmpty.setOnClickListener(v -> openAddGoalScreen());
    }

    private void setupRecycler() {
        adapter = new GoalAdapter(this, new GoalAdapter.GoalActionListener() {
            @Override public void onEdit(Goal goal)         { openEditGoalScreen(goal); }
            @Override public void onDelete(Goal goal)       { confirmDelete(goal); }
            @Override public void onMarkAchieved(Goal goal) { markAchieved(goal); }
        });
        recyclerGoals.setLayoutManager(new LinearLayoutManager(this));
        recyclerGoals.setAdapter(adapter);
    }

    private void setupFab() {
        if (fabAddGoal != null) fabAddGoal.setOnClickListener(v -> openAddGoalScreen());
    }

    private void openAddGoalScreen() {
        Intent intent = new Intent(this, AddGoalActivity.class);
        addGoalLauncher.launch(intent);
        overridePendingTransition(android.R.anim.fade_in, 0);
    }

    private void openEditGoalScreen(Goal goal) {
        Intent intent = new Intent(this, AddGoalActivity.class);
        intent.putExtra(AddGoalActivity.EXTRA_GOAL_ID,    goal.id);
        intent.putExtra(AddGoalActivity.EXTRA_GOAL_TITLE, goal.title);
        intent.putExtra(AddGoalActivity.EXTRA_GOAL_DESC,  goal.description);
        intent.putExtra(AddGoalActivity.EXTRA_GOAL_DATE,  goal.targetDate);
        addGoalLauncher.launch(intent);
        overridePendingTransition(android.R.anim.fade_in, 0);
    }

    // ── Load ──────────────────────────────────────────────────────────────

    private void loadGoals() {
        if (userId == -1) return;
        executor.execute(() -> {
            activeGoals   = goalDao.getActiveGoals(userId);
            achievedGoals = goalDao.getAchievedGoals(userId);
            int activeCount   = activeGoals.size();
            int achievedCount = achievedGoals.size();

            // Persist stats so Weekly Summary notification can read up-to-date numbers
            getSharedPreferences("GoalReflectPrefs", MODE_PRIVATE).edit()
                    .putInt("stat_active_goals", activeCount)
                    .putInt("stat_achieved_goals", achievedCount)
                    .apply();

            mainHandler.post(() -> {
                if (tvHeaderActiveCount   != null) tvHeaderActiveCount.setText(String.valueOf(activeCount));
                if (tvHeaderAchievedCount != null) tvHeaderAchievedCount.setText(String.valueOf(achievedCount));
                displayGoals(activeGoals);
            });
        });
    }

    private void displayGoals(List<Goal> goals) {
        adapter.setGoals(goals);
        boolean empty = goals.isEmpty();
        if (recyclerGoals    != null) recyclerGoals.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (layoutEmptyState != null) layoutEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    // ── Delete ────────────────────────────────────────────────────────────

    private void confirmDelete(Goal goal) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Goal")
                .setMessage("Are you sure you want to delete \"" + goal.title + "\"?")
                .setPositiveButton("Delete", (d, w) -> executor.execute(() -> {
                    goalDao.deleteGoal(goal);
                    mainHandler.post(() -> {
                        Toast.makeText(this, "Goal deleted", Toast.LENGTH_SHORT).show();
                        loadGoals();
                    });
                }))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Mark Achieved ─────────────────────────────────────────────────────

    private void markAchieved(Goal goal) {
        new AlertDialog.Builder(this)
                .setTitle("Mark as Achieved?")
                .setMessage("Great work! Mark \"" + goal.title + "\" as achieved?")
                .setPositiveButton("Yes!", (d, w) -> executor.execute(() -> {
                    goalDao.markGoalCompleted(goal.id);

                    // Cancel any pending deadline / warning alarms for this goal
                    NotificationHelper.cancelGoalDeadline(this, goal.id);
                    NotificationHelper.cancelGoalDeadlineWarning(this, goal.id);

                    // Read fresh counts and persist them for Weekly Summary notification
                    int newActive   = goalDao.countActiveGoals(userId);
                    int newAchieved = goalDao.countAchievedGoals(userId);
                    android.content.SharedPreferences prefs =
                            getSharedPreferences("GoalReflectPrefs", MODE_PRIVATE);
                    prefs.edit()
                            .putInt("stat_active_goals",   newActive)
                            .putInt("stat_achieved_goals", newAchieved)
                            .apply();

                    // Fire Achievement Alert notification if the toggle is enabled
                    if (prefs.getBoolean("notif_achievements", true)) {
                        NotificationHelper.postAchievementAlert(this, goal.title);
                    }

                    mainHandler.post(() -> {
                        Toast.makeText(this, "Goal achieved!", Toast.LENGTH_SHORT).show();
                        loadGoals();
                    });
                }))
                .setNegativeButton("Not yet", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
