package com.example.personal_refelection;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.personal_refelection.database.AppDatabase;
import com.example.personal_refelection.database.Goal;
import com.example.personal_refelection.database.GoalDao;
import com.example.personal_refelection.database.Reflection;
import com.example.personal_refelection.database.ReflectionDao;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReflectionsActivity extends BaseActivity {

    // ── Views ──────────────────────────────────────────────────────────
    private LinearLayout       reflectionsContainer;
    private LinearLayout       layoutEmpty;
    private TextView           tvTotalCount;

    // ── Data ───────────────────────────────────────────────────────────
    private ReflectionDao      reflectionDao;
    private GoalDao            goalDao;
    private int                userId = -1;
    private List<Goal>         userGoals = new ArrayList<>();

    // ── Mood selection ─────────────────────────────────────────────────
    private String             selectedMood = "";

    private final ExecutorService executor    = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reflections);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.reflectionsRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, 0, bars.right, 0);
            View topNav = v.findViewById(R.id.topNavInclude);
            if (topNav != null) topNav.setPadding(
                    topNav.getPaddingLeft(), bars.top + 8,
                    topNav.getPaddingRight(), topNav.getPaddingBottom());
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("GoalReflectPrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        AppDatabase db = AppDatabase.getInstance(this);
        reflectionDao  = db.reflectionDao();
        goalDao        = db.goalDao();

        bindViews();
        setupTopNav("MY REFLECTIONS");
        setupBottomNav(-1);
        loadAllReflections();

        // All entry points open the same bottom sheet
        View fabAdd   = findViewById(R.id.fabAddReflection);
        View emptyAdd = findViewById(R.id.btnAddReflectionEmpty);

        if (fabAdd   != null) fabAdd.setOnClickListener(v -> openAddReflectionSheet());
        if (emptyAdd != null) emptyAdd.setOnClickListener(v -> openAddReflectionSheet());

        // Override bottom nav FAB to also open reflection sheet on this screen
        View nav = findViewById(R.id.bottomNavInclude);
        if (nav != null) {
            View bottomFab = nav.findViewById(R.id.fabAdd);
            if (bottomFab != null) {
                bottomFab.setOnClickListener(v -> {
                    v.animate().scaleX(0.88f).scaleY(0.88f).setDuration(80).withEndAction(() ->
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                    ).start();
                    openAddReflectionSheet();
                });
            }
        }
    }

    // ── Bind ───────────────────────────────────────────────────────────
    private void bindViews() {
        reflectionsContainer = findViewById(R.id.reflectionsContainer);
        layoutEmpty          = findViewById(R.id.layoutEmpty);
        tvTotalCount         = findViewById(R.id.tvTotalCount);
    }

    // ── Bottom Sheet ───────────────────────────────────────────────────
    private void openAddReflectionSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View sheetView = LayoutInflater.from(this)
                .inflate(R.layout.bottom_sheet_add_reflection, null);
        sheet.setContentView(sheetView);

        // Views inside sheet
        TextInputLayout   tilContent    = sheetView.findViewById(R.id.tilSheetReflectionContent);
        TextInputEditText etContent     = sheetView.findViewById(R.id.etSheetReflectionContent);
        Spinner           spinnerGoal   = sheetView.findViewById(R.id.sheetSpinnerGoal);
        View              btnSave       = sheetView.findViewById(R.id.btnSheetSaveReflection);
        View              btnClose      = sheetView.findViewById(R.id.btnCloseSheet);

        // Mood buttons
        TextView moodHappy     = sheetView.findViewById(R.id.moodHappy);
        TextView moodNeutral   = sheetView.findViewById(R.id.moodNeutral);
        TextView moodSad       = sheetView.findViewById(R.id.moodSad);
        TextView moodMotivated = sheetView.findViewById(R.id.moodMotivated);

        selectedMood = "";
        setupMoodButtons(moodHappy, moodNeutral, moodSad, moodMotivated);

        // Load goals into spinner
        executor.execute(() -> {
            List<Goal> goals    = goalDao.getActiveGoals(userId);
            List<Goal> achieved = goalDao.getAchievedGoals(userId);
            userGoals = new ArrayList<>();
            userGoals.addAll(goals);
            userGoals.addAll(achieved);

            List<String> titles = new ArrayList<>();
            titles.add("— No goal linked —");
            for (Goal g : userGoals) titles.add(g.title);

            mainHandler.post(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item, titles);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerGoal.setAdapter(adapter);
            });
        });

        // Close button
        btnClose.setOnClickListener(v -> sheet.dismiss());

        // Save button
        btnSave.setOnClickListener(v -> {
            String content = etContent.getText() != null
                    ? etContent.getText().toString().trim() : "";

            if (TextUtils.isEmpty(content)) {
                tilContent.setError("Please write something to reflect on");
                etContent.requestFocus();
                return;
            }
            tilContent.setError(null);

            int selectedPos = spinnerGoal.getSelectedItemPosition();
            final int goalId;
            if (selectedPos > 0 && selectedPos - 1 < userGoals.size()) {
                goalId = userGoals.get(selectedPos - 1).id;
            } else {
                goalId = 0; // 0 = no goal linked, allowed
            }

            String moodTag = selectedMood.isEmpty() ? "" : " " + selectedMood;
            Reflection reflection = new Reflection(userId, goalId, content + moodTag);

            executor.execute(() -> {
                reflectionDao.insertReflection(reflection);
                mainHandler.post(() -> {
                    Toast.makeText(this, "Reflection saved! 💡", Toast.LENGTH_SHORT).show();
                    sheet.dismiss();
                    loadAllReflections();
                });
            });
        });

        sheet.show();
    }

    // ── Mood selection helpers ─────────────────────────────────────────
    private void setupMoodButtons(TextView happy, TextView neutral, TextView sad, TextView motivated) {
        View.OnClickListener listener = v -> {
            // Reset all
            resetMoodBg(happy, neutral, sad, motivated);
            TextView clicked = (TextView) v;
            clicked.setBackgroundResource(R.drawable.bg_chip_active_pill);
            if (v.getId() == R.id.moodHappy)          selectedMood = "😊";
            else if (v.getId() == R.id.moodNeutral)    selectedMood = "😐";
            else if (v.getId() == R.id.moodSad)        selectedMood = "😔";
            else if (v.getId() == R.id.moodMotivated)  selectedMood = "🔥";
        };
        happy.setOnClickListener(listener);
        neutral.setOnClickListener(listener);
        sad.setOnClickListener(listener);
        motivated.setOnClickListener(listener);
    }

    private void resetMoodBg(TextView... views) {
        for (TextView tv : views) tv.setBackgroundResource(R.drawable.bg_chip_inactive_pill);
    }

    // ── Load & Display All Reflections ────────────────────────────────
    private void loadAllReflections() {
        executor.execute(() -> {
            List<Reflection> reflections = reflectionDao.getAllReflections(userId);
            int total = reflectionDao.countTotalReflections(userId);
            mainHandler.post(() -> displayReflections(reflections, total));
        });
    }

    private void displayReflections(List<Reflection> reflections, int total) {
        reflectionsContainer.removeAllViews();
        tvTotalCount.setText(String.valueOf(total));

        if (reflections == null || reflections.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            reflectionsContainer.setVisibility(View.GONE);
            return;
        }

        layoutEmpty.setVisibility(View.GONE);
        reflectionsContainer.setVisibility(View.VISIBLE);

        for (Reflection r : reflections) {
            View item = LayoutInflater.from(this)
                    .inflate(R.layout.item_reflection_full, reflectionsContainer, false);

            TextView tvDate    = item.findViewById(R.id.tvReflectionDate);
            TextView tvContent = item.findViewById(R.id.tvReflectionContent);
            View     btnDelete = item.findViewById(R.id.btnDeleteReflection);

            tvDate.setText(formatDate(r.createdAt));
            tvContent.setText(r.content);
            btnDelete.setOnClickListener(v -> confirmDelete(r));

            reflectionsContainer.addView(item);
        }
    }

    // ── Confirm Delete ─────────────────────────────────────────────────
    private void confirmDelete(Reflection reflection) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Reflection")
                .setMessage("Are you sure you want to delete this reflection?")
                .setPositiveButton("Delete", (d, w) -> executor.execute(() -> {
                    reflectionDao.deleteReflection(reflection);
                    mainHandler.post(() -> {
                        Toast.makeText(this, "Reflection deleted", Toast.LENGTH_SHORT).show();
                        loadAllReflections();
                    });
                }))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Format date ────────────────────────────────────────────────────
    private String formatDate(long timestamp) {
        Date date = new Date(timestamp);
        Calendar reflCal = Calendar.getInstance();
        reflCal.setTime(date);
        Calendar today = Calendar.getInstance();

        if (reflCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            reflCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            return "Today at " + new SimpleDateFormat("h:mm a", Locale.getDefault()).format(date);
        }
        today.add(Calendar.DAY_OF_YEAR, -1);
        if (reflCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            reflCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            return "Yesterday at " + new SimpleDateFormat("h:mm a", Locale.getDefault()).format(date);
        }
        return new SimpleDateFormat("MMM d, yyyy  •  h:mm a", Locale.getDefault()).format(date);
    }


    @Override
    protected void onResume() {
        super.onResume();
        setupTopNav("MY REFLECTIONS");
        loadAllReflections();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
