package com.example.personal_refelection;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.personal_refelection.database.AppDatabase;
import com.example.personal_refelection.database.Goal;
import com.example.personal_refelection.database.GoalDao;
import com.example.personal_refelection.database.Reflection;
import com.example.personal_refelection.database.ReflectionDao;
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

public class ReflectionsActivity extends AppCompatActivity {

    // ── Views ──────────────────────────────────────────────────────────
    private TextInputLayout    tilReflectionContent;
    private TextInputEditText  etReflectionContent;
    private Spinner            spinnerGoal;
    private Button             btnSaveReflection;
    private LinearLayout       reflectionsContainer;
    private LinearLayout       layoutEmpty;
    private TextView           tvTotalCount;

    // ── Data ───────────────────────────────────────────────────────────
    private ReflectionDao      reflectionDao;
    private GoalDao            goalDao;
    private int                userId = -1;
    private List<Goal>         userGoals = new ArrayList<>();

    private final ExecutorService executor    = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reflections);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.reflectionsRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("GoalReflectPrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        AppDatabase db = AppDatabase.getInstance(this);
        reflectionDao  = db.reflectionDao();
        goalDao        = db.goalDao();

        bindViews();
        setupBack();
        loadGoalsForSpinner();
        loadAllReflections();

        btnSaveReflection.setOnClickListener(v -> saveReflection());
    }

    // ── Bind ───────────────────────────────────────────────────────────
    private void bindViews() {
        tilReflectionContent = findViewById(R.id.tilReflectionContent);
        etReflectionContent  = findViewById(R.id.etReflectionContent);
        spinnerGoal          = findViewById(R.id.spinnerGoal);
        btnSaveReflection    = findViewById(R.id.btnSaveReflection);
        reflectionsContainer = findViewById(R.id.reflectionsContainer);
        layoutEmpty          = findViewById(R.id.layoutEmpty);
        tvTotalCount         = findViewById(R.id.tvTotalCount);
    }

    // ── Load goals into Spinner ────────────────────────────────────────
    private void loadGoalsForSpinner() {
        executor.execute(() -> {
            List<Goal> goals = goalDao.getActiveGoals(userId);
            List<Goal> achieved = goalDao.getAchievedGoals(userId);
            List<Goal> all = new ArrayList<>();
            all.addAll(goals);
            all.addAll(achieved);
            userGoals = all;

            List<String> titles = new ArrayList<>();
            titles.add("— No goal linked —");
            for (Goal g : all) titles.add(g.title);

            mainHandler.post(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item, titles);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerGoal.setAdapter(adapter);
            });
        });
    }

    // ── Save Reflection ────────────────────────────────────────────────
    private void saveReflection() {
        String content = etReflectionContent.getText() != null
                ? etReflectionContent.getText().toString().trim() : "";

        if (TextUtils.isEmpty(content)) {
            tilReflectionContent.setError("Please write something to reflect on");
            etReflectionContent.requestFocus();
            return;
        }
        tilReflectionContent.setError(null);

        // Determine linked goal
        int selectedPos = spinnerGoal.getSelectedItemPosition();
        final int goalId;

        if (selectedPos > 0 && selectedPos - 1 < userGoals.size()) {
            goalId = userGoals.get(selectedPos - 1).id;
        } else {
            // No goal selected — need at least one goal in the DB to link
            if (userGoals.isEmpty()) {
                Toast.makeText(this,
                        "Please create a goal first before adding a reflection.",
                        Toast.LENGTH_LONG).show();
                return;
            }
            // Link to first available goal as default
            goalId = userGoals.get(0).id;
        }

        Reflection reflection = new Reflection(goalId, content);

        executor.execute(() -> {
            reflectionDao.insertReflection(reflection);
            mainHandler.post(() -> {
                Toast.makeText(this, "Reflection saved! 💡", Toast.LENGTH_SHORT).show();
                etReflectionContent.setText("");
                spinnerGoal.setSelection(0);
                loadAllReflections(); // refresh list
            });
        });
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
                .setPositiveButton("Delete", (d, w) -> {
                    executor.execute(() -> {
                        reflectionDao.deleteReflection(reflection);
                        mainHandler.post(() -> {
                            Toast.makeText(this, "Reflection deleted", Toast.LENGTH_SHORT).show();
                            loadAllReflections();
                        });
                    });
                })
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

    // ── Back ───────────────────────────────────────────────────────────
    private void setupBack() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { finish(); }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}

