package com.example.saydaliyati.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.saydaliyati.Database.AppDatabase;
import com.example.saydaliyati.Database.AuthorityDAO;
import com.example.saydaliyati.Models.Authority;
import com.example.saydaliyati.R;
import com.example.saydaliyati.Utils.DateUtils;
import com.example.saydaliyati.Utils.SecurityUtils;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AuthorityLoginActivity extends BaseActivity {

    private TextInputLayout usernameLayout, passwordLayout;
    private EditText usernameEditText, passwordEditText;
    private Button loginButton;
    private ProgressBar progressBar;
    private TextView forgotPasswordText;

    private AuthorityDAO authorityDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authority_login);

        // Initialize views
        usernameLayout = findViewById(R.id.usernameLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);

        // Get DAO instance
        authorityDAO = AppDatabase.getInstance(this).authorityDAO();

        // Check if already authenticated
        if (SecurityUtils.isAuthenticated(this)) {
            navigateToDashboard();
            return;
        }

        // Setup default admin user if database is empty (runs in background)
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (authorityDAO.getAllAuthorities().isEmpty()) {
                String passwordHash = SecurityUtils.hashPassword("admin123");
                Authority admin = new Authority("admin", passwordHash, "ADMIN");
                authorityDAO.insert(admin);
            }
        });

        // Setup click listeners
        loginButton.setOnClickListener(v -> attemptLogin());

        forgotPasswordText.setOnClickListener(v -> {

            Toast.makeText(this, "Default credentials: admin / admin123",
                    Toast.LENGTH_LONG).show();
        });
    }

    private void attemptLogin() {
        // Reset errors
        usernameLayout.setError(null);
        passwordLayout.setError(null);

        // Get input values
        final String username = usernameEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString().trim();

        // Validate inputs
        boolean cancel = false;
        View focusView = null;

        // Check for a valid password
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError(getString(R.string.error_field_required));
            focusView = passwordEditText;
            cancel = true;
        } else if (password.length() < 4) {
            passwordLayout.setError(getString(R.string.error_invalid_password));
            focusView = passwordEditText;
            cancel = true;
        }

        // Check for a valid username
        if (TextUtils.isEmpty(username)) {
            usernameLayout.setError(getString(R.string.error_field_required));
            focusView = usernameEditText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; focus the first form field with an error
            focusView.requestFocus();
        } else {
            // Show progress spinner and perform the login attempt
            showProgress(true);

            // Use background thread for database operations
            AppDatabase.databaseWriteExecutor.execute(() -> {
                // Find user by username
                Authority authority = authorityDAO.findByUsername(username);

                if (authority != null && SecurityUtils.verifyPassword(password, authority.getPasswordHash())) {
                    // Generate and save auth token
                    String authToken = SecurityUtils.generateToken();
                    SecurityUtils.saveAuthInfo(
                            AuthorityLoginActivity.this,
                            authority.getId(),
                            authority.getUsername(),
                            authToken
                    );

                    // Update last login timestamp
                    String currentDateTime = new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    authority.setLastLoginDate(currentDateTime);
                    authorityDAO.update(authority);

                    // Return to UI thread
                    runOnUiThread(() -> {
                        showProgress(false);
                        Toast.makeText(AuthorityLoginActivity.this,
                                "Login successful", Toast.LENGTH_SHORT).show();
                        navigateToDashboard();
                    });
                } else {
                    // Return to UI thread
                    runOnUiThread(() -> {
                        showProgress(false);
                        passwordLayout.setError(getString(R.string.error_incorrect_credentials));
                        passwordEditText.requestFocus();
                    });
                }
            });
        }
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(this, AuthorityDashboardActivity.class);
        startActivity(intent);
        finish(); // Close login activity
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!show);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // If user manually navigates back to login activity while authenticated,
        // clear the authentication (logout)
        if (SecurityUtils.isAuthenticated(this)) {
            SecurityUtils.clearAuthInfo(this);
        }
    }
}