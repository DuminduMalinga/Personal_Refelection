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

import com.example.personal_refelection.database.UserRepository;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private LinearLayout inputEmail, inputPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvRegister;

    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login_activity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
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
        etEmail          = findViewById(R.id.etEmail);
        etPassword       = findViewById(R.id.etPassword);
        inputEmail       = findViewById(R.id.inputEmail);
        inputPassword    = findViewById(R.id.inputPassword);
        btnLogin         = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister       = findViewById(R.id.tvRegister);
    }

    /**
     * Visually highlight the input container when the inner EditText gains focus.
     */
    private void setupFocusListeners() {
        etEmail.setOnFocusChangeListener((v, hasFocus) ->
                inputEmail.setBackgroundResource(hasFocus
                        ? R.drawable.bg_input_field_focused
                        : R.drawable.bg_input_field));

        etPassword.setOnFocusChangeListener((v, hasFocus) ->
                inputPassword.setBackgroundResource(hasFocus
                        ? R.drawable.bg_input_field_focused
                        : R.drawable.bg_input_field));
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void handleLogin() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // ── Client-side validation ────────────────────
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
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Please enter your password");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        dismissKeyboard();
        btnLogin.setEnabled(false);

        // ── Room DB login query ───────────────────────
        userRepository.login(email, password, user -> {
            btnLogin.setEnabled(true);
            if (user != null) {
                Toast.makeText(this, "Welcome back, " + user.fullName + "! 🌿", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to Home/Dashboard screen
            } else {
                etPassword.setError("Incorrect email or password");
                inputEmail.setBackgroundResource(R.drawable.bg_input_field_focused);
                inputPassword.setBackgroundResource(R.drawable.bg_input_field_focused);
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
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
