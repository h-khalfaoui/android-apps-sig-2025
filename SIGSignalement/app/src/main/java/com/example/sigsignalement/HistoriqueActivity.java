// com.example.sigsignalement.HistoriqueActivity.java
package com.example.sigsignalement;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import com.example.sigsignalement.model.AppDatabase;
import com.example.sigsignalement.model.Signalement;

import java.util.List;
import com.example.sigsignalement.adapter.SignalementAdapter;


public class HistoriqueActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    SignalementAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historique);

        recyclerView = findViewById(R.id.recyclerViewHistorique);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "signalements_db")
                .allowMainThreadQueries()
                .build();

        List<Signalement> signalements = db.signalementDao().getAll();

        adapter = new SignalementAdapter(signalements);
        recyclerView.setAdapter(adapter);
    }
}
