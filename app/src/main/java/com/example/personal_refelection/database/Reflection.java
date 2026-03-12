package com.example.personal_refelection.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Room Entity representing a reflection entry.
 * goal_id is optional (0 = no goal linked).
 * user_id allows direct queries without joining goals.
 */
@Entity(tableName = "reflections")
public class Reflection {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "user_id")
    public int userId;

    @ColumnInfo(name = "goal_id")
    public int goalId;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    // Constructor with userId (preferred)
    public Reflection(int userId, int goalId, String content) {
        this.userId    = userId;
        this.goalId    = goalId;
        this.content   = content;
        this.createdAt = System.currentTimeMillis();
    }

    // Legacy constructor (goalId only) — kept for any remaining callers, ignored by Room
    @Ignore
    public Reflection(int goalId, String content) {
        this.goalId    = goalId;
        this.content   = content;
        this.createdAt = System.currentTimeMillis();
    }
}
