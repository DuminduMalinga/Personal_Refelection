package com.example.personal_refelection.database;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for Dashboard operations.
 * Handles background thread execution and main thread callbacks.
 */
public class DashboardRepository {

    private final GoalDao goalDao;
    private final ReflectionDao reflectionDao;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public DashboardRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.goalDao = db.goalDao();
        this.reflectionDao = db.reflectionDao();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Callback for dashboard statistics.
     */
    public interface StatsCallback {
        void onResult(int activeGoals, int achievedGoals, int totalReflections);
    }

    /**
     * Callback for recent reflections list.
     */
    public interface ReflectionsCallback {
        void onResult(List<Reflection> reflections);
    }

    /**
     * Fetch dashboard statistics for a user.
     */
    public void getDashboardStats(int userId, StatsCallback callback) {
        executor.execute(() -> {
            int activeGoals = goalDao.countActiveGoals(userId);
            int achievedGoals = goalDao.countAchievedGoals(userId);
            int totalReflections = reflectionDao.countTotalReflections(userId);

            mainHandler.post(() -> callback.onResult(activeGoals, achievedGoals, totalReflections));
        });
    }

    /**
     * Fetch recent reflections for a user.
     */
    public void getRecentReflections(int userId, ReflectionsCallback callback) {
        executor.execute(() -> {
            List<Reflection> reflections = reflectionDao.getRecentReflections(userId);
            mainHandler.post(() -> callback.onResult(reflections));
        });
    }
}

