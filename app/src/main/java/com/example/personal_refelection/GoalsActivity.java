package com.example.personal_refelection;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personal_refelection.database.AppDatabase;
import com.example.personal_refelection.database.Goal;
import com.example.personal_refelection.database.GoalDao;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Goals screen — lists active & achieved goals with filter chips,
 * stats header, FAB to add, edit, delete and mark-achieved.
 */
public class GoalsActivity extends BaseActivity {

    // ── Views ─────────────────────────────────────────────────────────────
    private RecyclerView recyclerGoals;
    private LinearLayout layoutEmptyState;
    private TextView tvHeaderActiveCount, tvHeaderAchievedCount;
    private TextView chipAll, chipActive, chipAchieved;
    private LinearLayout fabAddGoal;

    // ── Data ──────────────────────────────────────────────────────────────
    private GoalAdapter adapter;
    private GoalDao goalDao;
    private int userId = -1;

    private List<Goal> allGoals    = new ArrayList<>();
    private List<Goal> activeGoals = new ArrayList<>();
    private List<Goal> achievedGoals = new ArrayList<>();

    private static final int FILTER_ALL      = 0;
    private static final int FILTER_ACTIVE   = 1;
    private static final int FILTER_ACHIEVED = 2;
    private int currentFilter = FILTER_ALL;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ── Chip colours ──────────────────────────────────────────────────────
    private static final int COLOR_CHIP_SELECTED_TEXT   = 0xFFFFFFFF;
    private static final int COLOR_CHIP_UNSELECTED_TEXT = 0xFF6B7280;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.goal_activity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.goalsRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, 0);
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("GoalReflectPrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        goalDao = AppDatabase.getInstance(this).goalDao();

        bindViews();
        setupRecycler();
        setupChips();
        setupFab();
        loadGoals();
        setupBottomNav(R.id.navGoals);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGoals();
    }

    // ── Bind ──────────────────────────────────────────────────────────────

    private void bindViews() {
        recyclerGoals        = findViewById(R.id.recyclerGoals);
        layoutEmptyState     = findViewById(R.id.layoutEmptyState);
        tvHeaderActiveCount  = findViewById(R.id.tvHeaderActiveCount);
        tvHeaderAchievedCount= findViewById(R.id.tvHeaderAchievedCount);
        chipAll              = findViewById(R.id.chipAll);
        chipActive           = findViewById(R.id.chipActive);
        chipAchieved         = findViewById(R.id.chipAchieved);
        fabAddGoal           = findViewById(R.id.fabAddGoal);

        // Empty state button
        View btnAddEmpty = findViewById(R.id.btnAddGoalEmpty);
        if (btnAddEmpty != null) {
            btnAddEmpty.setOnClickListener(v -> showAddEditDialog(null));
        }
    }

    private void setupRecycler() {
        adapter = new GoalAdapter(this, new GoalAdapter.GoalActionListener() {
            @Override public void onEdit(Goal goal)         { showAddEditDialog(goal); }
            @Override public void onDelete(Goal goal)       { confirmDelete(goal); }
            @Override public void onMarkAchieved(Goal goal) { markAchieved(goal); }
        });
        recyclerGoals.setLayoutManager(new LinearLayoutManager(this));
        recyclerGoals.setAdapter(adapter);
    }

    private void setupChips() {
        chipAll.setOnClickListener(v -> setFilter(FILTER_ALL));
        chipActive.setOnClickListener(v -> setFilter(FILTER_ACTIVE));
        chipAchieved.setOnClickListener(v -> setFilter(FILTER_ACHIEVED));
        updateChipUI();
    }

    private void setupFab() {
        fabAddGoal.setOnClickListener(v -> showAddEditDialog(null));
    }

    // ── Filter ────────────────────────────────────────────────────────────

    private void setFilter(int filter) {
        currentFilter = filter;
        updateChipUI();
        applyFilter();
    }

    private void updateChipUI() {
        styleChip(chipAll,      currentFilter == FILTER_ALL);
        styleChip(chipActive,   currentFilter == FILTER_ACTIVE);
        styleChip(chipAchieved, currentFilter == FILTER_ACHIEVED);
    }

    private void styleChip(TextView chip, boolean selected) {
        if (selected) {
            chip.setBackgroundResource(R.drawable.bg_chip_active_pill);
            chip.setTextColor(0xFFFFFFFF);
            chip.setAlpha(1.0f);
        } else {
            chip.setBackgroundResource(R.drawable.bg_avatar_circle_white);
            chip.setTextColor(0xFFFFFFFF);
            chip.setAlpha(0.70f);
        }
    }

    private void applyFilter() {
        List<Goal> toShow;
        switch (currentFilter) {
            case FILTER_ACTIVE:   toShow = activeGoals;   break;
            case FILTER_ACHIEVED: toShow = achievedGoals; break;
            default:              toShow = allGoals;       break;
        }
        adapter.setGoals(toShow);
        int count = toShow.size();

        boolean empty = (count == 0);
        recyclerGoals.setVisibility(empty ? View.GONE : View.VISIBLE);
        layoutEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    // ── Load ──────────────────────────────────────────────────────────────

    private void loadGoals() {
        if (userId == -1) return;
        executor.execute(() -> {
            allGoals      = goalDao.getAllGoals(userId);
            activeGoals   = goalDao.getActiveGoals(userId);
            achievedGoals = goalDao.getAchievedGoals(userId);
            int activeCount   = activeGoals.size();
            int achievedCount = achievedGoals.size();
            mainHandler.post(() -> {
                tvHeaderActiveCount.setText(String.valueOf(activeCount));
                tvHeaderAchievedCount.setText(String.valueOf(achievedCount));
                applyFilter();
            });
        });
    }

    // ── Add / Edit dialog ─────────────────────────────────────────────────

    private void showAddEditDialog(Goal existingGoal) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_goal, null);
        TextInputEditText etTitle = dialogView.findViewById(R.id.etGoalTitle);
        TextInputEditText etDesc  = dialogView.findViewById(R.id.etGoalDescription);

        boolean isEdit = (existingGoal != null);
        if (isEdit) {
            etTitle.setText(existingGoal.title);
            etDesc.setText(existingGoal.description);
        }

        new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Edit Goal" : "Add New Goal")
                .setView(dialogView)
                .setPositiveButton(isEdit ? "Save" : "Add", (d, w) -> {
                    String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
                    String desc  = etDesc.getText()  != null ? etDesc.getText().toString().trim()  : "";

                    if (TextUtils.isEmpty(title)) {
                        Toast.makeText(this, "Please enter a goal title", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isEdit) {
                        existingGoal.title = title;
                        existingGoal.description = desc;
                        executor.execute(() -> {
                            goalDao.updateGoal(existingGoal);
                            mainHandler.post(() -> {
                                Toast.makeText(this, "Goal updated ✅", Toast.LENGTH_SHORT).show();
                                loadGoals();
                            });
                        });
                    } else {
                        Goal newGoal = new Goal(userId, title, desc, null);
                        executor.execute(() -> {
                            goalDao.insertGoal(newGoal);
                            mainHandler.post(() -> {
                                Toast.makeText(this, "Goal added 🎯", Toast.LENGTH_SHORT).show();
                                loadGoals();
                            });
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Delete ────────────────────────────────────────────────────────────

    private void confirmDelete(Goal goal) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Goal")
                .setMessage("Are you sure you want to delete \"" + goal.title + "\"? This cannot be undone.")
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
                .setTitle("Mark as Achieved? 🏆")
                .setMessage("Great work! Mark \"" + goal.title + "\" as achieved?")
                .setPositiveButton("Yes!", (d, w) -> executor.execute(() -> {
                    goalDao.markGoalCompleted(goal.id);
                    mainHandler.post(() -> {
                        Toast.makeText(this, "Goal achieved! 🏆", Toast.LENGTH_SHORT).show();
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
