package com.example.personal_refelection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class GetStartedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);

        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        Button btnSignIn     = findViewById(R.id.btnSignIn);

        // Intercept back press – minimize instead of going back to splash
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                moveTaskToBack(true);
            }
        });

        // Animate views on entry
        animateEntrance();

        btnGetStarted.setOnClickListener(v -> {
            // Mark onboarding as seen
            SharedPreferences prefs = getSharedPreferences("GoalReflectPrefs", MODE_PRIVATE);
            prefs.edit().putBoolean("hasSeenOnboarding", true).apply();

            startActivity(new Intent(GetStartedActivity.this, RegisterActivity.class));
            overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
        });

        btnSignIn.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("GoalReflectPrefs", MODE_PRIVATE);
            prefs.edit().putBoolean("hasSeenOnboarding", true).apply();

            startActivity(new Intent(GetStartedActivity.this, LoginActivity.class));
            overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
        });
    }

    private void animateEntrance() {
        // Hero logo pop
        FrameLayout gsLogo = findViewById(R.id.gsLogoContainer);
        Animation logoPop = AnimationUtils.loadAnimation(this, R.anim.splash_logo_bounce);
        if (gsLogo != null) gsLogo.startAnimation(logoPop);

        // Quote card slide up
        CardView quoteCard = findViewById(R.id.gsQuoteCard);
        if (quoteCard != null) {
            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.splash_text_slide_up);
            slideUp.setStartOffset(200);
            quoteCard.startAnimation(slideUp);
        }

        // Features slide in
        LinearLayout features = findViewById(R.id.gsFeaturesContainer);
        if (features != null) {
            Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
            slideIn.setStartOffset(500);
            features.startAnimation(slideIn);
        }

        // Feature rows staggered
        int[] featureIds = {R.id.gsFeature1, R.id.gsFeature2, R.id.gsFeature3};
        for (int i = 0; i < featureIds.length; i++) {
            View feature = findViewById(featureIds[i]);
            if (feature != null) {
                Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                fadeIn.setStartOffset(600 + (i * 150L));
                feature.startAnimation(fadeIn);
            }
        }
    }
}

