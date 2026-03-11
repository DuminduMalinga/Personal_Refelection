package com.example.personal_refelection;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personal_refelection.database.AppDatabase;
import com.example.personal_refelection.database.Goal;
import com.example.personal_refelection.database.GoalDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Screen displaying all completed goals.
 */
public class AchievedActivity extends BaseActivity {

    private TextView tvAchievedCount;
    private RecyclerView recyclerAchieved;

    private GoalDao goalDao;
    private int userId = -1;

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

        tvAchievedCount  = findViewById(R.id.tvAchievedCount);
        recyclerAchieved = findViewById(R.id.recyclerAchieved);

        if (recyclerAchieved != null) {
            recyclerAchieved.setLayoutManager(new LinearLayoutManager(this));
        }

        setupTopNav("ACHIEVED GOALS");
        setupBottomNav(R.id.navAchieved);
        loadAchievedGoals();
    }

    private void loadAchievedGoals() {
        executor.execute(() -> {
            List<Goal> goals = goalDao.getAchievedGoals(userId);
            int count = goals.size();
            mainHandler.post(() -> {
                if (tvAchievedCount != null) tvAchievedCount.setText(String.valueOf(count));
                // Show/hide empty state
                View emptyLayout = findViewById(R.id.layoutAchievedEmpty);
                if (emptyLayout != null) {
                    emptyLayout.setVisibility(count == 0 ? android.view.View.VISIBLE : android.view.View.GONE);
                }
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
