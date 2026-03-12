package com.example.personal_refelection;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * About App screen — displays app info, mission, features, and tech stack.
 */
public class AboutAppActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_about_app);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.aboutRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, 0, bars.right, 0);
            View toolbar = v.findViewById(R.id.aboutToolbar);
            if (toolbar != null) toolbar.setPadding(
                    toolbar.getPaddingLeft(), bars.top,
                    toolbar.getPaddingRight(), toolbar.getPaddingBottom());
            return insets;
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
