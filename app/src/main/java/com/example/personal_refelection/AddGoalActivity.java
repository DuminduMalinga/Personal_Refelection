package com.example.personal_refelection;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.personal_refelection.database.AppDatabase;
import com.example.personal_refelection.database.Goal;
import com.example.personal_refelection.database.GoalDao;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddGoalActivity extends BaseActivity {

    public static final String EXTRA_GOAL_ID    = "goal_id";
    public static final String EXTRA_GOAL_TITLE = "goal_title";
    public static final String EXTRA_GOAL_DESC  = "goal_desc";
    public static final String EXTRA_GOAL_DATE  = "goal_date";

    // ── Views ──────────────────────────────────────────────────────────
    private TextInputLayout    tilGoalTitle;
    private TextInputEditText  etGoalTitle, etGoalDescription;
    private TextView           tvHeaderLabel, tvHeaderTitle, tvHeaderSubtitle;
    private TextView           tvProgressValue;
    private Button             chipLow, chipMedium, chipHigh;
    private TextView           chipCatHealth, chipCatStudy, chipCatCareer,
                               chipCatPersonal, chipCatFinance, chipCatOther;
    private Button             btnPickDate, btnSaveGoal;
    private SeekBar            seekBarProgress;
    private SwitchMaterial     switchReminder;

    // ── State ──────────────────────────────────────────────────────────
    private String  selectedDate     = null;
    private String  selectedPriority = "Medium";
    private String  selectedCategory = "";
    private int     progressTarget   = 50;
    private boolean reminderEnabled  = false;
    private boolean isEditMode       = false;
    private int     editGoalId       = -1;

    // ── DB ─────────────────────────────────────────────────────────────
    private GoalDao       goalDao;
    private int           userId = -1;
    private final ExecutorService executor    = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

    // ── Colours ────────────────────────────────────────────────────────
    private static final int COLOR_GREEN  = 0xFF06D6A0;
    private static final int COLOR_ORANGE = 0xFFFFB347;
    private static final int COLOR_RED    = 0xFFFF4757;
    private static final int COLOR_PURPLE = 0xFF6C63FF;
    private static final int COLOR_GREY   = 0xFF6B7280;
    private static final int COLOR_WHITE  = 0xFFFFFFFF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_goal);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addGoalRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, 0, bars.right, 0);
            View topNav = v.findViewById(R.id.topNavInclude);
            if (topNav != null) topNav.setPadding(
                    topNav.getPaddingLeft(), bars.top + 8,
                    topNav.getPaddingRight(), topNav.getPaddingBottom());
            return insets;
        });

        userId  = getSharedPreferences("GoalReflectPrefs", MODE_PRIVATE).getInt("user_id", -1);
        goalDao = AppDatabase.getInstance(this).goalDao();

        if (getIntent().hasExtra(EXTRA_GOAL_ID)) {
            isEditMode = true;
            editGoalId = getIntent().getIntExtra(EXTRA_GOAL_ID, -1);
        }

        bindViews();
        setupTopNav("MY GOALS");
        setupBottomNav(-1);
        setupHeaderMode();
        setupCategoryChips();
        setupPriorityChips();
        setupProgressSeekBar();
        setupDatePicker();
        setupReminder();
        setupSave();

        if (isEditMode) prefillFields();
    }

    // ── Bind ───────────────────────────────────────────────────────────
    private void bindViews() {
        tilGoalTitle      = findViewById(R.id.tilGoalTitle);
        etGoalTitle       = findViewById(R.id.etGoalTitle);
        etGoalDescription = findViewById(R.id.etGoalDescription);
        tvHeaderLabel     = findViewById(R.id.tvHeaderLabel);
        tvHeaderTitle     = findViewById(R.id.tvHeaderTitle);
        tvHeaderSubtitle  = findViewById(R.id.tvHeaderSubtitle);
        tvProgressValue   = findViewById(R.id.tvProgressValue);
        seekBarProgress   = findViewById(R.id.seekBarProgress);
        switchReminder    = findViewById(R.id.switchReminder);
        btnPickDate       = findViewById(R.id.btnPickDate);
        btnSaveGoal       = findViewById(R.id.btnSaveGoal);
        chipLow           = findViewById(R.id.chipLow);
        chipMedium        = findViewById(R.id.chipMedium);
        chipHigh          = findViewById(R.id.chipHigh);
        chipCatHealth     = findViewById(R.id.chipCatHealth);
        chipCatStudy      = findViewById(R.id.chipCatStudy);
        chipCatCareer     = findViewById(R.id.chipCatCareer);
        chipCatPersonal   = findViewById(R.id.chipCatPersonal);
        chipCatFinance    = findViewById(R.id.chipCatFinance);
        chipCatOther      = findViewById(R.id.chipCatOther);    }

    // ── Header ─────────────────────────────────────────────────────────
    private void setupHeaderMode() {
        if (isEditMode) {
            if (tvHeaderLabel   != null) tvHeaderLabel.setText("EDIT GOAL");
            if (tvHeaderTitle   != null) tvHeaderTitle.setText("Edit Goal");
            if (tvHeaderSubtitle!= null) tvHeaderSubtitle.setText("Update your goal details ✏️");
            if (btnSaveGoal     != null) btnSaveGoal.setText("Update Goal ");
        }
    }

    // ── Category Chips ─────────────────────────────────────────────────
    private void setupCategoryChips() {
        TextView[] cats = {chipCatHealth, chipCatStudy, chipCatCareer,
                           chipCatPersonal, chipCatFinance, chipCatOther};
        String[] labels = {"Health","Study","Career","Personal","Finance","Other"};
        for (int i = 0; i < cats.length; i++) {
            final String cat = labels[i];
            cats[i].setOnClickListener(v -> selectCategory(cat));
        }
    }

    private void selectCategory(String cat) {
        selectedCategory = cat;
        TextView[] chips = {chipCatHealth, chipCatStudy, chipCatCareer,
                            chipCatPersonal, chipCatFinance, chipCatOther};
        String[] labels  = {"Health","Study","Career","Personal","Finance","Other"};
        for (int i = 0; i < chips.length; i++) {
            boolean sel = labels[i].equals(cat);
            chips[i].setBackgroundResource(sel
                    ? R.drawable.bg_category_chip_selected
                    : R.drawable.bg_category_chip_unselected);
            chips[i].setTextColor(sel ? COLOR_WHITE : COLOR_PURPLE);
        }
    }

    // ── Priority Chips ─────────────────────────────────────────────────
    private void setupPriorityChips() {
        selectPriority("Medium");
        chipLow.setOnClickListener(v    -> selectPriority("Low"));
        chipMedium.setOnClickListener(v -> selectPriority("Medium"));
        chipHigh.setOnClickListener(v   -> selectPriority("High"));
    }

    private void selectPriority(String priority) {
        selectedPriority = priority;

        boolean low  = priority.equals("Low");
        boolean med  = priority.equals("Medium");
        boolean high = priority.equals("High");

        chipLow.setBackgroundResource(low  ? R.drawable.bg_priority_selected : R.drawable.bg_priority_unselected);
        chipLow.setTextColor(ColorStateList.valueOf(low  ? COLOR_WHITE : 0xFF06D6A0));

        chipMedium.setBackgroundResource(med  ? R.drawable.bg_priority_selected : R.drawable.bg_priority_unselected);
        chipMedium.setTextColor(ColorStateList.valueOf(med  ? COLOR_WHITE : 0xFFFFB347));

        chipHigh.setBackgroundResource(high ? R.drawable.bg_priority_selected : R.drawable.bg_priority_unselected);
        chipHigh.setTextColor(ColorStateList.valueOf(high ? COLOR_WHITE : 0xFFFF4757));
    }

    // ── Progress SeekBar ───────────────────────────────────────────────
    private void setupProgressSeekBar() {
        seekBarProgress.setProgress(50);
        tvProgressValue.setText("50%");
        seekBarProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean u) {
                progressTarget = p;
                tvProgressValue.setText(p + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
    }

    // ── Date & Time Picker ─────────────────────────────────────────────
    private void setupDatePicker() {
        // Post to ensure button is fully laid out before attaching listener
        btnPickDate.post(() -> btnPickDate.setOnClickListener(v -> showDatePicker()));
    }

    private void showDatePicker() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dlg = new DatePickerDialog(this,
                (view, year, month, day) -> showTimePicker(year, month, day),
                now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        dlg.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dlg.show();
    }

    private void showTimePicker(int year, int month, int day) {
        Calendar now = Calendar.getInstance();
        new android.app.TimePickerDialog(this,
                (view, hour, minute) -> {
                    selectedDate = String.format(Locale.US,
                            "%04d-%02d-%02d %02d:%02d", year, month + 1, day, hour, minute);
                    updateDateButton(selectedDate);
                },
                now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false).show();
    }

    private void updateDateButton(String dateStr) {
        try {
            String[] parts = dateStr.split(" ");
            String[] d     = parts[0].split("-");
            String[] t     = parts[1].split(":");
            int hour = Integer.parseInt(t[0]);
            int min  = Integer.parseInt(t[1]);
            int dH   = (hour == 0 ? 12 : (hour > 12 ? hour - 12 : hour));
            btnPickDate.setText(String.format(Locale.US,
                    "📅 %s/%s  %02d:%02d %s", d[2], d[1], dH, min, hour < 12 ? "AM" : "PM"));
        } catch (Exception e) {
            btnPickDate.setText("📅 " + dateStr);
        }
        btnPickDate.setTextColor(ColorStateList.valueOf(COLOR_PURPLE));
    }

    // ── Reminder ───────────────────────────────────────────────────────
    private void setupReminder() {
        switchReminder.setOnCheckedChangeListener((btn, checked) -> reminderEnabled = checked);
    }

    // ── Pre-fill ───────────────────────────────────────────────────────
    private void prefillFields() {
        String title = getIntent().getStringExtra(EXTRA_GOAL_TITLE);
        String desc  = getIntent().getStringExtra(EXTRA_GOAL_DESC);
        String date  = getIntent().getStringExtra(EXTRA_GOAL_DATE);

        if (title != null) etGoalTitle.setText(title);

        if (desc != null) {
            String displayDesc = desc;
            int idx = desc.lastIndexOf("\nPriority: ");
            if (idx >= 0) {
                selectedPriority = desc.substring(idx + 11).trim();
                displayDesc = desc.substring(0, idx);
                // Extract category if present
                int catIdx = displayDesc.lastIndexOf("\nCategory: ");
                if (catIdx >= 0) {
                    selectedCategory = displayDesc.substring(catIdx + 11).trim();
                    displayDesc = displayDesc.substring(0, catIdx);
                    selectCategory(selectedCategory);
                }
            }
            etGoalDescription.setText(displayDesc.trim());
            selectPriority(selectedPriority);
        }

        if (date != null && !date.isEmpty()) {
            selectedDate = date;
            updateDateButton(date);
        }
    }

    // ── Save ───────────────────────────────────────────────────────────
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

        // Build enriched description
        StringBuilder fullDesc = new StringBuilder(desc);
        if (!selectedCategory.isEmpty())
            fullDesc.append("\nCategory: ").append(selectedCategory);
        fullDesc.append("\nPriority: ").append(selectedPriority);
        fullDesc.append("\nTarget: ").append(progressTarget).append("%");
        if (reminderEnabled) fullDesc.append("\nReminder: enabled");

        String finalDesc = fullDesc.toString();

        if (isEditMode) {
            executor.execute(() -> {
                Goal goal = new Goal(userId, title, finalDesc, selectedDate);
                goal.id = editGoalId;
                goalDao.updateGoal(goal);

                // Cancel old deadline alarms and reschedule if a date is set
                NotificationHelper.cancelGoalDeadline(this, editGoalId);
                NotificationHelper.cancelGoalDeadlineWarning(this, editGoalId);
                if (selectedDate != null && !selectedDate.isEmpty()) {
                    NotificationHelper.scheduleGoalDeadline(this, editGoalId, title, selectedDate);
                    NotificationHelper.scheduleGoalDeadlineWarning(this, editGoalId, title, selectedDate);
                }

                mainHandler.post(() -> {
                    Toast.makeText(this, "Goal updated ", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finishWithAnimation();
                });
            });
        } else {
            Goal newGoal = new Goal(userId, title, finalDesc, selectedDate);
            executor.execute(() -> {
                long insertedId = goalDao.insertGoal(newGoal);

                // Schedule deadline notification and 5-min warning if a target date is set
                if (selectedDate != null && !selectedDate.isEmpty() && insertedId > 0) {
                    NotificationHelper.scheduleGoalDeadline(this, (int) insertedId, title, selectedDate);
                    NotificationHelper.scheduleGoalDeadlineWarning(this, (int) insertedId, title, selectedDate);
                }

                mainHandler.post(() -> {
                    Toast.makeText(this, "Goal saved! Keep going ", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finishWithAnimation();
                });
            });
        }
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
