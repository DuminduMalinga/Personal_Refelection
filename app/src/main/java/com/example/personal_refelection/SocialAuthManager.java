package com.example.personal_refelection;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.CustomCredential;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.GetCredentialCancellationException;

import com.example.personal_refelection.database.User;
import com.example.personal_refelection.database.UserRepository;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import java.util.concurrent.Executors;

/**
 * Handles Google Sign-In via Credential Manager API (bottom-sheet picker).
 * Uses GetSignInWithGoogleOption which works for ALL users — new and returning.
 * Facebook login is handled directly in the Activity via Facebook SDK.
 *
 * Both paths find-or-create a user in the local Room DB and
 * save the session to SharedPreferences.
 */
public class SocialAuthManager {

    private static final String TAG = "SocialAuthManager";

    public interface SocialAuthCallback {
        void onSuccess(User user);
        void onCancelled();
        void onError(String message);
    }

    private final Context context;
    private final UserRepository userRepository;
    private final SharedPreferences prefs;
    private final CredentialManager credentialManager;
    // Dedicated executor — separate from UserRepository's executor to avoid deadlock
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public SocialAuthManager(Context context) {
        this.context           = context;
        this.userRepository    = new UserRepository(context);
        this.prefs             = context.getSharedPreferences("GoalReflectPrefs", Context.MODE_PRIVATE);
        this.credentialManager = CredentialManager.create(context);
    }

    // ─────────────────────────────────────────────────────────────
    //  GOOGLE SIGN-IN via Credential Manager (bottom-sheet picker)
    // ─────────────────────────────────────────────────────────────

    public void signInWithGoogle(android.app.Activity activity, SocialAuthCallback callback) {
        String webClientId = context.getString(R.string.lbl_google_web_client_id);

        // GetSignInWithGoogleOption shows the Google account bottom-sheet for ALL users
        GetSignInWithGoogleOption signInWithGoogleOption = new GetSignInWithGoogleOption
                .Builder(webClientId)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build();

        credentialManager.getCredentialAsync(
                activity,
                request,
                new CancellationSignal(),
                // Use a fresh executor so it never shares threads with UserRepository
                Executors.newCachedThreadPool(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        // Parse on this background thread, then hand off to main thread
                        // so UserRepository's single-thread executor is free to run
                        parseGoogleCredentialAndRegister(result, callback);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        Log.e(TAG, "Credential error [" + e.getClass().getSimpleName() + "]: " + e.getMessage());
                        if (e instanceof GetCredentialCancellationException) {
                            mainHandler.post(callback::onCancelled);
                        } else {
                            String msg = e.getMessage() != null ? e.getMessage() : "Google sign-in failed";
                            mainHandler.post(() -> callback.onError(msg));
                        }
                    }
                }
        );
    }

    private void parseGoogleCredentialAndRegister(GetCredentialResponse response, SocialAuthCallback callback) {
        if (!(response.getCredential() instanceof CustomCredential)) {
            Log.e(TAG, "Not a CustomCredential: " + response.getCredential().getClass().getSimpleName());
            mainHandler.post(() -> callback.onError("Unexpected credential type."));
            return;
        }

        CustomCredential custom = (CustomCredential) response.getCredential();

        if (!GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(custom.getType())) {
            Log.e(TAG, "Wrong credential type: " + custom.getType());
            mainHandler.post(() -> callback.onError("Unexpected credential type: " + custom.getType()));
            return;
        }

        try {
            GoogleIdTokenCredential tokenCredential = GoogleIdTokenCredential.createFrom(custom.getData());

            String email    = tokenCredential.getId();
            String fullName = tokenCredential.getDisplayName() != null
                    ? tokenCredential.getDisplayName() : email;
            String username = generateUsername(email);

            Log.d(TAG, "Google credential parsed OK — email=" + email + " name=" + fullName);

            // Post to main thread so UserRepository's single executor is free
            mainHandler.post(() ->
                findOrCreateSocialUser(email, fullName, username, "google_oauth", callback)
            );

        } catch (Exception e) {
            Log.e(TAG, "Failed to parse GoogleIdTokenCredential: " + e.getMessage());
            mainHandler.post(() -> callback.onError("Failed to read Google account info."));
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  SHARED: Find-or-create social user in Room DB
    // ─────────────────────────────────────────────────────────────

    public void findOrCreateSocialUser(String email, String fullName,
                                        String username, String passwordToken,
                                        SocialAuthCallback callback) {

        String safeUsername = (username == null || username.length() < 3)
                ? "user" + Math.abs(email.hashCode() % 10000) : username;

        // Step 1 — check if user exists
        userRepository.getUserByEmail(email, existingUser -> {
            // This callback runs on the main thread (UserRepository guarantees it)
            if (existingUser != null) {
                Log.d(TAG, "Social user exists, logging in: " + email);
                saveSession(existingUser);
                callback.onSuccess(existingUser);
            } else {
                Log.d(TAG, "Social user new, registering: " + email);
                // Step 2 — register new user
                User newUser = new User(fullName, email, safeUsername, passwordToken);
                userRepository.register(newUser, result -> {
                    // Runs on main thread
                    if (result > 0) {
                        // Step 3 — fetch back the created user to get their DB id
                        userRepository.getUserByEmail(email, created -> {
                            if (created != null) {
                                Log.d(TAG, "Registered and fetched user id=" + created.id);
                                saveSession(created);
                                callback.onSuccess(created);
                            } else {
                                callback.onError("Could not retrieve new account.");
                            }
                        });

                    } else if (result == -3) {
                        // Username taken — retry with unique suffix
                        String alt = safeUsername + (System.currentTimeMillis() % 1000);
                        User retry = new User(fullName, email, alt, passwordToken);
                        userRepository.register(retry, r2 ->
                            userRepository.getUserByEmail(email, created -> {
                                if (created != null) {
                                    saveSession(created);
                                    callback.onSuccess(created);
                                } else {
                                    callback.onError("Registration failed after username retry.");
                                }
                            })
                        );

                    } else if (result == -2) {
                        // Email already exists — just log them in
                        userRepository.getUserByEmail(email, existing -> {
                            if (existing != null) {
                                saveSession(existing);
                                callback.onSuccess(existing);
                            } else {
                                callback.onError("Account exists but could not be loaded.");
                            }
                        });

                    } else {
                        callback.onError("Registration failed (code " + result + ").");
                    }
                });
            }
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────

    private void saveSession(User user) {
        prefs.edit()
                .putInt("user_id", user.id)
                .putString("user_name", user.fullName)
                .putString("user_email", user.email)
                .putBoolean("isLoggedIn", true)
                .apply();
    }

    /** "john.doe@gmail.com" → "johndoe" (min 3 chars guaranteed) */
    private String generateUsername(String email) {
        if (email == null) return "user" + (System.currentTimeMillis() % 10000);
        String local = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "").toLowerCase();
        return local.length() >= 3 ? local : "user" + Math.abs(email.hashCode() % 10000);
    }
}
