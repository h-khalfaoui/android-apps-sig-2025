package com.example.medcare.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.medcare.R;
import com.example.medcare.model.UserProfile;
import com.example.medcare.auth.SignInActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity implements PendingUserAdapter.OnUserActionListener {

    private static final String TAG = "AdminDashboardActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView recyclerViewPendingUsers;
    private ProgressBar progressBar;
    private TextView textViewNoPending;
    private PendingUserAdapter adapter;
    private List<UserProfile> pendingUserList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbarAdmin);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerViewPendingUsers = findViewById(R.id.recyclerViewPendingUsers);
        progressBar = findViewById(R.id.progressBarAdmin);
        textViewNoPending = findViewById(R.id.textViewNoPending);

        pendingUserList = new ArrayList<>();
        // Initialize adapter AFTER list initialization
        adapter = new PendingUserAdapter(pendingUserList, this);

        recyclerViewPendingUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPendingUsers.setAdapter(adapter);

        fetchPendingProfessionals();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when the activity resumes might be useful
        fetchPendingProfessionals();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchPendingProfessionals() {
        showLoading(true);
        textViewNoPending.setVisibility(View.GONE); // Hide initially

        db.collection("users")
                .whereEqualTo("role", "pending_professional") // Keep role check if needed
                .whereEqualTo("status", "pending") // Primary filter condition
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    pendingUserList.clear(); // Clear list before adding new data or showing "no pending"
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            textViewNoPending.setVisibility(View.GONE);
                            recyclerViewPendingUsers.setVisibility(View.VISIBLE);
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    UserProfile profile = document.toObject(UserProfile.class);

                                    profile.setUid(document.getId());
                                    pendingUserList.add(profile);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error converting document " + document.getId(), e);
                                    // Optionally add placeholder/error item or just skip
                                }
                            }
                        } else {
                            // Task successful, but no documents found
                            textViewNoPending.setVisibility(View.VISIBLE);
                            recyclerViewPendingUsers.setVisibility(View.GONE);
                            Log.d(TAG, "No pending users found.");
                        }
                        adapter.notifyDataSetChanged(); // Update the adapter regardless of whether items were added or list cleared
                    } else {
                        // Task failed
                        Log.w(TAG, "Error getting pending users.", task.getException());
                        Toast.makeText(AdminDashboardActivity.this, "Error fetching pending users.", Toast.LENGTH_SHORT).show();
                        textViewNoPending.setVisibility(View.VISIBLE);
                        textViewNoPending.setText(R.string.error_fetching_data);
                        recyclerViewPendingUsers.setVisibility(View.GONE);
                    }
                });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);

    }

    // --- Implementation of OnUserActionListener from Adapter ---

    @Override
    public void onApproveClick(String uid) {
        Log.d(TAG, "Approving user: " + uid);
        updateUserStatusAndRole(uid, "approved", "professional", "User Approved.", "Approval failed.");
    }

    @Override
    public void onRejectClick(String uid) {
        Log.d(TAG, "Rejecting user: " + uid);
        updateUserStatusAndRole(uid, "rejected", "pending_professional", "User Rejected.", "Rejection failed."); // Keep role as pending
    }

    // Helper method to update Firestore
    private void updateUserStatusAndRole(String uid, String newStatus, String newRole, String successMessage, String failureMessage) {
        showLoading(true); // Consider more granular loading indication

        // Create updates map - only update status and role
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        updates.put("role", newRole); // Update role on approval, keep pending on rejection

        db.collection("users").document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Log.d(TAG, "User " + uid + " status updated to " + newStatus + ", role to " + newRole);
                    Toast.makeText(AdminDashboardActivity.this, successMessage, Toast.LENGTH_SHORT).show();
                    fetchPendingProfessionals(); // Refresh the list
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.w(TAG, "Error updating user " + uid, e);
                    Toast.makeText(AdminDashboardActivity.this, failureMessage, Toast.LENGTH_SHORT).show();
                });
    }


    // --- Optional Menu for Logout ---

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_logout_admin) {
            mAuth.signOut();
            Intent intent = new Intent(AdminDashboardActivity.this, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}