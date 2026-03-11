package com.example.personal_refelection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.personal_refelection.database.DashboardRepository;
import com.example.personal_refelection.database.Reflection;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Dashboard screen showing overview of user's personal growth journey.
 */
public class DashboardActivity extends BaseActivity {

    private TextView tvGreeting, tvActiveGoalsCount, tvAchievedGoalsCount, tvTotalReflectionsCount;
    private TextView tvNoReflections;
    private LinearLayout recentReflectionsContainer;
    private ImageView ivDashboardProfileImage;

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
            return insets;
        });

        dashboardRepository = new DashboardRepository(this);
        sharedPreferences = getSharedPreferences("GoalReflectPrefs", MODE_PRIVATE);

        // Get user info from SharedPreferences
        userId = sharedPreferences.getInt("user_id", -1);
        userName = sharedPreferences.getString("user_name", "User");

        // Redirect to login if not logged in
        if (userId == -1) {
            // Check Firebase for active social session
            com.google.firebase.auth.FirebaseUser fbUser =
                    com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (fbUser == null) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }
            // Firebase session exists but local prefs lost — will be fixed in SplashActivity
            // Use Firebase display name as fallback
            userName = fbUser.getDisplayName() != null ? fbUser.getDisplayName() : "User";
        }

        bindViews();
        loadProfileAvatar();
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
        ivDashboardProfileImage = findViewById(R.id.ivDashboardProfileImage);
    }

    /**
     * Load the saved profile avatar into the dashboard header icon.
     * Priority: 1) local file saved by user, 2) Google/Facebook photo URL, 3) default icon
     */
    private void loadProfileAvatar() {
        if (ivDashboardProfileImage == null) return;

        // 1 — User-selected local photo (highest priority)
        String savedPath = sharedPreferences.getString("avatar_path", null);
        if (savedPath != null) {
            File f = new File(savedPath);
            if (f.exists()) {
                Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
                if (bmp != null) {
                    ivDashboardProfileImage.setImageBitmap(bmp);
                    ivDashboardProfileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    ivDashboardProfileImage.setBackground(
                            ContextCompat.getDrawable(this, R.drawable.bg_avatar_circle_white));
                    ivDashboardProfileImage.setPadding(0, 0, 0, 0);
                    ivDashboardProfileImage.setClipToOutline(true);
                    return;
                }
            }
        }

        // 2 — Google / Facebook social photo URL
        String socialPhotoUrl = sharedPreferences.getString("social_photo_url", null);
        if (socialPhotoUrl == null || socialPhotoUrl.isEmpty()) {
            // Also check FirebaseAuth directly in case prefs were lost
            com.google.firebase.auth.FirebaseUser fbUser =
                    com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (fbUser != null && fbUser.getPhotoUrl() != null) {
                socialPhotoUrl = fbUser.getPhotoUrl().toString();
            }
        }

        if (socialPhotoUrl != null && !socialPhotoUrl.isEmpty()) {
            ivDashboardProfileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ivDashboardProfileImage.setBackground(
                    ContextCompat.getDrawable(this, R.drawable.bg_avatar_circle_white));
            ivDashboardProfileImage.setPadding(0, 0, 0, 0);
            ivDashboardProfileImage.setClipToOutline(true);
            com.bumptech.glide.Glide.with(this)
                    .load(socialPhotoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(ivDashboardProfileImage);
            return;
        }

        // 3 — Default icon fallback
        ivDashboardProfileImage.setImageResource(R.drawable.ic_profile);
        ivDashboardProfileImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        int pad = Math.round(9 * getResources().getDisplayMetrics().density);
        ivDashboardProfileImage.setPadding(pad, pad, pad, pad);
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

        tvGreeting.setText(getString(R.string.greeting_format, greeting, userName));
    }

    private void setupClickListeners() {

        findViewById(R.id.btnViewGoals).setOnClickListener(v -> {
            startActivity(new Intent(this, GoalsActivity.class));
            overridePendingTransition(0, 0);
        });

        findViewById(R.id.btnViewAchieved).setOnClickListener(v -> {
            startActivity(new Intent(this, AchievedActivity.class));
            overridePendingTransition(0, 0);
        });

        // Total Reflections card → open ReflectionsActivity
        findViewById(R.id.cardTotalReflections).setOnClickListener(v -> openReflections());

        // "See All" text next to Recent Reflections
        View seeAll = findViewById(R.id.tvSeeAllReflections);
        if (seeAll != null) seeAll.setOnClickListener(v -> openReflections());

        // Shared bottom nav – BaseActivity handles all nav item wiring
        setupBottomNav(R.id.navDashboard);

        // Profile icon in header
        findViewById(R.id.ivProfileIcon).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(0, 0);
        });
    }

    /**
     * Navigate to the Reflections screen.
     */
    private void openReflections() {
        startActivity(new Intent(this, ReflectionsActivity.class));
        overridePendingTransition(0, 0);
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
        loadProfileAvatar();
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
        // Sign out from Firebase (covers Google + Facebook sessions)
        FirebaseAuth.getInstance().signOut();

        sharedPreferences.edit().clear().apply();

        Toast.makeText(this, "Logged out successfully 👋", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
