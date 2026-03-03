package com.example.personal_refelection;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * About App screen — displays app info, mission, features, and tech stack.
 */
public class AboutAppActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}

