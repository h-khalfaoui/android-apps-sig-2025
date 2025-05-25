package com.example.quietspaceeee;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.quietspaceeee.data.view.BibListActivity;
import com.example.quietspaceeee.data.view.CafeListActivity;
import com.example.quietspaceeee.data.view.CoworkListActivity;
import com.example.quietspaceeee.data.view.MesReservationsListActivity;
import com.example.quietspaceeee.data.view.ReservationListActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.quietspaceeee.data.view.AllListActivity;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    ImageButton btnCafes, btnLibraries, btnCoworking, btnAll, btnMyReservations, btnSupport;
    private BottomNavigationView bottomNav; // Déclaré comme variable de classe
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainn);
        TextInputEditText searchInput = findViewById(R.id.searchInput);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize buttons
        btnCafes = findViewById(R.id.btnCafes);
        btnLibraries = findViewById(R.id.btnLibraries);
        btnCoworking = findViewById(R.id.btnCoworking);
        btnAll = findViewById(R.id.btnAll);
        btnMyReservations = findViewById(R.id.btnMyReservations);
        btnSupport = findViewById(R.id.btnSupport);


        // Set click listeners
        btnCafes.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, CafeListActivity.class);
            startActivity(intent);
        });
        btnLibraries.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, BibListActivity.class);
            startActivity(intent);
        });
        btnCoworking.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, CoworkListActivity.class);
            startActivity(intent);
        });
        btnAll.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AllListActivity.class);
            startActivity(intent);
        });
        btnMyReservations.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, MesReservationsListActivity.class);
            SharedPreferences prefs = getSharedPreferences("QuietSpacePrefs", MODE_PRIVATE);
            String userEmail = prefs.getString("userEmail", null);
            startActivity(intent);
        });
        btnSupport.setOnClickListener(view -> showToast("Support"));
        findViewById(R.id.btnSupport).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SupportActivity.class);
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
                Intent intent = new Intent(MainActivity.this, MapSearchActivity1.class);
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



        View cardCafes = findViewById(R.id.cardCafes);
        View cardLibraries = findViewById(R.id.cardLibraries);
        View cardCoworking = findViewById(R.id.cardCoworking);
        View cardReserve = findViewById(R.id.ListAll);
        View cardMyReservations = findViewById(R.id.cardMyReservations);
        View cardSupport = findViewById(R.id.cardSupport);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim().toLowerCase();

                // Logique simple de filtre
                cardCafes.setVisibility(query.isEmpty() || "cafés".contains(query) || "cafe".contains(query) ? View.VISIBLE : View.GONE);
                cardLibraries.setVisibility(query.isEmpty() || "bibliothèques".contains(query) || "librairie".contains(query) || "biblio".contains(query) ? View.VISIBLE : View.GONE);
                cardCoworking.setVisibility(query.isEmpty() || "coworking".contains(query) ? View.VISIBLE : View.GONE);
                cardReserve.setVisibility(query.isEmpty() || "réserver".contains(query) ||"cafés".contains(query) ||"coworking".contains(query) ||"cafe".contains(query) || "librairie".contains(query) || "bibliothèques".contains(query) || "reserve".contains(query) ? View.VISIBLE : View.GONE);
                cardMyReservations.setVisibility(query.isEmpty() || "mes réservations".contains(query) || "reservations".contains(query) ? View.VISIBLE : View.GONE);
                cardSupport.setVisibility(query.isEmpty() || "support".contains(query) ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message + " clicked", Toast.LENGTH_SHORT).show();
    }
}
