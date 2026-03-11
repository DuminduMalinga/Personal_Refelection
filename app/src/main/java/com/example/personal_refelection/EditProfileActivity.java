package com.example.personal_refelection;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import androidx.core.content.FileProvider;

import com.example.personal_refelection.database.UserRepository;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Edit Profile screen — update name, username, password, and profile picture.
 * Supports camera capture and gallery selection with runtime permissions.
 */
public class EditProfileActivity extends AppCompatActivity {

    private static final String PREF_AVATAR_PATH = "avatar_path";

    private TextView tvEditDisplayName, tvEditDisplayEmail, btnSave;
    private EditText etFullName, etUsername, etEmail, etNewPassword, etConfirmPassword;
    private ImageView ivEditAvatar;

    private UserRepository userRepository;
    private SharedPreferences sharedPreferences;
    private String userEmail;

    // URI of the temp file created for camera capture
    private Uri cameraImageUri;

    // ── Activity Result Launchers ─────────────────────────────────

    /** Camera: take a photo and store it to cameraImageUri */
    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraImageUri != null) {
                    saveAndDisplayAvatar(cameraImageUri);
                }
            });

    /** Gallery: pick an image */
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    saveAndDisplayAvatar(uri);
                }
            });

    /** Camera permission request */
    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    launchCamera();
                } else {
                    showPermissionDeniedDialog(getString(R.string.lbl_camera_permission_denied));
                }
            });

    /** Storage / media permission request */
    private final ActivityResultLauncher<String> storagePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    pickImageLauncher.launch("image/*");
                } else {
                    showPermissionDeniedDialog(getString(R.string.lbl_storage_permission_denied));
                }
            });

    // ── Lifecycle ─────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        userRepository    = new UserRepository(this);
        sharedPreferences = getSharedPreferences("GoalReflectPrefs", MODE_PRIVATE);
        userEmail         = sharedPreferences.getString("user_email", "");

        if (TextUtils.isEmpty(userEmail)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        bindViews();
        loadCurrentData();
        restoreSavedAvatar();
        setupListeners();
    }

    // ── View Binding ──────────────────────────────────────────────

    private void bindViews() {
        tvEditDisplayName  = findViewById(R.id.tvEditDisplayName);
        tvEditDisplayEmail = findViewById(R.id.tvEditDisplayEmail);
        btnSave            = findViewById(R.id.btnSave);
        etFullName         = findViewById(R.id.etFullName);
        etUsername         = findViewById(R.id.etUsername);
        etEmail            = findViewById(R.id.etEmail);
        etNewPassword      = findViewById(R.id.etNewPassword);
        etConfirmPassword  = findViewById(R.id.etConfirmPassword);
        ivEditAvatar       = findViewById(R.id.ivEditAvatar);
    }

    // ── Restore saved avatar if any ───────────────────────────────

    private void restoreSavedAvatar() {
        // 1 — Local file saved by user
        String savedPath = sharedPreferences.getString(PREF_AVATAR_PATH, null);
        if (savedPath != null) {
            File f = new File(savedPath);
            if (f.exists()) {
                setAvatarFromFile(f);
                return;
            }
        }

        // 2 — Google / Facebook social photo URL
        String socialPhotoUrl = sharedPreferences.getString("social_photo_url", null);
        if (socialPhotoUrl == null || socialPhotoUrl.isEmpty()) {
            com.google.firebase.auth.FirebaseUser fbUser =
                    FirebaseAuth.getInstance().getCurrentUser();
            if (fbUser != null && fbUser.getPhotoUrl() != null) {
                socialPhotoUrl = fbUser.getPhotoUrl().toString();
            }
        }

        if (socialPhotoUrl != null && !socialPhotoUrl.isEmpty()) {
            ivEditAvatar.setPadding(0, 0, 0, 0);
            ivEditAvatar.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_avatar_circle));
            ivEditAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(this)
                    .load(socialPhotoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(ivEditAvatar);
        }
    }

    // ── Load current user data ────────────────────────────────────

    private void loadCurrentData() {
        userRepository.getUserByEmail(userEmail, user -> {
            if (user != null) {
                tvEditDisplayName.setText(user.fullName);
                tvEditDisplayEmail.setText(user.email);
                etFullName.setText(user.fullName);
                etUsername.setText(user.username);
                etEmail.setText(user.email);
            }
        });
    }

    // ── Click Listeners ───────────────────────────────────────────

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> handleSave());
        findViewById(R.id.btnSaveProfile).setOnClickListener(v -> handleSave());
        // Both the avatar image and its container open the photo picker
        findViewById(R.id.avatarContainer).setOnClickListener(v -> showPhotoPicker());
    }

    // ── Photo Picker Bottom Sheet ─────────────────────────────────

    private void showPhotoPicker() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheet = LayoutInflater.from(this)
                .inflate(R.layout.bottom_sheet_photo_picker, null, false);
        dialog.setContentView(sheet);

        sheet.findViewById(R.id.optionCamera).setOnClickListener(v -> {
            dialog.dismiss();
            requestCameraAndLaunch();
        });

        sheet.findViewById(R.id.optionGallery).setOnClickListener(v -> {
            dialog.dismiss();
            requestGalleryAndLaunch();
        });

        sheet.findViewById(R.id.optionRemove).setOnClickListener(v -> {
            dialog.dismiss();
            removeAvatar();
        });

        dialog.show();
    }

    // ── Camera Flow ───────────────────────────────────────────────

    private void requestCameraAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        try {
            File imageFile = createTempImageFile();
            cameraImageUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    imageFile);
            takePictureLauncher.launch(cameraImageUri);
        } catch (IOException e) {
            Toast.makeText(this, "Could not create image file", Toast.LENGTH_SHORT).show();
        }
    }

    private File createTempImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName  = "AVATAR_" + timeStamp + ".jpg";
        File cacheDir = new File(getCacheDir(), "images");
        //noinspection ResultOfMethodCallIgnored
        cacheDir.mkdirs();
        return new File(cacheDir, fileName);
    }

    // ── Gallery Flow ──────────────────────────────────────────────

    private void requestGalleryAndLaunch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+: check both full and partial media access
            boolean fullAccess = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
            boolean partialAccess = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED;
            if (fullAccess || partialAccess) {
                pickImageLauncher.launch("image/*");
            } else {
                storagePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                pickImageLauncher.launch("image/*");
            } else {
                storagePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            // Android 12 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                pickImageLauncher.launch("image/*");
            } else {
                storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    // ── Save & Display Avatar ─────────────────────────────────────

    /**
     * Copy the selected URI into app-private storage, save the path,
     * and display it as a circular avatar.
     */
    private void saveAndDisplayAvatar(Uri sourceUri) {
        try {
            // Decode and scale down to 256×256 to keep storage small
            InputStream inputStream = getContentResolver().openInputStream(sourceUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();

            if (bitmap == null) {
                Toast.makeText(this, "Could not load image", Toast.LENGTH_SHORT).show();
                return;
            }

            // Scale to square 256dp
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 256, 256, true);
            bitmap.recycle();

            // Save to app-private files dir
            File destDir = new File(getFilesDir(), "avatars");
            //noinspection ResultOfMethodCallIgnored
            destDir.mkdirs();
            File destFile = new File(destDir, "avatar_" + userEmail.hashCode() + ".jpg");
            FileOutputStream out = new FileOutputStream(destFile);
            scaled.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.close();

            // Persist path
            sharedPreferences.edit().putString(PREF_AVATAR_PATH, destFile.getAbsolutePath()).apply();

            // Display
            setAvatarFromFile(destFile);
            Toast.makeText(this, getString(R.string.lbl_photo_updated), Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

    private void setAvatarFromFile(File file) {
        Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (bmp != null) {
            // Remove the default icon padding, show photo full-bleed in circle
            ivEditAvatar.setPadding(0, 0, 0, 0);
            ivEditAvatar.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_avatar_circle));
            ivEditAvatar.setImageBitmap(bmp);
            ivEditAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    private void removeAvatar() {
        String savedPath = sharedPreferences.getString(PREF_AVATAR_PATH, null);
        if (savedPath != null) {
            //noinspection ResultOfMethodCallIgnored
            new File(savedPath).delete();
            sharedPreferences.edit().remove(PREF_AVATAR_PATH).apply();
        }
        // Restore default placeholder
        ivEditAvatar.setImageResource(R.drawable.ic_person);
        ivEditAvatar.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_profile_avatar));
        int pad20dp = Math.round(20 * getResources().getDisplayMetrics().density);
        ivEditAvatar.setPadding(pad20dp, pad20dp, pad20dp, pad20dp);
        ivEditAvatar.setScaleType(ImageView.ScaleType.CENTER);
        Toast.makeText(this, getString(R.string.lbl_photo_removed), Toast.LENGTH_SHORT).show();
    }


    // ── Permission Denied Dialog ──────────────────────────────────

    private void showPermissionDeniedDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage(message)
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton(getString(R.string.lbl_permission_settings), (d, w) -> {
                    d.dismiss();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", getPackageName(), null));
                    startActivity(intent);
                })
                .show();
    }

    // ── Save Profile ──────────────────────────────────────────────

    private void handleSave() {
        String newFullName     = etFullName.getText().toString().trim();
        String newUsername     = etUsername.getText().toString().trim();
        String newPassword     = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(newFullName)) {
            etFullName.setError(getString(R.string.profile_name_empty));
            etFullName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(newUsername)) {
            etUsername.setError(getString(R.string.hint_username));
            etUsername.requestFocus();
            return;
        }
        if (!TextUtils.isEmpty(newPassword) || !TextUtils.isEmpty(confirmPassword)) {
            if (newPassword.length() < 6) {
                etNewPassword.setError(getString(R.string.profile_password_short));
                etNewPassword.requestFocus();
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                etConfirmPassword.setError(getString(R.string.profile_password_mismatch));
                etConfirmPassword.requestFocus();
                return;
            }
        }

        userRepository.updateFullName(userEmail, newFullName, r1 ->
            userRepository.updateUsername(userEmail, newUsername, r2 -> {
                sharedPreferences.edit().putString("user_name", newFullName).apply();
                // Update header to reflect the newly saved name
                tvEditDisplayName.setText(newFullName);
                if (!TextUtils.isEmpty(newPassword)) {
                    userRepository.updatePassword(userEmail, newPassword, r3 -> {
                        Toast.makeText(this, getString(R.string.profile_updated), Toast.LENGTH_SHORT).show();
                        finish();
                    });
                } else {
                    Toast.makeText(this, getString(R.string.profile_updated), Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
        );
    }
}

