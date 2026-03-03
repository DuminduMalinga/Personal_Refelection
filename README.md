# 🌿 GoalReflect — Personal Goal Reflection App

> **Module:** ICT3214 — Mobile Application Development
> **Project Idea #8:** Personal Goal Reflection App
<<<<<<< Updated upstream

=======
>>>>>>> Stashed changes

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

### 🔐 Authentication
- **User Registration** — Full Name, Email, Username, Password with real-time validation
- **User Login** — Email + Password authenticated against the Room database
- **Forgot Password** — Two-step identity verification (Username + Email match) then password reset — no email required
- **Duplicate detection** — Email and username uniqueness enforced at the database level
- **Session-aware** — Logged-in users only see their own data

### 🎯 Goal Management *(Feature-Specific)*
- Create personal goals with title, description, and target date
- Mark goals as In Progress / Completed
- Edit and delete existing goals
- Full CRUD operations per user

### 📓 Reflection Journal *(Feature-Specific)*
- Write daily reflections linked to specific goals
- View reflection history per goal
- Edit and delete reflections

### 🎨 UI & Animations
- Smooth screen transitions and card animations
- Two-step forgot password flow with slide-up card transition
- Shake animation feedback on incorrect identity verification
- Focus-highlight effect on all input fields (green border on focus)
- Step indicator with animated dot progression

### 🛡️ Security
- Passwords are **never stored in plain text**
- Input validation on all forms (client-side)
- Unique constraints enforced at database level (email, username)

---

## 📱 Screens & UI

<<<<<<< Updated upstream
| Screen | Description |
|--------|-------------|
| **Login** | Email + Password login with "Forgot password?" and "Register" links |
| **Register** | Full Name, Email, Username, Password, Confirm Password with duplicate detection |
| **Forgot Password** | Step 1: Verify Username + Email → Step 2: Set new password |
| **Home / Dashboard** | *(Coming Soon)* Personal goal overview |
| **Goals** | *(Coming Soon)* CRUD for personal goals |
| **Reflection Journal** | *(Coming Soon)* Per-goal daily reflections |
=======
| Screen | Description | Status         |
|--------|-------------|----------------|
| **Login** | Email + Password login with "Forgot password?" and "Register" links | ✅ Complete     |
| **Register** | Full Name, Email, Username, Password, Confirm Password with duplicate detection | ✅ Complete     |
| **Forgot Password** | Step 1: Verify Username + Email → Step 2: Set new password | ✅ Complete     |
| **Dashboard** | Personal goal overview with stats, recent reflections, and quick actions | ✅ **Complete** |
| **Goals** | CRUD for personal goals | 🚧 Coming Soon |
| **Achieved** | View completed goals | 🚧 Coming Soon |
| **Profile** | User settings and information | 🚧 Coming Soon |

### Dashboard Features (NEW! 🎉)
- **Dynamic Greeting**: Time-based greeting (Morning/Afternoon/Evening) with user's name
- **Overview Cards**: Three minimal stat cards showing Active Goals, Achieved Goals, and Total Reflections
- **Recent Activity**: Display of 3 most recent reflections with timestamps
- **Quick Actions**: Buttons to navigate to Goals and Achieved screens
- **Floating Action Button**: Quick access to add new goals
- **Bottom Navigation**: Easy navigation between Dashboard, Goals, Achieved, and Profile
- **Session Management**: Auto-login on app restart, logout functionality
- **Smooth Animations**: Staggered card entrance animations
>>>>>>> Stashed changes

### Design Style
- **Background:** Soft neutral `#F2F4F7`
- **Primary colour:** Calming green `#2DC08E`
- **Cards:** White with `28dp` corner radius and `12dp` elevation
- **Typography:** `sans-serif-medium`, clean and readable
- **Icons:** Custom vector drawables (envelope, padlock, person, journal)

---

## 🛠️ Technologies Used

| Technology | Version | Purpose |
|-----------|---------|---------|
| **Java** | 11 | Primary programming language |
| **Android SDK** | API 36 (Android 16) | Target platform |
| **Min SDK** | API 24 (Android 7.0) | Minimum supported device |
| **Room Database** | 2.6.1 | Local SQLite ORM (Entity, DAO, Database) |
| **AndroidX AppCompat** | 1.7.1 | Backwards-compatible Activity/Fragment support |
| **Material Components** | 1.13.0 | Material Design UI widgets |
| **ConstraintLayout** | 2.2.1 | Flexible, flat UI layouts |
| **AndroidX Activity** | 1.12.4 | Edge-to-edge window support |
| **Gradle** | 9.0.1 | Build system |
| **Android Gradle Plugin** | 9.0.1 | Android build toolchain |

---

## 🗄️ Database Design

### Room Database: `goalreflect_db`

#### Table 1: `users`

| Column | Type | Constraint |
|--------|------|------------|
| `id` | INTEGER | Primary Key, Auto-increment |
| `full_name` | TEXT | Not Null |
| `email` | TEXT | **UNIQUE**, Not Null |
| `username` | TEXT | **UNIQUE**, Not Null |
| `password` | TEXT | Not Null |

#### Table 2: `goals` *(Feature-Specific)*

| Column | Type | Constraint |
|--------|------|------------|
| `id` | INTEGER | Primary Key, Auto-increment |
| `user_id` | INTEGER | **Foreign Key** → `users.id` |
| `title` | TEXT | Not Null |
| `description` | TEXT | — |
| `target_date` | TEXT | — |
| `status` | TEXT | `IN_PROGRESS` / `COMPLETED` |
| `created_at` | TEXT | Not Null |

#### Table 3: `reflections` *(Feature-Specific)*

| Column | Type | Constraint |
|--------|------|------------|
| `id` | INTEGER | Primary Key, Auto-increment |
| `goal_id` | INTEGER | **Foreign Key** → `goals.id` |
| `user_id` | INTEGER | **Foreign Key** → `users.id` |
| `content` | TEXT | Not Null |
| `reflection_date` | TEXT | Not Null |

### DAO Operations

| DAO Method | Purpose |
|------------|---------|
| `insertUser(user)` | Register new user |
| `loginWithEmail(email, password)` | Authenticate login |
| `findByUsernameAndEmail(username, email)` | Forgot password step 1 |
| `updatePassword(email, newPassword)` | Forgot password step 2 |
| `countByEmail(email)` | Duplicate email check |
| `countByUsername(username)` | Duplicate username check |

### Architecture

```
Activities
    └── UserRepository          ← Single source of truth
            └── UserDao         ← @Dao interface (SQL queries)
                    └── AppDatabase  ← @Database singleton
                            └── Room (SQLite)
```

All database queries are executed on a **background thread** via `ExecutorService` and results are posted back to the **main thread** via `Handler(Looper.getMainLooper())`.

---

## 📁 Project Structure

```
app/src/main/
├── java/com/example/personal_refelection/
│   ├── database/
│   │   ├── User.java                  ← @Entity — users table
│   │   ├── UserDao.java               ← @Dao — SQL queries
│   │   ├── AppDatabase.java           ← @Database — singleton Room DB
│   │   └── UserRepository.java        ← Repository — background thread ops
│   ├── LoginActivity.java             ← Login screen
│   ├── RegisterActivity.java          ← Registration screen
│   └── ForgotPasswordActivity.java    ← Two-step password recovery
│
├── res/
│   ├── layout/
│   │   ├── login_activity.xml         ← Login UI
│   │   ├── register_activity.xml      ← Register UI
│   │   └── forgot_password_activity.xml ← Forgot password UI
│   ├── drawable/
│   │   ├── ic_logo_journal.xml        ← App logo (open journal)
│   │   ├── ic_email.xml               ← Email field icon
│   │   ├── ic_lock.xml                ← Password field icon
│   │   ├── ic_person.xml              ← Name/username field icon
│   │   ├── ic_shield_check.xml        ← Identity verified icon
│   │   ├── bg_logo_container.xml      ← Green rounded logo background
│   │   ├── bg_card.xml                ← White card background
│   │   ├── bg_input_field.xml         ← Normal input background
│   │   ├── bg_input_field_focused.xml ← Focused input (green border)
│   │   ├── selector_input_field.xml   ← Input focus state selector
│   │   ├── selector_login_button.xml  ← Login button state selector
│   │   ├── selector_register_button.xml ← Register button state selector
│   │   ├── selector_reset_button.xml  ← Reset button state selector
│   │   ├── bg_step_dot_active.xml     ← Active step indicator dot
│   │   └── bg_step_dot_inactive.xml   ← Inactive step indicator dot
│   └── values/
│       ├── colors.xml                 ← Brand colour palette
│       ├── strings.xml                ← All UI text strings
│       ├── themes.xml                 ← App theme (Material3 Light)
│       └── dimens.xml                 ← Dimensions
│
└── AndroidManifest.xml
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
   - Tap **"Register"** to create an account
   - Log in with your registered email and password

> **Note:** The database is created automatically on first launch. No manual setup required.

---

## 👥 Team Members

| Role | Name | Responsibilities |
|------|------|-----------------|
| 👑 **Team Lead & Main Developer** | **Dumindu Malinga** | Project architecture, Room database setup, authentication logic (Login, Register, Forgot Password), activity development, GitHub management |
| 🎨 **UI Designer** | **Ishini Awanka** | Screen layouts (XML), colour palette, drawable resources, icon design, Material Design implementation, responsive UI |
| ⚙️ **Features & Animations** | **Theekshana Bandara** | Goal CRUD features, reflection journal, transition animations, shake feedback, step indicator animations, input focus effects |

### Module Details
- **Module:** ICT3214 — Mobile Application Development
- **Project:** Personal Goal Reflection App (Idea #8)
- **Academic Year:** 2025/2026

---

## 📋 GitHub Contribution Guidelines

Each team member must follow these rules for commits:

### ✅ Good commit messages
```
Add Room database entity and DAO for users table
Implement login validation with Room DB query
Design register screen layout with Material components
Add shake animation for forgot password mismatch
Fix duplicate email detection in UserRepository
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

