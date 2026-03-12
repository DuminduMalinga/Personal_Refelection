package com.example.personal_refelection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.personal_refelection.database.AppDatabase;
import com.example.personal_refelection.database.DashboardRepository;
import com.example.personal_refelection.database.Goal;
import com.example.personal_refelection.database.GoalDao;
import com.example.personal_refelection.database.Reflection;
import com.example.personal_refelection.database.ReflectionDao;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private ReflectionDao reflectionDao;
    private GoalDao goalDao;
    private List<Goal> cachedGoals = new ArrayList<>();
    private String selectedMood = "";

    private final ExecutorService executor    = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

    private int userId;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.dashboard_activity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboardRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, 0, bars.right, 0);
            View topNav = v.findViewById(R.id.topNavInclude);
            if (topNav != null) topNav.setPadding(
                    topNav.getPaddingLeft(), bars.top + 8,
                    topNav.getPaddingRight(), topNav.getPaddingBottom());
            return insets;
        });

        dashboardRepository = new DashboardRepository(this);
        sharedPreferences = getSharedPreferences("GoalReflectPrefs", MODE_PRIVATE);

        AppDatabase db = AppDatabase.getInstance(this);
        reflectionDao  = db.reflectionDao();
        goalDao        = db.goalDao();

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
        initNotifications();
    }

    /**
     * Ensure notification channels exist and schedule alarms that have never been set up yet.
     * This is safe to call every time — channels are idempotent, and alarms with the same
     * PendingIntent simply update the existing one.
     */
    private void initNotifications() {
        NotificationHelper.createChannels(this);

        // Schedule Goal Reminders if the pref is enabled (default true = first-run case)
        if (sharedPreferences.getBoolean("notif_goal_reminders", true)) {
            NotificationHelper.scheduleGoalReminder(this);
        }

        // Schedule Reflection Prompts if the pref is enabled (default true = first-run case)
        if (sharedPreferences.getBoolean("notif_reflection_prompts", true)) {
            NotificationHelper.scheduleReflectionPrompt(this);
        }

        // Weekly Summary is off by default — only schedule if user explicitly enabled it
        if (sharedPreferences.getBoolean("notif_weekly_summary", false)) {
            NotificationHelper.scheduleWeeklySummary(this);
        }
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


        // "See All" → navigate to ReflectionsActivity to see full list
        View seeAll = findViewById(R.id.tvSeeAllReflections);
        if (seeAll != null) seeAll.setOnClickListener(v -> openReflections());

        // "+ Add" button next to Recent Reflections → open bottom sheet
        View btnDashAdd = findViewById(R.id.btnDashAddReflection);
        if (btnDashAdd != null) btnDashAdd.setOnClickListener(v -> openAddReflectionSheet());

        // Shared bottom nav – BaseActivity handles all nav item wiring
        setupBottomNav(R.id.navDashboard);

        // Profile icon in header
        findViewById(R.id.ivProfileIcon).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(0, 0);
        });
    }

    /**
     * Open the Add Reflection bottom sheet directly from Dashboard.
     */
    private void openAddReflectionSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View sheetView = LayoutInflater.from(this)
                .inflate(R.layout.bottom_sheet_add_reflection, null);
        sheet.setContentView(sheetView);

        TextInputLayout   tilContent    = sheetView.findViewById(R.id.tilSheetReflectionContent);
        TextInputEditText etContent     = sheetView.findViewById(R.id.etSheetReflectionContent);
        Spinner           spinnerGoal   = sheetView.findViewById(R.id.sheetSpinnerGoal);
        View              btnSave       = sheetView.findViewById(R.id.btnSheetSaveReflection);
        View              btnClose      = sheetView.findViewById(R.id.btnCloseSheet);

        TextView moodHappy     = sheetView.findViewById(R.id.moodHappy);
        TextView moodNeutral   = sheetView.findViewById(R.id.moodNeutral);
        TextView moodSad       = sheetView.findViewById(R.id.moodSad);
        TextView moodMotivated = sheetView.findViewById(R.id.moodMotivated);

        selectedMood = "";
        setupMoodButtons(moodHappy, moodNeutral, moodSad, moodMotivated);

        // Load goals into spinner
        executor.execute(() -> {
            List<Goal> active   = goalDao.getActiveGoals(userId);
            List<Goal> achieved = goalDao.getAchievedGoals(userId);
            cachedGoals = new ArrayList<>();
            cachedGoals.addAll(active);
            cachedGoals.addAll(achieved);

            List<String> titles = new ArrayList<>();
            titles.add("— No goal linked —");
            for (Goal g : cachedGoals) titles.add(g.title);

            mainHandler.post(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item, titles);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerGoal.setAdapter(adapter);
            });
        });

        btnClose.setOnClickListener(v -> sheet.dismiss());

        btnSave.setOnClickListener(v -> {
            String content = etContent.getText() != null
                    ? etContent.getText().toString().trim() : "";

            if (TextUtils.isEmpty(content)) {
                tilContent.setError("Please write something to reflect on");
                etContent.requestFocus();
                return;
            }
            tilContent.setError(null);

            int selectedPos = spinnerGoal.getSelectedItemPosition();
            final int goalId;
            if (selectedPos > 0 && selectedPos - 1 < cachedGoals.size()) {
                goalId = cachedGoals.get(selectedPos - 1).id;
            } else {
                goalId = 0; // 0 = no goal linked, allowed
            }

            String moodTag = selectedMood.isEmpty() ? "" : " " + selectedMood;
            Reflection reflection = new Reflection(userId, goalId, content + moodTag);

            executor.execute(() -> {
                reflectionDao.insertReflection(reflection);
                mainHandler.post(() -> {
                    Toast.makeText(this, "Reflection saved! ", Toast.LENGTH_SHORT).show();
                    sheet.dismiss();
                    loadDashboardData(); // refresh counts + recent list
                });
            });
        });

        sheet.show();
    }

    private void setupMoodButtons(TextView happy, TextView neutral, TextView sad, TextView motivated) {
        View.OnClickListener listener = v -> {
            resetMoodBg(happy, neutral, sad, motivated);
            ((TextView) v).setBackgroundResource(R.drawable.bg_chip_active_pill);
            if      (v.getId() == R.id.moodHappy)     selectedMood = "😊";
            else if (v.getId() == R.id.moodNeutral)   selectedMood = "😐";
            else if (v.getId() == R.id.moodSad)       selectedMood = "😔";
            else if (v.getId() == R.id.moodMotivated) selectedMood = "🔥";
        };
        happy.setOnClickListener(listener);
        neutral.setOnClickListener(listener);
        sad.setOnClickListener(listener);
        motivated.setOnClickListener(listener);
    }

    private void resetMoodBg(TextView... views) {
        for (TextView tv : views) tv.setBackgroundResource(R.drawable.bg_chip_inactive_pill);
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

            // Persist latest stats so Weekly Summary notification can read them
            sharedPreferences.edit()
                    .putInt("stat_active_goals", activeGoals)
                    .putInt("stat_achieved_goals", achievedGoals)
                    .putInt("stat_total_reflections", totalReflections)
                    .apply();
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
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
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

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
