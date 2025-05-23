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
import android.widget.TextView;
import android.widget.Toast;

import com.example.medcare.admin.AdminDashboardActivity;
import com.example.medcare.MainActivity;
import com.example.medcare.admin.PendingVerificationActivity;
import com.example.medcare.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
// Firestore classes
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;


public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";

    // --- UI Elements ---
    TextInputLayout layoutEmail, layoutPassword;
    TextInputEditText editTextEmail, editTextPassword;
    Button buttonSignIn;
    TextView textViewForgotPassword;
    ImageView backArrow;
    ProgressBar progressBar;
    TextView textViewSignUpPatientLink, textViewSignUpProfessionalLink;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // --- Find Views By ID ---
        layoutEmail = findViewById(R.id.textFieldEmailSignIn);
        layoutPassword = findViewById(R.id.textFieldPasswordSignIn);
        editTextEmail = findViewById(R.id.editTextEmailSignIn);
        editTextPassword = findViewById(R.id.editTextPasswordSignIn);
        buttonSignIn = findViewById(R.id.buttonSignIn);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword);
        backArrow = findViewById(R.id.backArrowSignIn);
        progressBar = findViewById(R.id.progressBarSignIn);
        textViewSignUpPatientLink = findViewById(R.id.textViewSignUpPatientLink);
        textViewSignUpProfessionalLink = findViewById(R.id.textViewSignUpProfessionalLink);


        // --- Set Listeners ---
        backArrow.setOnClickListener(v -> finish());
        buttonSignIn.setOnClickListener(v -> attemptSignIn());
        textViewForgotPassword.setOnClickListener(v -> handleForgotPassword());

        // --- Set listeners for NEW links ---
        if (textViewSignUpPatientLink != null) {
            textViewSignUpPatientLink.setOnClickListener(v -> {
                Intent intent = new Intent(SignInActivity.this, SignUpPatientActivity.class);
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "textViewSignUpPatientLink not found in layout!");
        }


        if (textViewSignUpProfessionalLink != null) {
            textViewSignUpProfessionalLink.setOnClickListener(v -> {
                Intent intent = new Intent(SignInActivity.this, SignUpProfessionalActivity.class);
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "textViewSignUpProfessionalLink not found in layout!");
        }

    }

    // attemptSignIn method
    private void attemptSignIn() {
        layoutEmail.setError(null);
        layoutPassword.setError(null);
        String email = Objects.requireNonNull(editTextEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(editTextPassword.getText()).toString().trim();
        boolean isValid = true;
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError("Enter a valid email");
            isValid = false;
        }
        if (password.isEmpty()) {
            layoutPassword.setError("Password is required");
            isValid = false;
        }
        if (!isValid) return;

        showLoading(true);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkUserRoleAndNavigateFromFirestore(user.getUid());
                        } else {
                            showLoading(false);
                            Log.e(TAG, "signInWithEmail success but user is null");
                            Toast.makeText(SignInActivity.this, "Login failed: User data unavailable.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        showLoading(false);
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        try {
                            // Check for null exception before throwing, although task failure implies non-null usually
                            throw Objects.requireNonNull(task.getException());
                        } catch(FirebaseAuthInvalidUserException e) {
                            layoutEmail.setError("No account found with this email.");
                        } catch(FirebaseAuthInvalidCredentialsException e) {
                            layoutPassword.setError("Incorrect password.");
                        } catch(Exception e) { // Catch specific or base Exception
                            Toast.makeText(SignInActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Checks Firestore for role and status, navigating accordingly
    private void checkUserRoleAndNavigateFromFirestore(String userId) {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnCompleteListener(task -> {
            showLoading(false);
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    String userRole = document.getString("role");
                    String userStatus = document.getString("status");
                    Log.d(TAG, "User data from Firestore: role=" + userRole + ", status=" + userStatus);

                    Intent intent = null; // Initialize intent

                    if ("admin".equals(userRole)) {
                        intent = new Intent(SignInActivity.this, AdminDashboardActivity.class);
                    } else if ("professional".equals(userRole)) {
                        if("approved".equals(userStatus)){
                            // *** Navigate approved professional to MainActivity ***
                            intent = new Intent(SignInActivity.this, MainActivity.class);
                        } else {
                            // Handle professional role but wrong status
                            Log.w(TAG, "Professional user " + userId + " has incorrect status: " + userStatus + ". Signing out.");
                            Toast.makeText(SignInActivity.this, "Account status error.", Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                            // intent remains null
                        }
                    } else if ("pending_professional".equals(userRole)) {
                        if ("pending".equals(userStatus)) {
                            intent = new Intent(SignInActivity.this, PendingVerificationActivity.class);
                        } else if ("rejected".equals(userStatus)) {
                            // Professional was rejected - show message and stay on Sign In
                            Toast.makeText(SignInActivity.this, "Your professional application was rejected.", Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                            // intent remains null
                        } else {
                            // Handle pending role but wrong status
                            Log.w(TAG, "Pending professional user " + userId + " has unexpected status: " + userStatus + ". Signing out.");
                            Toast.makeText(SignInActivity.this, "Account status error.", Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                            // intent remains null
                        }
                    } else if ("patient".equals(userRole)) {
                        // *** Navigate patient to MainActivity ***
                        intent = new Intent(SignInActivity.this, MainActivity.class);
                    }

                    // Navigate if an intent was determined
                    if (intent != null) {
                        Log.d(TAG,"Navigating user to: " + intent.getComponent().getShortClassName());
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish(); // Finish SignInActivity
                    } else if (mAuth.getCurrentUser() != null && !"rejected".equals(userStatus)){
                        // If no intent created BUT user wasn't rejected, it's an unexpected state
                        Log.e(TAG, "Failed to determine navigation destination for role: " + userRole + ", status: " + userStatus + ". Signing out.");
                        Toast.makeText(SignInActivity.this, "Login failed: Account configuration error.", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                    }
                    // If status is 'rejected', no intent is set and user stays on SignIn after Toast shown

                } else {
                    // Firestore document doesn't exist
                    Log.e(TAG, "Firestore document not found for user: " + userId);
                    Toast.makeText(SignInActivity.this, "Login failed: User profile not found.", Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                }
            } else {
                // Firestore task failed
                Log.e(TAG, "Firestore get failed for user: " + userId, task.getException());
                Toast.makeText(SignInActivity.this, "Login failed: Could not retrieve user data.", Toast.LENGTH_LONG).show();
                mAuth.signOut();
            }
        });
    }

    // handleForgotPassword method remains the same...
    private void handleForgotPassword() {

        String email = Objects.requireNonNull(editTextEmail.getText()).toString().trim();
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError("Enter your registered email first");
            return;
        }
        layoutEmail.setError(null);
        showLoading(true);
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(SignInActivity.this, "Password reset email sent to " + email, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(SignInActivity.this, "Failed to send reset email.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    // showLoading method remains the same (including enabling/disabling new TextViews)
    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        buttonSignIn.setEnabled(!isLoading);
        editTextEmail.setEnabled(!isLoading);
        editTextPassword.setEnabled(!isLoading);
        textViewForgotPassword.setEnabled(!isLoading);
        if (textViewSignUpPatientLink != null) textViewSignUpPatientLink.setEnabled(!isLoading);
        if (textViewSignUpProfessionalLink != null) textViewSignUpProfessionalLink.setEnabled(!isLoading);
    }
}