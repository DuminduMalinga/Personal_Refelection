package com.example.personal_refelection.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

/**
 * Data-Access Object for the {@link User} entity.
 */
@Dao
public interface UserDao {

    /** Insert a new user; returns the new row-id, or -1 on conflict. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertUser(User user);

    /**
     * Find a user by email – used for login and duplicate-check during registration.
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    /**
     * Find a user by username – used for the forgot-password identity check.
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User getUserByUsername(String username);

    /**
     * Check if a username already exists (registration uniqueness check).
     */
    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    int countByUsername(String username);

    /**
     * Check if an email already exists (registration uniqueness check).
     */
    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    int countByEmail(String email);

    /**
     * Update the stored password hash for a given user id.
     * Used by the Forgot-Password flow after identity is verified.
     */
    @Query("UPDATE users SET password_hash = :newHash WHERE id = :userId")
    void updatePassword(int userId, String newHash);
}

