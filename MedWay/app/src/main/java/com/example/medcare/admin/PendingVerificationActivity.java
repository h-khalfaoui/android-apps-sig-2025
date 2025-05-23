package com.example.medcare.admin;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.medcare.R;
import com.example.medcare.auth.SignInActivity;
import com.google.firebase.auth.FirebaseAuth;

public class PendingVerificationActivity extends AppCompatActivity {

    Button buttonLogout;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_verification);

        mAuth = FirebaseAuth.getInstance();
        buttonLogout = findViewById(R.id.buttonLogoutPending);

        buttonLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(PendingVerificationActivity.this, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}