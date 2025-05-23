package com.example.medcare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.medcare.admin.AdminDashboardActivity;
import com.example.medcare.admin.PendingVerificationActivity;
import com.example.medcare.auth.SignInActivity;
import com.example.medcare.map.MapActivity;
import com.example.medcare.ui.ChatBotFragment;
import com.example.medcare.ui.ChatbotFragment2;
import com.example.medcare.ui.MedicalRecordFragment;
import com.example.medcare.ui.PatientEstablishmentListFragment;
import com.example.medcare.ui.PatientProfilFragment;
import com.example.medcare.ui.ProfilFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.medcare.ui.DisponibiliteFragment;
import com.example.medcare.ui.EtablissementFragment;
import com.example.medcare.ui.FileAttenteFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private BottomNavigationView bottomNavigationView;
    private FrameLayout fragmentContainer;
    private String currentUserRole = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fragmentContainer = findViewById(R.id.fragment_container);

        if (bottomNavigationView == null || fragmentContainer == null) {
            Log.e(TAG, "FATAL: Core UI elements not found!");
            handleFatalError();
            return;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            redirectToSignIn();
        } else {
            verifyUserAccessAndSetupUI(currentUser.getUid());
        }
    }

    private void redirectToSignIn() {
        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleFatalError() {
        Toast.makeText(this, "Erreur critique de l'interface.", Toast.LENGTH_LONG).show();
        if (mAuth != null && mAuth.getCurrentUser() != null) mAuth.signOut();
        redirectToSignIn();
    }


    @SuppressLint("SetTextI18n")
    private void verifyUserAccessAndSetupUI(String userId) {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                DocumentSnapshot document = task.getResult();
                String userRole = document.getString("role");
                String userStatus = document.getString("status");
                this.currentUserRole = userRole;

                boolean showMainUI = false;
                Intent redirectIntent = null;

                Log.d(TAG, "Role check: userRole = " + userRole);

                if ("admin".equals(userRole)) {
                    redirectIntent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                } else if ("professional".equals(userRole) && "approved".equals(userStatus)) {
                    showMainUI = true;
                } else if ("pending_professional".equals(userRole)) {
                    if ("pending".equals(userStatus)) {
                        redirectIntent = new Intent(MainActivity.this, PendingVerificationActivity.class);
                    } else {
                        handleInvalidAccess("rejected".equals(userStatus) ? "Your professional application was rejected." : "Account status error.");
                        return;
                    }
                } else if ("patient".equals(userRole)) {
                    showMainUI = true;
                }

                if (showMainUI) {
                    Log.d(TAG, "Setting up Main UI for role: " + this.currentUserRole);
                    setupBottomNavigationForRole();
                } else if (redirectIntent != null) {
                    Log.d(TAG, "Redirecting to: " + redirectIntent.getComponent().getShortClassName());
                    redirectIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(redirectIntent);
                    finish();
                } else {
                    handleInvalidAccess("Account access error or unhandled state.");
                }
            } else {
                Log.e(TAG, "Firestore profile check failed.", task.getException());
                handleInvalidAccess("Error loading profile. Please log in again.");
            }
        });
    }

    private void handleInvalidAccess(String toastMessage){
        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
        if (mAuth != null) mAuth.signOut();
        redirectToSignIn();
    }

    @SuppressLint("ResourceType")
    private void setupBottomNavigationForRole() {
        if (bottomNavigationView == null || currentUserRole == null) return;

        bottomNavigationView.getMenu().clear();
        int menuResId;
        int initialFragmentId;

        Log.d(TAG, "Inflating menu for role: " + currentUserRole);

        try {
            if ("professional".equals(currentUserRole)) {
                menuResId = R.menu.bottom_menu;
                initialFragmentId = R.id.nav_etablissement;
                bottomNavigationView.inflateMenu(menuResId);
                setupNavigationListenerProfessional();
            } else if ("patient".equals(currentUserRole)) {
                menuResId = R.menu.bottom_nav_menu_patient;
                initialFragmentId = R.id.nav_book_appointment;
                bottomNavigationView.inflateMenu(menuResId);
                setupNavigationListenerPatient();
            } else {
                Log.e(TAG, "Unsupported role for bottom nav: " + currentUserRole);
                bottomNavigationView.setVisibility(View.GONE);
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error inflating menu resource!", e);
            Toast.makeText(this, "Erreur de chargement du menu.", Toast.LENGTH_LONG).show();
            bottomNavigationView.setVisibility(View.GONE);
            replaceFragment(new Fragment());
            return;
        }

        bottomNavigationView.setVisibility(View.VISIBLE);

        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            Log.d(TAG,"Setting initial fragment ID: " + initialFragmentId);
            if (bottomNavigationView.getMenu().findItem(initialFragmentId) != null) {
                bottomNavigationView.setSelectedItemId(initialFragmentId);
            } else {
                Log.e(TAG,"Default menu item ID " + initialFragmentId + " not found in inflated menu!");
                loadFallbackFragment();
            }
        }
    }

    private void setupNavigationListenerProfessional() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment selectedFragment = null;
            if (id == R.id.nav_etablissement) selectedFragment = new EtablissementFragment();
            else if (id == R.id.nav_disponibilite) selectedFragment = new DisponibiliteFragment();
            else if (id == R.id.nav_file) selectedFragment = new FileAttenteFragment();
            else if (id == R.id.nav_chatbot) selectedFragment = new ChatBotFragment();
            else if (id == R.id.nav_profile) selectedFragment = new ProfilFragment();


            // Add more items...
            if (selectedFragment != null) replaceFragment(selectedFragment);
            return selectedFragment != null;
        });
    }


    private void setupNavigationListenerPatient() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment selectedFragment = null;

            if (id == R.id.nav_map) {

                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_medical_dossier) {
                selectedFragment = new MedicalRecordFragment();
            } else if (id == R.id.nav_book_appointment) {
                selectedFragment = new PatientEstablishmentListFragment();
            } else if (id == R.id.nav_chatbot) {
                selectedFragment = new ChatbotFragment2();
            } else if (id == R.id.nav_profile) {
                selectedFragment = new PatientProfilFragment();
            }
            if (selectedFragment != null) {
                replaceFragment(selectedFragment);
                return true;
            }

            return false;
        });
    }

    private void loadFallbackFragment() {
        Log.w(TAG, "Loading fallback fragment due to missing initial item ID.");
        Fragment fallbackFragment;
        if ("patient".equals(currentUserRole)) {
            fallbackFragment = new PatientEstablishmentListFragment();
        } else if ("professional".equals(currentUserRole)) {
            fallbackFragment = new EtablissementFragment();
        } else {
            fallbackFragment = new Fragment();
        }
        replaceFragment(fallbackFragment);
    }

    private void replaceFragment(Fragment fragment) {
        if (fragmentContainer == null || fragment == null) return;
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
            Log.d(TAG, "Fragment already displayed: " + fragment.getClass().getSimpleName());
            return;
        }
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, fragment.getClass().getSimpleName());
        transaction.commitAllowingStateLoss();
        Log.d(TAG,"Replacing fragment with: " + fragment.getClass().getSimpleName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.action_logout) {
            Log.d(TAG, "Logout selected.");
            mAuth.signOut();
            redirectToSignIn();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
