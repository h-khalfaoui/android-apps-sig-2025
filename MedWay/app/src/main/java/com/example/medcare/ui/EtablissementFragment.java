package com.example.medcare.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.medcare.R;

import java.util.List;

import com.example.medcare.model.Etablissement;
import com.example.medcare.viewmodel.EtablissementViewModel;
import com.example.medcare.viewmodel.EtablissementViewModelFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class EtablissementFragment extends Fragment {

    private static final String TAG = "EtablissementFragment";

    private EditText nomInput, adresseInput, contactInput, emailInput,
            responsableInput, typeInput, siteWebInput, imageUrlInput;
    private Button ajouterBtn;
    private LinearLayout listeContainer;
    private ProgressBar loadingIndicatorEtab;

    private EtablissementViewModel viewModel;
    private String currentProfessionalId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_etablissement_fragment, container, false);

        nomInput = view.findViewById(R.id.input_nom);
        adresseInput = view.findViewById(R.id.input_adresse);
        contactInput = view.findViewById(R.id.input_contact);
        emailInput = view.findViewById(R.id.input_email);
        responsableInput = view.findViewById(R.id.input_responsable);
        typeInput = view.findViewById(R.id.input_type);
        siteWebInput = view.findViewById(R.id.input_siteweb);
        imageUrlInput = view.findViewById(R.id.input_imageurl);
        ajouterBtn = view.findViewById(R.id.btn_ajouter);
        listeContainer = view.findViewById(R.id.liste_etablissements);
        loadingIndicatorEtab = view.findViewById(R.id.loading_indicator_etab);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentProfessionalId = currentUser.getUid();
        } else {
            Log.e(TAG, "Error: Current professional user ID is null.");
            Toast.makeText(getContext(), "Erreur: Utilisateur non connecté.", Toast.LENGTH_LONG).show();

        }

        EtablissementViewModelFactory factory = new EtablissementViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(EtablissementViewModel.class);

        ajouterBtn.setOnClickListener(v -> ajouterEtablissement());

        setupObservers();

        return view;
    }

    private void setupObservers() {
        viewModel.getAllEtablissementsStream().observe(getViewLifecycleOwner(), this::afficherListe);

        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if(loadingIndicatorEtab != null) {
                loadingIndicatorEtab.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
            ajouterBtn.setEnabled(!isLoading);
        });

        viewModel.getOperationSuccessId().observe(getViewLifecycleOwner(), successId -> {
            if (successId != null && !successId.isEmpty()) {
                Toast.makeText(getContext(), "Etablissement ajouté/mis à jour!", Toast.LENGTH_SHORT).show();
                viderChamps();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Erreur: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void ajouterEtablissement() {
        if (currentProfessionalId == null) {
            Toast.makeText(getContext(), "Erreur: Non connecté.", Toast.LENGTH_SHORT).show();
            return;
        }

        String nom = nomInput.getText().toString().trim();
        String adresse = adresseInput.getText().toString().trim();
        String telephone = contactInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String responsable = responsableInput.getText().toString().trim();
        String type = typeInput.getText().toString().trim();
        String siteWeb = siteWebInput.getText().toString().trim();
        String imageUrl = imageUrlInput.getText().toString().trim();

        if (TextUtils.isEmpty(nom) || TextUtils.isEmpty(adresse) || TextUtils.isEmpty(telephone)) {
            Toast.makeText(getContext(), "Nom, Adresse et Téléphone sont obligatoires", Toast.LENGTH_SHORT).show();
            return;
        }

        Etablissement e = new Etablissement(nom, adresse, telephone, email, responsable, type, siteWeb, imageUrl, currentProfessionalId);

        viewModel.insert(e);
    }

    private void viderChamps() {
        nomInput.setText("");
        adresseInput.setText("");
        contactInput.setText("");
        emailInput.setText("");
        responsableInput.setText("");
        typeInput.setText("");
        siteWebInput.setText("");
        imageUrlInput.setText("");
        nomInput.requestFocus();
    }

    private void afficherListe(List<Etablissement> etablissements) {
        listeContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (etablissements == null || etablissements.isEmpty()) {
            TextView tv = new TextView(requireContext());
            tv.setText("Aucun Etablissement Trouvé.");
            tv.setTextSize(16f);
            tv.setPadding(24, 24, 24, 24);
            listeContainer.addView(tv);
            return;
        }

        for (Etablissement e : etablissements) {
            if (e == null) continue;

            View itemView = inflater.inflate(R.layout.view_etablissement_item, listeContainer, false);

            ImageView imageView = itemView.findViewById(R.id.img_etablissement);
            TextView nomView = itemView.findViewById(R.id.tv_nom);
            TextView typeView = itemView.findViewById(R.id.tv_type);
            TextView adresseView = itemView.findViewById(R.id.tv_adresse);

            imageView.setImageResource(R.drawable.hospital1);

            nomView.setText(e.getNom() != null ? e.getNom() : "Nom Inconnu");
            typeView.setText(e.getType() != null ? e.getType() : "Type Inconnu");
            adresseView.setText(e.getAdresse() != null ? e.getAdresse() : "Adresse Inconnue");

            listeContainer.addView(itemView);
        }
    }


}