package com.example.personal_refelection;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Privacy Policy screen — static informational screen.
 */
public class PrivacyPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_privacy_policy);

        View toolbar = findViewById(R.id.privacyToolbar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.privacyRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, 0, bars.right, 0);
            if (toolbar != null) toolbar.setPadding(4, bars.top, 16, 0);
            return insets;
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
