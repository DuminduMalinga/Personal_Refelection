# 🌿 GoalReflect — Personal Goal Reflection App

> **Module:** ICT3214 — Mobile Application Development
> **Project Idea #8:** Personal Goal Reflection App

---

## 📖 Table of Contents

- [Project Description](#-project-description)
- [Features](#-features)
- [Screens & UI](#-screens--ui)
- [Technologies Used](#-technologies-used)
- [Database Design](#-database-design)
- [Project Structure](#-project-structure)
- [Setup & Installation](#-setup--installation)
- [Team Members](#-team-members)
- [GitHub Contribution Guidelines](#-github-contribution-guidelines)

---

## 📝 Project Description

**GoalReflect** is a calm, minimal Android application designed to help users set personal goals, track their progress, and reflect on their self-growth journey. The app provides a peaceful and motivating environment that encourages daily self-reflection without overwhelm.

Users can register a personal account, log in securely (including Google Sign-In via Firebase), create and manage goals, write reflections, and monitor their journey over time — all stored locally using a Room (SQLite) database. The app features a full dark / light / system theme engine, rich notification scheduling (goal reminders, reflection prompts, achievement alerts, deadline warnings, and weekly summaries), and a weekly progress report screen.

The app's design philosophy is rooted in **minimalism**, **clarity**, and **calm motivation** — inspired by journaling and personal growth practices.

---

## ✨ Features

### 🚀 Onboarding Flow
- **Splash Screen** — Full-screen branded launch screen with animated logo bounce, app name, tagline, and loading indicator; auto-navigates based on session state
- **Get Started Screen** — Motivational onboarding screen shown on first launch; hero gradient header, rotating quote card, 3 feature highlights, and dual CTA buttons (Register / Sign In)
- **Smart routing** — Returning users skip onboarding and go directly to Login or Dashboard

### 🔐 Authentication
- **User Registration** — Full Name, Email, Username, Password with real-time validation
- **User Login** — Email + Password authenticated against the Room database
- **Google Sign-In** — Firebase Authentication with Google; profile photo loaded via Glide
- **Forgot Password** — Two-step identity verification (Username + Email match) then password reset — no email required
- **Duplicate detection** — Email and username uniqueness enforced at the database level
- **Session-aware** — Logged-in users only see their own data

### 🎯 Goal Management
- Create goals with title, description, category, priority (Low / Medium / High), and target date & time
- Mark goals as In Progress / Completed with a single tap
- Edit existing goals via a dialog (title, description, target date)
- Delete goals with a confirmation swipe / button
- Goals list filtered by status — Active / All in `GoalsActivity`
- Add New Goal screen (`AddGoalActivity`) — modern card-based form with category chips, priority selector, date-time picker, and animated Save button
- Navigate directly to `AddGoalActivity` from the Dashboard FAB (+)

### 📓 Reflection Journal
- Dedicated **Reflections screen** (`ReflectionsActivity`) accessible from the bottom navigation bar
- Write reflections with an optional goal link using a bottom-sheet dialog
- View all reflections in a full-page RecyclerView with smart timestamps
- Delete reflections

### 🏆 Achieved Goals
- `AchievedActivity` — dedicated screen for all completed goals
- Trophy card layout with completion date, goal title, and "Completed" badge
- Summary header with total achieved count and motivational text
- Tap a card to view goal details
- Empty state with illustration and "View Active Goals" button
- **Weekly Report** button opens the weekly growth summary screen

### 📊 Weekly Report
- `WeeklyReportActivity` — full weekly growth summary screen
- Displays: active goal count, goals achieved this week, new goals started this week, reflections written this week
- Scrollable lists of goals achieved and reflections written during the week
- Navigate between past weeks using Prev / Next arrow buttons
- Auto-triggered every **Sunday at 9:00 PM** via AlarmManager
- Past-weeks data browsable from the Achieved Goals screen

### 🔔 Notification System
- **Goal Reminders** — Daily notification to check on active goals (scheduled at a fixed time daily)
- **Reflection Prompts** — Evening notification prompting the user to write a daily reflection
- **Achievement Alerts** — Instant notification when a goal is marked as completed
- **Deadline Notifications** — Exact alarm fires when a goal's target date/time is reached
- **5-Minute Deadline Warning** — Alert fires 5 minutes before a goal's deadline
- **Weekly Summary** — Sunday 9:00 PM summary notification with active/achieved/reflection counts
- All schedules survive device reboots via `BootReceiver` (re-registers all alarms on `BOOT_COMPLETED`)
- All channels managed by `NotificationHelper`; channel preferences persisted via `NotificationReceiver`

### 🎨 Theme Engine
- `ThemeManager` — centralised Light / Dark / System theme controller
- User selects theme from **Profile → App Theme** (opens `AppThemeActivity` dialog)
- Choice persisted in `SharedPreferences` and applied globally via `MyApp.onCreate()`
- All screens (including sub-screens like Edit Profile, Notification Settings, Privacy Policy, About App) respect the chosen theme
- Night-mode resource overrides in `res/values-night/`

### 👤 Profile & Account Settings
- **View Profile** — Display full name, email, username, and live journey stats (active goals, achieved goals, total reflections)
- **Edit Profile** — Update full name, username, email, and password with validation; camera/gallery avatar picker
- **Profile Picture** — Camera capture or gallery selection with runtime permission handling; image stored in app-private storage and persisted across sessions
- **App Theme** — Light / Dark / System picker (shown in Account Settings row with current selection preview)
- **Notification Settings** — Toggle goal reminders, reflection prompts, achievement alerts, and weekly summary; preferences persisted in SharedPreferences
- **Privacy Policy** — Static informational screen
- **About App** — App mission, feature overview, version, and tech stack info
- **Logout** — Confirmation dialog; clears session and redirects to Login

### 🎨 UI & Animations
- **Edge-to-edge display** — `WindowInsetsCompat` applied on all screens; status bar drawn over gradient headers (no white padding)
- **Shared Top Navigation Bar** — `BaseActivity.setupTopNav()` injects a unified header (screen label, time-based greeting, user name, avatar) on every screen via `layout_top_nav.xml` include; profile avatar click navigates to Profile
- **Shared Bottom Navigation Bar** — `BaseActivity.setupBottomNav()` injects the 5-tab bar on every screen; active tab highlighted per screen
- **Splash logo bounce** — Scale + overshoot + fade animation on logo reveal
- **Splash text slide-up** — App name and tagline animate in after logo settles
- **Get Started entrance** — Staggered animations: logo pop → quote card slide-up → feature rows fade-in
- Category chips in Add Goal — colour-coded emoji chips (Health, Study, Career, Personal, Finance, Other) in a properly aligned 3×2 grid
- Priority buttons — Low / Medium / High selector with accent colour highlight
- Smooth screen transitions and card animations
- Two-step forgot password flow with slide-up card transition
- Shake animation feedback on incorrect identity verification
- Focus-highlight effect on all input fields (coloured border on focus)
- Step indicator with animated dot progression
- Green online status dot on Dashboard profile avatar

---

## 📱 Screens & UI

| Screen | Description | Status |
|--------|-------------|--------|
| **Splash** | Animated logo + app name on gradient; routes to Get Started / Login / Dashboard | ✅ Complete |
| **Get Started** | Motivational onboarding; hero header, quote card, feature highlights, Register / Sign In buttons | ✅ Complete |
| **Login** | Email + Password login; Google Sign-In button; "Forgot password?" and "Register" links | ✅ Complete |
| **Register** | Full Name, Email, Username, Password, Confirm Password with duplicate detection | ✅ Complete |
| **Forgot Password** | Step 1: Verify Username + Email → Step 2: Set new password | ✅ Complete |
| **Dashboard** | Personal overview with stats cards, recent reflections, FAB (+), shared top & bottom nav | ✅ Complete |
| **Goals** | Active goals list with CRUD; Add / Edit / Delete / Mark Achieved | ✅ Complete |
| **Add Goal** | Full-screen add form — title, description, category chips, priority, date-time picker | ✅ Complete |
| **Achieved** | Completed goals with trophy cards; summary header; Weekly Report button | ✅ Complete |
| **Reflections** | Full reflection journal with bottom-sheet add dialog; delete support | ✅ Complete |
| **Weekly Report** | Week-by-week growth summary; prev/next week navigation; auto-triggered Sunday 9 PM | ✅ Complete |
| **Profile** | User info, journey stats, settings rows, logout with confirmation dialog | ✅ Complete |
| **Edit Profile** | Update name/username/email/password; camera/gallery avatar picker | ✅ Complete |
| **Notification Settings** | Toggle all 4 notification types; persisted via SharedPreferences | ✅ Complete |
| **App Theme** | Light / Dark / System theme picker; applied globally | ✅ Complete |
| **Privacy Policy** | Static informational screen | ✅ Complete |
| **About App** | App info, mission, features, and tech stack | ✅ Complete |

### Splash Screen ✅
- **Full-screen gradient** — Diagonal gradient background
- **Animated white logo circle** — 120dp circle; logo bounces in with scale + overshoot animation (800ms)
- **App name + tagline** — Slide-up fade animation (delayed 900ms after logo)
- **Loading indicator** — Small progress spinner + "Loading your reflection space…" text at bottom
- **Smart routing** — After 2.8s: checks session → navigates to Dashboard (logged in), Login (returning user), or Get Started (first launch)

### Get Started Screen ✅
- **Hero gradient header** — 300dp section with rounded bottom corners, decorative circles, logo, app name
- **Quote card** — Floating white card (elevation 8dp) with motivational quote; overlaps hero with `-20dp` top margin
- **Feature highlights** — 3 rows: Set Powerful Goals 🎯, Daily Reflections 📓, Celebrate Achievements 🏆
- **CTA buttons** — "Get Started — It's Free" (filled gradient) and "Already have an account? Sign In" (outlined)
- **Privacy note** — "🔒 No spam. No ads. Your data stays private."
- **Staggered animations** — Logo → quote card → features animate in sequence on screen entry

### Dashboard Features ✅
- **Dynamic Greeting** — Time-based greeting (Morning / Afternoon / Evening) with user's name
- **Stat Cards** — Two equal-height gradient cards: Active Goals (purple) and Achieved Goals (teal), icons and counts centred, labels below
- **Recent Reflections** — Displays 3 most recent reflections with smart timestamps
- **FAB (+)** — "Add New Goal" floating action button navigates to `AddGoalActivity`
- **Add Reflection Button** — Dedicated button opens the add-reflection bottom sheet
- **Bottom Navigation** — 5-item nav bar (Dashboard, Goals, Add, Achieved, Reflections) pinned flush to screen bottom
- **Shared Top Nav** — Screen label "MY GOALS", greeting, avatar with green online dot
- **Data Refresh** — Stats and reflections reload automatically on every `onResume()`

### Add Goal Screen ✅
- **Top Nav** — Shared gradient top bar (no back button, no white gap)
- **Bottom Nav** — Full 5-tab bottom nav present
- **Goal Title** — Mandatory text input
- **Description** — Optional multi-line text input
- **Category Chips** — 6 chips in a 3-column grid: 💪 Health, 📚 Study, 💼 Career, 🌱 Personal, 💰 Finance, ✨ Other; white label text on purple background, proper alignment
- **Priority** — Low / Medium / High selector buttons with colour accent
- **Target Date & Time** — Clickable row opens `DatePickerDialog` then `TimePickerDialog`; selected date/time displayed in row
- **Save Goal Button** — Large gradient button at bottom

### Goals Screen ✅
- **RecyclerView** — All goals displayed via `GoalAdapter` in `item_goal_activity.xml` cards
- **Status badge** — "Active" (blue) or "Achieved ✓" (green) badge per card
- **Actions** — Edit (opens dialog), Delete (confirmation), Mark Achieved (moves to Achieved list)
- **Empty state** — Shown when no goals exist with illustration and prompt

### Achieved Goals Screen ✅
- **Summary header** — Total count, motivational message
- **RecyclerView** — `AchievedGoalAdapter` renders trophy cards with completion date and "Completed" badge
- **Weekly Report button** — Opens `WeeklyReportActivity`
- **Empty state** — Trophy illustration + "Start working on your goals!" message + "View Active Goals" button

### Reflections Screen ✅
- **Full-page RecyclerView** — All reflections sorted newest-first
- **Add Reflection** — Bottom-sheet dialog (`bottom_sheet_add_reflection.xml`) with optional goal selector
- **Delete** — Long-press or swipe to delete a reflection
- **No back button / back arrow** — Tab-based navigation only

### Weekly Report Screen ✅
- **Week range header** — "Mar 10 – Mar 16" style label with Prev / Next navigation arrows
- **Stat rows** — Active goals, goals achieved this week, new goals started, reflections written
- **Goal list** — RecyclerView of goals achieved in the selected week
- **Reflection list** — RecyclerView of reflections written in the selected week
- **Empty states** — Shown individually if no goals or reflections for the week
- **Motivational message** — Changes based on achievements

### Profile Features ✅
- **Hero Header** — Circular avatar (camera/gallery/remove), full name, email, `@username`
- **Journey Stats** — Live stat cards: Active Goals, Achieved Goals, Total Reflections
- **Account Settings Rows** — Edit Profile, Notification Settings, App Theme (shows current mode), Privacy Policy, About App, Logout
- **Logout Dialog** — Clears all SharedPreferences; resets theme preference; redirects to Login
- **Avatar Persistence** — Saved avatar restored from app-private storage on every `onResume()`
- **Modern Top Nav** — No white status bar gap; gradient extends edge-to-edge; bottom of header rounded

### Edit Profile / Notification / Privacy / About Screens ✅
- All share the same gradient header style (no white status bar)
- Bottom edges of header card rounded
- Edge-to-edge layout; system insets applied correctly

---

## 🛠️ Technologies Used

| Technology | Version | Purpose |
|-----------|---------|---------|
| **Java** | 11 | Primary programming language |
| **Android SDK** | API 36 (Android 16) | Target platform |
| **Min SDK** | API 24 (Android 7.0) | Minimum supported device |
| **Room Database** | 2.6.1 | Local SQLite ORM (Entity, DAO, Database) — v4 schema |
| **AndroidX AppCompat** | 1.7.1 | Backwards-compatible Activity support |
| **Material Components** | 1.13.0 | Material Design UI widgets (BottomNavigationView, BottomSheetDialog, SwitchMaterial, etc.) |
| **ConstraintLayout** | 2.2.1 | Flexible, flat UI layouts |
| **AndroidX Activity** | 1.12.4 | Edge-to-edge window support; `ActivityResultLauncher` for camera/gallery; `OnBackPressedDispatcher` |
| **Firebase BOM** | 34.10.0 | Firebase dependency management |
| **Firebase Auth** | — (BOM) | Google Sign-In via Firebase Authentication |
| **Firebase Analytics** | — (BOM) | Usage analytics |
| **Google Play Services Auth** | 21.3.0 | Google Sign-In credential flow |
| **Glide** | 4.16.0 | Load Google profile photos from URL; avatar image rendering |
| **AlarmManager** | — | Exact alarms for goal deadlines, 5-min warnings, daily reminders, weekly summaries |
| **NotificationManager** | — | 5 notification channels; `NotificationCompat.Builder` |
| **SharedPreferences** | — | Session management; onboarding state; notification toggles; avatar path; theme; per-goal alarm flags |
| **FileProvider** | — | Secure camera URI sharing (AndroidX Core) |
| **Gradle** | 9.0.1 | Build system |
| **Android Gradle Plugin** | 9.0.1 | Android build toolchain |

---

## 🗄️ Database Design

### Room Database: `goalreflect_db` (Version 4)

#### Table 1: `users`

| Column | Type | Constraint |
|--------|------|------------|
| `id` | INTEGER | Primary Key, Auto-increment |
| `full_name` | TEXT | Not Null |
| `email` | TEXT | **UNIQUE**, Not Null |
| `username` | TEXT | **UNIQUE**, Not Null |
| `password` | TEXT | Not Null |

#### Table 2: `goals`

| Column | Type | Constraint |
|--------|------|------------|
| `id` | INTEGER | Primary Key, Auto-increment |
| `user_id` | INTEGER | **Foreign Key** → `users.id`, CASCADE |
| `title` | TEXT | Not Null |
| `description` | TEXT | — |
| `target_date` | TEXT | — |
| `is_completed` | BOOLEAN | Default: 0 (false) |
| `created_at` | INTEGER | Timestamp (ms) |

#### Table 3: `reflections`

| Column | Type | Constraint |
|--------|------|------------|
| `id` | INTEGER | Primary Key, Auto-increment |
| `user_id` | INTEGER | Direct user reference (no join needed) |
| `goal_id` | INTEGER | Optional link → `goals.id` (0 = standalone) |
| `content` | TEXT | Not Null |
| `created_at` | INTEGER | Timestamp (ms) |

### DAO Operations

| DAO Method | Purpose |
|------------|---------|
| **UserDao** | |
| `insertUser(user)` | Register new user |
| `loginWithEmail(email, password)` | Authenticate login |
| `findByUsernameAndEmail(username, email)` | Forgot password step 1 |
| `updatePassword(email, newPassword)` | Forgot password step 2 / Edit Profile |
| `countByEmail(email)` | Duplicate email check |
| `countByUsername(username)` | Duplicate username check |
| `getUserByEmail(email)` | Fetch user after registration / profile load |
| `updateFullName(email, fullName)` | Edit Profile — name update |
| `updateUsername(email, username)` | Edit Profile — username update |
| **GoalDao** | |
| `insertGoal(goal)` | Add a new goal |
| `updateGoal(goal)` | Edit goal fields |
| `deleteGoal(goal)` | Delete a goal |
| `markGoalCompleted(goalId)` | Set is_completed = 1 |
| `countActiveGoals(userId)` | Count in-progress goals |
| `countAchievedGoals(userId)` | Count completed goals |
| `getActiveGoals(userId)` | Fetch active goals list |
| `getAchievedGoals(userId)` | Fetch achieved goals list |
| `getAllGoals(userId)` | Fetch all goals for user |
| `getGoalsAchievedThisWeek(userId, weekStart, weekEnd)` | Weekly report — achieved goals |
| `countGoalsCreatedThisWeek(userId, weekStart, weekEnd)` | Weekly report — new goals count |
| **ReflectionDao** | |
| `insertReflection(reflection)` | Add a new reflection |
| `deleteReflection(reflection)` | Delete a reflection |
| `countTotalReflections(userId)` | Count all user reflections |
| `getRecentReflections(userId)` | Fetch 3 most recent reflections |
| `getAllReflections(userId)` | Full reflection history |
| `getReflectionsByGoal(userId, goalId)` | Reflections for a specific goal |
| `countReflectionsThisWeek(userId, weekStart, weekEnd)` | Weekly report — reflection count |
| `getRecentReflectionsThisWeek(userId, weekStart, weekEnd)` | Weekly report — top 3 reflections |

### Architecture

```
Activities / Fragments
    ├── BaseActivity              ← Shared top nav + bottom nav injection
    ├── UserRepository            ← Single source of truth (Auth + Profile updates)
    └── DashboardRepository       ← Dashboard & Profile stats
            └── UserDao / GoalDao / ReflectionDao  ← @Dao interfaces
                    └── AppDatabase (v4)  ← @Database singleton
                            └── Room (SQLite)
```

All database queries run on a **background thread** via `ExecutorService` and post results back to the **main thread** via `Handler(Looper.getMainLooper())`.

---

## 📁 Project Structure

```
app/src/main/
├── java/com/example/personal_refelection/
│   ├── database/
│   │   ├── User.java                  ← @Entity — users table
│   │   ├── Goal.java                  ← @Entity — goals table
│   │   ├── Reflection.java            ← @Entity — reflections table (user_id + optional goal_id)
│   │   ├── UserDao.java               ← @Dao — User SQL queries (auth + profile updates)
│   │   ├── GoalDao.java               ← @Dao — Goal SQL queries (CRUD + weekly stats)
│   │   ├── ReflectionDao.java         ← @Dao — Reflection queries (CRUD + weekly stats)
│   │   ├── AppDatabase.java           ← @Database singleton — Room DB v4; fallbackToDestructiveMigration
│   │   ├── UserRepository.java        ← Repository — auth + profile operations
│   │   └── DashboardRepository.java   ← Repository — dashboard & profile stats
│   ├── models/
│   │   └── DashboardStats.java        ← Data model for dashboard stats
│   ├── MyApp.java                     ← Application class — applies saved theme globally
│   ├── BaseActivity.java              ← Abstract base — injects shared top nav & bottom nav
│   ├── ThemeManager.java              ← Light / Dark / System theme manager (SharedPreferences)
│   ├── NotificationHelper.java        ← Notification channels, scheduling, and post helpers
│   ├── NotificationReceiver.java      ← BroadcastReceiver — handles all alarm intents
│   ├── BootReceiver.java              ← BroadcastReceiver — re-registers alarms after device reboot
│   ├── SplashActivity.java            ← Splash screen — animated logo, smart routing
│   ├── GetStartedActivity.java        ← Onboarding — motivational first-launch screen
│   ├── LoginActivity.java             ← Login screen + Google Sign-In + session handling
│   ├── RegisterActivity.java          ← Registration + auto-login
│   ├── ForgotPasswordActivity.java    ← Two-step password recovery
│   ├── DashboardActivity.java         ← Main dashboard (stats, FAB, add reflection, shared nav)
│   ├── GoalsActivity.java             ← Goals list screen (GoalAdapter, CRUD)
│   ├── GoalAdapter.java               ← RecyclerView adapter for active/all goals
│   ├── AddGoalActivity.java           ← Add New Goal form (category chips, priority, date-time picker)
│   ├── AchievedActivity.java          ← Achieved goals screen (AchievedGoalAdapter, weekly report link)
│   ├── AchievedGoalAdapter.java       ← RecyclerView adapter for achieved goals cards
│   ├── ReflectionsActivity.java       ← Full reflection journal (add via bottom sheet, delete)
│   ├── WeeklyReportActivity.java      ← Weekly growth summary (prev/next week navigation)
│   ├── ProfileActivity.java           ← Profile screen (stats, settings rows, logout, theme row)
│   ├── EditProfileActivity.java       ← Edit name/username/email/password + avatar picker
│   ├── NotificationSettingsActivity.java ← Notification toggles UI (4 switches)
│   ├── PrivacyPolicyActivity.java     ← Static privacy policy screen
│   ├── AboutAppActivity.java          ← About app — info, mission, features, tech stack
│   └── SocialAuthManager.java         ← Google Sign-In helper (Firebase Auth wrapper)
│
├── res/
│   ├── layout/
│   │   ├── activity_splash.xml                 ← Splash UI
│   │   ├── activity_get_started.xml            ← Get Started UI
│   │   ├── login_activity.xml                  ← Login UI (Email + Google Sign-In)
│   │   ├── register_activity.xml               ← Register UI
│   │   ├── forgot_password_activity.xml        ← Forgot password UI (2 steps)
│   │   ├── dashboard_activity.xml              ← Dashboard UI (stat cards, FAB, recent reflections)
│   │   ├── goal_activity.xml                   ← Goals list UI
│   │   ├── activity_add_goal.xml               ← Add Goal form UI
│   │   ├── goal_achieved.xml                   ← Achieved goals screen UI
│   │   ├── activity_reflections.xml            ← Reflections full-page UI
│   │   ├── activity_weekly_report.xml          ← Weekly report UI
│   │   ├── profile.xml                         ← Profile UI (hero header, stats, settings rows)
│   │   ├── activity_edit_profile.xml           ← Edit Profile UI
│   │   ├── activity_notification_settings.xml  ← Notification toggles UI
│   │   ├── activity_privacy_policy.xml         ← Privacy Policy static UI
│   │   ├── activity_about_app.xml              ← About App static UI
│   │   ├── layout_top_nav.xml                  ← Shared top navigation bar (included in all screens)
│   │   ├── layout_bottom_nav.xml               ← Shared bottom navigation bar (included in all screens)
│   │   ├── bottom_sheet_add_reflection.xml     ← Add reflection bottom sheet
│   │   ├── bottom_sheet_photo_picker.xml       ← Camera / Gallery / Remove bottom sheet
│   │   ├── dialog_add_edit_goal.xml            ← Add / Edit goal dialog
│   │   ├── item_goal_activity.xml              ← Goal card list item
│   │   ├── item_achieved_goal.xml              ← Achieved goal card list item
│   │   ├── item_reflection.xml                 ← Dashboard recent reflection list item
│   │   └── item_reflection_full.xml            ← Full reflection list item (ReflectionsActivity)
│   ├── menu/
│   │   ├── bottom_nav_menu.xml        ← 5-item bottom navigation menu
│   │   └── dashboard_menu.xml         ← Overflow menu (Logout)
│   ├── anim/
│   │   ├── splash_logo_bounce.xml     ← Logo scale + overshoot + fade animation
│   │   ├── splash_text_slide_up.xml   ← Text slide-up fade-in (delayed 900ms)
│   │   ├── splash_exit.xml            ← Scale-out fade for splash exit transition
│   │   ├── slide_in_right.xml         ← Slide-in from right
│   │   ├── fade_in.xml                ← Generic fade-in animation
│   │   └── scale_up.xml               ← Generic scale-up animation
│   ├── drawable/
│   │   └── ... (80+ vector drawables — backgrounds, icons, selectors, chips, cards)
│   ├── values/
│   │   ├── colors.xml                 ← Brand colour palette
│   │   ├── strings.xml                ← All UI text strings
│   │   ├── themes.xml                 ← App theme (Material3 Light) + Splash theme
│   │   └── dimens.xml                 ← Dimensions
│   ├── values-night/
│   │   └── themes.xml                 ← Dark mode theme overrides
│   └── xml/
│       └── ...                        ← FileProvider paths, backup rules
│
└── AndroidManifest.xml                ← Activities, receivers (NotificationReceiver, BootReceiver), permissions
```

---

## 🔄 App Navigation Flow

```
App Launch
    └── SplashActivity (2.8s animated)
            ├── isLoggedIn = true  ──────────────────→ DashboardActivity
            ├── hasSeenOnboarding = true  ───────────→ LoginActivity
            └── first launch  ───────────────────────→ GetStartedActivity
                                                              ├── "Get Started" → RegisterActivity
                                                              └── "Sign In"     → LoginActivity

RegisterActivity ──→ DashboardActivity (auto-login)
LoginActivity    ──→ DashboardActivity

DashboardActivity
    ├── FAB (+)           ──────────────────────────→ AddGoalActivity
    ├── Add Reflection    ──────────────────────────→ bottom_sheet_add_reflection
    └── Bottom Nav ───────────────────────────────── Goals | Achieved | Reflections | Profile

GoalsActivity          ← full CRUD (GoalAdapter + dialog_add_edit_goal)
AddGoalActivity        ← full add form (category, priority, date-time picker)
AchievedActivity       ← completed goals + Weekly Report button → WeeklyReportActivity
ReflectionsActivity    ← all reflections + bottom-sheet add
ProfileActivity        ← stats + settings rows
    ├── Edit Profile   → EditProfileActivity
    ├── Notifications  → NotificationSettingsActivity
    ├── App Theme      → theme picker dialog (Light / Dark / System)
    ├── Privacy Policy → PrivacyPolicyActivity
    ├── About App      → AboutAppActivity
    └── Logout         → LoginActivity

AlarmManager (background)
    ├── Daily Goal Reminder     (→ NotificationReceiver → postGoalReminder)
    ├── Daily Reflection Prompt (→ NotificationReceiver → postReflectionPrompt)
    ├── Per-Goal Deadline       (→ NotificationReceiver → postDeadlineNotification)
    ├── Per-Goal 5-min Warning  (→ NotificationReceiver → postDeadlineWarning)
    ├── Weekly Summary (Sun 9PM)(→ NotificationReceiver → postWeeklySummary)
    └── Achievement Alert       (→ NotificationReceiver → postAchievementAlert) [fired on goal completion]
```

---

## ⚙️ Setup & Installation

### Prerequisites
- Android Studio **Meerkat (2024.3.1)** or later
- JDK 11 or higher
- Android device / emulator running **API 24+**
- A `google-services.json` file in `app/` (required for Firebase / Google Sign-In)

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/<your-org>/Personal_Refelection.git
   ```

2. **Open in Android Studio**
   - File → Open → Select the cloned folder

3. **Sync Gradle**
   - Android Studio will auto-sync. If not, click **"Sync Now"** in the notification bar.

4. **Configure Firebase (if needed)**
   - Add your own `google-services.json` to `app/`
   - Enable Google Sign-In in the Firebase Console under Authentication → Sign-in methods

5. **Run the app**
   - Select a device / emulator
   - Click ▶ **Run** or press `Shift + F10`

6. **First use**
   - The **Splash Screen** appears for ~3 seconds with animated logo
   - The **Get Started** screen presents the app's key features
   - Tap **"Get Started — It's Free"** to register, or **"Already have an account? Sign In"** to log in

> **Note:** The database is created automatically on first launch (Room v4, `fallbackToDestructiveMigration`). No manual setup required.

> **Notification Permissions:** On Android 13+ the app will request `POST_NOTIFICATIONS` permission at runtime. Grant it to receive reminders and alerts.

---

## 👥 Team Members

| Role | Name | Responsibilities |
|------|------|-----------------|
| 👑 **Team Lead & Main Developer** | **Dumindu Malinga** | Project architecture, Room database setup, authentication logic (Login, Register, Forgot Password, Google Sign-In), Splash & Get Started screens, notification system, theme engine, weekly report, activity development, GitHub management |
| 🎨 **UI Designer** | **Ishini Awanka** | Screen layouts (XML), colour palette, drawable resources, icon design, Material Design implementation, responsive UI, dark-mode resource overrides |
| ⚙️ **Features & Animations** | **Theekshana Bandara** | Goal CRUD features, Add Goal screen (category chips, priority, date-time picker), reflection journal, achieved goals screen, transition animations, splash animations, shake feedback, step indicator animations, input focus effects |

### Module Details
- **Module:** ICT3214 — Mobile Application Development
- **Project:** Personal Goal Reflection App (Idea #8)
- **Academic Year:** 2025/2026

---

## 📋 GitHub Contribution Guidelines

Each team member must follow these rules for commits:

### ✅ Good commit messages
```
Add Splash screen with animated logo bounce and smart routing
Add Get Started onboarding screen with motivational quote and feature highlights
Add Room database entity and DAO for users table
Implement login validation with Room DB query
Design register screen layout with Material components
Add shake animation for forgot password mismatch
Fix duplicate email detection in UserRepository
Fix dashboard bottom navigation bar edge-to-edge alignment
Pin quick action buttons above bottom navigation bar
Implement Profile screen with stats, settings rows, and logout dialog
Add Edit Profile screen with camera/gallery avatar picker
Add Notification Settings screen with SharedPreferences persistence
Add Privacy Policy and About App static screens
Add AddGoalActivity with category chips, priority selector, and date-time picker
Add AchievedActivity with trophy cards and empty state
Add ReflectionsActivity with bottom-sheet add dialog
Add WeeklyReportActivity with prev/next week navigation
Add NotificationHelper with 5 channels and AlarmManager scheduling
Add BootReceiver to re-register alarms after device reboot
Add ThemeManager with Light/Dark/System preference support
Add Google Sign-In via Firebase Authentication
Fix status bar white padding on sub-screens
Fix rounded bottom edges on gradient header cards
```

### ❌ Avoid
```
update
fix
final version
changes
```

### Branch Strategy
```
main        ← stable, working code only
dev         ← integration branch
feature/*   ← individual feature branches
```

---

## 📄 License

This project is developed for academic purposes as part of the **ICT3214 — Mobile Application Development** module.

---

<div align="center">

**GoalReflect** — *Your journey to growth* 🌿

Made with ❤️ by **Dumindu Malinga**, **Ishini Awanka** & **Theekshana Bandara**

</div>
