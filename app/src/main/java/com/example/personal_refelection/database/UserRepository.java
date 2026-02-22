package com.example.personal_refelection.database;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository — single source of truth for all User DB operations.
 * All queries are executed on a background thread via ExecutorService.
 * Results are delivered back on the main thread via Handler.
 */
public class UserRepository {

    private final UserDao userDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainThread      = new Handler(Looper.getMainLooper());

    public UserRepository(Context context) {
        userDao = AppDatabase.getInstance(context).userDao();
    }

    // ── Callbacks ─────────────────────────────────────────────────

    public interface Callback<T> {
        void onResult(T result);
    }

    // ── Register ──────────────────────────────────────────────────

    /**
     * Insert a new user. Callback receives:
     *  > 0  → success (new row ID)
     *  -1   → conflict (email or username already taken)
     *  -2   → email already used
     *  -3   → username already used
     */
    public void register(User user, Callback<Long> callback) {
        executor.execute(() -> {
            long result;
            int emailCount    = userDao.countByEmail(user.email);
            int usernameCount = userDao.countByUsername(user.username);

            if (emailCount > 0) {
                result = -2; // email taken
            } else if (usernameCount > 0) {
                result = -3; // username taken
            } else {
                result = userDao.insertUser(user);
            }

            final long finalResult = result;
            mainThread.post(() -> callback.onResult(finalResult));
        });
    }

    // ── Login ─────────────────────────────────────────────────────

    /**
     * Login by email + password. Callback receives the matched User or null.
     */
    public void login(String email, String password, Callback<User> callback) {
        executor.execute(() -> {
            User user = userDao.loginWithEmail(email, password);
            mainThread.post(() -> callback.onResult(user));
        });
    }

    // ── Forgot Password — Step 1 ──────────────────────────────────

    /**
     * Verify username + email pair. Callback receives matched User or null.
     */
    public void findByUsernameAndEmail(String username, String email, Callback<User> callback) {
        executor.execute(() -> {
            User user = userDao.findByUsernameAndEmail(username, email);
            mainThread.post(() -> callback.onResult(user));
        });
    }

    // ── Forgot Password — Step 2 ──────────────────────────────────

    /**
     * Update password for the given email. Callback receives true on success.
     */
    public void updatePassword(String email, String newPassword, Callback<Boolean> callback) {
        executor.execute(() -> {
            userDao.updatePassword(email, newPassword);
            mainThread.post(() -> callback.onResult(true));
        });
    }
}

