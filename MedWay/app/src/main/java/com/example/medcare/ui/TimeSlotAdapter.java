package com.example.medcare.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medcare.R;
import com.example.medcare.model.TimeSlot;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder> {

    private List<TimeSlot> slots = new ArrayList<>();
    private final OnSlotClickListener listener;
    private final Context context;

    public interface OnSlotClickListener {
        void onSlotClick(TimeSlot slot);
    }

    public TimeSlotAdapter(Context context, OnSlotClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void submitList(List<TimeSlot> newSlots) {
        this.slots.clear();
        if (newSlots != null) {
            this.slots.addAll(newSlots);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TimeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timeslot, parent, false);
        return new TimeSlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeSlotViewHolder holder, int position) {
        TimeSlot slot = slots.get(position);
        holder.bind(slot, listener, context);
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    static class TimeSlotViewHolder extends RecyclerView.ViewHolder {
        MaterialButton timeButton;

        TimeSlotViewHolder(@NonNull View itemView) {
            super(itemView);
            timeButton = itemView.findViewById(R.id.button_timeslot);
        }

        void bind(final TimeSlot slot, final OnSlotClickListener listener, Context context) {
            timeButton.setText(slot.time);
            timeButton.setEnabled(slot.isAvailable);

            if (slot.isAvailable) {

            } else {

            }

            timeButton.setOnClickListener(v -> {
                if (slot.isAvailable) {
                    listener.onSlotClick(slot);
                }
            });
        }
    }
}