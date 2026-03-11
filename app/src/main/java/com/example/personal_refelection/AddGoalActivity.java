package com.example.personal_refelection;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Button;
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
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Full-screen Add / Edit Goal activity.
 * Pass EXTRA_GOAL_ID to open in edit mode.
 */
public class AddGoalActivity extends AppCompatActivity {

    public static final String EXTRA_GOAL_ID    = "goal_id";
    public static final String EXTRA_GOAL_TITLE = "goal_title";
    public static final String EXTRA_GOAL_DESC  = "goal_desc";
    public static final String EXTRA_GOAL_DATE  = "goal_date";

    // ── Views ──────────────────────────────────────────────────────────
    private TextInputLayout tilGoalTitle;
    private TextInputEditText etGoalTitle, etGoalDescription;
    private TextView tvTargetDate, tvHeaderSubtitle, tvHeaderLabel, tvHeaderTitle;
    private TextView chipLow, chipMedium, chipHigh;
    private LinearLayout btnPickDate;
    private Button btnSaveGoal;

    // ── State ──────────────────────────────────────────────────────────
    private String selectedDate = null;
    private String selectedPriority = "Medium";
    private boolean isEditMode = false;
    private int editGoalId = -1;

    // ── DB ─────────────────────────────────────────────────────────────
    private GoalDao goalDao;
    private int userId = -1;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ── Priority colours ───────────────────────────────────────────────
    private static final int COLOR_GREEN_TXT  = 0xFF06D6A0;
    private static final int COLOR_ORANGE_TXT = 0xFFFFB347;
    private static final int COLOR_RED_TXT    = 0xFFFF4757;

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

        userId  = getSharedPreferences("GoalReflectPrefs", MODE_PRIVATE).getInt("user_id", -1);
        goalDao = AppDatabase.getInstance(this).goalDao();

        // Check if edit mode
        if (getIntent().hasExtra(EXTRA_GOAL_ID)) {
            isEditMode = true;
            editGoalId = getIntent().getIntExtra(EXTRA_GOAL_ID, -1);
        }

        bindViews();
        setupHeaderMode();
        setupPriorityChips();
        setupDatePicker();
        setupSave();
        setupBack();

        // Pre-fill if editing
        if (isEditMode) {
            prefillFields();
        }
    }

    // ── Bind ───────────────────────────────────────────────────────────

    private void bindViews() {
        tilGoalTitle      = findViewById(R.id.tilGoalTitle);
        etGoalTitle       = findViewById(R.id.etGoalTitle);
        etGoalDescription = findViewById(R.id.etGoalDescription);
        tvTargetDate      = findViewById(R.id.tvTargetDate);
        btnPickDate       = findViewById(R.id.btnPickDate);
        chipLow           = findViewById(R.id.chipLow);
        chipMedium        = findViewById(R.id.chipMedium);
        chipHigh          = findViewById(R.id.chipHigh);
        btnSaveGoal       = findViewById(R.id.btnSaveGoal);
        tvHeaderLabel     = findViewById(R.id.tvHeaderLabel);
        tvHeaderTitle     = findViewById(R.id.tvHeaderTitle);
        tvHeaderSubtitle  = findViewById(R.id.tvHeaderSubtitle);
    }

    // ── Header mode ────────────────────────────────────────────────────

    private void setupHeaderMode() {
        if (isEditMode) {
            if (tvHeaderLabel    != null) tvHeaderLabel.setText("EDIT GOAL");
            if (tvHeaderTitle    != null) tvHeaderTitle.setText("Edit Goal");
            if (tvHeaderSubtitle != null) tvHeaderSubtitle.setText("Update your goal details ✏️");
            if (btnSaveGoal      != null) btnSaveGoal.setText("Update Goal ✅");
        }
    }

    // ── Pre-fill for edit ──────────────────────────────────────────────

    private void prefillFields() {
        String title = getIntent().getStringExtra(EXTRA_GOAL_TITLE);
        String desc  = getIntent().getStringExtra(EXTRA_GOAL_DESC);
        String date  = getIntent().getStringExtra(EXTRA_GOAL_DATE);

        if (title != null) etGoalTitle.setText(title);

        // Strip "Priority: X" suffix from description for display
        if (desc != null) {
            String displayDesc = desc;
            int priorityIdx = desc.lastIndexOf("\nPriority: ");
            if (priorityIdx >= 0) {
                String prio = desc.substring(priorityIdx + 11).trim();
                displayDesc = desc.substring(0, priorityIdx).trim();
                selectedPriority = prio;
            }
            etGoalDescription.setText(displayDesc);
        }

        if (date != null && !date.isEmpty()) {
            selectedDate = date;
            // Display date nicely
            try {
                String[] parts = date.split("-");
                tvTargetDate.setText(parts[2] + " / " + parts[1] + " / " + parts[0]);
                tvTargetDate.setTextColor(0xFF1A1A2E);
            } catch (Exception ignored) {}
        }

        selectPriority(selectedPriority);
    }

    // ── Priority chips ─────────────────────────────────────────────────

    private void setupPriorityChips() {
        selectPriority(selectedPriority);
        chipLow.setOnClickListener(v    -> selectPriority("Low"));
        chipMedium.setOnClickListener(v -> selectPriority("Medium"));
        chipHigh.setOnClickListener(v   -> selectPriority("High"));
    }

    private void selectPriority(String priority) {
        selectedPriority = priority;
        setChipState(chipLow,    priority.equals("Low"),    COLOR_GREEN_TXT);
        setChipState(chipMedium, priority.equals("Medium"), COLOR_ORANGE_TXT);
        setChipState(chipHigh,   priority.equals("High"),   COLOR_RED_TXT);
    }

    private void setChipState(TextView chip, boolean selected, int txtColor) {
        chip.setAlpha(selected ? 1.0f : 0.40f);
        chip.setTextColor(txtColor);
        chip.setScaleX(selected ? 1.06f : 1.0f);
        chip.setScaleY(selected ? 1.06f : 1.0f);
    }

    // ── Date Picker ────────────────────────────────────────────────────

    private void setupDatePicker() {
        btnPickDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
                        tvTargetDate.setText(String.format(Locale.US, "%02d / %02d / %04d", day, month + 1, year));
                        tvTargetDate.setTextColor(0xFF1A1A2E);
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH))
                    .show();
        });
    }

    // ── Save / Update ──────────────────────────────────────────────────

    private void setupSave() {
        btnSaveGoal.setOnClickListener(v -> saveGoal());
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

        // Append priority tag
        String fullDesc = desc + (desc.isEmpty() ? "" : "\n") + "Priority: " + selectedPriority;

        if (isEditMode) {
            // Update existing goal
            executor.execute(() -> {
                Goal goal = new Goal(userId, title, fullDesc, selectedDate);
                goal.id = editGoalId;
                goalDao.updateGoal(goal);
                mainHandler.post(() -> {
                    Toast.makeText(this, "Goal updated ✅", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finishWithAnimation();
                });
            });
        } else {
            // Insert new goal
            Goal newGoal = new Goal(userId, title, fullDesc, selectedDate);
            executor.execute(() -> {
                goalDao.insertGoal(newGoal);
                mainHandler.post(() -> {
                    Toast.makeText(this, "Goal saved! Keep going 🎯", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finishWithAnimation();
                });
            });
        }
    }

    // ── Back ───────────────────────────────────────────────────────────

    private void setupBack() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finishWithAnimation());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { finishWithAnimation(); }
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
