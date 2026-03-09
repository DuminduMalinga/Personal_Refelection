package com.example.personal_refelection;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Base activity — injects the shared custom bottom navigation bar
 * into every screen. Call setupBottomNav(currentScreenId) after setContentView.
 *
 * Screen IDs: R.id.navDashboard, R.id.navGoals, R.id.navAchieved, R.id.navProfile
 */
public abstract class BaseActivity extends AppCompatActivity {

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

        // 3D FAB — bounce animation then go to Goals
        nav.findViewById(R.id.fabAdd).setOnClickListener(v -> {
            v.animate().scaleX(0.88f).scaleY(0.88f).setDuration(80).withEndAction(() ->
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            ).start();
            startActivity(new Intent(this, GoalsActivity.class));
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

            if (icon != null) {
                icon.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
            }
            if (text != null) {
                text.setTextColor(color);
            }
        }
    }
}
