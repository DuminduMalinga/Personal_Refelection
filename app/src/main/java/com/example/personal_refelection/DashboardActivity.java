package com.example.personal_refelection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.personal_refelection.database.DashboardRepository;
import com.example.personal_refelection.database.Reflection;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Dashboard screen showing overview of user's personal growth journey.
 * Displays active goals, achieved goals, total reflections, and recent activity.
 */
public class DashboardActivity extends AppCompatActivity {

    private TextView tvGreeting, tvActiveGoalsCount, tvAchievedGoalsCount, tvTotalReflectionsCount;
    private TextView tvNoReflections;
    private LinearLayout recentReflectionsContainer;
    private BottomNavigationView bottomNavigation;

    private DashboardRepository dashboardRepository;
    private SharedPreferences sharedPreferences;

    private int userId;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.dashboard_activity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboardRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            findViewById(R.id.bottomNavigation).setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        dashboardRepository = new DashboardRepository(this);
        sharedPreferences = getSharedPreferences("GoalReflectPrefs", MODE_PRIVATE);

        // Get user info from SharedPreferences
        userId = sharedPreferences.getInt("user_id", -1);
        userName = sharedPreferences.getString("user_name", "User");

        // Redirect to login if not logged in
        if (userId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        bindViews();
        setupGreeting();
        setupClickListeners();
        loadDashboardData();
    }

    private void bindViews() {
        tvGreeting = findViewById(R.id.tvGreeting);
        tvActiveGoalsCount = findViewById(R.id.tvActiveGoalsCount);
        tvAchievedGoalsCount = findViewById(R.id.tvAchievedGoalsCount);
        tvTotalReflectionsCount = findViewById(R.id.tvTotalReflectionsCount);
        tvNoReflections = findViewById(R.id.tvNoReflections);
        recentReflectionsContainer = findViewById(R.id.recentReflectionsContainer);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    /**
     * Set greeting based on time of day.
     */
    private void setupGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (hour < 12) {
            greeting = getString(R.string.greeting_morning);
        } else if (hour < 18) {
            greeting = getString(R.string.greeting_afternoon);
        } else {
            greeting = getString(R.string.greeting_evening);
        }

        tvGreeting.setText(greeting + ", " + userName + " 👋");
    }

    /**
     * Animate stat cards with staggered fade-in effect.
     */
    private void animateCards() {
        Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);

        findViewById(R.id.cardActiveGoals).startAnimation(scaleUp);

        findViewById(R.id.cardAchievedGoals).postDelayed(() -> {
            Animation scaleUp2 = AnimationUtils.loadAnimation(this, R.anim.scale_up);
            findViewById(R.id.cardAchievedGoals).startAnimation(scaleUp2);
        }, 100);

        findViewById(R.id.cardTotalReflections).postDelayed(() -> {
            Animation scaleUp3 = AnimationUtils.loadAnimation(this, R.anim.scale_up);
            findViewById(R.id.cardTotalReflections).startAnimation(scaleUp3);
        }, 200);
    }

    private void setupClickListeners() {

        findViewById(R.id.btnViewGoals).setOnClickListener(v -> {
            // TODO: Navigate to Goals screen
            Toast.makeText(this, "Goals screen coming soon! 📋", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnViewAchieved).setOnClickListener(v -> {
            // TODO: Navigate to Achieved screen
            Toast.makeText(this, "Achieved screen coming soon! 🏆", Toast.LENGTH_SHORT).show();
        });

        // Bottom Navigation
        bottomNavigation.setSelectedItemId(R.id.nav_dashboard);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {
                return true; // Already on dashboard
            } else if (itemId == R.id.nav_goals) {
                Toast.makeText(this, "Goals screen coming soon! 📋", Toast.LENGTH_SHORT).show();
                return false;
            } else if (itemId == R.id.nav_add) {
                Toast.makeText(this, "Add Goal feature coming soon! 🎯", Toast.LENGTH_SHORT).show();
                return false;
            } else if (itemId == R.id.nav_achieved) {
                Toast.makeText(this, "Achieved screen coming soon! 🏆", Toast.LENGTH_SHORT).show();
                return false;
            } else if (itemId == R.id.nav_profile) {
                Toast.makeText(this, "Profile screen coming soon! 👤", Toast.LENGTH_SHORT).show();
                return false;
            }
            return false;
        });

        // Profile icon click
        findViewById(R.id.ivProfileIcon).setOnClickListener(v -> {
            Toast.makeText(this, "Profile screen coming soon! 👤", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Load dashboard statistics and recent reflections.
     */
    private void loadDashboardData() {
        // Load stats
        dashboardRepository.getDashboardStats(userId, (activeGoals, achievedGoals, totalReflections) -> {
            tvActiveGoalsCount.setText(String.valueOf(activeGoals));
            tvAchievedGoalsCount.setText(String.valueOf(achievedGoals));
            tvTotalReflectionsCount.setText(String.valueOf(totalReflections));
        });

        // Load recent reflections
        dashboardRepository.getRecentReflections(userId, this::displayRecentReflections);
    }

    /**
     * Display recent reflections or show empty state.
     */
    private void displayRecentReflections(List<Reflection> reflections) {
        recentReflectionsContainer.removeAllViews();

        if (reflections == null || reflections.isEmpty()) {
            tvNoReflections.setVisibility(View.VISIBLE);
            recentReflectionsContainer.addView(tvNoReflections);
        } else {
            tvNoReflections.setVisibility(View.GONE);

            for (Reflection reflection : reflections) {
                View itemView = LayoutInflater.from(this)
                        .inflate(R.layout.item_reflection, recentReflectionsContainer, false);

                TextView tvDate = itemView.findViewById(R.id.tvReflectionDate);
                TextView tvContent = itemView.findViewById(R.id.tvReflectionContent);

                tvDate.setText(formatReflectionDate(reflection.createdAt));
                tvContent.setText(reflection.content);

                recentReflectionsContainer.addView(itemView);
            }
        }
    }

    /**
     * Format timestamp to human-readable date.
     */
    private String formatReflectionDate(long timestamp) {
        Date date = new Date(timestamp);
        Calendar reflectionCal = Calendar.getInstance();
        reflectionCal.setTime(date);
        Calendar today = Calendar.getInstance();

        // Check if today
        if (reflectionCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            reflectionCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            return "Today at " + timeFormat.format(date);
        }

        // Check if yesterday
        today.add(Calendar.DAY_OF_YEAR, -1);
        if (reflectionCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            reflectionCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            return "Yesterday at " + timeFormat.format(date);
        }

        // Otherwise show full date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        return dateFormat.format(date);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to dashboard
        loadDashboardData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Log out user and clear session data.
     */
    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "Logged out successfully 👋", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

