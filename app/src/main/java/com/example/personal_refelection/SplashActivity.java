package com.example.personal_refelection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2800; // ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full-screen immersive splash
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        setContentView(R.layout.activity_splash);

        FrameLayout logoContainer = findViewById(R.id.logoContainer);
        LinearLayout splashContent = findViewById(R.id.splashContent);
        LinearLayout splashBottom  = findViewById(R.id.splashBottom);

        // 1. Logo bounce animation
        Animation logoBounce = AnimationUtils.loadAnimation(this, R.anim.splash_logo_bounce);
        logoContainer.startAnimation(logoBounce);

        // 2. Text slide-up animation (delayed inside anim xml)
        Animation textSlide = AnimationUtils.loadAnimation(this, R.anim.splash_text_slide_up);
        splashContent.startAnimation(textSlide);

        // 3. Bottom indicator fade in
        Animation bottomFade = AnimationUtils.loadAnimation(this, R.anim.splash_text_slide_up);
        splashBottom.startAnimation(bottomFade);

        // 4. Navigate after SPLASH_DURATION
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("GoalReflectPrefs", MODE_PRIVATE);
            boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
            boolean hasSeenOnboarding = prefs.getBoolean("hasSeenOnboarding", false);

            Intent intent;
            if (isLoggedIn) {
                intent = new Intent(SplashActivity.this, DashboardActivity.class);
            } else if (hasSeenOnboarding) {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, GetStartedActivity.class);
            }

            // Exit animation
            Animation exitAnim = AnimationUtils.loadAnimation(this, R.anim.splash_exit);
            findViewById(R.id.splashContent).startAnimation(exitAnim);

            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, SPLASH_DURATION);
    }
}



