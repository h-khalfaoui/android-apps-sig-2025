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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.medcare.model.Etablissement;
import com.example.medcare.model.FileAttente;
import com.example.medcare.viewmodel.EtablissementViewModel;
import com.example.medcare.viewmodel.EtablissementViewModelFactory;
import com.example.medcare.viewmodel.FileAttenteViewModel;
import com.example.medcare.viewmodel.FileAttenteViewModelFactory;

public class FileAttenteFragment extends Fragment {

    private static final String TAG = "FileAttenteFragment";

    private Spinner spinnerEtab;
    private EditText nomInput, prenomInput, motifInput;
    private Button btnAjouter;
    private LinearLayout listeContainer;
    private ProgressBar loadingIndicatorFile;

    private EtablissementViewModel etablissementVM;
    private FileAttenteViewModel fileVM;

    private List<Etablissement> etablissementsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_file_attente_fragment, container, false);

        spinnerEtab = view.findViewById(R.id.spinner_etab_file);
        nomInput = view.findViewById(R.id.input_nom_client);
        prenomInput = view.findViewById(R.id.input_prenom_client);
        motifInput = view.findViewById(R.id.input_motif);
        btnAjouter = view.findViewById(R.id.btn_ajouter_client);
        listeContainer = view.findViewById(R.id.liste_file_attente);
        loadingIndicatorFile = view.findViewById(R.id.loading_indicator_file);

        EtablissementViewModelFactory etabFactory = new EtablissementViewModelFactory();
        etablissementVM = new ViewModelProvider(this, etabFactory).get(EtablissementViewModel.class);

        FileAttenteViewModelFactory fileFactory = new FileAttenteViewModelFactory();
        fileVM = new ViewModelProvider(this, fileFactory).get(FileAttenteViewModel.class);

        setupObservers();

        btnAjouter.setOnClickListener(v -> ajouterClient());

        spinnerEtab.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (!etablissementsList.isEmpty() && position >= 0 && position < etablissementsList.size()) {
                    String selectedEtabDocumentId = etablissementsList.get(position).getDocumentId();
                    if (selectedEtabDocumentId != null && !selectedEtabDocumentId.isEmpty()) {
                        afficherFile(selectedEtabDocumentId);
                    } else {
                        listeContainer.removeAllViews();
                        TextView tv = new TextView(requireContext());
                        tv.setText("ID d'établissement invalide.");
                        listeContainer.addView(tv);
                    }
                } else {
                    listeContainer.removeAllViews();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                listeContainer.removeAllViews();
            }
        });

        return view;
    }

    private void setupObservers() {
        etablissementVM.getAllEtablissementsStream().observe(getViewLifecycleOwner(), etabs -> {
            if (etabs != null) {
                etablissementsList = etabs;
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item,
                        getNomsEtablissements(etabs));
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerEtab.setAdapter(adapter);

                if(!etablissementsList.isEmpty() && spinnerEtab.getSelectedItemPosition() != AdapterView.INVALID_POSITION){
                    String initialEtabId = etablissementsList.get(0).getDocumentId();
                    if(initialEtabId != null && !initialEtabId.isEmpty()){
                        afficherFile(initialEtabId);
                    }
                }

            } else {
                etablissementsList.clear();
                spinnerEtab.setAdapter(null);
                listeContainer.removeAllViews();
                TextView tv = new TextView(requireContext());
                tv.setText("Impossible de charger les établissements.");
                listeContainer.addView(tv);
            }
        });

        fileVM.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if(loadingIndicatorFile != null) {
                loadingIndicatorFile.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
            btnAjouter.setEnabled(!isLoading);
            spinnerEtab.setEnabled(!isLoading);
        });

        fileVM.getOperationSuccessId().observe(getViewLifecycleOwner(), successId -> {
            if (successId != null && !successId.isEmpty()) {
                Toast.makeText(getContext(), "Client ajouté à la file!", Toast.LENGTH_SHORT).show();
                clearInputFields();
            }
        });

        fileVM.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Erreur: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void ajouterClient() {
        int index = spinnerEtab.getSelectedItemPosition();
        if (index == AdapterView.INVALID_POSITION || etablissementsList.isEmpty() || index >= etablissementsList.size()) {
            Toast.makeText(getContext(), "Veuillez sélectionner un établissement valide.", Toast.LENGTH_SHORT).show();
            return;
        }

        String etabDocumentId = etablissementsList.get(index).getDocumentId();
        if (etabDocumentId == null || etabDocumentId.isEmpty()){
            Toast.makeText(getContext(), "Erreur: ID établissement invalide.", Toast.LENGTH_SHORT).show();
            return;
        }

        String nom = nomInput.getText().toString().trim();
        String prenom = prenomInput.getText().toString().trim();
        String motif = motifInput.getText().toString().trim();
        String heure = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        String statut = "En attente";


        if (TextUtils.isEmpty(nom) || TextUtils.isEmpty(prenom) || TextUtils.isEmpty(motif)) {
            Toast.makeText(getContext(), "Nom, Prénom et Motif sont requis", Toast.LENGTH_SHORT).show();
            return;
        }

        FileAttente client = new FileAttente(etabDocumentId, nom, prenom, heure, motif, statut);
        fileVM.insert(client);
    }

    private void afficherFile(String etabDocumentId) {
        fileVM.getFileAttenteForEtablissementStream(etabDocumentId)
                .observe(getViewLifecycleOwner(), list -> {
                    listeContainer.removeAllViews();
                    LayoutInflater inflater = LayoutInflater.from(requireContext());

                    if (list != null) {
                        if (list.isEmpty()) {
                            TextView tv = new TextView(requireContext());
                            tv.setText("La File d'Attente est Vide.");
                            tv.setPadding(16, 8, 16, 8);
                            listeContainer.addView(tv);
                        } else {
                            for (FileAttente f : list) {
                                View itemView = inflater.inflate(R.layout.view_file_item, listeContainer, false);

                                TextView tvNomPrenom = itemView.findViewById(R.id.tv_nom_prenom);
                                TextView tvHeure = itemView.findViewById(R.id.tv_heure);
                                TextView tvMotif = itemView.findViewById(R.id.tv_motif);
                                TextView tvStatut = itemView.findViewById(R.id.tv_statut);

                                tvNomPrenom.setText(f.getPrenomPersonne() + " " + f.getNomPersonne());
                                tvHeure.setText("Heure : " + f.getHeureArrivee());
                                tvMotif.setText("Motif : " + f.getMotif());
                                tvStatut.setText("Statut : " + f.getStatut());

                                // Ajout de l'action au clic
                                itemView.setOnClickListener(v -> showOptionsForClient(f));

                                listeContainer.addView(itemView);
                            }
                        }
                    } else {
                        TextView tv = new TextView(requireContext());
                        tv.setText("Erreur au Chargement de la File d'Attente.");
                        tv.setPadding(16, 8, 16, 8);
                        listeContainer.addView(tv);
                    }
                });
    }


    private List<String> getNomsEtablissements(List<Etablissement> etabs) {
        List<String> noms = new ArrayList<>();
        if (etabs == null) return noms;
        for (Etablissement etab : etabs) {
            if(etab != null && etab.getNom() != null) {
                noms.add(etab.getNom());
            }
        }
        if (noms.isEmpty()) {
            noms.add("Aucun établissement");
        }
        return noms;
    }

    private void clearInputFields() {
        nomInput.setText("");
        prenomInput.setText("");
        motifInput.setText("");
        nomInput.requestFocus();
    }

    private void showOptionsForClient(FileAttente client) {
        String clientId = client.getDocumentId();
        String currentStatus = client.getStatut();

        if (clientId == null || clientId.isEmpty()) {
            Toast.makeText(getContext(), "ID Client invalide.", Toast.LENGTH_SHORT).show();
            return;
        }

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Options pour: " + client.getPrenomPersonne() + " " + client.getNomPersonne());

        String[] options;
        if ("En attente".equals(currentStatus)) {
            options = new String[]{"Marquer Servi", "Supprimer"};
        } else if ("Servi".equals(currentStatus)) {
            options = new String[]{"Marquer En attente", "Supprimer"};
        } else {
            options = new String[]{"Supprimer"}; // Default or other statuses
        }


        builder.setItems(options, (dialog, which) -> {
            String selectedOption = options[which];
            switch (selectedOption) {
                case "Marquer Servi":
                    client.setStatut("Servi");
                    fileVM.update(client, clientId);
                    break;
                case "Marquer En attente":
                    client.setStatut("En attente");
                    fileVM.update(client, clientId);
                    break;
                case "Supprimer":
                    fileVM.delete(clientId);
                    break;
            }
        });

        builder.setNegativeButton("Annuler", null);
        builder.show();
    }
}