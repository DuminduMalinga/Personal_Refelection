package com.example.personal_refelection.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

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
}

