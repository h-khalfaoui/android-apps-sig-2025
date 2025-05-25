package com.example.quietspaceeee;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.quietspaceeee.R;
import com.example.quietspaceeee.data.db.UserDatabaseHelper;
import com.example.quietspaceeee.data.model.User;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView tvUserName, tvUserEmail, tvSelectedLanguage;
    private View languageOption, editProfile, logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());


        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        editProfile = findViewById(R.id.editProfile);
        logout = findViewById(R.id.logout);
        languageOption = findViewById(R.id.languageOption);
        tvSelectedLanguage = findViewById(R.id.tvSelectedLanguage);

        SharedPreferences prefs = getSharedPreferences("QuietSpacePrefs", MODE_PRIVATE);
        String userEmail = prefs.getString("userEmail", null);

        if (userEmail != null) {
            UserDatabaseHelper dbHelper = new UserDatabaseHelper(this);
            User user = dbHelper.getUserByEmail(userEmail);
            if (user != null) {
                tvUserName.setText(user.getName());
                tvUserEmail.setText(user.getEmail());
            }
        }

        // Aller vers l'écran d'édition
        editProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        logout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // Gestion des clics
        findViewById(R.id.changePhotoText).setOnClickListener(v -> changeProfilePhoto());
        languageOption.setOnClickListener(v -> changeLanguage());

    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("QuietSpacePrefs", MODE_PRIVATE);
        String userEmail = prefs.getString("userEmail", null);

        if (userEmail != null) {
            UserDatabaseHelper dbHelper = new UserDatabaseHelper(this);
            User user = dbHelper.getUserByEmail(userEmail);
            if (user != null) {
                tvUserName.setText(user.getName());
                tvUserEmail.setText(user.getEmail());
            }
        }

        tvSelectedLanguage.setText(getString(R.string.french));
    }

    private void changeProfilePhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    private void changeLanguage() {
        Toast.makeText(this, "Fonctionnalité langue à venir", Toast.LENGTH_SHORT).show();

        /* À décommenter quand LanguageSelectionActivity sera implémentée
        Intent intent = new Intent(this, LanguageSelectionActivity.class);
        startActivity(intent);
        */
    }

    private void editProfile() {
        Toast.makeText(this, "Édition du profil à venir", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void logoutUser() {
        Toast.makeText(this, "Déconnexion simulée", Toast.LENGTH_SHORT).show();


        SharedPreferences prefs = getSharedPreferences("QuietSpacePrefs", MODE_PRIVATE);
        prefs.edit().remove("userEmail").apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            // Mettre à jour l'image de profil
            profileImage.setImageURI(imageUri);
            // Ici vous devriez aussi sauvegarder l'image dans votre backend
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recharger les données au cas où elles auraient été modifiées
        loadUserData();
    }
}