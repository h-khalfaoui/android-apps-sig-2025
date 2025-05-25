package com.example.quietspaceeee;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.quietspaceeee.R;

public class SupportActivity extends AppCompatActivity {

    private EditText etFullName;
    private EditText etChannelId;
    private EditText etDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        // Initialisation des vues
        etFullName = findViewById(R.id.etFullName);
        etChannelId = findViewById(R.id.etChannelId);
        etDescription = findViewById(R.id.etDescription);

        // Configuration des listeners pour les options de contact
        findViewById(R.id.optionCall).setOnClickListener(v -> callSupport());
        findViewById(R.id.optionEmail).setOnClickListener(v -> emailSupport());
        findViewById(R.id.optionFAQs).setOnClickListener(v -> searchFAQs());

        // Bouton Upload Screenshot
        findViewById(R.id.btnUploadScreenshot).setOnClickListener(v -> uploadScreenshot());

        // Bouton Envoyer
        findViewById(R.id.btnSend).setOnClickListener(v -> sendSupportRequest());
    }

    private void callSupport() {
        // Implémentation de l'appel téléphonique
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:+2126345678")); // Remplacez par votre numéro de support
        startActivity(intent);
    }

    private void emailSupport() {
        // Implémentation de l'envoi d'email
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:support@quietspace.com")); // Remplacez par votre email de support
        intent.putExtra(Intent.EXTRA_SUBJECT, "Demande de support");
        startActivity(Intent.createChooser(intent, "Envoyer un email"));
    }

    private void searchFAQs() {
        // Implémentation de la recherche dans les FAQs
        Toast.makeText(this, "Ouverture des FAQs", Toast.LENGTH_SHORT).show();
        // Vous pouvez ajouter ici une Intent pour ouvrir une activité FAQ ou une page web
    }

    private void uploadScreenshot() {
        // Implémentation du téléchargement de capture d'écran
        Toast.makeText(this, "Sélectionnez une image", Toast.LENGTH_SHORT).show();

        // Créer une Intent pour sélectionner une image
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    private void sendSupportRequest() {
        // Validation et envoi du formulaire de support
        String fullName = etFullName.getText().toString().trim();
        String channelId = etChannelId.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Veuillez entrer votre nom complet");
            return;
        }

        if (description.isEmpty()) {
            etDescription.setError("Veuillez décrire votre problème");
            return;
        }

        // Ici, vous pouvez implémenter la logique pour envoyer la demande à votre backend
        // Pour l'instant, nous affichons simplement un message de confirmation
        Toast.makeText(this, "Demande envoyée avec succès", Toast.LENGTH_LONG).show();

        // Vous pourriez aussi envoyer un email avec les détails
        sendSupportEmail(fullName, channelId, description);

        // Fermer l'activité après envoi
        finish();
    }

    private void sendSupportEmail(String fullName, String channelId, String description) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:support@quietspace.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Nouvelle demande de support de " + fullName);
        intent.putExtra(Intent.EXTRA_TEXT,
                "Nom complet: " + fullName + "\n" +
                        "Channel ID: " + channelId + "\n" +
                        "Description: " + description);

        try {
            startActivity(Intent.createChooser(intent, "Envoyer la demande"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Aucune application email installée", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            // Traitez l'image sélectionnée ici
            Toast.makeText(this, "Image sélectionnée: " + imageUri.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}