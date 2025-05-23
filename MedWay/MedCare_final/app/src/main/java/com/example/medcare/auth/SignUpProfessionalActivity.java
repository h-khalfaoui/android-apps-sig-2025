package com.example.medcare.auth;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.medcare.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class SignUpProfessionalActivity extends AppCompatActivity {

    private static final String TAG = "SignUpProfActivity";

    TextInputLayout layoutName, layoutEmail, layoutPassword, layoutLicense, layoutSpecialty;
    TextInputEditText editTextName, editTextEmail, editTextPassword, editTextLicense, editTextSpecialty;
    Button buttonSignUpProf;
    ImageView backArrow;
    ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_professional);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        layoutName = findViewById(R.id.textFieldNameProf);
        layoutEmail = findViewById(R.id.textFieldEmailProf);
        layoutPassword = findViewById(R.id.textFieldPasswordProf);
        layoutLicense = findViewById(R.id.textFieldLicenseNumber);
        layoutSpecialty = findViewById(R.id.textFieldSpecialty);
        editTextName = findViewById(R.id.editTextNameProf);
        editTextEmail = findViewById(R.id.editTextEmailProf);
        editTextPassword = findViewById(R.id.editTextPasswordProf);
        editTextLicense = findViewById(R.id.editTextLicenseNumber);
        editTextSpecialty = findViewById(R.id.editTextSpecialty);
        buttonSignUpProf = findViewById(R.id.buttonSignUpProf);
        backArrow = findViewById(R.id.backArrowSignUpProf);
        progressBar = findViewById(R.id.progressBarSignUpProf);

        backArrow.setOnClickListener(v -> finish());
        buttonSignUpProf.setOnClickListener(v -> attemptSignUpProfessional());
    }

    private void attemptSignUpProfessional() {
        layoutName.setError(null);
        layoutEmail.setError(null);
        layoutPassword.setError(null);
        layoutLicense.setError(null);
        layoutSpecialty.setError(null);

        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String license = editTextLicense.getText().toString().trim();
        String specialty = editTextSpecialty.getText().toString().trim();

        boolean isValid = true;
        if (name.isEmpty()) { layoutName.setError("Name required"); isValid = false; }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) { layoutEmail.setError("Valid email required"); isValid = false; }
        if (password.isEmpty() || password.length() < 6) { layoutPassword.setError("Password min 6 chars required"); isValid = false; }
        if (license.isEmpty()) { layoutLicense.setError("License number required"); isValid = false; }
        if (specialty.isEmpty()) { layoutSpecialty.setError("Specialty required"); isValid = false; }

        if (!isValid) return;

        showLoading(true);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveProfessionalProfile(user, name, license, specialty);
                        } else {
                            showLoading(false);
                            Toast.makeText(SignUpProfessionalActivity.this, "Signup failed: User data unavailable.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        showLoading(false);
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthUserCollisionException e) {
                            layoutEmail.setError("Email address already in use.");
                        } catch (Exception e) {
                            Toast.makeText(SignUpProfessionalActivity.this, "Sign up failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveProfessionalProfile(FirebaseUser user, String name, String license, String specialty) {
        String uid = user.getUid();
        String email = user.getEmail();

        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("uid", uid);
        userProfile.put("name", name);
        userProfile.put("email", email);
        userProfile.put("role", "pending_professional");
        userProfile.put("status", "pending");
        userProfile.put("licenseNumber", license);
        userProfile.put("specialty", specialty);
        userProfile.put("createdAt", FieldValue.serverTimestamp());

        db.collection("users").document(uid).set(userProfile)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User profile created in Firestore for " + uid);
                    // Navigate after profile save is successful
                    showLoading(false);
                    Toast.makeText(SignUpProfessionalActivity.this, "Sign up successful. Awaiting verification.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(SignUpProfessionalActivity.this, SignInActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.w(TAG, "Error writing user profile", e);
                    Toast.makeText(SignUpProfessionalActivity.this, "Failed to save profile.", Toast.LENGTH_SHORT).show();

                });
    }



    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        buttonSignUpProf.setEnabled(!isLoading);
        editTextName.setEnabled(!isLoading);
        editTextEmail.setEnabled(!isLoading);
        editTextPassword.setEnabled(!isLoading);
        editTextLicense.setEnabled(!isLoading);
        editTextSpecialty.setEnabled(!isLoading);
    }
}