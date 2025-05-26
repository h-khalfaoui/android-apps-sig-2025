package com.example.sigsignalement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.sigsignalement.model.AppDatabase;
import com.example.sigsignalement.model.User;

public class RegisterActivity extends AppCompatActivity {

    EditText editTextEmail, editTextPassword;
    CheckBox checkBoxAdmin; // ✅ Ajouté
    Button buttonRegister;

    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        checkBoxAdmin = findViewById(R.id.checkBoxAdmin); // ✅ Lier la CheckBox
        buttonRegister = findViewById(R.id.buttonRegister);

        db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "signalements_db")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        buttonRegister.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            boolean isAdmin = checkBoxAdmin.isChecked(); // ✅ Lire la valeur cochée

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Tous les champs sont obligatoires", Toast.LENGTH_SHORT).show();
                return;
            }

            if (db.userDao().findByEmail(email) != null) {
                Toast.makeText(this, "Cet email est déjà utilisé", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Créer l'utilisateur avec admin ou pas
            User newUser = new User(email, password);
            newUser.isAdmin = isAdmin;

            db.userDao().insert(newUser);
            Toast.makeText(this, "Inscription réussie", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }
}
