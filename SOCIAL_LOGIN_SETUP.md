# Social Login Setup Guide

Google Sign-In and Facebook Login are fully integrated into the app.  
You need to supply your own credentials before the buttons will work on a real device.

---

## 1. Google Sign-In Setup

### Step 1 — Create a Google Cloud Project
1. Go to [console.cloud.google.com](https://console.cloud.google.com)
2. Create a new project (or select an existing one)
3. Enable the **People API** (optional but recommended)

### Step 2 — Create an OAuth 2.0 Web Client ID
1. Go to **APIs & Services → Credentials**
2. Click **Create Credentials → OAuth client ID**
3. Choose **Web application**
4. Copy the generated **Client ID** (ends in `.apps.googleusercontent.com`)

### Step 3 — Create an Android OAuth Client ID
1. Click **Create Credentials → OAuth client ID** again
2. Choose **Android**
3. Enter package name: `com.example.personal_refelection`
4. Get your **SHA-1** fingerprint:
   ```
   cd C:\Users\HashTag\.android
   keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
5. Paste the SHA-1 and save

### Step 4 — Update `strings.xml`
Open `app/src/main/res/values/strings.xml` and replace:
```xml
<string name="lbl_google_web_client_id" translatable="false">YOUR_WEB_CLIENT_ID.apps.googleusercontent.com</string>
```
With your actual Web Client ID:
```xml
<string name="lbl_google_web_client_id" translatable="false">123456789-abcdefgh.apps.googleusercontent.com</string>
```

---

## 2. Facebook Login Setup

### Step 1 — Create a Facebook App
1. Go to [developers.facebook.com](https://developers.facebook.com)
2. Click **My Apps → Create App**
3. Choose **Consumer** type
4. Give it a name and create

### Step 2 — Add Facebook Login Product
1. On the app dashboard, click **Add Product → Facebook Login → Set Up**
2. Choose **Android**

### Step 3 — Add your Android Key Hash
Run this command to get your debug key hash:
```
keytool -exportcert -alias androiddebugkey -keystore %HOMEPATH%\.android\debug.keystore | openssl sha1 -binary | openssl base64
```
> Password is: `android`

Paste the result in **Facebook Developer Dashboard → Settings → Basic → Android → Key Hashes**

### Step 4 — Get your App ID and Client Token
- **App ID**: Found on the dashboard at the top (e.g., `123456789012345`)
- **Client Token**: Go to **Settings → Advanced → Client Token**

### Step 5 — Update `strings.xml`
Replace the placeholder values:
```xml
<string name="facebook_app_id" translatable="false">123456789012345</string>
<string name="facebook_client_token" translatable="false">abcdef1234567890abcdef1234567890</string>
<string name="fb_login_protocol_scheme" translatable="false">fb123456789012345</string>
```

> ⚠️ `fb_login_protocol_scheme` = `fb` + your App ID (all lowercase, no spaces)

---

## How it works

| Action | Flow |
|--------|------|
| **Sign In with Google** | Google account picker → gets name + email → finds or creates Room DB user → saves session |
| **Sign In with Facebook** | Facebook dialog → graph API fetch name + email → finds or creates Room DB user → saves session |
| **Returning user** | If email already in DB → logs them in directly |
| **New social user** | Auto-registered with `google_oauth` or `facebook_oauth` as password token |
| **Username** | Auto-generated from email local part (e.g. `john.doe@gmail.com` → `johndoe`) |

All data stays **100% local** — no backend, no Firebase needed.

