package com.example.personal_refelection;

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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Firebase-backed social authentication.
 *
 * Google  → GoogleSignIn picks the account  → Firebase signInWithCredential
 * Facebook → Facebook SDK returns AccessToken → Firebase signInWithCredential
 *
 * After Firebase auth succeeds, the user is found-or-created in the local Room DB
 * so the rest of the app (goals, reflections, etc.) works completely offline too.
 */
public class SocialAuthManager {

    private static final String TAG = "SocialAuthManager";
    public  static final int    RC_GOOGLE_SIGN_IN = 9001;

    public interface SocialAuthCallback {
        void onSuccess(User user);
        void onCancelled();
        void onError(String message);
    }

    private final Context           context;
    private final UserRepository    userRepository;
    private final SharedPreferences prefs;
    private final FirebaseAuth      firebaseAuth;
    private final GoogleSignInClient googleSignInClient;

    public SocialAuthManager(Context context) {
        this.context        = context;
        this.userRepository = new UserRepository(context);
        this.prefs          = context.getSharedPreferences("GoalReflectPrefs", Context.MODE_PRIVATE);
        this.firebaseAuth   = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();
        this.googleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    // ────────────────────────────────────────────────────────────
    //  GOOGLE — Step 1: get Intent to launch account picker
    // ────────────────────────────────────────────────────────────

    public Intent getGoogleSignInIntent() {
        googleSignInClient.signOut(); // always show picker
        return googleSignInClient.getSignInIntent();
    }

    // ────────────────────────────────────────────────────────────
    //  GOOGLE — Step 2: handle result from onActivityResult
    // ────────────────────────────────────────────────────────────

    public void handleGoogleSignInResult(Intent data, SocialAuthCallback callback) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            String idToken = account.getIdToken();
            if (idToken == null) {
                callback.onError("Google sign-in failed: no ID token received.");
                return;
            }
            Log.d(TAG, "Google ID token received, authenticating with Firebase...");
            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
            firebaseSignIn(credential, callback);

        } catch (ApiException e) {
            Log.e(TAG, "Google ApiException code=" + e.getStatusCode());
            if (e.getStatusCode() == 12501) {
                callback.onCancelled();
            } else if (e.getStatusCode() == 10) {
                callback.onError("SHA-1 not registered in Firebase Console (code 10). See setup guide.");
            } else {
                callback.onError("Google sign-in failed (code " + e.getStatusCode() + ").");
            }
        }
    }

    // ────────────────────────────────────────────────────────────
    //  FACEBOOK — call this with the AccessToken from Facebook SDK
    // ────────────────────────────────────────────────────────────

    public void handleFacebookAccessToken(com.facebook.AccessToken token, SocialAuthCallback callback) {
        Log.d(TAG, "Facebook token received, authenticating with Firebase...");
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseSignIn(credential, callback);
    }

    // ────────────────────────────────────────────────────────────
    //  SHARED — Firebase sign-in → find/create local Room user
    // ────────────────────────────────────────────────────────────

    private void firebaseSignIn(AuthCredential credential, SocialAuthCallback callback) {
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser fbUser = authResult.getUser();
                    if (fbUser == null) {
                        callback.onError("Firebase returned null user.");
                        return;
                    }
                    String email    = fbUser.getEmail();
                    String fullName = fbUser.getDisplayName();
                    if (email == null) email = fbUser.getUid() + "@social.user";
                    if (fullName == null || fullName.isEmpty()) fullName = email;

                    Log.d(TAG, "Firebase auth OK  uid=" + fbUser.getUid() + "  email=" + email);
                    String username = generateUsername(email);
                    findOrCreateLocalUser(email, fullName, username, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firebase signInWithCredential failed: " + e.getMessage());
                    callback.onError("Authentication failed: " + e.getMessage());
                });
    }

    // ────────────────────────────────────────────────────────────
    //  Find-or-create user in local Room DB (runs on main thread)
    // ────────────────────────────────────────────────────────────

    public void findOrCreateLocalUser(String email, String fullName,
                                       String username, SocialAuthCallback callback) {
        String safeUsername = (username == null || username.length() < 3)
                ? "user" + Math.abs(email.hashCode() % 10000) : username;

        userRepository.getUserByEmail(email, existing -> {
            if (existing != null) {
                Log.d(TAG, "Local user found, logging in: " + email);
                saveSession(existing);
                callback.onSuccess(existing);
                return;
            }

            Log.d(TAG, "Local user not found, creating: " + email);
            User newUser = new User(fullName, email, safeUsername, "firebase_oauth");
            userRepository.register(newUser, result -> {
                if (result > 0) {
                    userRepository.getUserByEmail(email, created -> {
                        if (created != null) {
                            saveSession(created);
                            callback.onSuccess(created);
                        } else {
                            callback.onError("Could not retrieve new account.");
                        }
                    });
                } else if (result == -3) {
                    String alt = safeUsername + (System.currentTimeMillis() % 9000 + 1000);
                    User retry = new User(fullName, email, alt, "firebase_oauth");
                    userRepository.register(retry, r2 ->
                        userRepository.getUserByEmail(email, created -> {
                            if (created != null) { saveSession(created); callback.onSuccess(created); }
                            else callback.onError("Registration failed after username retry.");
                        })
                    );
                } else if (result == -2) {
                    userRepository.getUserByEmail(email, exists -> {
                        if (exists != null) { saveSession(exists); callback.onSuccess(exists); }
                        else callback.onError("Account exists but could not be loaded.");
                    });
                } else {
                    callback.onError("Local registration failed (code " + result + ").");
                }
            });
        });
    }

    // ────────────────────────────────────────────────────────────
    //  Helpers
    // ────────────────────────────────────────────────────────────

    private void saveSession(User user) {
        prefs.edit()
                .putInt("user_id",        user.id)
                .putString("user_name",   user.fullName)
                .putString("user_email",  user.email)
                .putBoolean("isLoggedIn", true)
                .apply();
    }

    private String generateUsername(String email) {
        if (email == null) return "user" + (System.currentTimeMillis() % 10000);
        String local = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "").toLowerCase();
        return local.length() >= 3 ? local : "user" + Math.abs(email.hashCode() % 10000);
    }
}
