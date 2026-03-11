package com.example.personal_refelection;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.personal_refelection.database.AppDatabase;
import com.example.personal_refelection.database.Goal;
import com.example.personal_refelection.database.GoalDao;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Full-screen Add Goal activity.
 * Supports title, description, target date, and priority level.
 */
public class AddGoalActivity extends AppCompatActivity {

    // ── Views ──────────────────────────────────────────────────────────
    private TextInputLayout tilGoalTitle;
    private TextInputEditText etGoalTitle, etGoalDescription;
    private TextView tvTargetDate;
    private TextView chipLow, chipMedium, chipHigh;
    private LinearLayout btnPickDate;

    // ── State ──────────────────────────────────────────────────────────
    private String selectedDate = null;
    private String selectedPriority = "Medium"; // default

    // ── DB ─────────────────────────────────────────────────────────────
    private GoalDao goalDao;
    private int userId = -1;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ── Priority colours ───────────────────────────────────────────────
    private static final int COLOR_GREEN_BG  = 0xFFE8FFF5;
    private static final int COLOR_ORANGE_BG = 0xFFFFF7ED;
    private static final int COLOR_RED_BG    = 0xFFFFF0F0;
    private static final int COLOR_GREEN_TXT = 0xFF06D6A0;
    private static final int COLOR_ORANGE_TXT= 0xFFFFB347;
    private static final int COLOR_RED_TXT   = 0xFFFF4757;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_goal);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addGoalRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        userId = getSharedPreferences("GoalReflectPrefs", MODE_PRIVATE).getInt("user_id", -1);
        goalDao = AppDatabase.getInstance(this).goalDao();

        bindViews();
        setupPriorityChips();
        setupDatePicker();
        setupSave();
        setupBack();
    }

    // ── Bind ───────────────────────────────────────────────────────────

    private void bindViews() {
        tilGoalTitle       = findViewById(R.id.tilGoalTitle);
        etGoalTitle        = findViewById(R.id.etGoalTitle);
        etGoalDescription  = findViewById(R.id.etGoalDescription);
        tvTargetDate       = findViewById(R.id.tvTargetDate);
        btnPickDate        = findViewById(R.id.btnPickDate);
        chipLow            = findViewById(R.id.chipLow);
        chipMedium         = findViewById(R.id.chipMedium);
        chipHigh           = findViewById(R.id.chipHigh);
    }

    // ── Priority chips ─────────────────────────────────────────────────

    private void setupPriorityChips() {
        selectPriority("Medium"); // default selection
        chipLow.setOnClickListener(v    -> selectPriority("Low"));
        chipMedium.setOnClickListener(v -> selectPriority("Medium"));
        chipHigh.setOnClickListener(v   -> selectPriority("High"));
    }

    private void selectPriority(String priority) {
        selectedPriority = priority;

        // Reset all to dim state
        setChipState(chipLow,    false, COLOR_GREEN_BG,  COLOR_GREEN_TXT);
        setChipState(chipMedium, false, COLOR_ORANGE_BG, COLOR_ORANGE_TXT);
        setChipState(chipHigh,   false, COLOR_RED_BG,    COLOR_RED_TXT);

        // Highlight selected
        switch (priority) {
            case "Low":
                setChipState(chipLow, true, COLOR_GREEN_BG, COLOR_GREEN_TXT);
                break;
            case "High":
                setChipState(chipHigh, true, COLOR_RED_BG, COLOR_RED_TXT);
                break;
            default:
                setChipState(chipMedium, true, COLOR_ORANGE_BG, COLOR_ORANGE_TXT);
                break;
        }
    }

    private void setChipState(TextView chip, boolean selected, int bgColor, int txtColor) {
        chip.setAlpha(selected ? 1.0f : 0.45f);
        chip.setTextColor(txtColor);
        if (selected) {
            chip.setScaleX(1.05f);
            chip.setScaleY(1.05f);
        } else {
            chip.setScaleX(1.0f);
            chip.setScaleY(1.0f);
        }
    }

    // ── Date Picker ────────────────────────────────────────────────────

    private void setupDatePicker() {
        btnPickDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        selectedDate = year + "-" + String.format("%02d", month + 1) + "-" + String.format("%02d", day);
                        tvTargetDate.setText(day + " / " + (month + 1) + " / " + year);
                        tvTargetDate.setTextColor(0xFF1A1A2E);
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH))
                    .show();
        });
    }

    // ── Save ───────────────────────────────────────────────────────────

    private void setupSave() {
        findViewById(R.id.btnSaveGoal).setOnClickListener(v -> saveGoal());
    }

    private void saveGoal() {
        String title = etGoalTitle.getText() != null ? etGoalTitle.getText().toString().trim() : "";
        String desc  = etGoalDescription.getText() != null ? etGoalDescription.getText().toString().trim() : "";

        if (TextUtils.isEmpty(title)) {
            tilGoalTitle.setError("Please enter a goal title");
            etGoalTitle.requestFocus();
            return;
        }
        tilGoalTitle.setError(null);

        // Append priority to description if provided
        String fullDesc = desc.isEmpty() ? "" : desc;
        if (!selectedPriority.isEmpty()) {
            fullDesc = (fullDesc.isEmpty() ? "" : fullDesc + "\n") + "Priority: " + selectedPriority;
        }

        Goal newGoal = new Goal(userId, title, fullDesc, selectedDate);

        executor.execute(() -> {
            goalDao.insertGoal(newGoal);
            mainHandler.post(() -> {
                Toast.makeText(this, "Goal saved! Keep going 🎯", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
                overridePendingTransition(0, android.R.anim.fade_out);
            });
        });
    }

    // ── Back ───────────────────────────────────────────────────────────

    private void setupBack() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finishWithAnimation());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishWithAnimation();
            }
        });
    }

    private void finishWithAnimation() {
        finish();
        overridePendingTransition(0, android.R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}

