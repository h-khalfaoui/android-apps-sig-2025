package com.example.geotourisme;



import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.geotourisme.data.db.AppDatabase;
import com.example.geotourisme.data.db.UserDao;
import com.example.geotourisme.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class signUpActivity extends AppCompatActivity {
    private TextInputLayout nameContainer;
    private EditText emailInput, passwordInput, nameInput;
    private MaterialButton loginButton, switchModeButton;
    private boolean isLoginMode = true;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        if (prefs.getBoolean("logged_in", false)) {
            // Already signed up/logged in → go straight to MainActivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_sign_up);
        // Initialisation des vues
        nameContainer = findViewById(R.id.nameContainer);
        nameInput = findViewById(R.id.NameInput);
        emailInput = findViewById(R.id.EmailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        switchModeButton = findViewById(R.id.switchModeButton);

        setupButtonListeners();
    }

    private void setupButtonListeners() {
        switchModeButton.setOnClickListener(v -> toggleMode());
        loginButton.setOnClickListener(v -> handleAuth());
    }

    private void toggleMode() {
        isLoginMode = !isLoginMode;
        nameContainer.setVisibility(isLoginMode ? View.GONE : View.VISIBLE);
        loginButton.setText(isLoginMode ? R.string.login : R.string.signup);
        switchModeButton.setText(isLoginMode ? R.string.switch_to_signup : R.string.switch_to_login);
        updateButtonIcons();
        clearForm();
        hideKeyboard();
    }

    private void updateButtonIcons() {
        int iconRes = isLoginMode ? R.drawable.ic_user : R.drawable.ic_lock;
        loginButton.setIcon(ContextCompat.getDrawable(this, iconRes));
    }

    private void handleAuth() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String name = nameInput.getText().toString().trim();

        if (!validateInputs(email, password, name)) return;

        executor.execute(() -> processAuth(email, password, name));
    }

    private boolean validateInputs(String email, String password, String name) {
        emailInput.setError(null);
        passwordInput.setError(null);
        if (email.isEmpty() || password.isEmpty() || (!isLoginMode && name.isEmpty())) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isValidEmail(email)) {
            emailInput.setError(getString(R.string.invalid_email));
            return false;
        }

        if (!isValidPassword(password)) {
            passwordInput.setError(getString(R.string.weak_password));
            return false;
        }

        return true;
    }

    private void processAuth(String email, String password, String name) {
        AppDatabase db = AppDatabase.getInstance(this);
        UserDao userDao = db.userDao();

        if (isLoginMode) {
            User user = userDao.getUserByEmail(email);
            Log.d("AUTH", "fetched user → " + user);
            if (user != null) {
                Log.d("AUTH", "stored hash = " + user.getPassword());
                Log.d("AUTH", "verifyPassword(raw) = " + user.verifyPassword(password));
            }
            if (user != null && user.verifyPassword(password)) {
                runOnUiThread(() -> handleLoginResult(user));
                SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                prefs.edit()
                        .putBoolean("logged_in", true)
                        .putString("user_name", user.getNom())
                        .putString("user_password", password)
                        .putString("user_email", user.getEmail())  // optional
                        .apply();
            } else {
                runOnUiThread(() ->
                        Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show());
            }
        } else {
            if (userDao.getUserByEmail(email) != null) {
                runOnUiThread(() ->
                        Toast.makeText(this, R.string.email_exists, Toast.LENGTH_SHORT).show());
                return;
            }
            User newUser = new User(name, email, password);
            userDao.insertUser(newUser);
            runOnUiThread(this::handleSignupSuccess);
        }
    }

    private void handleLoginResult(User user) {
        if (user != null) {
            startActivity(new Intent(this, MainActivity.class)
                    .putExtra("USER_EMAIL", user.getEmail()));
            finish();
        } else {
            Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSignupSuccess() {
        Toast.makeText(this, R.string.signup_success, Toast.LENGTH_SHORT).show();
        toggleMode();
    }
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    private void clearForm() {
        nameInput.getText().clear();
        emailInput.getText().clear();
        passwordInput.getText().clear();
        emailInput.setError(null);
        passwordInput.setError(null);
    }
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View focusedView = getCurrentFocus();
        if (focusedView != null) {
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8;
    }
}
