package com.example.personal_refelection;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.CancellationSignal;
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
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import java.util.concurrent.Executors;

/**
 * Handles Google Sign-In via Credential Manager API.
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

    public SocialAuthManager(Context context) {
        this.context      = context;
        this.userRepository = new UserRepository(context);
        this.prefs        = context.getSharedPreferences("GoalReflectPrefs", Context.MODE_PRIVATE);
        this.credentialManager = CredentialManager.create(context);
    }

    // ─────────────────────────────────────────────────────────────
    //  GOOGLE SIGN-IN via Credential Manager
    // ─────────────────────────────────────────────────────────────

    public void signInWithGoogle(android.app.Activity activity, SocialAuthCallback callback) {
        String webClientId = context.getString(R.string.lbl_google_web_client_id);

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)   // show all accounts, not just previously signed-in
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                activity,
                request,
                new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleGoogleCredential(result, callback);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        if (e instanceof GetCredentialCancellationException) {
                            callback.onCancelled();
                        } else {
                            Log.e(TAG, "Google sign-in error: " + e.getMessage());
                            callback.onError(e.getMessage());
                        }
                    }
                }
        );
    }

    private void handleGoogleCredential(GetCredentialResponse response, SocialAuthCallback callback) {
        if (response.getCredential() instanceof CustomCredential) {
            CustomCredential customCredential = (CustomCredential) response.getCredential();
            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(customCredential.getType())) {
                try {
                    GoogleIdTokenCredential googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(customCredential.getData());

                    String email    = googleIdTokenCredential.getId();
                    String fullName = googleIdTokenCredential.getDisplayName() != null
                            ? googleIdTokenCredential.getDisplayName() : email;
                    String username = generateUsername(email);

                    findOrCreateSocialUser(email, fullName, username, "google_oauth", callback);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse Google credential: " + e.getMessage());
                    callback.onError("Failed to parse Google account info.");
                }
            } else {
                callback.onError("Unexpected credential type.");
            }
        } else {
            callback.onError("Unexpected credential type.");
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  SHARED: Find-or-create a social user in Room DB
    // ─────────────────────────────────────────────────────────────

    /**
     * Called after obtaining verified identity from Google or Facebook.
     * If email already exists in DB → log them in.
     * If not → auto-register them with a generated password.
     */
    public void findOrCreateSocialUser(
            String email,
            String fullName,
            String username,
            String passwordToken,
            SocialAuthCallback callback) {

        userRepository.getUserByEmail(email, existingUser -> {
            if (existingUser != null) {
                // User already registered — log them in
                saveSession(existingUser);
                callback.onSuccess(existingUser);
            } else {
                // Auto-register with social token as password
                User newUser = new User(fullName, email, username, passwordToken);
                userRepository.register(newUser, result -> {
                    if (result > 0) {
                        userRepository.getUserByEmail(email, createdUser -> {
                            if (createdUser != null) {
                                saveSession(createdUser);
                                callback.onSuccess(createdUser);
                            } else {
                                callback.onError("Failed to retrieve new account.");
                            }
                        });
                    } else if (result == -3) {
                        // Username taken — retry with a different suffix
                        String altUsername = username + "_" + System.currentTimeMillis() % 1000;
                        User retryUser = new User(fullName, email, altUsername, passwordToken);
                        userRepository.register(retryUser, r2 -> {
                            userRepository.getUserByEmail(email, createdUser -> {
                                if (createdUser != null) {
                                    saveSession(createdUser);
                                    callback.onSuccess(createdUser);
                                } else {
                                    callback.onError("Registration failed.");
                                }
                            });
                        });
                    } else {
                        callback.onError("Registration failed.");
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

    /** Convert "john.doe@gmail.com" → "johndoe" */
    private String generateUsername(String email) {
        if (email == null) return "user" + System.currentTimeMillis() % 10000;
        String local = email.split("@")[0];
        return local.replaceAll("[^a-zA-Z0-9_]", "").toLowerCase();
    }
}

