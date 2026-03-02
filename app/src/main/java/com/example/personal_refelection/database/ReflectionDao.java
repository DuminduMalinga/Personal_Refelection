package com.example.personal_refelection.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object for Reflection entity.
 */
@Dao
public interface ReflectionDao {

    @Insert
    long insertReflection(Reflection reflection);

    @Query("SELECT COUNT(*) FROM reflections WHERE goal_id IN (SELECT id FROM goals WHERE user_id = :userId)")
    int countTotalReflections(int userId);

    @Query("SELECT r.* FROM reflections r " +
           "INNER JOIN goals g ON r.goal_id = g.id " +
           "WHERE g.user_id = :userId " +
           "ORDER BY r.created_at DESC LIMIT 3")
    List<Reflection> getRecentReflections(int userId);
}

