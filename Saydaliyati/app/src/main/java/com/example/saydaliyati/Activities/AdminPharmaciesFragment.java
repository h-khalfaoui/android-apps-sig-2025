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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saydaliyati.Activities.AddPharmacyActivity;
import com.example.saydaliyati.Adapters.PharmacyAdapter;
import com.example.saydaliyati.Database.AppDatabase;
import com.example.saydaliyati.Models.Pharmacy;
import com.example.saydaliyati.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class AdminPharmaciesFragment extends Fragment {

    private RecyclerView pharmacyRecyclerView;
    private TextView noPharmaciesText;
    private FloatingActionButton addPharmacyFab;
    private PharmacyAdapter adapter;
    private List<Pharmacy> pharmacyList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_pharmacies, container, false);

        // Initialiser les vues
        pharmacyRecyclerView = view.findViewById(R.id.adminPharmacyRecyclerView);
        noPharmaciesText = view.findViewById(R.id.noPharmaciesText);
        addPharmacyFab = view.findViewById(R.id.addPharmacyFab);

        // Configurer RecyclerView
        pharmacyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PharmacyAdapter(pharmacyList, requireContext());
        pharmacyRecyclerView.setAdapter(adapter);

        // Configurer le bouton d'ajout
        addPharmacyFab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddPharmacyActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Charger les pharmacies chaque fois que le fragment redevient visible
        loadPharmacies();
    }

    private void loadPharmacies() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (getActivity() == null) return;

            List<Pharmacy> pharmacies = AppDatabase.getInstance(requireContext()).pharmacyDAO().getAllPharmacies();

            requireActivity().runOnUiThread(() -> {
                pharmacyList.clear();
                pharmacyList.addAll(pharmacies);
                adapter.notifyDataSetChanged();

                // Afficher un message si aucune pharmacie n'est trouvée
                if (pharmacies.isEmpty()) {
                    noPharmaciesText.setVisibility(View.VISIBLE);
                    pharmacyRecyclerView.setVisibility(View.GONE);
                } else {
                    noPharmaciesText.setVisibility(View.GONE);
                    pharmacyRecyclerView.setVisibility(View.VISIBLE);
                }
            });
        });
    }
}