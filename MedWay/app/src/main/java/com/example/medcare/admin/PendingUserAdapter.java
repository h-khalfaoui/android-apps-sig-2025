package com.example.medcare.admin;


import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medcare.R;
import com.example.medcare.model.UserProfile;

import java.util.List;

public class PendingUserAdapter extends RecyclerView.Adapter<PendingUserAdapter.UserViewHolder> {

    private List<UserProfile> userList;
    private final OnUserActionListener listener;

    // Interface for handling button clicks in the Activity
    public interface OnUserActionListener {
        void onApproveClick(String uid);
        void onRejectClick(String uid);
    }

    public PendingUserAdapter(List<UserProfile> userList, OnUserActionListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pending_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserProfile user = userList.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    // Method to update the list if needed
    public void setUserList(List<UserProfile> newUserList) {
        this.userList = newUserList;
        notifyDataSetChanged();
    }

    // ViewHolder class
    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, emailTextView, licenseTextView, specialtyTextView;
        Button approveButton, rejectButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textViewPendingName);
            emailTextView = itemView.findViewById(R.id.textViewPendingEmail);
            licenseTextView = itemView.findViewById(R.id.textViewPendingLicense);
            specialtyTextView = itemView.findViewById(R.id.textViewPendingSpecialty);
            approveButton = itemView.findViewById(R.id.buttonApprove);
            rejectButton = itemView.findViewById(R.id.buttonReject);
        }

        @SuppressLint("SetTextI18n")
        public void bind(final UserProfile user, final OnUserActionListener listener) {
            nameTextView.setText(user.getName() != null ? user.getName() : "N/A");
            emailTextView.setText(user.getEmail() != null ? user.getEmail() : "N/A");
            licenseTextView.setText("Numéro de Licence: " + (user.getLicenseNumber() != null ? user.getLicenseNumber() : "N/A"));
            specialtyTextView.setText("Specialté: " + (user.getSpecialty() != null ? user.getSpecialty() : "N/A"));

            approveButton.setOnClickListener(v -> {
                if (listener != null && user.getUid() != null) {
                    listener.onApproveClick(user.getUid());
                }
            });

            rejectButton.setOnClickListener(v -> {
                if (listener != null && user.getUid() != null) {
                    listener.onRejectClick(user.getUid());
                }
            });
        }
    }
}