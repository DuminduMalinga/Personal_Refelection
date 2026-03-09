package com.example.personal_refelection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.personal_refelection.database.User;
import com.example.personal_refelection.database.UserRepository;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

/**
 * Google Sign-In using the proven play-services-auth GoogleSignIn API.
 * Works on all Android devices that have Google Play Services.
 *
 * Usage:
 *   1. Call getGoogleSignInIntent() to get the Intent and launch it with startActivityForResult
 *   2. In onActivityResult pass the data to handleGoogleSignInResult()
 */
public class SocialAuthManager {

    private static final String TAG = "SocialAuthManager";
    public  static final int    RC_GOOGLE_SIGN_IN = 9001;

    public interface SocialAuthCallback {
        void onSuccess(User user);
        void onCancelled();
        void onError(String message);
    }

    private final Context        context;
    private final UserRepository userRepository;
    private final SharedPreferences prefs;
    private final GoogleSignInClient googleSignInClient;

    public SocialAuthManager(Context context) {
        this.context        = context;
        this.userRepository = new UserRepository(context);
        this.prefs          = context.getSharedPreferences("GoalReflectPrefs", Context.MODE_PRIVATE);

        // Build GoogleSignInOptions with the Web Client ID
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.lbl_google_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        this.googleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    // ── Step 1: get the sign-in Intent ────────────────────────────

    /** Call this to launch the Google account picker */
    public Intent getGoogleSignInIntent() {
        // Always sign out first so the picker always shows the account chooser
        googleSignInClient.signOut();
        return googleSignInClient.getSignInIntent();
    }

    // ── Step 2: handle the result ─────────────────────────────────

    /** Call this from onActivityResult when requestCode == RC_GOOGLE_SIGN_IN */
    public void handleGoogleSignInResult(Intent data, SocialAuthCallback callback) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            String email    = account.getEmail();
            String fullName = account.getDisplayName();
            if (email == null) {
                callback.onError("Google account has no email address.");
                return;
            }
            if (fullName == null || fullName.isEmpty()) fullName = email;
            String username = generateUsername(email);

            Log.d(TAG, "Google sign-in OK  email=" + email + "  name=" + fullName);
            findOrCreateSocialUser(email, fullName, username, "google_oauth", callback);

        } catch (ApiException e) {
            Log.e(TAG, "Google sign-in failed  code=" + e.getStatusCode() + "  msg=" + e.getMessage());
            if (e.getStatusCode() == 12501) {
                // User pressed back / cancelled
                callback.onCancelled();
            } else if (e.getStatusCode() == 10) {
                // Developer error — SHA-1 not registered in Google Cloud Console
                callback.onError("Configuration error (10): Please register the app SHA-1 in Google Cloud Console.");
            } else if (e.getStatusCode() == 7) {
                // Network error
                callback.onError("Network error. Please check your internet connection.");
            } else {
                callback.onError("Google sign-in failed (code " + e.getStatusCode() + "). Check Logcat for details.");
            }
        }
    }

    // ── Find-or-create social user in Room DB ─────────────────────
    // Called on the MAIN thread → UserRepository callbacks are safe

    public void findOrCreateSocialUser(String email, String fullName,
                                        String username, String passwordToken,
                                        SocialAuthCallback callback) {

        String safeUsername = (username == null || username.length() < 3)
                ? "user" + Math.abs(email.hashCode() % 10000) : username;

        // 1 — Does the user already exist?
        userRepository.getUserByEmail(email, existingUser -> {
            if (existingUser != null) {
                Log.d(TAG, "Social user exists, logging in: " + email);
                saveSession(existingUser);
                callback.onSuccess(existingUser);
                return;
            }

            // 2 — New user — register them
            Log.d(TAG, "Social user not found, registering: " + email);
            User newUser = new User(fullName, email, safeUsername, passwordToken);
            userRepository.register(newUser, result -> {
                if (result > 0) {
                    // 3 — Fetch the newly created user to get their DB id
                    userRepository.getUserByEmail(email, created -> {
                        if (created != null) {
                            Log.d(TAG, "Registered user id=" + created.id);
                            saveSession(created);
                            callback.onSuccess(created);
                        } else {
                            callback.onError("Could not retrieve new account.");
                        }
                    });

                } else if (result == -3) {
                    // Username collision — retry with unique suffix
                    String alt   = safeUsername + (System.currentTimeMillis() % 9000 + 1000);
                    User   retry = new User(fullName, email, alt, passwordToken);
                    userRepository.register(retry, r2 ->
                        userRepository.getUserByEmail(email, created -> {
                            if (created != null) { saveSession(created); callback.onSuccess(created); }
                            else callback.onError("Registration failed after username retry.");
                        })
                    );

                } else if (result == -2) {
                    // Email exists (race) — just log them in
                    userRepository.getUserByEmail(email, existing -> {
                        if (existing != null) { saveSession(existing); callback.onSuccess(existing); }
                        else callback.onError("Account exists but could not be loaded.");
                    });

                } else {
                    callback.onError("Registration failed (code " + result + ").");
                }
            });
        });
    }

    // ── Helpers ───────────────────────────────────────────────────

    private void saveSession(User user) {
        prefs.edit()
                .putInt("user_id",    user.id)
                .putString("user_name",  user.fullName)
                .putString("user_email", user.email)
                .putBoolean("isLoggedIn", true)
                .apply();
    }

    private String generateUsername(String email) {
        if (email == null) return "user" + (System.currentTimeMillis() % 10000);
        String local = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "").toLowerCase();
        return local.length() >= 3 ? local : "user" + Math.abs(email.hashCode() % 10000);
    }
}
