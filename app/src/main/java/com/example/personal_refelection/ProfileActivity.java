package com.example.personal_refelection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.example.personal_refelection.database.DashboardRepository;
import com.example.personal_refelection.database.UserRepository;

import java.io.File;

/**
 * Profile screen — shows user info, journey stats, account settings list,
 * and logout with a confirmation dialog.
 */
public class ProfileActivity extends BaseActivity {

    private TextView tvFullName, tvEmail, tvUsername;
    private TextView tvStatActiveGoals, tvStatAchievedGoals, tvStatTotalReflections;
    private ImageView ivProfileImage;

    private UserRepository userRepository;
    private DashboardRepository dashboardRepository;
    private SharedPreferences sharedPreferences;

    private String userEmail;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        userRepository      = new UserRepository(this);
        dashboardRepository = new DashboardRepository(this);
        sharedPreferences   = getSharedPreferences("GoalReflectPrefs", MODE_PRIVATE);

        userId    = sharedPreferences.getInt("user_id", -1);
        userEmail = sharedPreferences.getString("user_email", "");

        if (userId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        bindViews();
        loadUserData();
        loadStats();
        setupSettingsRows();
        // Use shared bottom nav from BaseActivity, highlight Profile
        setupBottomNav(R.id.navProfile);
    }

    // ── View Binding ──────────────────────────────────────────────

    private void bindViews() {
        tvFullName             = findViewById(R.id.tvFullName);
        tvEmail                = findViewById(R.id.tvEmail);
        tvUsername             = findViewById(R.id.tvUsername);
        tvStatActiveGoals      = findViewById(R.id.tvStatActiveGoals);
        tvStatAchievedGoals    = findViewById(R.id.tvStatAchievedGoals);
        tvStatTotalReflections = findViewById(R.id.tvStatTotalReflections);
        ivProfileImage         = findViewById(R.id.ivProfileImage);

        findViewById(R.id.btnEditAvatar).setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));
    }

    // ── Load User Header ──────────────────────────────────────────

    private void loadUserData() {
        userRepository.getUserByEmail(userEmail, user -> {
            if (user != null) {
                tvFullName.setText(user.fullName);
                tvEmail.setText(user.email);
                tvUsername.setText(getString(R.string.format_username, user.username));
            }
        });
    }

    // ── Load Stats ────────────────────────────────────────────────

    private void loadStats() {
        dashboardRepository.getDashboardStats(userId, (activeGoals, achievedGoals, totalReflections) -> {
            tvStatActiveGoals.setText(String.valueOf(activeGoals));
            tvStatAchievedGoals.setText(String.valueOf(achievedGoals));
            tvStatTotalReflections.setText(String.valueOf(totalReflections));
        });
    }

    // ── Settings Rows ─────────────────────────────────────────────

    private void setupSettingsRows() {
        findViewById(R.id.rowEditProfile).setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        findViewById(R.id.rowNotificationSettings).setOnClickListener(v ->
                startActivity(new Intent(this, NotificationSettingsActivity.class)));

        findViewById(R.id.rowPrivacyPolicy).setOnClickListener(v ->
                startActivity(new Intent(this, PrivacyPolicyActivity.class)));

        findViewById(R.id.rowAboutApp).setOnClickListener(v ->
                startActivity(new Intent(this, AboutAppActivity.class)));

        // Row tap triggers logout dialog
        findViewById(R.id.rowLogout).setOnClickListener(v -> showLogoutDialog());
    }

    // ── Logout ────────────────────────────────────────────────────

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.lbl_logout_confirm_title))
                .setMessage(getString(R.string.lbl_logout_confirm_message))
                .setNegativeButton(getString(R.string.btn_logout_cancel), (d, w) -> d.dismiss())
                .setPositiveButton(getString(R.string.btn_logout_confirm), (d, w) -> performLogout())
                .setCancelable(true)
                .show();
    }

    private void performLogout() {
        // Reset dark mode to default before clearing prefs
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        sharedPreferences.edit().clear().apply();
        Toast.makeText(this, "Logged out successfully 👋", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
        loadStats();
        restoreSavedAvatar();
    }

    // ── Restore saved avatar ──────────────────────────────────────

    private void restoreSavedAvatar() {
        String savedPath = sharedPreferences.getString("avatar_path", null);
        if (savedPath != null) {
            File f = new File(savedPath);
            if (f.exists()) {
                Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
                if (bmp != null) {
                    ivProfileImage.setPadding(0, 0, 0, 0);
                    ivProfileImage.setBackground(
                            ContextCompat.getDrawable(this, R.drawable.bg_avatar_circle));
                    ivProfileImage.setImageBitmap(bmp);
                    ivProfileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            }
        }
    }
}
