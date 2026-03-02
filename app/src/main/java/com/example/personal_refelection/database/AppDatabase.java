package com.example.personal_refelection.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Singleton Room Database.
 * Increment version number whenever the schema changes.
 */
@Database(entities = {User.class, Goal.class, Reflection.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    private static final String DB_NAME = "goalreflect_db";

    public abstract UserDao userDao();
    public abstract GoalDao goalDao();
    public abstract ReflectionDao reflectionDao();

    /** Thread-safe singleton accessor. */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DB_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

