package com.example.personal_refelection.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Represents a registered user in the local Room (SQLite) database.
 * Passwords are stored as a BCrypt / SHA-256 hash – never plain text.
 */
@Entity(
        tableName = "users",
        indices = {
                @Index(value = "email",    unique = true),
                @Index(value = "username", unique = true)
        }
)
public class User {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "full_name")
    public String fullName;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "username")
    public String username;

    /** Stores a SHA-256 hex hash of the user's password. */
    @ColumnInfo(name = "password_hash")
    public String passwordHash;

    /** Convenience constructor used when inserting a new user. */
    public User(String fullName, String email, String username, String passwordHash) {
        this.fullName     = fullName;
        this.email        = email;
        this.username     = username;
        this.passwordHash = passwordHash;
    }
}

