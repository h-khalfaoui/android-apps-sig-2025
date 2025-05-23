package com.example.medcare.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.medcare.R;
import com.example.medcare.model.MedicalRecord;
import com.google.firebase.firestore.FirebaseFirestore;

public class MedicalRecordFragment extends Fragment {

    private EditText editNom, editPrenom, editDateNaissance, editSexe, editGroupeSanguin,
            editAllergies, editMaladiesChroniques, editChirurgies, editHospitalisations,
            editMedications, editDosage, editAnalyses, editImageries;
    private Button btnSave, btnModifier, btnSupprimer;
    private LinearLayout formLayout, ficheLayout;
    private TextView ficheText;
    private FirebaseFirestore db;
    private String documentId = "patient_record";

    public MedicalRecordFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medical_record, container, false);

        db = FirebaseFirestore.getInstance();

        // Champs de formulaire
        editNom = view.findViewById(R.id.editNom);
        editPrenom = view.findViewById(R.id.editPrenom);
        editDateNaissance = view.findViewById(R.id.editDateNaissance);
        editSexe = view.findViewById(R.id.editSexe);
        editGroupeSanguin = view.findViewById(R.id.editGroupeSanguin);
        editAllergies = view.findViewById(R.id.editAllergies);
        editMaladiesChroniques = view.findViewById(R.id.editMaladiesChroniques);
        editChirurgies = view.findViewById(R.id.editChirurgies);
        editHospitalisations = view.findViewById(R.id.editHospitalisations);
        editMedications = view.findViewById(R.id.editMedications);
        editDosage = view.findViewById(R.id.editDosage);
        editAnalyses = view.findViewById(R.id.editAnalyses);
        editImageries = view.findViewById(R.id.editImageries);

        // Layouts et boutons
        formLayout = view.findViewById(R.id.formLayout);
        ficheLayout = view.findViewById(R.id.ficheLayout);
        ficheText = view.findViewById(R.id.ficheText);
        btnSave = view.findViewById(R.id.btnSave);
        btnModifier = view.findViewById(R.id.btnModifier);
        btnSupprimer = view.findViewById(R.id.btnSupprimer);

        loadMedicalRecord();

        btnSave.setOnClickListener(v -> {
            MedicalRecord record = new MedicalRecord(
                    documentId,
                    editNom.getText().toString(),
                    editPrenom.getText().toString(),
                    editDateNaissance.getText().toString(),
                    editSexe.getText().toString(),
                    editGroupeSanguin.getText().toString(),
                    editAllergies.getText().toString(),
                    editMaladiesChroniques.getText().toString(),
                    editChirurgies.getText().toString(),
                    editHospitalisations.getText().toString(),
                    editMedications.getText().toString(),
                    editDosage.getText().toString(),
                    editAnalyses.getText().toString(),
                    editImageries.getText().toString()
            );

            db.collection("medical_records").document(documentId)
                    .set(record)
                    .addOnSuccessListener(unused -> {
                        showFiche(record);
                    });
        });

        btnModifier.setOnClickListener(v -> {
            formLayout.setVisibility(View.VISIBLE);
            ficheLayout.setVisibility(View.GONE);
        });

        btnSupprimer.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Confirmation de suppression")
                    .setMessage("Êtes-vous sûr de vouloir supprimer ce dossier médical ? Cette action est irréversible.")
                    .setPositiveButton("Oui", (dialog, which) -> {
                        db.collection("medical_records").document(documentId).delete()
                                .addOnSuccessListener(unused -> {
                                    formLayout.setVisibility(View.VISIBLE);
                                    ficheLayout.setVisibility(View.GONE);

                                    // Vider tous les champs du formulaire
                                    editNom.setText("");
                                    editPrenom.setText("");
                                    editDateNaissance.setText("");
                                    editSexe.setText("");
                                    editGroupeSanguin.setText("");
                                    editAllergies.setText("");
                                    editMaladiesChroniques.setText("");
                                    editChirurgies.setText("");
                                    editHospitalisations.setText("");
                                    editMedications.setText("");
                                    editDosage.setText("");
                                    editAnalyses.setText("");
                                    editImageries.setText("");
                                });
                    })
                    .setNegativeButton("Non", null)
                    .show();
        });


        return view;
    }

    private void loadMedicalRecord() {
        db.collection("medical_records").document(documentId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        MedicalRecord record = doc.toObject(MedicalRecord.class);
                        if (record != null) {
                            showFiche(record);
                        }
                    }
                });
    }

    private void showFiche(MedicalRecord record) {
        String text = "Nom : " + record.getNom() + "\n"
                + "Prénom : " + record.getPrenom() + "\n"
                + "Date de naissance : " + record.getDateNaissance() + "\n"
                + "Sexe : " + record.getSexe() + "\n"
                + "Groupe sanguin : " + record.getGroupeSanguin() + "\n"
                + "Allergies : " + record.getAllergies() + "\n"
                + "Maladies chroniques : " + record.getMaladiesChroniques() + "\n"
                + "Chirurgies : " + record.getChirurgies() + "\n"
                + "Hospitalisations : " + record.getHospitalisations() + "\n"
                + "Médicaments : " + record.getMedicaments() + "\n"
                + "Dosage : " + record.getDosage() + "\n"
                + "Analyses : " + record.getAnalyses() + "\n"
                + "Imageries : " + record.getImageries();

        ficheText.setText(text);

        formLayout.setVisibility(View.GONE);
        ficheLayout.setVisibility(View.VISIBLE);
    }
}
