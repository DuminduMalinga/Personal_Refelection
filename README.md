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

Users can register a personal account, log in securely, create and manage goals, write reflections, and monitor their journey over time — all stored locally using a Room (SQLite) database.

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
- **Forgot Password** — Two-step identity verification (Username + Email match) then password reset — no email required
- **Duplicate detection** — Email and username uniqueness enforced at the database level
- **Session-aware** — Logged-in users only see their own data

### 🎯 Goal Management
- Create personal goals with title, description, and target date
- Mark goals as In Progress / Completed
- Edit and delete existing goals
- Full CRUD operations per user

### 📓 Reflection Journal
- Write daily reflections linked to specific goals
- View reflection history per goal
- Edit and delete reflections

### 👤 Profile & Account Settings
- **View Profile** — Display full name, email, username, and live journey stats (active goals, achieved goals, total reflections)
- **Edit Profile** — Update full name, username, email, and password with validation
- **Profile Picture** — Camera capture or gallery selection with runtime permission handling; image stored in app-private storage and persisted across sessions
- **Notification Settings** — Toggle goal reminders, reflection prompts, achievement alerts, and weekly summary; preferences persisted in SharedPreferences
- **Privacy Policy** — Static informational screen
- **About App** — App mission, feature overview, and tech stack info
- **Logout** — Confirmation dialog; clears session and redirects to Login

### 🎨 UI & Animations
- **Splash logo bounce** — Scale + overshoot + fade animation on logo reveal
- **Splash text slide-up** — App name and tagline animate in after logo settles
- **Get Started entrance** — Staggered animations: logo pop → quote card slide-up → feature rows fade-in
- Smooth screen transitions and card animations
- Two-step forgot password flow with slide-up card transition
- Shake animation feedback on incorrect identity verification
- Focus-highlight effect on all input fields (green border on focus)
- Step indicator with animated dot progression
- Edge-to-edge display with proper system window inset handling
- Pinned action buttons above bottom navigation bar on Dashboard
- Bottom navigation bar pinned flush to screen bottom edge on all screens

### 🛡️ Security
- Passwords are **never stored in plain text**
- Input validation on all forms (client-side)
- Unique constraints enforced at database level (email, username)
- Runtime permission handling for camera and media access (Android 7–16 compatible)
- FileProvider used for secure camera URI sharing

---

## 📱 Screens & UI

| Screen | Description | Status |
|--------|-------------|--------|
| **Splash** | Animated logo + app name on green gradient; routes to Get Started / Login / Dashboard | ✅ Complete |
| **Get Started** | Motivational onboarding; hero header, quote card, feature highlights, Register / Sign In buttons | ✅ Complete |
| **Login** | Email + Password login with "Forgot password?" and "Register" links | ✅ Complete |
| **Register** | Full Name, Email, Username, Password, Confirm Password with duplicate detection | ✅ Complete |
| **Forgot Password** | Step 1: Verify Username + Email → Step 2: Set new password | ✅ Complete |
| **Dashboard** | Personal overview with stats, recent reflections, pinned quick actions & bottom nav | ✅ Complete |
| **Profile** | User info, journey stats, settings rows, logout with confirmation dialog | ✅ Complete |
| **Edit Profile** | Update name, username, email, password; camera/gallery avatar picker | ✅ Complete |
| **Notification Settings** | Toggle notifications; persisted via SharedPreferences | ✅ Complete |
| **Privacy Policy** | Static informational screen | ✅ Complete |
| **About App** | App info, mission, features, and tech stack | ✅ Complete |
| **Goals** | CRUD for personal goals | 🚧 Coming Soon |
| **Achieved** | View completed goals | 🚧 Coming Soon |

### Splash Screen ✅
- **Full-screen green gradient** — Diagonal gradient `#1D9B6A → #27C483 → #2EDBA5`
- **Animated white logo circle** — 120dp circle; logo bounces in with scale + overshoot animation (800ms)
- **App name + tagline** — Slide-up fade animation (delayed 900ms after logo)
- **Loading indicator** — Small progress spinner + "Loading your reflection space…" text at bottom
- **Smart routing** — After 2.8s: checks session → navigates to Dashboard (logged in), Login (returning user), or Get Started (first launch)
- **Exit animation** — Scale-out fade on transition away

### Get Started Screen ✅
- **Hero gradient header** — 300dp green gradient section with rounded bottom corners, decorative circles, logo, app name, and "Track. Reflect. Achieve." tagline
- **Quote card** — Floating white card (elevation 8dp) with motivational quote and author; overlaps the hero with `-20dp` top margin
- **Feature highlights** — 3 rows: Set Powerful Goals (🎯), Daily Reflections (📓), Celebrate Achievements (🏆); each with coloured icon circle and descriptive text
- **CTA buttons** — "Get Started — It's Free" (filled green gradient) and "Already have an account? Sign In" (outlined); both with `stateListAnimator="@null"` for crisp press response
- **Privacy note** — "🔒 No spam. No ads. Your data stays private."
- **Back press** — Minimizes app instead of navigating back
- **Staggered animations** — Logo → quote card → features animate in sequence on screen entry

### Dashboard Features ✅
- **Dynamic Greeting** — Time-based greeting (Morning / Afternoon / Evening) with user's name
- **Stat Cards** — Three equal-height cards showing Active Goals, Achieved Goals, and Total Reflections
- **Recent Reflections** — Displays 3 most recent reflections with smart timestamps (Today / Yesterday / Full date)
- **Pinned Quick Actions** — "View My Goals" and "View Achieved" buttons always visible just above the bottom navigation bar, regardless of scroll position
- **Bottom Navigation** — 5-item nav bar (Dashboard, Goals, Add, Achieved, Profile) pinned flush to the screen bottom edge
- **Edge-to-Edge Layout** — Full-bleed display; system window insets applied correctly so no gap appears below the navigation bar
- **Session Management** — Auto-redirects to login if no session; logout via overflow menu
- **Data Refresh** — Stats and reflections reload automatically on every `onResume()`

### Profile Features ✅
- **Hero Header** — Circular avatar (camera/gallery/remove), full name, email, and `@username`
- **Journey Stats** — Live stat cards for Active Goals, Achieved Goals, and Total Reflections
- **Settings Rows** — Edit Profile, Notification Settings, Privacy Policy, About App, Logout
- **Logout Dialog** — Confirmation dialog; clears all SharedPreferences and resets dark-mode preference
- **Avatar Persistence** — Saved avatar restored from app-private storage on every `onResume()`
- **Bottom Navigation** — Active "Profile" tab highlighted; seamless navigation to other screens
- **Edge-to-Edge Layout** — CoordinatorLayout with ScrollView; `paddingBottom` ensures content clears the bottom nav bar

### Edit Profile Features ✅
- **Editable Fields** — Full Name, Username, Email, New Password, Confirm Password
- **Optional Password Change** — Leave password fields blank to keep existing password
- **Avatar Picker Bottom Sheet** — Choose Camera, Gallery, or Remove photo
- **Camera Support** — Runtime permission request; FileProvider-backed temp URI; stores JPEG in `files/avatars/`
- **Gallery Support** — Handles `READ_MEDIA_IMAGES` / `READ_MEDIA_VISUAL_USER_SELECTED` / `READ_EXTERNAL_STORAGE` across Android 7–16
- **Image Scaling** — Resizes selected image to 256×256 before saving to minimise storage

### Design Style
- **Background:** Soft neutral `#F8FAFB`
- **Primary colour:** Calming green `#27C483`
- **Splash / Get Started:** Rich green gradient `#1D9B6A → #27C483`
- **Cards:** White (`#FFFFFF`) with elevation
- **Typography:** `sans-serif-medium`, clean and readable
- **Icons:** Custom vector drawables

---

## 🛠️ Technologies Used

| Technology | Version | Purpose |
|-----------|---------|---------|
| **Java** | 11 | Primary programming language |
| **Android SDK** | API 36 (Android 16) | Target platform |
| **Min SDK** | API 24 (Android 7.0) | Minimum supported device |
| **Room Database** | 2.6.1 | Local SQLite ORM (Entity, DAO, Database) |
| **AndroidX AppCompat** | 1.7.1 | Backwards-compatible Activity support |
| **Material Components** | 1.13.0 | Material Design UI widgets (BottomNavigationView, BottomSheetDialog, SwitchMaterial, etc.) |
| **ConstraintLayout** | 2.2.1 | Flexible, flat UI layouts |
| **AndroidX Activity** | 1.12.4 | Edge-to-edge window support (`EdgeToEdge.enable()`); `ActivityResultLauncher` for camera/gallery; `OnBackPressedDispatcher` |
| **FileProvider** | — | Secure camera URI sharing (AndroidX Core) |
| **SharedPreferences** | — | Session management; onboarding state; notification toggles; avatar path persistence |
| **Gradle** | 9.0.1 | Build system |
| **Android Gradle Plugin** | 9.0.1 | Android build toolchain |

---

## 🗄️ Database Design

### Room Database: `goalreflect_db` (Version 2)

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
| `created_at` | INTEGER | Timestamp |

#### Table 3: `reflections`

| Column | Type | Constraint |
|--------|------|------------|
| `id` | INTEGER | Primary Key, Auto-increment |
| `goal_id` | INTEGER | **Foreign Key** → `goals.id`, CASCADE |
| `content` | TEXT | Not Null |
| `created_at` | INTEGER | Timestamp |

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
| `countActiveGoals(userId)` | Count in-progress goals |
| `countAchievedGoals(userId)` | Count completed goals |
| `getActiveGoals(userId)` | Fetch active goals list |
| `getAchievedGoals(userId)` | Fetch achieved goals list |
| **ReflectionDao** | |
| `countTotalReflections(userId)` | Count all user reflections |
| `getRecentReflections(userId)` | Fetch 3 most recent reflections |

### Architecture

```
Activities
    ├── UserRepository          ← Single source of truth (Auth + Profile updates)
    └── DashboardRepository     ← Dashboard & Profile stats
            └── UserDao / GoalDao / ReflectionDao  ← @Dao interfaces
                    └── AppDatabase  ← @Database singleton
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
│   │   ├── Reflection.java            ← @Entity — reflections table
│   │   ├── UserDao.java               ← @Dao — User SQL queries (incl. profile updates)
│   │   ├── GoalDao.java               ← @Dao — Goal SQL queries
│   │   ├── ReflectionDao.java         ← @Dao — Reflection SQL queries
│   │   ├── AppDatabase.java           ← @Database — singleton Room DB (v2)
│   │   ├── UserRepository.java        ← Repository — auth + profile operations
│   │   └── DashboardRepository.java   ← Repository — dashboard & profile stats
│   ├── models/
│   │   └── DashboardStats.java        ← Data model for dashboard stats
│   ├── SplashActivity.java            ← Splash screen — animated logo, smart routing
│   ├── GetStartedActivity.java        ← Onboarding — motivational first-launch screen
│   ├── LoginActivity.java             ← Login screen + session handling
│   ├── RegisterActivity.java          ← Registration + auto-login
│   ├── ForgotPasswordActivity.java    ← Two-step password recovery
│   ├── DashboardActivity.java         ← Main dashboard (EdgeToEdge, pinned nav)
│   ├── ProfileActivity.java           ← Profile screen (stats, settings, logout)
│   ├── EditProfileActivity.java       ← Edit name/username/email/password + avatar
│   ├── NotificationSettingsActivity.java ← Notification toggles UI
│   ├── PrivacyPolicyActivity.java     ← Static privacy policy screen
│   ├── AboutAppActivity.java          ← About app — info, mission, tech stack
│   ├── GoalsActivity.java             ← Goals screen (CRUD)
│   └── AchievedActivity.java          ← Achieved goals screen
│
├── res/
│   ├── layout/
│   │   ├── activity_splash.xml                 ← Splash UI (green gradient, logo, progress)
│   │   ├── activity_get_started.xml            ← Get Started UI (hero, quote card, features, CTA)
│   │   ├── login_activity.xml                  ← Login UI
│   │   ├── register_activity.xml               ← Register UI
│   │   ├── forgot_password_activity.xml        ← Forgot password UI
│   │   ├── dashboard_activity.xml              ← Dashboard UI (CoordinatorLayout + pinned panel)
│   │   ├── profile.xml                         ← Profile UI (hero header, stats, settings rows)
│   │   ├── activity_edit_profile.xml           ← Edit Profile UI (fields + avatar picker)
│   │   ├── activity_notification_settings.xml  ← Notification toggles UI
│   │   ├── activity_privacy_policy.xml         ← Privacy Policy static UI
│   │   ├── activity_about_app.xml              ← About App static UI
│   │   ├── bottom_sheet_photo_picker.xml       ← Camera / Gallery / Remove bottom sheet
│   │   └── item_reflection.xml                 ← Recent reflection list item
│   ├── menu/
│   │   ├── bottom_nav_menu.xml        ← 5-item bottom navigation menu
│   │   └── dashboard_menu.xml         ← Overflow menu (Logout)
│   ├── anim/
│   │   ├── splash_logo_bounce.xml     ← Logo scale + overshoot + fade animation
│   │   ├── splash_text_slide_up.xml   ← Text slide-up fade-in (delayed 900ms)
│   │   ├── splash_exit.xml            ← Scale-out fade for splash exit transition
│   │   ├── slide_in_right.xml         ← Slide-in from right (Get Started → Register/Login)
│   │   ├── fade_in.xml                ← Generic fade-in animation
│   │   └── scale_up.xml               ← Generic scale-up animation
│   ├── drawable/
│   │   ├── bg_splash.xml              ← Diagonal green gradient (Splash background)
│   │   ├── bg_splash_logo_circle.xml  ← White oval logo container
│   │   ├── bg_get_started.xml         ← Light green gradient (Get Started background)
│   │   ├── bg_get_started_hero.xml    ← Green gradient with rounded bottom corners
│   │   ├── bg_get_started_button.xml  ← Green gradient CTA button (filled)
│   │   ├── bg_sign_in_button.xml      ← Outlined green ghost button
│   │   ├── bg_feature_icon.xml        ← Green pastel oval for feature row icons
│   │   ├── ic_logo_journal.xml        ← App logo
│   │   ├── ic_target.xml              ← Goals icon
│   │   ├── ic_check_circle.xml        ← Achieved icon
│   │   ├── ic_file_text.xml           ← Reflections icon
│   │   └── ... (all other existing drawables)
│   └── values/
│       ├── colors.xml                 ← Brand colour palette
│       ├── strings.xml                ← All UI text strings (incl. splash & get started)
│       ├── themes.xml                 ← App theme (Material3 Light) + Splash theme
│       └── dimens.xml                 ← Dimensions
│
└── AndroidManifest.xml                ← SplashActivity as launcher; GetStartedActivity registered
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
DashboardActivity ←─────────────────────────────────── all bottom nav screens
```

---

## ⚙️ Setup & Installation

### Prerequisites
- Android Studio **Meerkat (2024.3.1)** or later
- JDK 11 or higher
- Android device / emulator running **API 24+**

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/<your-org>/Personal_Refelection.git
   ```

2. **Open in Android Studio**
   - File → Open → Select the cloned folder

3. **Sync Gradle**
   - Android Studio will auto-sync. If not, click **"Sync Now"** in the notification bar.

4. **Run the app**
   - Select a device / emulator
   - Click ▶ **Run** or press `Shift + F10`

5. **First use**
   - The **Splash Screen** appears for ~3 seconds with animated logo
   - The **Get Started** screen presents the app's key features
   - Tap **"Get Started — It's Free"** to register, or **"Already have an account? Sign In"** to log in

> **Note:** The database is created automatically on first launch. No manual setup required.

---

## 👥 Team Members

| Role | Name | Responsibilities |
|------|------|-----------------|
| 👑 **Team Lead & Main Developer** | **Dumindu Malinga** | Project architecture, Room database setup, authentication logic (Login, Register, Forgot Password), Splash & Get Started screens, activity development, GitHub management |
| 🎨 **UI Designer** | **Ishini Awanka** | Screen layouts (XML), colour palette, drawable resources, icon design, Material Design implementation, responsive UI |
| ⚙️ **Features & Animations** | **Theekshana Bandara** | Goal CRUD features, reflection journal, transition animations, splash animations, shake feedback, step indicator animations, input focus effects |

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
