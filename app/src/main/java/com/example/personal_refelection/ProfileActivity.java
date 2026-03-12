package com.example.personal_refelection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Outline;
import android.os.Bundle;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.example.personal_refelection.database.DashboardRepository;
import com.example.personal_refelection.database.UserRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;

/**
 * Profile screen — shows user info, journey stats, account settings list,
 * and logout with a confirmation dialog.
 */
public class ProfileActivity extends BaseActivity {

    private TextView tvFullName, tvEmail, tvUsername;
    private TextView tvStatActiveGoals, tvStatAchievedGoals, tvStatTotalReflections;
    private TextView tvCurrentTheme;
    private ImageView ivProfileImage;

    private UserRepository userRepository;
    private DashboardRepository dashboardRepository;
    private SharedPreferences sharedPreferences;

    private String userEmail;
    private int userId;

    // track whether bindViews() was called successfully
    private boolean viewsBound = false;

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
            // Check if Firebase has a current user (social login)
            com.google.firebase.auth.FirebaseUser fbUser =
                    FirebaseAuth.getInstance().getCurrentUser();
            if (fbUser != null && fbUser.getEmail() != null) {
                userEmail = fbUser.getEmail();
                // Try to resolve the local user by email, then reload
                userRepository.getUserByEmail(userEmail, user -> {
                    if (user != null) {
                        sharedPreferences.edit()
                                .putInt("user_id", user.id)
                                .putString("user_name", user.fullName)
                                .putString("user_email", user.email)
                                .putBoolean("isLoggedIn", true)
                                .apply();
                        // Restart this activity with the fixed session
                        runOnUiThread(() -> {
                            startActivity(new Intent(this, ProfileActivity.class));
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> {
                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
                        });
                    }
                });
                return;
            }
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        bindViews();
        viewsBound = true;
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
        tvCurrentTheme         = findViewById(R.id.tvCurrentTheme);

        // Apply circular clip in Java (safe for all API levels)
        if (ivProfileImage != null) {
            ivProfileImage.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });
            ivProfileImage.setClipToOutline(true);
        }

        View editBtn = findViewById(R.id.btnEditAvatar);
        if (editBtn != null) {
            editBtn.setOnClickListener(v ->
                    startActivity(new Intent(this, EditProfileActivity.class)));
        }
    }

    // ── Load User Header ──────────────────────────────────────────

    private void loadUserData() {
        if (userEmail == null || userEmail.isEmpty()) return;
        userRepository.getUserByEmail(userEmail, user -> {
            if (user != null) {
                runOnUiThread(() -> {
                    if (tvFullName != null)
                        tvFullName.setText(user.fullName != null ? user.fullName : "");
                    if (tvEmail != null)
                        tvEmail.setText(user.email != null ? user.email : "");
                    if (tvUsername != null) {
                        String uname = user.username != null ? user.username : "";
                        tvUsername.setText(getString(R.string.format_username, uname));
                    }
                });
            }
        });
    }

    // ── Load Stats ────────────────────────────────────────────────

    private void loadStats() {
        if (userId == -1) return;
        dashboardRepository.getDashboardStats(userId,
                (activeGoals, achievedGoals, totalReflections) -> runOnUiThread(() -> {
                    if (tvStatActiveGoals      != null)
                        tvStatActiveGoals.setText(String.valueOf(activeGoals));
                    if (tvStatAchievedGoals    != null)
                        tvStatAchievedGoals.setText(String.valueOf(achievedGoals));
                    if (tvStatTotalReflections != null)
                        tvStatTotalReflections.setText(String.valueOf(totalReflections));
                }));
    }

    // ── Settings Rows ─────────────────────────────────────────────

    private void setupSettingsRows() {
        View rowEdit = findViewById(R.id.rowEditProfile);
        if (rowEdit != null) rowEdit.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        View rowNotif = findViewById(R.id.rowNotificationSettings);
        if (rowNotif != null) rowNotif.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationSettingsActivity.class)));

        // ── App Theme ─────────────────────────────────────────────
        View rowTheme = findViewById(R.id.rowAppTheme);
        if (rowTheme != null) rowTheme.setOnClickListener(v -> showThemeDialog());
        updateThemeSubtitle();

        View rowPrivacy = findViewById(R.id.rowPrivacyPolicy);
        if (rowPrivacy != null) rowPrivacy.setOnClickListener(v ->
                startActivity(new Intent(this, PrivacyPolicyActivity.class)));

        View rowAbout = findViewById(R.id.rowAboutApp);
        if (rowAbout != null) rowAbout.setOnClickListener(v ->
                startActivity(new Intent(this, AboutAppActivity.class)));

        View rowLogout = findViewById(R.id.rowLogout);
        if (rowLogout != null) rowLogout.setOnClickListener(v -> showLogoutDialog());
    }

    /** Shows a single-choice dialog for Light / Dark / Follow System. */
    private void showThemeDialog() {
        String[] options = {"☀️ Light", "🌙 Dark", "📱 Follow System"};
        int current = ThemeManager.getSavedTheme(this);

        new AlertDialog.Builder(this)
                .setTitle("Choose App Theme")
                .setSingleChoiceItems(options, current, null)
                .setPositiveButton("Apply", (dialog, which) -> {
                    android.widget.ListView lv =
                            ((AlertDialog) dialog).getListView();
                    int chosen = lv.getCheckedItemPosition();
                    if (chosen == android.widget.AdapterView.INVALID_POSITION)
                        chosen = current;
                    ThemeManager.saveAndApplyTheme(this, chosen);
                    updateThemeSubtitle();
                    // Recreate so colours/drawables refresh
                    recreate();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Refreshes the subtitle under "App Theme" to reflect the current selection. */
    private void updateThemeSubtitle() {
        if (tvCurrentTheme == null) return;
        int saved = ThemeManager.getSavedTheme(this);
        String label;
        switch (saved) {
            case ThemeManager.THEME_LIGHT:  label = "Light";        break;
            case ThemeManager.THEME_DARK:   label = "Dark";         break;
            default:                        label = "Follow System"; break;
        }
        tvCurrentTheme.setText(label);
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
        // Sign out from Firebase (covers Google + Facebook sessions)
        FirebaseAuth.getInstance().signOut();

        // Reset dark mode to default before clearing prefs
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        sharedPreferences.edit().clear().apply();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Only call data/avatar methods if views were successfully bound
        if (!viewsBound) return;
        loadUserData();
        loadStats();
        restoreSavedAvatar();
    }

    // ── Restore saved avatar ──────────────────────────────────────
    // Priority: 1) local file, 2) social photo URL from Google/Facebook, 3) default

    private void restoreSavedAvatar() {
        if (ivProfileImage == null) return;

        // 1 — User-selected local photo
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
                    return;
                }
            }
        }

        // 2 — Google / Facebook social photo URL
        String socialPhotoUrl = sharedPreferences.getString("social_photo_url", null);
        if (socialPhotoUrl == null || socialPhotoUrl.isEmpty()) {
            com.google.firebase.auth.FirebaseUser fbUser =
                    FirebaseAuth.getInstance().getCurrentUser();
            if (fbUser != null && fbUser.getPhotoUrl() != null) {
                socialPhotoUrl = fbUser.getPhotoUrl().toString();
            }
        }

        if (socialPhotoUrl != null && !socialPhotoUrl.isEmpty()) {
            ivProfileImage.setPadding(0, 0, 0, 0);
            ivProfileImage.setBackground(
                    ContextCompat.getDrawable(this, R.drawable.bg_avatar_circle));
            ivProfileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            com.bumptech.glide.Glide.with(this)
                    .load(socialPhotoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(ivProfileImage);
        }
    }
}
