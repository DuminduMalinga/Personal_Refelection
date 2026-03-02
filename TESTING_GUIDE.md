# 🧪 Testing Guide - Adding Sample Data

## Overview
Since the Goals and Reflections CRUD screens are not yet implemented, you can add sample data directly to the database to test the dashboard display.

---

## Method 1: Using Android Studio Database Inspector

### Steps:
1. **Run the app** on an emulator or device
2. **Register/Login** to create a user account
3. In Android Studio: **View → Tool Windows → App Inspection**
4. Select **Database Inspector** tab
5. Select your app and the `goalreflect_db` database
6. Navigate to the **goals** table
7. Click **"+"** to add a new row:
   ```
   user_id: 1 (your user ID)
   title: "Learn Android Development"
   description: "Master Room, UI, and animations"
   target_date: "2026-04-01"
   is_completed: 0
   created_at: 1709395200000
   ```
8. Add more goals (mix of active and completed)
9. Navigate to **reflections** table
10. Add reflections linked to goal IDs

---

## Method 2: SQL Insert Statements

### Using ADB Shell:

```bash
# Connect to your device/emulator
adb shell

# Open the database
run-as com.example.personal_refelection
cd databases
sqlite3 goalreflect_db

# Insert sample goals
INSERT INTO goals (user_id, title, description, target_date, is_completed, created_at)
VALUES (1, 'Learn Android Development', 'Master Room, UI, and animations', '2026-04-01', 0, 1709395200000);

INSERT INTO goals (user_id, title, description, target_date, is_completed, created_at)
VALUES (1, 'Read 12 Books This Year', 'Focus on personal development', '2026-12-31', 0, 1709395200000);

INSERT INTO goals (user_id, title, description, target_date, is_completed, created_at)
VALUES (1, 'Exercise 3x Per Week', 'Build a consistent fitness habit', '2026-06-01', 1, 1709395200000);

# Insert sample reflections
INSERT INTO reflections (goal_id, content, created_at)
VALUES (1, 'Today I learned about Room Database and DAOs. The repository pattern makes everything so clean!', 1709481600000);

INSERT INTO reflections (goal_id, content, created_at)
VALUES (1, 'Implemented the dashboard with animations. Feeling proud of the progress!', 1709568000000);

INSERT INTO reflections (goal_id, content, created_at)
VALUES (2, 'Started reading "Atomic Habits" - great insights on building systems.', 1709395200000);

# Exit
.quit
exit
exit
```

---

## Method 3: Create a Test Data Helper Class

Create this in your project (temporary):

```java
package com.example.personal_refelection.utils;

import android.content.Context;
import com.example.personal_refelection.database.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestDataGenerator {
    
    public static void insertSampleData(Context context, int userId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AppDatabase db = AppDatabase.getInstance(context);
        
        executor.execute(() -> {
            // Insert goals
            Goal goal1 = new Goal(userId, 
                "Learn Android Development", 
                "Master Room, UI, and animations", 
                "2026-04-01");
            long g1Id = db.goalDao().insertGoal(goal1);
            
            Goal goal2 = new Goal(userId, 
                "Read 12 Books This Year", 
                "Focus on personal development", 
                "2026-12-31");
            long g2Id = db.goalDao().insertGoal(goal2);
            
            Goal goal3 = new Goal(userId, 
                "Exercise 3x Per Week", 
                "Build a consistent fitness habit", 
                "2026-06-01");
            goal3.isCompleted = true;
            long g3Id = db.goalDao().insertGoal(goal3);
            
            // Insert reflections
            Reflection r1 = new Reflection((int)g1Id, 
                "Today I learned about Room Database and DAOs. The repository pattern makes everything so clean!");
            db.reflectionDao().insertReflection(r1);
            
            Reflection r2 = new Reflection((int)g1Id, 
                "Implemented the dashboard with animations. Feeling proud of the progress!");
            db.reflectionDao().insertReflection(r2);
            
            Reflection r3 = new Reflection((int)g2Id, 
                "Started reading 'Atomic Habits' - great insights on building systems.");
            db.reflectionDao().insertReflection(r3);
        });
    }
}
```

Then call it from DashboardActivity (temporarily):
```java
// In onCreate() after loadDashboardData():
// TestDataGenerator.insertSampleData(this, userId);
```

**Remember to remove this before production!**

---

## Method 4: Quick ADB One-Liner

```bash
adb shell "run-as com.example.personal_refelection sqlite3 databases/goalreflect_db \"INSERT INTO goals (user_id, title, description, target_date, is_completed, created_at) VALUES (1, 'Test Goal', 'Description', '2026-04-01', 0, 1709395200000);\""
```

---

## Expected Dashboard After Sample Data

### Stats Should Show:
- **Active Goals**: 2
- **Achieved Goals**: 1
- **Total Reflections**: 3

### Recent Reflections Should Show:
1. "Implemented the dashboard..." (most recent)
2. "Today I learned about Room..." 
3. "Started reading Atomic Habits..."

---

## Verifying Data

### Check via Database Inspector:
1. Open **Database Inspector**
2. Navigate to **goals** table
3. Verify rows exist with your user_id
4. Navigate to **reflections** table
5. Verify reflections linked to goal_ids

### Check via Logcat:
Add this to DashboardActivity:
```java
private void loadDashboardData() {
    dashboardRepository.getDashboardStats(userId, (active, achieved, total) -> {
        Log.d("Dashboard", "Stats: Active=" + active + ", Achieved=" + achieved + ", Total=" + total);
        tvActiveGoalsCount.setText(String.valueOf(active));
        tvAchievedGoalsCount.setText(String.valueOf(achieved));
        tvTotalReflectionsCount.setText(String.valueOf(total));
    });
}
```

---

## Troubleshooting

### Dashboard Shows All Zeros
- Check user_id in SharedPreferences matches goals.user_id
- Verify goals table has data: `SELECT * FROM goals WHERE user_id = 1;`
- Check Room database version updated to 2

### No Reflections Showing
- Verify reflections.goal_id matches existing goals.id
- Check query: `SELECT * FROM reflections WHERE goal_id IN (SELECT id FROM goals WHERE user_id = 1);`
- Ensure created_at timestamp is valid

### App Crashes on Dashboard
- Check Logcat for stack trace
- Verify all Room entities compiled
- Ensure database migration successful

---

## 🎯 Recommended Test Flow

1. **Register** new user: "Test User"
2. **Add 3-5 sample goals** (mix active/completed)
3. **Add 5-10 reflections** across different goals
4. **Restart app** → Should auto-login to dashboard
5. **Verify stats** display correctly
6. **Check reflections** show recent 3
7. **Test animations** smooth and staggered
8. **Logout** and verify session cleared
9. **Login again** → Dashboard loads with data intact

---

## 📸 Screenshot Checklist

Capture these views for documentation:
- [ ] Dashboard with zero stats (new user)
- [ ] Dashboard with populated stats
- [ ] Recent reflections list
- [ ] Card entrance animations (video)
- [ ] Bottom navigation highlighting
- [ ] Overflow menu with logout
- [ ] FAB pressed state
- [ ] Quick action button states

---

**Testing Status**: Ready for QA  
**Sample Data**: Available via methods above  
**Next Steps**: Implement Goal CRUD to add data naturally

---

🧪 Happy Testing! 🌿

