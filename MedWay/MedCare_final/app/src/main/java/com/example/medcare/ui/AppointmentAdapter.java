package com.example.medcare.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medcare.R;
import com.example.medcare.model.RendezVous;

import java.util.Objects;

public class AppointmentAdapter extends ListAdapter<RendezVous, AppointmentAdapter.AppointmentViewHolder> {

    private final OnAppointmentActionListener listener;

    // Interface for actions
    public interface OnAppointmentActionListener {
        void onCancelClick(RendezVous rendezVous);
    }

    // Constructor
    public AppointmentAdapter(@NonNull OnAppointmentActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        RendezVous current = getItem(position);
        if (current != null) {
            holder.bind(current, listener);
        }
    }

    // ViewHolder
    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView textDate, textTime, textEstablishment, textStatus;
        Button buttonCancel;

        AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find views by ID
            textDate = itemView.findViewById(R.id.item_appt_date);
            textTime = itemView.findViewById(R.id.item_appt_time);
            textEstablishment = itemView.findViewById(R.id.item_appt_establishment);
            textStatus = itemView.findViewById(R.id.item_appt_status);
            buttonCancel = itemView.findViewById(R.id.item_appt_button_cancel);
        }

        void bind(final RendezVous rdv, final OnAppointmentActionListener listener) {
            textDate.setText(Objects.toString(rdv.getAppointmentDate(), "N/A"));
            textTime.setText(Objects.toString(rdv.getAppointmentTime(), "N/A"));

            textStatus.setText("Statut: " + Objects.toString(rdv.getStatus(), "Inconnu"));

            // Handle Cancel Button visibility and action
            if ("CANCELLED".equalsIgnoreCase(rdv.getStatus())) {
                buttonCancel.setVisibility(View.GONE); // Hide if already cancelled
            } else {
                buttonCancel.setVisibility(View.VISIBLE);
                buttonCancel.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onCancelClick(rdv);
                    }
                });
            }

        }
    }

    // DiffUtil Callback
    private static final DiffUtil.ItemCallback<RendezVous> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<RendezVous>() {
                @Override
                public boolean areItemsTheSame(@NonNull RendezVous oldItem, @NonNull RendezVous newItem) {
                    return Objects.equals(oldItem.getDocumentId(), newItem.getDocumentId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull RendezVous oldItem, @NonNull RendezVous newItem) {
                    // Compare relevant fields that determine if UI should update
                    return Objects.equals(oldItem.getAppointmentDate(), newItem.getAppointmentDate()) &&
                            Objects.equals(oldItem.getAppointmentTime(), newItem.getAppointmentTime()) &&
                            Objects.equals(oldItem.getStatus(), newItem.getStatus()) &&
                            Objects.equals(oldItem.getEstablishmentId(), newItem.getEstablishmentId());
                }
            };
}