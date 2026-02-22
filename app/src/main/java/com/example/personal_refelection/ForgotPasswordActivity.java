package com.example.personal_refelection;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.personal_refelection.database.UserRepository;

public class ForgotPasswordActivity extends AppCompatActivity {

    // ── Step 1 views ──────────────────────────────────
    private EditText etUsername, etEmail;
    private LinearLayout inputUsername, inputEmail;
    private Button btnVerify;
    private CardView cardStep1;

    // ── Step 2 views ──────────────────────────────────
    private EditText etNewPassword, etConfirmNewPassword;
    private LinearLayout inputNewPassword, inputConfirmNewPassword;
    private Button btnChangePassword;
    private CardView cardStep2;

    // ── Step indicator ────────────────────────────────
    private View dot1, dot2;
    private TextView tvStep1Label, tvStep2Label;

    // ── Common ────────────────────────────────────────
    private TextView tvBackToLogin;

    // ── Room DB ───────────────────────────────────────
    private UserRepository userRepository;
    /** Holds the verified email so Step 2 knows which record to update. */
    private String verifiedEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.forgot_password_activity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.forgotRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userRepository = new UserRepository(this);

        bindViews();
        setupFocusListeners();
        setupClickListeners();
    }

    private void bindViews() {
        // Step 1
        etUsername    = findViewById(R.id.etUsername);
        etEmail       = findViewById(R.id.etEmail);
        inputUsername = findViewById(R.id.inputUsername);
        inputEmail    = findViewById(R.id.inputEmail);
        btnVerify     = findViewById(R.id.btnVerify);
        cardStep1     = findViewById(R.id.cardStep1);

        // Step 2
        etNewPassword          = findViewById(R.id.etNewPassword);
        etConfirmNewPassword   = findViewById(R.id.etConfirmNewPassword);
        inputNewPassword       = findViewById(R.id.inputNewPassword);
        inputConfirmNewPassword = findViewById(R.id.inputConfirmNewPassword);
        btnChangePassword      = findViewById(R.id.btnChangePassword);
        cardStep2              = findViewById(R.id.cardStep2);

        // Step indicator
        dot1          = findViewById(R.id.dot1);
        dot2          = findViewById(R.id.dot2);
        tvStep1Label  = findViewById(R.id.tvStep1Label);
        tvStep2Label  = findViewById(R.id.tvStep2Label);

        // Common
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
    }

    private void setupFocusListeners() {
        setFocusHighlight(etUsername, inputUsername);
        setFocusHighlight(etEmail, inputEmail);
        setFocusHighlight(etNewPassword, inputNewPassword);
        setFocusHighlight(etConfirmNewPassword, inputConfirmNewPassword);
    }

    private void setFocusHighlight(EditText editText, LinearLayout container) {
        editText.setOnFocusChangeListener((v, hasFocus) ->
                container.setBackgroundResource(hasFocus
                        ? R.drawable.bg_input_field_focused
                        : R.drawable.bg_input_field));
    }

    private void setupClickListeners() {
        btnVerify.setOnClickListener(v -> handleVerifyIdentity());
        btnChangePassword.setOnClickListener(v -> handleChangePassword());

        tvBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    // ──────────────────────────────────────────────────────────────
    //  STEP 1 — Verify username + email match
    // ──────────────────────────────────────────────────────────────
    private void handleVerifyIdentity() {
        String username = etUsername.getText().toString().trim();
        String email    = etEmail.getText().toString().trim();

        // Validate fields
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Please enter your username");
            etUsername.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Please enter your email address");
            etEmail.requestFocus();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        dismissKeyboard();
        btnVerify.setEnabled(false);

        // ── Room DB identity check ────────────────────
        userRepository.findByUsernameAndEmail(username, email, user -> {
            btnVerify.setEnabled(true);
            if (user != null) {
                verifiedEmail = user.email; // store for Step 2 update
                transitionToStep2();
            } else {
                shakeView(inputUsername);
                shakeView(inputEmail);
                Toast.makeText(this,
                        "No account found with that username and email.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ──────────────────────────────────────────────────────────────
    //  STEP 2 — Change password
    // ──────────────────────────────────────────────────────────────
    private void handleChangePassword() {
        String newPassword     = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmNewPassword.getText().toString().trim();

        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("Please enter a new password");
            etNewPassword.requestFocus();
            return;
        }
        if (newPassword.length() < 6) {
            etNewPassword.setError("Password must be at least 6 characters");
            etNewPassword.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmNewPassword.setError("Please confirm your new password");
            etConfirmNewPassword.requestFocus();
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            etConfirmNewPassword.setError("Passwords do not match");
            shakeView(inputConfirmNewPassword);
            etConfirmNewPassword.requestFocus();
            return;
        }

        dismissKeyboard();
        btnChangePassword.setEnabled(false);
        btnChangePassword.setAlpha(0.6f);

        // ── Room DB password update ───────────────────
        userRepository.updatePassword(verifiedEmail, newPassword, success -> {
            if (success) {
                etNewPassword.setEnabled(false);
                etConfirmNewPassword.setEnabled(false);
                Toast.makeText(this, getString(R.string.lbl_password_changed), Toast.LENGTH_LONG).show();

                // Auto-navigate to LoginActivity after brief delay
                cardStep2.postDelayed(() -> {
                    Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }, 1800);
            } else {
                btnChangePassword.setEnabled(true);
                btnChangePassword.setAlpha(1f);
                Toast.makeText(this, "Failed to update password. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ──────────────────────────────────────────────────────────────
    //  UI transition: hide Step 1 card, reveal Step 2 card
    // ──────────────────────────────────────────────────────────────
    private void transitionToStep2() {
        // Fade out Step 1
        cardStep1.animate()
                .alpha(0f)
                .setDuration(250)
                .withEndAction(() -> {
                    cardStep1.setVisibility(View.GONE);

                    // Show Step 2 with a slide-up + fade-in
                    cardStep2.setAlpha(0f);
                    cardStep2.setTranslationY(40f);
                    cardStep2.setVisibility(View.VISIBLE);
                    cardStep2.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(350)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();
                })
                .start();

        // Update step indicator: dot1 stays green, dot2 turns green
        dot2.setBackgroundResource(R.drawable.bg_step_dot_active);
        tvStep2Label.setTextColor(getColor(R.color.primary_green));
        tvStep2Label.setTypeface(null, android.graphics.Typeface.BOLD);

        // Grey out step 1 label
        tvStep1Label.setTextColor(getColor(R.color.text_hint));
        tvStep1Label.setTypeface(null, android.graphics.Typeface.NORMAL);
    }

    // ──────────────────────────────────────────────────────────────
    //  Helpers
    // ──────────────────────────────────────────────────────────────
    private void dismissKeyboard() {
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    /** Horizontal shake animation to indicate an error on a view. */
    private void shakeView(View view) {
        ObjectAnimator shakeX = ObjectAnimator.ofFloat(view, "translationX",
                0f, -16f, 16f, -12f, 12f, -8f, 8f, 0f);
        shakeX.setDuration(500);
        AnimatorSet set = new AnimatorSet();
        set.play(shakeX);
        set.start();
    }
}
