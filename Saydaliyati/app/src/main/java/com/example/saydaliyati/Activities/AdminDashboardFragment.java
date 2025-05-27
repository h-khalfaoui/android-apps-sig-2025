package com.example.saydaliyati.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.saydaliyati.Activities.AddPharmacyActivity;
import com.example.saydaliyati.Activities.AssignGuardDateActivity;
import com.example.saydaliyati.Database.AppDatabase;
import com.example.saydaliyati.R;
import com.example.saydaliyati.Utils.SecurityUtils;

public class AdminDashboardFragment extends Fragment {

    private TextView welcomeText;
    private TextView statsText;
    private Button addPharmacyButton;
    private Button assignGuardDateButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        // Initialiser les vues
        welcomeText = view.findViewById(R.id.welcomeText);
        statsText = view.findViewById(R.id.statsText);
        addPharmacyButton = view.findViewById(R.id.addPharmacyButton);
        assignGuardDateButton = view.findViewById(R.id.assignGuardDateButton);

        // Définir le texte de bienvenue
        String username = SecurityUtils.getCurrentUsername(requireContext());
        if (username != null) {
            welcomeText.setText("Bienvenue, " + username + "!");
        }

        // Configurer les actions des boutons
        addPharmacyButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddPharmacyActivity.class);
            startActivity(intent);
        });

        assignGuardDateButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AssignGuardDateActivity.class);
            startActivity(intent);
        });

        // Charger les statistiques
        loadStats();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recharger les statistiques à chaque reprise de l'affichage
        loadStats();
    }

    private void loadStats() {
        // Charger les statistiques en arrière-plan
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (getActivity() == null) return;

            int pharmacyCount = AppDatabase.getInstance(requireContext()).pharmacyDAO().getAllPharmacies().size();
            int guardDateCount = AppDatabase.getInstance(requireContext()).guardDateDAO().getAllGuardDates().size();

            // Mettre à jour l'interface utilisateur sur le thread principal
            requireActivity().runOnUiThread(() -> {
                String stats = "Statistiques:\n" +
                        "- Nombre de pharmacies: " + pharmacyCount + "\n" +
                        "- Dates de garde assignées: " + guardDateCount;
                statsText.setText(stats);
            });
        });
    }
}