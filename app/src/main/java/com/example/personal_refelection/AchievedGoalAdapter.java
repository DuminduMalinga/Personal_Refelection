package com.example.personal_refelection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personal_refelection.database.Goal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for the Achieved Goals screen.
 * Displays each completed goal in a modern trophy card.
 */
public class AchievedGoalAdapter extends RecyclerView.Adapter<AchievedGoalAdapter.AchievedViewHolder> {

    public interface OnGoalClickListener {
        void onGoalClick(Goal goal);
    }

    private final Context context;
    private List<Goal> goals = new ArrayList<>();
    private final OnGoalClickListener clickListener;

    private static final SimpleDateFormat DATE_FMT =
            new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);

    public AchievedGoalAdapter(Context context, OnGoalClickListener listener) {
        this.context = context;
        this.clickListener = listener;
    }

    public void setGoals(List<Goal> goals) {
        this.goals = goals != null ? new ArrayList<>(goals) : new ArrayList<>();
        notifyItemRangeChanged(0, this.goals.size());
    }

    public List<Goal> getGoals() {
        return goals;
    }

    @NonNull
    @Override
    public AchievedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_achieved_goal, parent, false);
        return new AchievedViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievedViewHolder holder, int position) {
        Goal goal = goals.get(position);

        // Title
        holder.tvTitle.setText(goal.title != null ? goal.title : "");

        // Description preview
        if (goal.description != null && !goal.description.isEmpty()) {
            holder.tvDesc.setText(goal.description);
            holder.tvDesc.setVisibility(View.VISIBLE);
        } else {
            holder.tvDesc.setVisibility(View.GONE);
        }

        // Category — derive from description keyword or default
        holder.tvCategory.setText(getCategoryLabel(goal));

        // Completion date — use targetDate if set, else createdAt
        String dateStr;
        if (goal.targetDate != null && !goal.targetDate.isEmpty()) {
            dateStr = goal.targetDate;
        } else {
            dateStr = DATE_FMT.format(new Date(goal.createdAt));
        }
        holder.tvDate.setText(dateStr);

        // Tap → show details
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onGoalClick(goal);
        });

        // Card entrance animation — subtle slide-in from bottom
        holder.itemView.setTranslationY(40f);
        holder.itemView.setAlpha(0f);
        holder.itemView.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(300)
                .setStartDelay(position * 60L)
                .start();
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    /** Simple category heuristic based on title keywords */
    private String getCategoryLabel(Goal goal) {
        if (goal.title == null) return "General";
        String t = goal.title.toLowerCase(Locale.getDefault());
        if (t.contains("health") || t.contains("gym") || t.contains("exercise") || t.contains("run"))
            return "💪 Health";
        if (t.contains("study") || t.contains("learn") || t.contains("course") || t.contains("read"))
            return "📚 Study";
        if (t.contains("work") || t.contains("job") || t.contains("career") || t.contains("project"))
            return "💼 Career";
        if (t.contains("finance") || t.contains("save") || t.contains("money") || t.contains("invest"))
            return "💰 Finance";
        return "✨ Personal";
    }

    static class AchievedViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvCategory, tvDate;

        AchievedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle    = itemView.findViewById(R.id.tvAchievedTitle);
            tvDesc     = itemView.findViewById(R.id.tvAchievedDesc);
            tvCategory = itemView.findViewById(R.id.tvAchievedCategory);
            tvDate     = itemView.findViewById(R.id.tvAchievedDate);
        }
    }
}
