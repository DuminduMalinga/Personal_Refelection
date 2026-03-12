package com.example.personal_refelection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.util.Calendar;

/**
 * Base activity — injects the shared custom bottom navigation bar
 * and the shared top navigation bar into every screen.
 * Call setupBottomNav(currentScreenId) and setupTopNav(screenLabel) after setContentView.
 */
public abstract class BaseActivity extends AppCompatActivity {

    // ── TOP NAV ────────────────────────────────────────────────────────

    /**
     * Populates the shared top nav bar (layout_top_nav.xml include).
     *
     * @param screenLabel  e.g. "MY GOALS"  shown above the greeting
     */
    protected void setupTopNav(String screenLabel) {
        View topNav = findViewById(R.id.topNavInclude);
        if (topNav == null) return;

        SharedPreferences prefs = getSharedPreferences("GoalReflectPrefs", MODE_PRIVATE);
        String userName = prefs.getString("user_name", "User");

        // ── Screen label ──────────────────────────────────────────────
        TextView tvLabel = topNav.findViewById(R.id.topNavScreenLabel);
        if (tvLabel != null && screenLabel != null) tvLabel.setText(screenLabel);

        // ── Time-of-day greeting ──────────────────────────────────────
        TextView tvGreeting = topNav.findViewById(R.id.topNavGreeting);
        if (tvGreeting != null) {
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            String g = hour < 12 ? "Good Morning" : hour < 18 ? "Good Afternoon" : "Good Evening";
            tvGreeting.setText(g + ", " + userName + " 👋");
        }

        // ── Tagline ───────────────────────────────────────────────────
        // already set in XML, leave as is

        // ── Profile avatar ────────────────────────────────────────────
        ImageView ivAvatar = topNav.findViewById(R.id.topNavProfileImage);
        if (ivAvatar != null) loadTopNavAvatar(ivAvatar, prefs);

        // ── Profile icon click → ProfileActivity ──────────────────────
        View profileIcon = topNav.findViewById(R.id.topNavProfileIcon);
        if (profileIcon != null) {
            profileIcon.setOnClickListener(v -> {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
            });
        }
    }

    private void loadTopNavAvatar(ImageView iv, SharedPreferences prefs) {
        // 1 — local file
        String savedPath = prefs.getString("avatar_path", null);
        if (savedPath != null) {
            File f = new File(savedPath);
            if (f.exists()) {
                Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
                if (bmp != null) {
                    iv.setImageBitmap(bmp);
                    iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    iv.setClipToOutline(true);
                    return;
                }
            }
        }

        // 2 — social / Firebase photo URL
        String socialUrl = prefs.getString("social_photo_url", null);
        if (socialUrl == null || socialUrl.isEmpty()) {
            FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
            if (fbUser != null && fbUser.getPhotoUrl() != null)
                socialUrl = fbUser.getPhotoUrl().toString();
        }
        if (socialUrl != null && !socialUrl.isEmpty()) {
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setClipToOutline(true);
            Glide.with(this).load(socialUrl).circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(iv);
            return;
        }

        // 3 — default
        iv.setImageResource(R.drawable.ic_profile);
        iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        int pad = Math.round(9 * getResources().getDisplayMetrics().density);
        iv.setPadding(pad, pad, pad, pad);
    }

    // ── BOTTOM NAV ─────────────────────────────────────────────────────

    protected void setupBottomNav(int activeNavId) {
        View nav = findViewById(R.id.bottomNavInclude);
        if (nav == null) return;

        highlightNavItem(nav, activeNavId);

        nav.findViewById(R.id.navDashboard).setOnClickListener(v -> {
            if (activeNavId == R.id.navDashboard) return;
            startActivity(new Intent(this, DashboardActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        nav.findViewById(R.id.navGoals).setOnClickListener(v -> {
            if (activeNavId == R.id.navGoals) return;
            startActivity(new Intent(this, GoalsActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        nav.findViewById(R.id.navAchieved).setOnClickListener(v -> {
            if (activeNavId == R.id.navAchieved) return;
            startActivity(new Intent(this, AchievedActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        nav.findViewById(R.id.navProfile).setOnClickListener(v -> {
            if (activeNavId == R.id.navProfile) return;
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        // 3D FAB — bounce animation then go to Add Goal
        nav.findViewById(R.id.fabAdd).setOnClickListener(v -> {
            // Already on AddGoalActivity — do nothing to avoid stacking
            if (this instanceof AddGoalActivity) return;
            v.animate().scaleX(0.88f).scaleY(0.88f).setDuration(80).withEndAction(() ->
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            ).start();
            startActivity(new Intent(this, AddGoalActivity.class));
            overridePendingTransition(0, 0);
        });
    }

    /** Tint active icon + label purple, others grey. */
    private void highlightNavItem(View nav, int activeNavId) {
        int[] navIds  = { R.id.navDashboard, R.id.navGoals, R.id.navAchieved, R.id.navProfile };
        int[] iconIds = { R.id.navIconDashboard, R.id.navIconGoals, R.id.navIconAchieved, R.id.navIconProfile };
        int[] textIds = { R.id.navTextDashboard, R.id.navTextGoals, R.id.navTextAchieved, R.id.navTextProfile };

        int purple = 0xFF6C63FF;
        int grey   = 0xFFB0B7C3;

        for (int i = 0; i < navIds.length; i++) {
            boolean active = (navIds[i] == activeNavId);
            int color = active ? purple : grey;

            ImageView icon = nav.findViewById(iconIds[i]);
            TextView  text = nav.findViewById(textIds[i]);

            if (icon != null) icon.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
            if (text != null) text.setTextColor(color);
        }
    }
}
