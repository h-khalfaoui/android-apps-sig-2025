package com.example.saydaliyati.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saydaliyati.Models.Pharmacy;
import com.example.saydaliyati.R;

import java.util.List;

public class PharmacyAdapter extends RecyclerView.Adapter<PharmacyAdapter.PharmacyViewHolder> {

    private final List<Pharmacy> pharmacyList;
    private final Context context;
    private final OnPharmacyClickListener listener;

    private Pharmacy selectedPharmacy;

    public void setSelectedPharmacy(Pharmacy pharmacy) {
        this.selectedPharmacy = pharmacy;
        notifyDataSetChanged();
    }

    // Interface for click handling
    public interface OnPharmacyClickListener {
        void onPharmacyClick(Pharmacy pharmacy);
    }

    public PharmacyAdapter(List<Pharmacy> pharmacyList, Context context, OnPharmacyClickListener listener) {
        this.pharmacyList = pharmacyList;
        this.context = context;
        this.listener = listener;
    }

    // Constructor that supports null listener for backward compatibility
    public PharmacyAdapter(List<Pharmacy> pharmacyList, Context context) {
        this(pharmacyList, context, null);
    }

    @NonNull
    @Override
    public PharmacyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pharmacy, parent, false);
        return new PharmacyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PharmacyViewHolder holder, int position) {
        Pharmacy pharmacy = pharmacyList.get(position);

        // Set basic info
        holder.nameText.setText(pharmacy.getName());
        holder.addressText.setText(pharmacy.getAddress());

        // Set distance if available
        if (pharmacy.getDistance() != null) {
            double distance = pharmacy.getDistance();
            String distanceText;

            if (distance < 1000) {
                distanceText = String.format("%.0f m", distance);
            } else {
                distanceText = String.format("%.1f km", distance / 1000);
            }

            holder.distanceText.setText(distanceText);
            holder.distanceText.setVisibility(View.VISIBLE);
        } else {
            holder.distanceText.setVisibility(View.GONE);
        }

        // Set phone if available
        if (pharmacy.getPhone() != null && !pharmacy.getPhone().isEmpty()) {
            holder.phoneText.setText(pharmacy.getPhone());
            holder.phoneText.setVisibility(View.VISIBLE);
        } else {
            holder.phoneText.setVisibility(View.GONE);
        }

        // Set hours if available
        if (pharmacy.getHours() != null && !pharmacy.getHours().isEmpty()) {
            holder.hoursText.setText(pharmacy.getHours());
            holder.hoursText.setVisibility(View.VISIBLE);
        } else {
            holder.hoursText.setVisibility(View.GONE);
        }

        // Handle item click
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPharmacyClick(pharmacy);
            } else {
                // Fallback to simple toast if no listener provided
                Toast.makeText(context, "Pharmacy: " + pharmacy.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        // Handle directions button click
        holder.directionsButton.setOnClickListener(v -> {
            double lat = pharmacy.getLatitude();
            double lon = pharmacy.getLongitude();

            // Open Google Maps with direction intent
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lon);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            // If Google Maps is not installed, use browser instead
            if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(mapIntent);
            } else {
                // Fallback to web URL
                String uri = "http://maps.google.com/maps?daddr=" + lat + "," + lon;
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
            }
        });

        // Handle call button if phone is available
        if (holder.callButton != null) {
            if (pharmacy.getPhone() != null && !pharmacy.getPhone().isEmpty()) {
                holder.callButton.setVisibility(View.VISIBLE);
                holder.callButton.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + pharmacy.getPhone()));
                    context.startActivity(intent);
                });
            } else {
                holder.callButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return pharmacyList.size();
    }

    public void updatePharmacies(List<Pharmacy> pharmacies) {
        this.pharmacyList.clear();
        this.pharmacyList.addAll(pharmacies);
        notifyDataSetChanged();
    }

    public static class PharmacyViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView nameText, addressText, distanceText, phoneText, hoursText;
        Button directionsButton, callButton;

        public PharmacyViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.pharmacyCardView);
            nameText = itemView.findViewById(R.id.pharmacyNameText);
            addressText = itemView.findViewById(R.id.pharmacyAddressText);
            distanceText = itemView.findViewById(R.id.pharmacyDistanceText);
            phoneText = itemView.findViewById(R.id.pharmacyPhoneText);
            hoursText = itemView.findViewById(R.id.pharmacyHoursText);
            directionsButton = itemView.findViewById(R.id.directionsButton);
            callButton = itemView.findViewById(R.id.callButton);
        }
    }
}