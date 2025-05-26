package com.example.saydaliyati.Activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.saydaliyati.R;
import com.example.saydaliyati.Utils.SecurityUtils;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AuthorityDashboardActivity extends BaseActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authority_dashboard);

        // Vérifier si l'utilisateur est authentifié
        if (!SecurityUtils.isAuthenticated(this)) {
            finish(); // Retourner à l'écran précédent si non authentifié
            return;
        }

        // Configurer la barre d'action
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.title_admin_dashboard));
            actionBar.setSubtitle("Connecté en tant que: " + SecurityUtils.getCurrentUsername(this));
        }

        // Initialiser les vues
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        logoutButton = findViewById(R.id.logoutButton);

        // Configurer l'adaptateur pour ViewPager2
        setupViewPager();

        // Configurer le bouton de déconnexion
        logoutButton.setOnClickListener(v -> {
            SecurityUtils.clearAuthInfo(this);
            finish();
        });
    }

    private void setupViewPager() {
        // Créer et configurer l'adaptateur de pages
        AuthorityPagerAdapter pagerAdapter = new AuthorityPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Connecter TabLayout avec ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Tableau de bord");
                    break;
                case 1:
                    tab.setText("Pharmacies");
                    break;
                case 2:
                    tab.setText("Calendrier");
                    break;
            }
        }).attach();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}