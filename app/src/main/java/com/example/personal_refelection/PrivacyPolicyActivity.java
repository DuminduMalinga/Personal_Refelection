package com.example.personal_refelection;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Privacy Policy screen — static informational screen.
 */
public class PrivacyPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}

