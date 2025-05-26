package com.example.projetdevmobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ActiviteInscription extends AppCompatActivity {

    EditText champEmail, champMdp;
    Button boutonInscription;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activite_inscription);

        champEmail = findViewById(R.id.champ_email);
        champMdp = findViewById(R.id.champ_mdp);
        boutonInscription = findViewById(R.id.bouton_inscription);
        auth = FirebaseAuth.getInstance();

        boutonInscription.setOnClickListener(v -> {
            String email = champEmail.getText().toString().trim();
            String mdp = champMdp.getText().toString().trim();

            auth.createUserWithEmailAndPassword(email, mdp)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser utilisateur = auth.getCurrentUser();
                            if (utilisateur != null) {
                                utilisateur.sendEmailVerification()
                                        .addOnCompleteListener(verificationTask -> {
                                            if (verificationTask.isSuccessful()) {
                                                Toast.makeText(this, "Vérifiez votre e-mail puis connectez-vous à votre compte", Toast.LENGTH_LONG).show();
                                                auth.signOut();
                                                startActivity(new Intent(this, ActiviteConnexion.class));
                                                finish();
                                            } else {
                                                Toast.makeText(this, "Échec de l'envoi de l'e-mail", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(this, "Échec de l’inscription", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
