package com.example.medcare.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.medcare.R;
import com.example.medcare.auth.SignInActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfilFragment extends Fragment {

    private TextInputEditText editName, editEmail, editPassword, editLicense, editSpecialty;
    private ProgressBar progressBar;
    private Button buttonUpdate, logoutButton, buttonGiveFeedback;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_professional_profile, container, false);

        // Initialisation des vues
        editName = view.findViewById(R.id.editTextProfileName);
        editEmail = view.findViewById(R.id.editTextProfileEmail);
        editPassword = view.findViewById(R.id.editTextProfilePassword);
        editLicense = view.findViewById(R.id.editTextProfileLicense);
        editSpecialty = view.findViewById(R.id.editTextProfileSpecialty);
        progressBar = view.findViewById(R.id.progressBarProfile);
        buttonUpdate = view.findViewById(R.id.buttonUpdateProfile);
        logoutButton = view.findViewById(R.id.logoutButton);
        buttonGiveFeedback = view.findViewById(R.id.buttonGiveFeedback);


        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadProfile();

        buttonUpdate.setOnClickListener(v -> updateProfile());

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        // Action du bouton "Donner un avis"
        buttonGiveFeedback.setOnClickListener(v -> {
            new AvisBottomSheetDialog().show(getParentFragmentManager(), "AvisDialog");
        });

        return view;
    }

    private void loadProfile() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        editEmail.setText(user.getEmail());
        editPassword.setText("********");

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        editName.setText(documentSnapshot.getString("name"));
                        editLicense.setText(documentSnapshot.getString("licenseNumber"));
                        editSpecialty.setText(documentSnapshot.getString("specialty"));
                    }
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Échec du chargement", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void updateProfile() {
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String license = editLicense.getText().toString().trim();
        String specialty = editSpecialty.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(license) || TextUtils.isEmpty(specialty)) {
            Toast.makeText(getContext(), "Tous les champs sont requis", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        progressBar.setVisibility(View.VISIBLE);

        user.updateEmail(email).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(getContext(), "Échec mise à jour email", Toast.LENGTH_SHORT).show();
            }
        });

        user.updatePassword(password).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(getContext(), "Échec mise à jour mot de passe", Toast.LENGTH_SHORT).show();
            }
        });

        String uid = user.getUid();
        db.collection("users").document(uid).update(
                "name", name,
                "licenseNumber", license,
                "specialty", specialty,
                "email", email
        ).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Profil mis à jour", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Échec de la mise à jour", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        });
    }
}
