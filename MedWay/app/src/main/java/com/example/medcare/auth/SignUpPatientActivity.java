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

import com.example.medcare.MainActivity;
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
import java.util.Objects;

public class SignUpPatientActivity extends AppCompatActivity {

    private static final String TAG = "SignUpPatientActivity";

    TextInputLayout layoutName, layoutEmail, layoutPassword;
    TextInputEditText editTextName, editTextEmail, editTextPassword;
    Button buttonSignUpPatient;
    ImageView backArrow;
    ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_patient);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // FindViewByIds...
        layoutName = findViewById(R.id.textFieldNamePatient);
        layoutEmail = findViewById(R.id.textFieldEmailPatient);
        layoutPassword = findViewById(R.id.textFieldPasswordPatient);
        editTextName = findViewById(R.id.editTextNamePatient);
        editTextEmail = findViewById(R.id.editTextEmailPatient);
        editTextPassword = findViewById(R.id.editTextPasswordPatient);
        buttonSignUpPatient = findViewById(R.id.buttonSignUpPatient);
        backArrow = findViewById(R.id.backArrowSignUpPatient);
        progressBar = findViewById(R.id.progressBarSignUpPatient);

        backArrow.setOnClickListener(v -> finish());
        buttonSignUpPatient.setOnClickListener(v -> attemptSignUpPatient());
    }

    private void attemptSignUpPatient() {

        layoutName.setError(null);
        layoutEmail.setError(null);
        layoutPassword.setError(null);

        String name = Objects.requireNonNull(editTextName.getText()).toString().trim();
        String email = Objects.requireNonNull(editTextEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(editTextPassword.getText()).toString().trim();

        boolean isValid = true;
        if (name.isEmpty()) { layoutName.setError("Name required"); isValid = false; }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) { layoutEmail.setError("Valid email required"); isValid = false; }
        if (password.isEmpty() || password.length() < 6) { layoutPassword.setError("Password min 6 chars required"); isValid = false; }

        if (!isValid) return;

        showLoading(true);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success (Patient)");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            savePatientProfile(user, name);
                        } else {
                            showLoading(false);
                            Toast.makeText(SignUpPatientActivity.this, "Signup failed: User data unavailable.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        showLoading(false);
                        Log.w(TAG, "createUserWithEmail:failure (Patient)", task.getException());
                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch (FirebaseAuthUserCollisionException e) {
                            layoutEmail.setError("Email address already in use.");
                        } catch (Exception e) {
                            Toast.makeText(SignUpPatientActivity.this, "Sign up failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void savePatientProfile(FirebaseUser user, String name) {
        String uid = user.getUid();
        String email = user.getEmail();

        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("uid", uid);
        userProfile.put("name", name);
        userProfile.put("email", email);
        // Set role and status correctly for a patient
        userProfile.put("role", "patient");
        userProfile.put("status", "active");
        userProfile.put("createdAt", FieldValue.serverTimestamp());

        db.collection("users").document(uid).set(userProfile)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Patient profile created in Firestore for " + uid);
                    showLoading(false);
                    Toast.makeText(SignUpPatientActivity.this, "Sign up successful!", Toast.LENGTH_LONG).show();

                    // Navigate to the shared MainActivity after signup
                    Intent intent = new Intent(SignUpPatientActivity.this, MainActivity.class);

                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); // Finish signup activity
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.w(TAG, "Error writing patient profile", e);
                    Toast.makeText(SignUpPatientActivity.this, "Failed to save profile data.", Toast.LENGTH_SHORT).show();
                    // Consider deleting the auth user if profile save fails
                    // if (user != null) user.delete(); // Use with caution
                });
    }

    private void showLoading(boolean isLoading) {

        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        buttonSignUpPatient.setEnabled(!isLoading);
        editTextName.setEnabled(!isLoading);
        editTextEmail.setEnabled(!isLoading);
        editTextPassword.setEnabled(!isLoading);
    }
}