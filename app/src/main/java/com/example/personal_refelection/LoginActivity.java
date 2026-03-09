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

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private LinearLayout inputEmail, inputPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvRegister;
    private LinearLayout btnGoogleLogin, btnFacebookLogin;

    private UserRepository userRepository;
    private SharedPreferences sharedPreferences;
    private SocialAuthManager socialAuthManager;
    private CallbackManager facebookCallbackManager;

    // Pending callback for when Google sign-in returns
    private SocialAuthManager.SocialAuthCallback pendingGoogleCallback;

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
        etEmail          = findViewById(R.id.etEmail);
        etPassword       = findViewById(R.id.etPassword);
        inputEmail       = findViewById(R.id.inputEmail);
        inputPassword    = findViewById(R.id.inputPassword);
        btnLogin         = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister       = findViewById(R.id.tvRegister);
        btnGoogleLogin   = findViewById(R.id.btnGoogleLogin);
        btnFacebookLogin = findViewById(R.id.btnFacebookLogin);
    }

    private void setupFocusListeners() {
        etEmail.setOnFocusChangeListener((v, hasFocus) ->
                inputEmail.setBackgroundResource(hasFocus
                        ? R.drawable.bg_input_field_focused : R.drawable.bg_input_field));
        etPassword.setOnFocusChangeListener((v, hasFocus) ->
                inputPassword.setBackgroundResource(hasFocus
                        ? R.drawable.bg_input_field_focused : R.drawable.bg_input_field));
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());
        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));
        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
        btnGoogleLogin.setOnClickListener(v -> handleGoogleLogin());
        btnFacebookLogin.setOnClickListener(v -> handleFacebookLogin());
    }

    // ── Google Login ──────────────────────────────────────────────

    private void handleGoogleLogin() {
        btnGoogleLogin.setAlpha(0.6f);

        pendingGoogleCallback = new SocialAuthManager.SocialAuthCallback() {
            @Override
            public void onSuccess(User user) {
                btnGoogleLogin.setAlpha(1f);
                Toast.makeText(LoginActivity.this,
                        "Welcome, " + user.fullName + "! 🎉", Toast.LENGTH_SHORT).show();
                navigateToDashboard();
            }
            @Override
            public void onCancelled() {
                btnGoogleLogin.setAlpha(1f);
                Toast.makeText(LoginActivity.this,
                        getString(R.string.lbl_social_login_cancelled), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(String message) {
                btnGoogleLogin.setAlpha(1f);
                Toast.makeText(LoginActivity.this,
                        getString(R.string.lbl_social_login_failed) + "\n" + message,
                        Toast.LENGTH_LONG).show();
            }
        };

        // Launch the Google account picker
        startActivityForResult(socialAuthManager.getGoogleSignInIntent(),
                SocialAuthManager.RC_GOOGLE_SIGN_IN);
    }

    // ── Facebook Login ────────────────────────────────────────────

    private void handleFacebookLogin() {
        LoginManager.getInstance().logInWithReadPermissions(
                this, facebookCallbackManager,
                Arrays.asList("public_profile", "email"));
    }

    private void setupFacebookCallback() {
        LoginManager.getInstance().registerCallback(facebookCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        fetchFacebookProfile(loginResult);
                    }
                    @Override
                    public void onCancel() {
                        Toast.makeText(LoginActivity.this,
                                getString(R.string.lbl_social_login_cancelled), Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(LoginActivity.this,
                                getString(R.string.lbl_social_login_failed), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchFacebookProfile(LoginResult loginResult) {
        com.facebook.GraphRequest request = com.facebook.GraphRequest.newMeRequest(
                loginResult.getAccessToken(),
                (object, response) -> {
                    try {
                        String email = object != null && object.has("email")
                                ? object.getString("email")
                                : loginResult.getAccessToken().getUserId() + "@facebook.com";
                        String name  = object != null && object.has("name")
                                ? object.getString("name") : "Facebook User";
                        String username = name.replaceAll("[^a-zA-Z0-9_]", "").toLowerCase();
                        if (username.length() < 3) username = "fb_" + username;

                        final String fEmail = email, fName = name, fUsername = username;
                        socialAuthManager.findOrCreateSocialUser(fEmail, fName, fUsername,
                                "facebook_oauth", new SocialAuthManager.SocialAuthCallback() {
                                    @Override
                                    public void onSuccess(User user) {
                                        runOnUiThread(() -> {
                                            Toast.makeText(LoginActivity.this,
                                                    "Welcome, " + user.fullName + "! 🎉",
                                                    Toast.LENGTH_SHORT).show();
                                            navigateToDashboard();
                                        });
                                    }
                                    @Override public void onCancelled() { }
                                    @Override
                                    public void onError(String message) {
                                        runOnUiThread(() -> Toast.makeText(LoginActivity.this,
                                                getString(R.string.lbl_social_login_failed),
                                                Toast.LENGTH_SHORT).show());
                                    }
                                });
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this,
                                getString(R.string.lbl_social_login_failed),
                                Toast.LENGTH_SHORT).show());
                    }
                });
        android.os.Bundle params = new android.os.Bundle();
        params.putString("fields", "id,name,email");
        request.setParameters(params);
        request.executeAsync();
    }

    // ── Activity Result ───────────────────────────────────────────

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Facebook SDK
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Google Sign-In
        if (requestCode == SocialAuthManager.RC_GOOGLE_SIGN_IN) {
            btnGoogleLogin.setAlpha(1f);
            if (data != null && pendingGoogleCallback != null) {
                socialAuthManager.handleGoogleSignInResult(data, pendingGoogleCallback);
            } else if (pendingGoogleCallback != null) {
                pendingGoogleCallback.onCancelled();
            }
            pendingGoogleCallback = null;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    // ── Email/Password Login ──────────────────────────────────────

    private void handleLogin() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Please enter your email"); etEmail.requestFocus(); return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address"); etEmail.requestFocus(); return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Please enter your password"); etPassword.requestFocus(); return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters"); etPassword.requestFocus(); return;
        }

        dismissKeyboard();
        btnLogin.setEnabled(false);

        userRepository.login(email, password, user -> {
            btnLogin.setEnabled(true);
            if (user != null) {
                sharedPreferences.edit()
                        .putInt("user_id", user.id)
                        .putString("user_name", user.fullName)
                        .putString("user_email", user.email)
                        .putBoolean("isLoggedIn", true)
                        .apply();
                Toast.makeText(this, "Welcome back, " + user.fullName + "! 🌿", Toast.LENGTH_SHORT).show();
                navigateToDashboard();
            } else {
                etPassword.setError("Incorrect email or password");
                inputEmail.setBackgroundResource(R.drawable.bg_input_field_focused);
                inputPassword.setBackgroundResource(R.drawable.bg_input_field_focused);
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
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
