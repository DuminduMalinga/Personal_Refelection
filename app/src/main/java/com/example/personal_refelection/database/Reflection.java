package com.example.personal_refelection.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Room Entity representing a reflection entry.
 * Linked to Goal via goal_id foreign key.
 */
@Entity(
    tableName = "reflections",
    foreignKeys = @ForeignKey(
        entity = Goal.class,
        parentColumns = "id",
        childColumns = "goal_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("goal_id")}
)
public class Reflection {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "goal_id")
    public int goalId;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    // ── Constructor ───────────────────────────────────
    public Reflection(int goalId, String content) {
        this.goalId = goalId;
        this.content = content;
        this.createdAt = System.currentTimeMillis();
    }
}

