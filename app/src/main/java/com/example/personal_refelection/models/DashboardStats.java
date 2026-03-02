package com.example.personal_refelection.models;

/**
 * Simple data class to hold dashboard statistics.
 */
public class DashboardStats {
    public int activeGoals;
    public int achievedGoals;
    public int totalReflections;

    public DashboardStats(int activeGoals, int achievedGoals, int totalReflections) {
        this.activeGoals = activeGoals;
        this.achievedGoals = achievedGoals;
        this.totalReflections = totalReflections;
    }
}

