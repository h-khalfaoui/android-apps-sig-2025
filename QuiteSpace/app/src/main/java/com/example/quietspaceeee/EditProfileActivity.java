package com.example.quietspaceeee;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.quietspaceeee.data.db.UserDatabaseHelper;
import com.example.quietspaceeee.data.model.User;
import com.google.android.material.textfield.TextInputEditText;
import com.example.quietspaceeee.R;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView ivProfile, ivBack;
    private String userEmail;

    private TextInputEditText  etName, etEmail, etPassword;
    private Button btnSave;
    private UserDatabaseHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);


        // Initialisation des vues
        ivProfile = findViewById(R.id.ivProfile);
        ivBack = findViewById(R.id.ivBack);
        etName = findViewById(R.id.etName);
        etPassword = findViewById(R.id.etPassword);
        etEmail = findViewById(R.id.etEmail);
        btnSave = findViewById(R.id.btnSave);


        SharedPreferences prefs = getSharedPreferences("QuietSpacePrefs", MODE_PRIVATE);
        userEmail = prefs.getString("userEmail", null);
        dbHelper = new UserDatabaseHelper(this);
        User user = dbHelper.getUserByEmail(userEmail);
        if (user != null) {
            etName.setText(user.getName());
            etEmail.setText(userEmail);
            etPassword.setText(user.getPassword());
        }

        btnSave.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            String newPass = etPassword.getText().toString().trim();

            dbHelper.updateUser(userEmail, newName, newPass);
            Toast.makeText(this, "Profil mis à jour", Toast.LENGTH_SHORT).show();
            finish();
            });


        // Charger les données actuelles du profil
        loadProfileData();

        // Gestion du clic sur le bouton retour
        ivBack.setOnClickListener(v -> finish());

        // Gestion du changement de photo
        findViewById(R.id.ivProfile).setOnClickListener(v -> changeProfilePhoto());


    }

    private void loadProfileData() {
        // Ici vous devriez charger les données réelles depuis votre source de données
        // Pour l'exemple, nous utilisons des valeurs statiques



        // Charger la photo de profil si elle existe
        // Glide.with(this).load(profileImageUrl).into(ivProfile);
    }

    private void changeProfilePhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            // Mettre à jour l'image de profil
            ivProfile.setImageURI(imageUri);
            // Ici vous devriez aussi sauvegarder l'image dans votre backend
        }
    }
}