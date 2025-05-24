package com.example.medcare.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.medcare.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.example.medcare.model.Disponibilite;
import com.example.medcare.model.Etablissement;
import com.example.medcare.viewmodel.DisponibiliteViewModel;
import com.example.medcare.viewmodel.DisponibiliteViewModelFactory;
import com.example.medcare.viewmodel.EtablissementViewModel;
import com.example.medcare.viewmodel.EtablissementViewModelFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DisponibiliteFragment extends Fragment {

    private static final String TAG = "DisponibiliteFragment";

    private Spinner spinnerEtab;
    private Spinner spinnerJour;
    private EditText debutInput, finInput;
    private Button btnAjouter;
    private LinearLayout listeContainer;
    private ProgressBar loadingIndicator;

    private DisponibiliteViewModel disponibiliteVM;
    private EtablissementViewModel etablissementVM;

    private List<Etablissement> etablissementsList = new ArrayList<>();
    private String currentProfessionalId;

    private static final List<String> DAYS_OF_WEEK = Arrays.asList(
            "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_disponibilite_fragment, container, false);

        spinnerEtab = view.findViewById(R.id.spinner_etab);
        spinnerJour = view.findViewById(R.id.spinner_jour);
        debutInput = view.findViewById(R.id.input_debut);
        finInput = view.findViewById(R.id.input_fin);
        btnAjouter = view.findViewById(R.id.btn_ajouter_dispo);
        listeContainer = view.findViewById(R.id.liste_dispos);
        loadingIndicator = view.findViewById(R.id.loading_indicator_dispo);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentProfessionalId = currentUser.getUid();
        } else {
            Log.e(TAG, "Error: Current professional user ID is null.");
            Toast.makeText(getContext(), "Erreur: Utilisateur non connecté.", Toast.LENGTH_LONG).show();
        }

        EtablissementViewModelFactory etabFactory = new EtablissementViewModelFactory();
        etablissementVM = new ViewModelProvider(requireActivity(), etabFactory).get(EtablissementViewModel.class);

        DisponibiliteViewModelFactory dispoFactory = new DisponibiliteViewModelFactory();
        disponibiliteVM = new ViewModelProvider(this, dispoFactory).get(DisponibiliteViewModel.class);

        setupDaySpinner();
        setupObservers();

        btnAjouter.setOnClickListener(v -> ajouterOuModifierDisponibilite());

        spinnerEtab.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (!etablissementsList.isEmpty() && position >= 0 && position < etablissementsList.size()) {
                    if (currentProfessionalId != null) {
                        afficherDisponibilites(currentProfessionalId);
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return view;
    }

    private void setupDaySpinner() {
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, DAYS_OF_WEEK);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerJour.setAdapter(dayAdapter);
    }

    private void setupObservers() {
        etablissementVM.getAllEtablissementsStream().observe(getViewLifecycleOwner(), etabs -> {
            Log.d(TAG, "Tous les établissements reçus du ViewModel: " + (etabs != null ? etabs.size() : "null"));
            if (etabs != null) {
                etablissementsList.clear();
                etablissementsList.addAll(etabs);

                if (etablissementsList.isEmpty()) {
                    Toast.makeText(getContext(), "Aucun établissement n'est disponible.", Toast.LENGTH_LONG).show();
                    spinnerEtab.setAdapter(null);
                    listeContainer.removeAllViews();
                } else {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_item,
                            getNomsEtablissements(etablissementsList));
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerEtab.setAdapter(adapter);

                    if (spinnerEtab.getCount() > 0) {
                        spinnerEtab.setSelection(0);
                        if (currentProfessionalId != null) {
                            afficherDisponibilites(currentProfessionalId);
                        }
                    }
                }
            } else {
                Log.w(TAG, "La liste d'établissements du ViewModel est null.");
                etablissementsList.clear();
                ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item,
                        Collections.singletonList("Aucun établissement"));
                emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerEtab.setAdapter(emptyAdapter);
                spinnerEtab.setEnabled(false);

                listeContainer.removeAllViews();
                Toast.makeText(getContext(), "Erreur lors du chargement des établissements.", Toast.LENGTH_SHORT).show();
            }
        });

        disponibiliteVM.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnAjouter.setEnabled(!isLoading && !etablissementsList.isEmpty());
            spinnerEtab.setEnabled(!isLoading && !etablissementsList.isEmpty());
            spinnerJour.setEnabled(!isLoading);
        });

        disponibiliteVM.getOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(getContext(), "Disponibilité enregistrée!", Toast.LENGTH_SHORT).show();
                if (currentProfessionalId != null) {
                    afficherDisponibilites(currentProfessionalId);
                }
            }
        });

        disponibiliteVM.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Erreur: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void ajouterOuModifierDisponibilite() { // Renamed method
        if (currentProfessionalId == null) {
            Toast.makeText(getContext(), "Erreur: Utilisateur professionnel non identifié.", Toast.LENGTH_SHORT).show();
            return;
        }

        int etabIndex = spinnerEtab.getSelectedItemPosition();
        int jourIndex = spinnerJour.getSelectedItemPosition();

        if (etabIndex == AdapterView.INVALID_POSITION || etablissementsList.isEmpty() || etabIndex >= etablissementsList.size()) {
            Toast.makeText(getContext(), "Veuillez sélectionner un établissement.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (jourIndex == AdapterView.INVALID_POSITION ) {
            Toast.makeText(getContext(), "Veuillez sélectionner un jour.", Toast.LENGTH_SHORT).show();
            return;
        }


        String etabDocumentId = etablissementsList.get(etabIndex).getDocumentId();
        if (etabDocumentId == null || etabDocumentId.isEmpty()){
            Toast.makeText(getContext(), "Erreur: ID établissement invalide.", Toast.LENGTH_SHORT).show();
            return;
        }

        // <<< GET DAY FROM SPINNER >>>
        String jour = (String) spinnerJour.getSelectedItem();
        String debut = debutInput.getText().toString().trim();
        String fin = finInput.getText().toString().trim();
        boolean ouvert = true; // Assuming adding/editing means it's open. Need a Checkbox/Switch to set this to false.

        // Basic validation (improve time validation)
        if (TextUtils.isEmpty(debut) || TextUtils.isEmpty(fin) || !debut.matches("\\d{2}:\\d{2}") || !fin.matches("\\d{2}:\\d{2}")) {
            Toast.makeText(getContext(), "Format Heure Début/Fin invalide (HH:mm).", Toast.LENGTH_SHORT).show();
            return;
        }

        // Construct Firestore object
        Disponibilite newDispo = new Disponibilite(etabDocumentId, currentProfessionalId, jour, debut, fin, ouvert);

        // Construct the specific Firestore Document ID using the *standardized* day name
        String availabilityDocId = currentProfessionalId + "_" + jour;

        Log.d(TAG, "Upserting Availability - DocID: " + availabilityDocId + ", EstabID: " + etabDocumentId + ", Jour: " + jour);
        disponibiliteVM.upsert(newDispo, availabilityDocId);
    }

    private void afficherDisponibilites(String professionalId) {
        if (professionalId == null) return;

        disponibiliteVM.getDisponibilitesForProfessionalStream(professionalId)
                .observe(getViewLifecycleOwner(), list -> {
                    listeContainer.removeAllViews();
                    LayoutInflater inflater = LayoutInflater.from(getContext());

                    if (list != null) {
                        if (list.isEmpty()) {
                            TextView tv = new TextView(getContext());
                            tv.setText("Aucune Disponibilité Définie.");
                            tv.setPadding(16, 8, 16, 8);
                            listeContainer.addView(tv);
                        } else {
                            for (Disponibilite d : list) {
                                View itemView = inflater.inflate(R.layout.view_disponibilite_item, listeContainer, false);

                                TextView tvEtab = itemView.findViewById(R.id.tv_etablissement);
                                TextView tvJour = itemView.findViewById(R.id.tv_jour);
                                TextView tvHoraire = itemView.findViewById(R.id.tv_horaire);
                                TextView tvStatut = itemView.findViewById(R.id.tv_statut);

                                String etabName = findEtabNameById(d.getEtablissementId());
                                tvEtab.setText(etabName != null ? etabName : "Nom inconnu");
                                tvJour.setText("Jour : " + (d.getJour() != null ? d.getJour() : "Non Précisé"));
                                tvHoraire.setText("Horaire : " + d.getHeureDebut() + " - " + d.getHeureFin());
                                tvStatut.setText("Statut : " + (d.isOuvert() ? "Ouvert" : "Fermé"));

                                listeContainer.addView(itemView);
                            }
                        }
                    } else {
                        TextView tv = new TextView(getContext());
                        tv.setText("Erreur au chargement des disponibilités.");
                        tv.setPadding(16, 8, 16, 8);
                        listeContainer.addView(tv);
                    }
                });
    }


    private List<String> getNomsEtablissements(List<Etablissement> etabs) {
        List<String> noms = new ArrayList<>();
        if(etabs == null) return noms;
        for (Etablissement etab : etabs) {
            if(etab != null && etab.getNom() != null) {
                noms.add(etab.getNom());
            }
        }
        return noms;
    }

    private String findEtabNameById(String docId) {
        if(docId == null || docId.isEmpty() || etablissementsList == null) return null;
        for (Etablissement etab : etablissementsList) {
            if (docId.equals(etab.getDocumentId())) {
                return etab.getNom();
            }
        }
        return null;
    }


}