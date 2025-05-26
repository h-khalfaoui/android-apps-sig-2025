package com.example.sigsignalement;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.sigsignalement.fragments.AdminFragment;
import com.example.sigsignalement.fragments.HomeFragment;
import com.example.sigsignalement.fragments.MapsFragment;
import com.example.sigsignalement.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment;

            int id = item.getItemId();
            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_map) {
                selectedFragment = new MapsFragment();
            } else if (id == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            } else if (id == R.id.nav_admin) {
                SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
                boolean isAdmin = prefs.getBoolean("isAdmin", false);
                if (isAdmin) {
                    selectedFragment = new AdminFragment();
                } else {
                    Toast.makeText(this, "Accès réservé à l'administrateur", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } else {
                selectedFragment = new HomeFragment();
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();

            return true;
        });

        // Charger le fragment initial
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }
}
