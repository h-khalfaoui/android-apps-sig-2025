package com.example.saydaliyati.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saydaliyati.Activities.AssignGuardDateActivity;
import com.example.saydaliyati.Database.AppDatabase;
import com.example.saydaliyati.Database.GuardDateDAO;
import com.example.saydaliyati.Database.PharmacyDAO;
import com.example.saydaliyati.Models.GuardDate;
import com.example.saydaliyati.Models.Pharmacy;
import com.example.saydaliyati.R;
import com.example.saydaliyati.Utils.DateUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminCalendarFragment extends Fragment {

    private RecyclerView guardDateRecyclerView;
    private TextView noGuardDatesText;
    private FloatingActionButton addGuardDateFab;

    private GuardDateDAO guardDateDAO;
    private PharmacyDAO pharmacyDAO;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_calendar, container, false);

        // Initialiser les vues
        guardDateRecyclerView = view.findViewById(R.id.guardDateRecyclerView);
        noGuardDatesText = view.findViewById(R.id.noGuardDatesText);
        addGuardDateFab = view.findViewById(R.id.addGuardDateFab);

        // Initialiser DAO
        guardDateDAO = AppDatabase.getInstance(requireContext()).guardDateDAO();
        pharmacyDAO = AppDatabase.getInstance(requireContext()).pharmacyDAO();

        // Configurer RecyclerView
        guardDateRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Configurer FAB pour ajouter de nouvelles dates de garde
        addGuardDateFab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AssignGuardDateActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Charger les dates de garde chaque fois que le fragment redevient visible
        loadGuardDates();
    }

    private void loadGuardDates() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (getActivity() == null) return;

            // Obtenir toutes les dates de garde
            List<GuardDate> guardDates = guardDateDAO.getAllGuardDates();

            // Obtenir tous les pharmacies pour référence
            List<Pharmacy> allPharmacies = pharmacyDAO.getAllPharmacies();
            Map<Integer, Pharmacy> pharmacyMap = new HashMap<>();
            for (Pharmacy pharmacy : allPharmacies) {
                pharmacyMap.put(pharmacy.getId(), pharmacy);
            }

            // Préparer les données pour l'affichage
            List<String> displayItems = new ArrayList<>();

            for (GuardDate guardDate : guardDates) {
                Pharmacy pharmacy = pharmacyMap.get(guardDate.getPharmacyId());
                if (pharmacy != null) {
                    String formattedDate = DateUtils.formatForDisplay(guardDate.getGuardDate());
                    String timeInfo = "";

                    if (guardDate.getStartTime() != null && guardDate.getEndTime() != null) {
                        timeInfo = " (" + guardDate.getStartTime() + " - " + guardDate.getEndTime() + ")";
                    }

                    String item = formattedDate + timeInfo + " - " + pharmacy.getName();
                    displayItems.add(item);
                }
            }

            requireActivity().runOnUiThread(() -> {

                ArrayAdapterSimple adapter = new ArrayAdapterSimple(requireContext(), displayItems);
                guardDateRecyclerView.setAdapter(adapter);

                // Afficher un message si aucune date de garde n'est trouvée
                if (guardDates.isEmpty()) {
                    noGuardDatesText.setVisibility(View.VISIBLE);
                    guardDateRecyclerView.setVisibility(View.GONE);
                } else {
                    noGuardDatesText.setVisibility(View.GONE);
                    guardDateRecyclerView.setVisibility(View.VISIBLE);
                }
            });
        });
    }


    private static class ArrayAdapterSimple extends RecyclerView.Adapter<ArrayAdapterSimple.ViewHolder> {
        private final List<String> items;
        private final Context context;

        public ArrayAdapterSimple(Context context, List<String> items) {
            this.context = context;
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.textView.setText(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}