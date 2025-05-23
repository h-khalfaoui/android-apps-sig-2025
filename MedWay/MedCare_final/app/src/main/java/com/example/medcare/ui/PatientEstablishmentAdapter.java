package com.example.medcare.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medcare.R;
import com.example.medcare.model.Etablissement;

import java.util.Objects;

public class PatientEstablishmentAdapter extends ListAdapter<Etablissement, PatientEstablishmentAdapter.EtablissementViewHolder> {

    private final OnEtablissementClickListener clickListener;

    public interface OnEtablissementClickListener {
        void onEtablissementClick(Etablissement etablissement);
    }

    public PatientEstablishmentAdapter(@NonNull OnEtablissementClickListener listener) {
        super(DIFF_CALLBACK);
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public EtablissementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patient_establishment, parent, false);
        return new EtablissementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EtablissementViewHolder holder, int position) {
        Etablissement current = getItem(position);
        if (current != null) {
            holder.bind(current, clickListener);
        }
    }

    static class EtablissementViewHolder extends RecyclerView.ViewHolder {
        TextView textNom, textType, textAdresse;
        ImageView hospitalIconView;
        ImageView availabilityIconView;

        EtablissementViewHolder(@NonNull View itemView) {
            super(itemView);
            textNom = itemView.findViewById(R.id.text_view_hospital_name);
            textType = itemView.findViewById(R.id.text_view_hospital_type);
            textAdresse = itemView.findViewById(R.id.text_view_hospital_address);
            hospitalIconView = itemView.findViewById(R.id.image_view_hospital_icon);
            availabilityIconView = itemView.findViewById(R.id.icon_view_availability);
        }

        void bind(final Etablissement etablissement, final OnEtablissementClickListener listener) {
            if (etablissement != null) {
                textNom.setText(Objects.toString(etablissement.getNom(), "Nom non disponible"));
                textType.setText(Objects.toString(etablissement.getType(), "Type non spécifié"));
                textAdresse.setText(Objects.toString(etablissement.getAdresse(), "Adresse non disponible"));
            } else {
                textNom.setText("Information manquante");
                textType.setText("");
                textAdresse.setText("");
            }

            itemView.setOnClickListener(v -> {
                if (listener != null && etablissement != null) {
                    listener.onEtablissementClick(etablissement);
                }
            });
        }
    }

    private static final DiffUtil.ItemCallback<Etablissement> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Etablissement>() {
                @Override
                public boolean areItemsTheSame(@NonNull Etablissement oldItem, @NonNull Etablissement newItem) {
                    return Objects.equals(oldItem.getDocumentId(), newItem.getDocumentId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Etablissement oldItem, @NonNull Etablissement newItem) {
                    return Objects.equals(oldItem.getNom(), newItem.getNom()) &&
                            Objects.equals(oldItem.getType(), newItem.getType()) &&
                            Objects.equals(oldItem.getAdresse(), newItem.getAdresse());
                }
            };
}