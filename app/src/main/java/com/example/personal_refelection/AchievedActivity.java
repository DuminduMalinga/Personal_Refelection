package com.example.personal_refelection;

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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personal_refelection.database.AppDatabase;
import com.example.personal_refelection.database.Goal;
import com.example.personal_refelection.database.GoalDao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Screen displaying all completed goals with modern card UI,
 * filter tabs (This Month / All Time), and animated entrance.
 */
public class AchievedActivity extends BaseActivity {

    private TextView tvAchievedCount;
    private RecyclerView recyclerAchieved;
    private AchievedGoalAdapter adapter;

    private LinearLayout tabThisMonth, tabAllTime;
    private LinearLayout layoutEmpty;

    private GoalDao goalDao;
    private int userId = -1;

    private List<Goal> allGoals = new ArrayList<>();
    private boolean showingThisMonth = true;

    private final ExecutorService executor    = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.goal_achieved);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.achievedRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, 0);
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("GoalReflectPrefs", MODE_PRIVATE);
        userId  = prefs.getInt("user_id", -1);
        goalDao = AppDatabase.getInstance(this).goalDao();

        bindViews();
        setupTabs();
        setupAdapter();

        setupTopNav("ACHIEVED GOALS");
        setupBottomNav(R.id.navAchieved);
        loadAchievedGoals();
    }

    private void bindViews() {
        tvAchievedCount  = findViewById(R.id.tvAchievedCount);
        recyclerAchieved = findViewById(R.id.recyclerAchieved);
        tabThisMonth     = findViewById(R.id.tabThisMonth);
        tabAllTime       = findViewById(R.id.tabAllTime);
        layoutEmpty      = findViewById(R.id.layoutAchievedEmpty);

        LinearLayout btnViewActiveGoals = findViewById(R.id.btnViewActiveGoals);
        if (btnViewActiveGoals != null) {
            btnViewActiveGoals.setOnClickListener(v -> {
                startActivity(new Intent(this, GoalsActivity.class));
                overridePendingTransition(0, 0);
            });
        }
    }

    private void setupAdapter() {
        adapter = new AchievedGoalAdapter(this, goal ->
                Toast.makeText(this, "🏆 " + goal.title, Toast.LENGTH_SHORT).show());
        if (recyclerAchieved != null) {
            recyclerAchieved.setLayoutManager(new LinearLayoutManager(this));
            recyclerAchieved.setAdapter(adapter);
        }
    }

    private void setupTabs() {
        if (tabThisMonth != null) {
            tabThisMonth.setOnClickListener(v -> {
                showingThisMonth = true;
                updateTabUI();
                filterAndDisplay();
            });
        }
        if (tabAllTime != null) {
            tabAllTime.setOnClickListener(v -> {
                showingThisMonth = false;
                updateTabUI();
                filterAndDisplay();
            });
        }
    }

    private void updateTabUI() {
        if (tabThisMonth == null || tabAllTime == null) return;
        tabThisMonth.setBackgroundResource(showingThisMonth
                ? R.drawable.bg_category_chip_selected
                : R.drawable.bg_category_chip_unselected);
        tabAllTime.setBackgroundResource(showingThisMonth
                ? R.drawable.bg_category_chip_unselected
                : R.drawable.bg_category_chip_selected);

        TextView tvM = findViewById(R.id.tvTabThisMonth);
        TextView tvA = findViewById(R.id.tvTabAllTime);
        if (tvM != null) tvM.setTextColor(showingThisMonth ? 0xFFFFFFFF : 0xFF4A3FCC);
        if (tvA != null) tvA.setTextColor(showingThisMonth ? 0xFF4A3FCC : 0xFFFFFFFF);
    }

    private void filterAndDisplay() {
        if (allGoals.isEmpty()) {
            showEmpty(true);
            return;
        }

        List<Goal> filtered;
        if (showingThisMonth) {
            Calendar cal = Calendar.getInstance();
            int curMonth = cal.get(Calendar.MONTH);
            int curYear  = cal.get(Calendar.YEAR);
            filtered = new ArrayList<>();
            for (Goal g : allGoals) {
                Calendar gc = Calendar.getInstance();
                gc.setTimeInMillis(g.createdAt);
                if (gc.get(Calendar.MONTH) == curMonth && gc.get(Calendar.YEAR) == curYear) {
                    filtered.add(g);
                }
            }
        } else {
            filtered = new ArrayList<>(allGoals);
        }

        adapter.setGoals(filtered);
        showEmpty(filtered.isEmpty());

        if (tvAchievedCount != null) {
            tvAchievedCount.setText(String.valueOf(allGoals.size()));
        }
    }

    private void showEmpty(boolean empty) {
        if (recyclerAchieved != null)
            recyclerAchieved.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (layoutEmpty != null)
            layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void loadAchievedGoals() {
        executor.execute(() -> {
            List<Goal> goals = userId == -1 ? new ArrayList<>() : goalDao.getAchievedGoals(userId);
            mainHandler.post(() -> {
                allGoals = goals;
                filterAndDisplay();
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAchievedGoals();
        setupTopNav("ACHIEVED GOALS");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
