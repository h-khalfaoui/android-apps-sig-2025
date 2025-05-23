package com.example.medcare.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.medcare.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AvisBottomSheetDialog extends BottomSheetDialogFragment {

    private RatingBar ratingBar;
    private TextInputEditText editTextComment;
    private Button buttonSend;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_avis, container, false);

        ratingBar = view.findViewById(R.id.ratingBarAvis);
        editTextComment = view.findViewById(R.id.editTextCommentAvis);
        buttonSend = view.findViewById(R.id.buttonEnvoyerAvis);

        buttonSend.setOnClickListener(v -> envoyerAvis());

        return view;
    }

    private void envoyerAvis() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Veuillez vous connecter pour envoyer un avis", Toast.LENGTH_SHORT).show();
            return;
        }

        float rating = ratingBar.getRating();
        String commentaire = editTextComment.getText().toString().trim();

        if (rating == 0.0f) {
            Toast.makeText(getContext(), "Veuillez donner une note", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(commentaire)) {
            Toast.makeText(getContext(), "Veuillez saisir un commentaire", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> avis = new HashMap<>();
        avis.put("rating", rating);
        avis.put("commentaire", commentaire);
        avis.put("userId", user.getUid());
        avis.put("timestamp", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance().collection("avis")
                .add(avis)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Avis envoyé avec succès", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur lors de l'envoi : " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
