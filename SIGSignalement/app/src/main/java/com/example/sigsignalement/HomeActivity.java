package com.example.sigsignalement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;



public class HomeActivity extends AppCompatActivity {

    Button buttonSignaler, buttonLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        buttonSignaler = findViewById(R.id.buttonSignaler);
        Button buttonHistorique = findViewById(R.id.buttonHistorique);

        buttonLogout = findViewById(R.id.buttonLogout);


        buttonHistorique.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, HistoriqueActivity.class);
            startActivity(intent);
        });

        buttonSignaler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(HomeActivity.this, SignalementActivity.class);
                startActivity(intent);

            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retour à la page de connexion
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
