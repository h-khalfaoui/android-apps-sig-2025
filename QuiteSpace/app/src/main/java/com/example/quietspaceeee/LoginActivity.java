package com.example.quietspaceeee;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quietspaceeee.data.db.UserDatabaseHelper;
import com.example.quietspaceeee.data.view.MainActivityADMIN;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etEmail, etPassword;
    private MaterialButton buttonLogin;
    private TextView buttonRegister;
    private UserDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialisation des vues
        etEmail = findViewById(R.id.editTextEmail);
        etPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);

        // Initialisation de la base de données
        dbHelper = new UserDatabaseHelper(this);

        buttonLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            } else {
                // Vérification de l'admin
                if (email.equals("admin@gmail.com") && password.equals("admin123")) {
                    startActivity(new Intent(LoginActivity.this, MainActivityADMIN.class));
                    finish();
                } else {
                    // Vérification dans la base de données SQLite
                    boolean loginSuccess = dbHelper.checkUser(email, password);

                    if (loginSuccess) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                        intent.putExtra("userEmail", email);  // Ajoute l'email
                        SharedPreferences prefs = getSharedPreferences("QuietSpacePrefs", MODE_PRIVATE);
                        prefs.edit().putString("userEmail", email).apply();
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(LoginActivity.this, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        buttonRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }
}