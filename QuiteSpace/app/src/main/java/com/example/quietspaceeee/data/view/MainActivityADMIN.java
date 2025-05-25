package com.example.quietspaceeee.data.view;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.quietspaceeee.MapSearchActivity;
import com.example.quietspaceeee.ProfileActivity;
import com.example.quietspaceeee.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivityADMIN extends AppCompatActivity {

    ImageButton btnCafes, btnLibraries, btnCoworking, btnReserve, btn_reservations, btnSupport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_adm);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize buttons
        btnCafes = findViewById(R.id.btn_cafes);
        btn_reservations = findViewById(R.id.btn_reservations);


        // Set click listeners
        btnCafes.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivityADMIN.this, AllListActivityadmin.class);
            startActivity(intent);
        });
        btn_reservations.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivityADMIN.this, ReservationListActivity.class);
            startActivity(intent);
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // You can keep this as fragment change or also move to HomeActivity
                return true;
            } else if (itemId == R.id.nav_action_center) {
                // 👉 Start the MapSearchActivity
                Intent intent = new Intent(MainActivityADMIN.this, MapSearchActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                // You could also launch a ProfileActivity here
                Intent profileIntent = new Intent(this, ProfileActivity.class);
                startActivity(profileIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            }

            return false;
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message + " clicked", Toast.LENGTH_SHORT).show();
    }
}
