package com.example.personal_refelection;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etUsername, etPassword, etConfirmPassword;
    private LinearLayout inputFullName, inputEmail, inputUsername, inputPassword, inputConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private LinearLayout btnGoogleSignup, btnFacebookSignup;

    private UserRepository userRepository;
    private SharedPreferences sharedPreferences;
    private SocialAuthManager socialAuthManager;
    private CallbackManager facebookCallbackManager;

    private SocialAuthManager.SocialAuthCallback pendingGoogleCallback;

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

        userRepository          = new UserRepository(this);
        sharedPreferences       = getSharedPreferences("GoalReflectPrefs", MODE_PRIVATE);
        socialAuthManager       = new SocialAuthManager(this);
        facebookCallbackManager = CallbackManager.Factory.create();

        bindViews();
        setupFocusListeners();
        setupClickListeners();
        setupFacebookCallback();
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
        btnGoogleSignup      = findViewById(R.id.btnGoogleSignup);
        btnFacebookSignup    = findViewById(R.id.btnFacebookSignup);
    }

    private void setupFocusListeners() {
        setInputFocus(etFullName,        inputFullName);
        setInputFocus(etEmail,           inputEmail);
        setInputFocus(etUsername,        inputUsername);
        setInputFocus(etPassword,        inputPassword);
        setInputFocus(etConfirmPassword, inputConfirmPassword);
    }

    private void setInputFocus(EditText et, LinearLayout container) {
        et.setOnFocusChangeListener((v, hasFocus) ->
                container.setBackgroundResource(hasFocus
                        ? R.drawable.bg_input_field_focused : R.drawable.bg_input_field));
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> handleRegister());
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            finish();
        });
        btnGoogleSignup.setOnClickListener(v -> handleGoogleSignup());
        btnFacebookSignup.setOnClickListener(v -> handleFacebookSignup());
    }

    // ── Google Sign-Up ────────────────────────────────────────────

    private void handleGoogleSignup() {
        btnGoogleSignup.setAlpha(0.6f);

        pendingGoogleCallback = new SocialAuthManager.SocialAuthCallback() {
            @Override
            public void onSuccess(User user) {
                btnGoogleSignup.setAlpha(1f);
                Toast.makeText(RegisterActivity.this,
                        "Welcome, " + user.fullName + "! 🌱", Toast.LENGTH_SHORT).show();
                navigateToDashboard();
            }
            @Override
            public void onCancelled() {
                btnGoogleSignup.setAlpha(1f);
                Toast.makeText(RegisterActivity.this,
                        getString(R.string.lbl_social_login_cancelled), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(String message) {
                btnGoogleSignup.setAlpha(1f);
                Toast.makeText(RegisterActivity.this,
                        getString(R.string.lbl_social_login_failed) + "\n" + message,
                        Toast.LENGTH_LONG).show();
            }
        };

        startActivityForResult(socialAuthManager.getGoogleSignInIntent(),
                SocialAuthManager.RC_GOOGLE_SIGN_IN);
    }

    // ── Facebook Sign-Up ──────────────────────────────────────────

    private void handleFacebookSignup() {
        LoginManager.getInstance().logInWithReadPermissions(
                this, facebookCallbackManager,
                Arrays.asList("public_profile", "email"));
    }

    private void setupFacebookCallback() {
        LoginManager.getInstance().registerCallback(facebookCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // Pass access token directly to Firebase
                        socialAuthManager.handleFacebookAccessToken(
                                loginResult.getAccessToken(),
                                new SocialAuthManager.SocialAuthCallback() {
                                    @Override
                                    public void onSuccess(User user) {
                                        Toast.makeText(RegisterActivity.this,
                                                "Welcome, " + user.fullName + "! 🌱",
                                                Toast.LENGTH_SHORT).show();
                                        navigateToDashboard();
                                    }
                                    @Override
                                    public void onCancelled() { }
                                    @Override
                                    public void onError(String message) {
                                        Toast.makeText(RegisterActivity.this,
                                                getString(R.string.lbl_social_login_failed) + "\n" + message,
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                    @Override
                    public void onCancel() {
                        Toast.makeText(RegisterActivity.this,
                                getString(R.string.lbl_social_login_cancelled), Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(RegisterActivity.this,
                                getString(R.string.lbl_social_login_failed), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ── Activity Result ───────────────────────────────────────────

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SocialAuthManager.RC_GOOGLE_SIGN_IN) {
            btnGoogleSignup.setAlpha(1f);
            if (data != null && pendingGoogleCallback != null) {
                socialAuthManager.handleGoogleSignInResult(data, pendingGoogleCallback);
            } else if (pendingGoogleCallback != null) {
                pendingGoogleCallback.onCancelled();
            }
            pendingGoogleCallback = null;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    // ── Email/Password Register ───────────────────────────────────

    private void handleRegister() {
        String fullName        = etFullName.getText().toString().trim();
        String email           = etEmail.getText().toString().trim();
        String username        = etUsername.getText().toString().trim();
        String password        = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(fullName))        { etFullName.setError("Please enter your full name"); etFullName.requestFocus(); return; }
        if (fullName.length() < 2)              { etFullName.setError("Name must be at least 2 characters"); etFullName.requestFocus(); return; }
        if (TextUtils.isEmpty(email))           { etEmail.setError("Please enter your email"); etEmail.requestFocus(); return; }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { etEmail.setError("Enter a valid email address"); etEmail.requestFocus(); return; }
        if (TextUtils.isEmpty(username))        { etUsername.setError("Please enter a username"); etUsername.requestFocus(); return; }
        if (username.length() < 3)              { etUsername.setError("Username must be at least 3 characters"); etUsername.requestFocus(); return; }
        if (TextUtils.isEmpty(password))        { etPassword.setError("Please enter a password"); etPassword.requestFocus(); return; }
        if (password.length() < 6)              { etPassword.setError("Password must be at least 6 characters"); etPassword.requestFocus(); return; }
        if (TextUtils.isEmpty(confirmPassword)) { etConfirmPassword.setError("Please confirm your password"); etConfirmPassword.requestFocus(); return; }
        if (!password.equals(confirmPassword))  { etConfirmPassword.setError("Passwords do not match"); etConfirmPassword.requestFocus(); return; }

        dismissKeyboard();
        btnRegister.setEnabled(false);

        User newUser = new User(fullName, email, username, password);
        userRepository.register(newUser, result -> {
            btnRegister.setEnabled(true);
            if (result > 0) {
                Toast.makeText(this, "Welcome to GoalReflect, " + fullName + "! 🌱", Toast.LENGTH_SHORT).show();
                userRepository.getUserByEmail(email, user -> {
                    if (user != null) {
                        sharedPreferences.edit()
                                .putInt("user_id", user.id)
                                .putString("user_name", user.fullName)
                                .putString("user_email", user.email)
                                .putBoolean("isLoggedIn", true)
                                .apply();
                        navigateToDashboard();
                    }
                });
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

    private void navigateToDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void dismissKeyboard() {
        View focus = getCurrentFocus();
        if (focus != null)
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(focus.getWindowToken(), 0);
    }
}
