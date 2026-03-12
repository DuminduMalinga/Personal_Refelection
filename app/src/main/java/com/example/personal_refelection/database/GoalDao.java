package com.example.personal_refelection.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

/**
 * Data Access Object for Goal entity.
 */
@Dao
public interface GoalDao {

    @Insert
    long insertGoal(Goal goal);

    @Query("SELECT COUNT(*) FROM goals WHERE user_id = :userId AND is_completed = 0")
    int countActiveGoals(int userId);

    @Query("SELECT COUNT(*) FROM goals WHERE user_id = :userId AND is_completed = 1")
    int countAchievedGoals(int userId);

    @Query("SELECT * FROM goals WHERE user_id = :userId AND is_completed = 0 ORDER BY created_at DESC")
    List<Goal> getActiveGoals(int userId);

    @Query("SELECT * FROM goals WHERE user_id = :userId AND is_completed = 1 ORDER BY created_at DESC")
    List<Goal> getAchievedGoals(int userId);

    @Query("SELECT * FROM goals WHERE user_id = :userId ORDER BY created_at DESC")
    List<Goal> getAllGoals(int userId);

    @Update
    void updateGoal(Goal goal);

    @Delete
    void deleteGoal(Goal goal);

    @Query("UPDATE goals SET is_completed = 1 WHERE id = :goalId")
    void markGoalCompleted(int goalId);

    /** Goals completed this week (created_at between weekStart and weekEnd millis). */
    @Query("SELECT * FROM goals WHERE user_id = :userId AND is_completed = 1 AND created_at >= :weekStart AND created_at <= :weekEnd ORDER BY created_at DESC")
    List<Goal> getGoalsAchievedThisWeek(int userId, long weekStart, long weekEnd);

    /** Goals created (started) this week. */
    @Query("SELECT COUNT(*) FROM goals WHERE user_id = :userId AND created_at >= :weekStart AND created_at <= :weekEnd")
    int countGoalsCreatedThisWeek(int userId, long weekStart, long weekEnd);
}

