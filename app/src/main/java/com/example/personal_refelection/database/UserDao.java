package com.example.personal_refelection.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

/**
 * Data Access Object for User table.
 * All DB operations run on a background thread via the Repository.
 */
@Dao
public interface UserDao {

    // ── Register ──────────────────────────────────────────────────

    /** Insert a new user. Returns the new row ID, or -1 on conflict. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertUser(User user);

    // ── Login ─────────────────────────────────────────────────────

    /** Look up a user by email AND password — used for login. */
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    User loginWithEmail(String email, String password);

    // ── Forgot Password — Step 1 verify ──────────────────────────

    /** Check if a username + email pair exists in the DB. */
    @Query("SELECT * FROM users WHERE username = :username AND email = :email LIMIT 1")
    User findByUsernameAndEmail(String username, String email);

    // ── Forgot Password — Step 2 update ──────────────────────────

    /** Update the password for the user with the given email. */
    @Query("UPDATE users SET password = :newPassword WHERE email = :email")
    void updatePassword(String email, String newPassword);

    // ── Uniqueness checks (used before insert) ────────────────────

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    int countByEmail(String email);

    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    int countByUsername(String username);
}

