package com.example.quietspaceeee;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.quietspaceeee.data.model.User;
import com.example.quietspaceeee.data.viewmodel.UserViewModel;


public class RegisterActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etEmail, etPhone, etPassword, etConfirmPassword;
    private Button btnRegister;
    private ImageView ivBackToLogin;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialisation des vues
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        ivBackToLogin = findViewById(R.id.ivBackToLogin);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        ivBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        btnRegister.setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            boolean valid = true;

            // Réinitialiser les erreurs
            etFirstName.setError(null);
            etLastName.setError(null);
            etEmail.setError(null);
            etPhone.setError(null);
            etPassword.setError(null);
            etConfirmPassword.setError(null);

            if (firstName.isEmpty()) {
                etFirstName.setError("Le prénom est requis");
                valid = false;
            }

            if (lastName.isEmpty()) {
                etLastName.setError("Le nom est requis");
                valid = false;
            }

            if (email.isEmpty()) {
                etEmail.setError("L'email est requis");
                valid = false;
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Email invalide");
                valid = false;
            }

            if (phone.isEmpty()) {
                etPhone.setError("Le numéro de téléphone est requis");
                valid = false;
            } else if (!phone.matches("^(\\+212|0)([5-7])\\d{8}$")) {
                etPhone.setError("Numéro marocain invalide");
                valid = false;
            }

            if (password.isEmpty()) {
                etPassword.setError("Le mot de passe est requis");
                valid = false;
            } else if (password.length() < 6) {
                etPassword.setError("Minimum 6 caractères");
                valid = false;
            }

            if (confirmPassword.isEmpty()) {
                etConfirmPassword.setError("Veuillez confirmer le mot de passe");
                valid = false;
            } else if (!confirmPassword.equals(password)) {
                etConfirmPassword.setError("Les mots de passe ne correspondent pas");
                valid = false;
            }

            if (!valid) {
                Toast.makeText(RegisterActivity.this, "Veuillez corriger les erreurs", Toast.LENGTH_SHORT).show();
                return;
            }

            // Si tous les champs sont valides
            String fullName = firstName + " " + lastName;
            User user = new User(email, password, fullName);
            userViewModel.register(user);
        });

        userViewModel.getRegisterResult().observe(this, success -> {
            if (success) {
                Toast.makeText(RegisterActivity.this, "Inscription réussie", Toast.LENGTH_SHORT).show();
                SharedPreferences prefs = getSharedPreferences("QuietSpacePrefs", MODE_PRIVATE);
                prefs.edit().putString("userEmail", etEmail.getText().toString().trim()).apply();
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                finish();
            } else {
                etEmail.setError("Cet email est déjà utilisé");
                Toast.makeText(RegisterActivity.this, "Erreur : Email déjà utilisé", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
