package com.example.personal_refelection.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object for Reflection entity.
 * Queries use user_id directly — no goal join needed.
 */
@Dao
public interface ReflectionDao {

    @Insert
    long insertReflection(Reflection reflection);

    @Delete
    void deleteReflection(Reflection reflection);

    @Query("SELECT COUNT(*) FROM reflections WHERE user_id = :userId")
    int countTotalReflections(int userId);

    @Query("SELECT * FROM reflections WHERE user_id = :userId ORDER BY created_at DESC LIMIT 3")
    List<Reflection> getRecentReflections(int userId);

    @Query("SELECT * FROM reflections WHERE user_id = :userId ORDER BY created_at DESC")
    List<Reflection> getAllReflections(int userId);

    @Query("SELECT * FROM reflections WHERE user_id = :userId AND goal_id = :goalId ORDER BY created_at DESC")
    List<Reflection> getReflectionsByGoal(int userId, int goalId);

    /** Reflections written this week (created_at between weekStart and weekEnd millis). */
    @Query("SELECT COUNT(*) FROM reflections WHERE user_id = :userId AND created_at >= :weekStart AND created_at <= :weekEnd")
    int countReflectionsThisWeek(int userId, long weekStart, long weekEnd);

    /** Latest 3 reflections from this week for the report preview. */
    @Query("SELECT * FROM reflections WHERE user_id = :userId AND created_at >= :weekStart AND created_at <= :weekEnd ORDER BY created_at DESC LIMIT 3")
    List<Reflection> getRecentReflectionsThisWeek(int userId, long weekStart, long weekEnd);
}
