package com.example.saydaliyati.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saydaliyati.Models.GuardDate;
import com.example.saydaliyati.R;
import com.example.saydaliyati.Utils.DateUtils;

import java.util.List;

public class GuardDateAdapter extends RecyclerView.Adapter<GuardDateAdapter.GuardDateViewHolder> {

    private final List<GuardDate> guardDateList;
    private final Context context;
    private final OnGuardDateActionListener listener;

    // Interface for action callbacks
    public interface OnGuardDateActionListener {
        void onDeleteGuardDate(GuardDate guardDate);
    }

    public GuardDateAdapter(List<GuardDate> guardDateList, Context context, OnGuardDateActionListener listener) {
        this.guardDateList = guardDateList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GuardDateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_guard_date, parent, false);
        return new GuardDateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GuardDateViewHolder holder, int position) {
        GuardDate guardDate = guardDateList.get(position);

        // Format date for display
        String formattedDate = DateUtils.formatForDisplay(guardDate.getGuardDate());
        holder.dateText.setText(formattedDate);

        // Set time information
        String timeInfo = guardDate.getStartTime();
        if (guardDate.getEndTime() != null && !guardDate.getEndTime().isEmpty()) {
            timeInfo += " - " + guardDate.getEndTime();
        }
        holder.timeText.setText(timeInfo);

        // Check if date is in the past
        boolean isPastDate = DateUtils.isDateInPast(guardDate.getGuardDate());
        if (isPastDate) {
            holder.cardView.setCardBackgroundColor(
                    context.getResources().getColor(R.color.pastDateBackground));
            holder.statusText.setText("Past");
            holder.statusText.setVisibility(View.VISIBLE);
        } else {
            holder.cardView.setCardBackgroundColor(
                    context.getResources().getColor(R.color.colorCardBackground));
            holder.statusText.setVisibility(View.GONE);
        }

        // Handle delete action
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteGuardDate(guardDate);
            }
        });
    }

    @Override
    public int getItemCount() {
        return guardDateList.size();
    }

    public static class GuardDateViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView dateText, timeText, statusText;
        ImageButton deleteButton;

        public GuardDateViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.guardDateCardView);
            dateText = itemView.findViewById(R.id.guardDateText);
            timeText = itemView.findViewById(R.id.guardTimeText);
            statusText = itemView.findViewById(R.id.guardStatusText);
            deleteButton = itemView.findViewById(R.id.deleteGuardDateButton);
        }
    }
}