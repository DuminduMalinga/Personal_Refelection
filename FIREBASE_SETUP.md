# Firebase Setup Guide — Google & Facebook Login

The app code is fully ready. You just need to connect it to your Firebase project.
Follow every step carefully.

---

## PART 1 — Create Firebase Project

1. Go to **[console.firebase.google.com](https://console.firebase.google.com)**
2. Click **"Add project"**
3. Name it: `Personal-Refelection`
4. Disable Google Analytics (optional) → **Create project**

---

## PART 2 — Add Android App to Firebase

1. In Firebase Console → click the **Android icon** (Add app)
2. Enter:
   - **Package name:** `com.example.personal_refelection`
   - **App nickname:** Personal Refelection
   - **SHA-1:** `BC:B4:93:A8:AD:E9:46:8D:F9:9B:DA:56:CF:DD:F5:74:6C:BD:E1:27`
3. Click **Register app**
4. **Download `google-services.json`**
5. **Replace** the file at:
   ```
   C:\Users\HashTag\Documents\GitHub\Personal_Refelection\app\google-services.json
   ```
   with the downloaded one (overwrite completely)

---

## PART 3 — Enable Google Sign-In in Firebase

1. Firebase Console → **Authentication** → **Sign-in method**
2. Click **Google** → Enable toggle → Set support email → **Save**

---

## PART 4 — Enable Facebook Login in Firebase

### 4A — Create Facebook App
1. Go to **[developers.facebook.com](https://developers.facebook.com)**
2. **My Apps → Create App → Consumer → Next**
3. Enter app name → **Create**
4. Dashboard → **Add Product → Facebook Login → Set Up → Android**

### 4B — Get Facebook App ID and Secret
- Dashboard top-left shows your **App ID** (e.g. `123456789012345`)
- **Settings → Basic → App Secret** (click Show)

### 4C — Enable Facebook in Firebase
1. Firebase Console → **Authentication → Sign-in method → Facebook**
2. Enable toggle
3. Paste **App ID** and **App Secret** from Facebook Dashboard
4. Copy the **OAuth redirect URI** shown by Firebase
   (looks like: `https://personal-refelection.firebaseapp.com/__/auth/handler`)
5. Click **Save**

### 4D — Add OAuth Redirect URI in Facebook
1. Facebook Dashboard → **Facebook Login → Settings**
2. Under **Valid OAuth Redirect URIs** → paste the URI from step 4C
3. Save Changes

### 4E — Update strings.xml with your Facebook App ID
Open `app/src/main/res/values/strings.xml` and replace:
```xml
<string name="facebook_app_id" translatable="false">YOUR_FACEBOOK_APP_ID</string>
<string name="facebook_client_token" translatable="false">YOUR_FACEBOOK_CLIENT_TOKEN</string>
<string name="fb_login_protocol_scheme" translatable="false">fbYOUR_FACEBOOK_APP_ID</string>
```
With your real values:
```xml
<string name="facebook_app_id" translatable="false">123456789012345</string>
<string name="facebook_client_token" translatable="false">abcdef1234567890abcdef1234567890</string>
<string name="fb_login_protocol_scheme" translatable="false">fb123456789012345</string>
```
> **Client Token** → Facebook Dashboard → **Settings → Advanced → Client Token**
> **fb_login_protocol_scheme** = `fb` + your App ID (no spaces)

### 4F — Add Key Hash to Facebook App
1. Facebook Dashboard → **Settings → Basic → Android section**
2. Add Key Hash: `vLSTqK3pRo35m9pWz931dGy94Sc=`
   (This is your debug key hash — already computed for you)
3. **Save Changes**

---

## PART 5 — Rebuild & Test

1. Replace `google-services.json` (from Part 2 Step 4)
2. Update `strings.xml` with Facebook values (from Part 4E)
3. In Android Studio → **Build → Clean Project**
4. **Run → Run 'app'** on your device

---

## How it works after setup

| Button | Flow |
|--------|------|
| **Continue with Google** | Google account picker → Firebase verifies ID token → creates/finds local Room DB user → goes to Dashboard |
| **Continue with Facebook** | Facebook login dialog → Firebase verifies access token → creates/finds local Room DB user → goes to Dashboard |
| **Returning user** | Firebase signs them in → local DB lookup by email → straight to Dashboard |
| **New social user** | Firebase signs them in → auto-registered in local Room DB → goes to Dashboard |

---

## Your Debug SHA-1 (already added to Firebase step)
```
BC:B4:93:A8:AD:E9:46:8D:F9:9B:DA:56:CF:DD:F5:74:6C:BD:E1:27
```

## Your Debug Key Hash (for Facebook)
```
vLSTqK3pRo35m9pWz931dGy94Sc=
```

---

## Troubleshooting

| Error | Fix |
|-------|-----|
| Google sign-in error code **10** | SHA-1 not added to Firebase. Re-check Part 2 Step 2 |
| Google sign-in error code **7** | No internet connection |
| Facebook login fails | Check App ID, Client Token, Key Hash are all correct |
| `google-services.json` errors | Make sure you replaced the placeholder file with the real downloaded one |
| App crashes on launch | `google-services.json` is invalid — re-download from Firebase Console |

