package com.example.geotourisme;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SuggestChangeActivity extends AppCompatActivity {

    CheckBox cbContact, cbDescription, cbDoublon, cbFerme, cbLocalisation,
            cbPhoto, cbNexistePlus, cbNomModifie, cbAutre;
    EditText editOther;
    Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggest_change);

        // Initialiser tous les éléments
        cbContact = findViewById(R.id.cb_contact);
        cbDescription = findViewById(R.id.cb_description);
        cbDoublon = findViewById(R.id.cb_doublon);
        cbFerme = findViewById(R.id.cb_ferme);
        cbLocalisation = findViewById(R.id.cb_localisation);
        cbPhoto = findViewById(R.id.cb_photo);
        cbNexistePlus = findViewById(R.id.cb_nexisteplus);
        cbNomModifie = findViewById(R.id.cb_nommodifie);
        cbAutre = findViewById(R.id.cb_autre);
        editOther = findViewById(R.id.editOther);
        btnSend = findViewById(R.id.btn_send);

        // Gérer la visibilité du champ "autre"
        cbAutre.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                editOther.setVisibility(View.VISIBLE);
            } else {
                editOther.setVisibility(View.GONE);
            }
        });

        btnSend.setOnClickListener(v -> {
            StringBuilder suggestions = new StringBuilder("Suggestions:\n");

            if (cbContact.isChecked()) suggestions.append("- Contact modifié\n");
            if (cbDescription.isChecked()) suggestions.append("- Description inexacte\n");
            if (cbDoublon.isChecked()) suggestions.append("- Doublon\n");
            if (cbFerme.isChecked()) suggestions.append("- Fermé temporairement\n");
            if (cbLocalisation.isChecked()) suggestions.append("- Mauvaise localisation\n");
            if (cbPhoto.isChecked()) suggestions.append("- Mauvaise photo\n");
            if (cbNexistePlus.isChecked()) suggestions.append("- N'existe plus\n");
            if (cbNomModifie.isChecked()) suggestions.append("- Nom modifié/inaccessible\n");
            if (cbAutre.isChecked()) {
                String otherText = editOther.getText().toString().trim();
                if (!otherText.isEmpty()) {
                    suggestions.append("- Autre : ").append(otherText).append("\n");
                } else {
                    editOther.setError("Merci de préciser");
                    return;
                }
            }

            // Intent pour envoi par e-mail
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:")); // Obligatoire pour que seuls les clients email s’ouvrent
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"douaseman@gmail.com"});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Suggestion de modification");
            emailIntent.putExtra(Intent.EXTRA_TEXT, suggestions.toString());

            // Vérifier s’il existe une application pour gérer l’intent
            if (emailIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(emailIntent);
            } else {
                Toast.makeText(this, "Aucune application de messagerie trouvée", Toast.LENGTH_SHORT).show();
            }

            // Fin de l'activité
            finish();
        });

    }
}
