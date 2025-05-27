package com.example.sigsignalement;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.sigsignalement.model.AppDatabase;
import com.example.sigsignalement.model.User;

public class LoginActivity extends AppCompatActivity {

    EditText editTextEmail, editTextPassword;
    Button buttonLogin, buttonGoRegister;

    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonGoRegister = findViewById(R.id.buttonGoRegister);

        // Ajout du fallbackToDestructiveMigration pour éviter crash Room
        db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "signalements_db")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries() // ✅ pour tests, à déplacer en async plus tard
                .build();

        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            User user = db.userDao().login(email, password);

            if (user != null) {
                SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("email", user.getEmail());
                editor.putString("nom", user.getNom());
                editor.putBoolean("isAdmin", user.isAdmin); // ✅ Sauvegarde du statut admin
                editor.apply();

                Toast.makeText(this, "Connexion réussie", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }


        });

        buttonGoRegister.setOnClickListener(v -> {
            // Tu peux rediriger vers RegisterActivity si tu en as une
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
}
