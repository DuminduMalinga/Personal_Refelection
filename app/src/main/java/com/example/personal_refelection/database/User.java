package com.example.personal_refelection.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Room Entity representing a registered user.
 * username and email are unique — enforced at DB level.
 */
@Entity(
    tableName = "users",
    indices = {
        @Index(value = "username", unique = true),
        @Index(value = "email",    unique = true)
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

    @ColumnInfo(name = "password")
    public String password;

    // ── Constructor ───────────────────────────────────
    public User(String fullName, String email, String username, String password) {
        this.fullName = fullName;
        this.email    = email;
        this.username = username;
        this.password = password;
    }
}

