# 🔧 Login Crash Debugging Guide

## What Was Fixed

1. ✅ **SharedPreferences Initialization** - Now properly initialized in LoginActivity
2. ✅ **Session Auto-Login** - Now checks if user is already logged in on app launch
3. ✅ **Layout Hardcoded Strings** - Fixed "Overview" text to use string resource
4. ✅ **Greeting Text** - Changed to use string resource instead of hardcoded text

## Build Status

✅ **BUILD SUCCESSFUL** - No compilation errors

## How to Reproduce the Crash

To identify the exact crash, follow these steps:

### Step 1: Open Logcat
1. In Android Studio, go to **View → Tool Windows → Logcat**
2. Make sure you're seeing logs from your connected device/emulator

### Step 2: Clear Previous Logs
1. Click the **X icon** in Logcat to clear previous logs
2. Or press **Ctrl+L**

### Step 3: Run the App
1. Click ▶ **Run** button
2. Let app fully launch and go to Login screen

### Step 4: Try to Login
1. Enter email: `test@example.com`
2. Enter password: `test123`
3. Click Login button
4. Observe if it crashes

### Step 5: Check Logcat for Errors
Look for red error messages in Logcat showing:
- **Exception type** (e.g., NullPointerException, ActivityNotFoundException)
- **Stack trace** with line numbers
- **Caused by** information

## Common Crash Reasons & Fixes

### Crash 1: NullPointerException on findViewById()
**Error:** `NullPointerException: Attempt to invoke virtual method on a null object reference`

**Reason:** A view ID doesn't exist in the layout

**Fix:**
```java
// In DashboardActivity.bindViews(), verify all findViewById calls:
tvGreeting = findViewById(R.id.tvGreeting);  // ✅ Exists in layout
fabAddGoal = findViewById(R.id.fabAddGoal);  // ✅ Exists in layout
bottomNavigation = findViewById(R.id.bottomNavigation);  // ✅ Exists
```

### Crash 2: SharedPreferences Not Initialized
**Error:** `NullPointerException: Cannot invoke method on null object reference`

**Reason:** `sharedPreferences` is null when trying to use it

**Fix:** Already done! ✅
```java
// LoginActivity.onCreate()
sharedPreferences = getSharedPreferences("GoalReflectPrefs", MODE_PRIVATE);
```

### Crash 3: Activity Not Declared in Manifest
**Error:** `ActivityNotFoundException: Unable to find explicit activity class`

**Reason:** DashboardActivity not in AndroidManifest.xml

**Fix:** Already done! ✅
```xml
<!-- AndroidManifest.xml -->
<activity android:name=".DashboardActivity" android:exported="false" />
```

### Crash 4: Missing Layout File
**Error:** `InflateException: Could not find view in layout xml`

**Reason:** dashboard_activity.xml is missing or doesn't have required views

**Check:** Run this command
```bash
adb shell ls -la /data/app/com.example.personal_refelection-*/base.apk
```

## Quick Crash Checklist

- [ ] Can you register a new account? (If yes, database is working)
- [ ] After registration, does dashboard appear?
- [ ] If it crashes, what does Logcat say?
- [ ] Are you seeing a red error message?
- [ ] Does the error mention a specific line number?

## If You Still See Crashes

### Option 1: Send Logcat Error
Copy the **complete stack trace** from Logcat (all the red text) and share it. It will show exactly where the crash happens.

### Option 2: Check Database
Maybe the user wasn't actually created. Try:
1. Uninstall the app completely
2. Clear app data:
   ```bash
   adb shell pm clear com.example.personal_refelection
   ```
3. Reinstall and try registering again

### Option 3: Try Test Login
If you previously created an account with these credentials:
- **Email:** `test@example.com`
- **Password:** `test123`

Try logging in with those. If it fails, try registering a new account.

## Most Likely Cause

Based on the code review, the most likely issue was:
**SharedPreferences not being initialized in LoginActivity**

This is now fixed! ✅

## Testing After Fix

1. **Uninstall the app:**
   ```bash
   adb uninstall com.example.personal_refelection
   ```

2. **Run the updated app:**
   - Click ▶ Run in Android Studio

3. **Register a new account:**
   - Name: Any name
   - Email: Any valid email
   - Username: Any username
   - Password: Any 6+ character password

4. **Expected Result:**
   - Should go directly to Dashboard
   - Should NOT crash
   - Should show greeting with your name

5. **Close and reopen app:**
   - App should auto-login (no login screen)
   - Should show Dashboard directly

## Still Crashing?

If you still see a crash after these fixes:

1. **Check Logcat** - Copy the exact error message
2. **Check Android Manifest** - Make sure all activities are declared
3. **Check Layout Files** - Make sure dashboard_activity.xml exists and has all view IDs
4. **Check R.java** - Sometimes Android needs to rebuild resource IDs:
   ```
   Build → Clean Project
   Build → Rebuild Project
   ```

---

**Updated:** March 2, 2026  
**Build Status:** ✅ Successful  
**All Fixes:** Applied

Let me know if you still see the crash and share the Logcat error message!

