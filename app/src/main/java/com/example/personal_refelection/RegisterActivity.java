package com.example.personal_refelection;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etUsername, etPassword, etConfirmPassword;
    private LinearLayout inputFullName, inputEmail, inputUsername, inputPassword, inputConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.register_activity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindViews();
        setupFocusListeners();
        setupClickListeners();
    }

    private void bindViews() {
        etFullName           = findViewById(R.id.etFullName);
        etEmail              = findViewById(R.id.etEmail);
        etUsername           = findViewById(R.id.etUsername);
        etPassword           = findViewById(R.id.etPassword);
        etConfirmPassword    = findViewById(R.id.etConfirmPassword);
        inputFullName        = findViewById(R.id.inputFullName);
        inputEmail           = findViewById(R.id.inputEmail);
        inputUsername        = findViewById(R.id.inputUsername);
        inputPassword        = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        btnRegister          = findViewById(R.id.btnRegister);
        tvLogin              = findViewById(R.id.tvLogin);
    }

    /**
     * Highlight the input container border when its EditText gains focus.
     */
    private void setupFocusListeners() {
        setInputFocus(etFullName, inputFullName);
        setInputFocus(etEmail, inputEmail);
        setInputFocus(etUsername, inputUsername);
        setInputFocus(etPassword, inputPassword);
        setInputFocus(etConfirmPassword, inputConfirmPassword);
    }

    private void setInputFocus(EditText editText, LinearLayout container) {
        editText.setOnFocusChangeListener((v, hasFocus) ->
                container.setBackgroundResource(hasFocus
                        ? R.drawable.bg_input_field_focused
                        : R.drawable.bg_input_field));
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> handleRegister());

        tvLogin.setOnClickListener(v -> {
            // Navigate back to Login screen
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void handleRegister() {
        String fullName       = etFullName.getText().toString().trim();
        String email          = etEmail.getText().toString().trim();
        String username       = etUsername.getText().toString().trim();
        String password       = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Full Name validation
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Please enter your full name");
            etFullName.requestFocus();
            return;
        }
        if (fullName.length() < 2) {
            etFullName.setError("Name must be at least 2 characters");
            etFullName.requestFocus();
            return;
        }

        // Email validation
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Please enter your email");
            etEmail.requestFocus();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        // Username validation
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Please enter a username");
            etUsername.requestFocus();
            return;
        }
        if (username.length() < 3) {
            etUsername.setError("Username must be at least 3 characters");
            etUsername.requestFocus();
            return;
        }

        // Password validation
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Please enter a password");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        // Confirm Password validation
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Dismiss keyboard
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }

        Toast.makeText(this, "Welcome to GoalReflect, " + fullName + "! 🌱", Toast.LENGTH_SHORT).show();
    }
}

