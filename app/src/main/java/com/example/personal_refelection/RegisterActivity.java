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

import com.example.personal_refelection.database.User;
import com.example.personal_refelection.database.UserRepository;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etUsername, etPassword, etConfirmPassword;
    private LinearLayout inputFullName, inputEmail, inputUsername, inputPassword, inputConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;

    private UserRepository userRepository;

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

        userRepository = new UserRepository(this);

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
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void handleRegister() {
        String fullName        = etFullName.getText().toString().trim();
        String email           = etEmail.getText().toString().trim();
        String username        = etUsername.getText().toString().trim();
        String password        = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // ── Client-side validation ────────────────────
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

        dismissKeyboard();
        btnRegister.setEnabled(false);

        // ── Room DB insert ────────────────────────────
        User newUser = new User(fullName, email, username, password);

        userRepository.register(newUser, result -> {
            btnRegister.setEnabled(true);
            if (result > 0) {
                // Success
                Toast.makeText(this,
                        "Welcome to GoalReflect, " + fullName + "! 🌱",
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            } else if (result == -2) {
                etEmail.setError("This email is already registered");
                inputEmail.setBackgroundResource(R.drawable.bg_input_field_focused);
                etEmail.requestFocus();
            } else if (result == -3) {
                etUsername.setError("This username is already taken");
                inputUsername.setBackgroundResource(R.drawable.bg_input_field_focused);
                etUsername.requestFocus();
            } else {
                Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void dismissKeyboard() {
        View focus = getCurrentFocus();
        if (focus != null) {
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(focus.getWindowToken(), 0);
        }
    }
}
