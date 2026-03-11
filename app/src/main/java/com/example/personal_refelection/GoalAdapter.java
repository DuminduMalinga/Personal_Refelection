package com.example.personal_refelection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personal_refelection.database.Goal;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying Goal cards in GoalsActivity.
 */
public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {

    public interface GoalActionListener {
        void onEdit(Goal goal);
        void onDelete(Goal goal);
        void onMarkAchieved(Goal goal);
    }

    private final Context context;
    private List<Goal> goals = new ArrayList<>();
    private GoalActionListener listener;

    public GoalAdapter(Context context, GoalActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setGoals(List<Goal> goals) {
        this.goals = goals != null ? goals : new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<Goal> getGoals() {
        return goals;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_goal_activity, parent, false);
        return new GoalViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        Goal goal = goals.get(position);
        holder.tvTitle.setText(goal.title != null ? goal.title : "");
        holder.tvDescription.setText(
                (goal.description != null && !goal.description.isEmpty()) ? goal.description : "No description");

        // Status badge
        if (goal.isCompleted) {
            holder.tvStatus.setText("Achieved ✓");
            holder.tvStatus.setTextColor(0xFF06D6A0); // stat_green
            holder.tvStatus.setBackgroundResource(R.drawable.bg_goal_status_achieved);
            holder.btnMarkAchieved.setVisibility(View.GONE);
        } else {
            holder.tvStatus.setText("Active");
            holder.tvStatus.setTextColor(0xFF6C63FF); // stat_blue
            holder.tvStatus.setBackgroundResource(R.drawable.bg_goal_status_active);
            holder.btnMarkAchieved.setVisibility(View.VISIBLE);
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(goal);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(goal);
        });
        holder.btnMarkAchieved.setOnClickListener(v -> {
            if (listener != null) listener.onMarkAchieved(goal);
        });
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvStatus;
        LinearLayout btnEdit, btnDelete, btnMarkAchieved;

        GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvGoalTitle);
            tvDescription = itemView.findViewById(R.id.tvGoalDescription);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnMarkAchieved = itemView.findViewById(R.id.btnMarkAchieved);
        }
    }
}

