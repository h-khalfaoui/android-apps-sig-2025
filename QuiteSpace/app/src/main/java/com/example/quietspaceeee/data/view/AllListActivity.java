package com.example.quietspaceeee.data.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.example.quietspaceeee.R;

import java.util.ArrayList;

import com.example.quietspaceeee.data.db.CafeRepository;
import com.example.quietspaceeee.data.model.Cafe;

public class AllListActivity extends Activity {

    EditText searchBar;
    Spinner availabilitySpinner, noiseSpinner;
    Button applyFilterButton;

    Button equipmentButton;
    ArrayList<String> selectedEquipments = new ArrayList<>();
    ListView cafeListView;

    ArrayList<Cafe> fullCafeList;
    CafeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cafe_list);

        // Initialiser les vues
        searchBar = findViewById(R.id.searchBar);
        availabilitySpinner = findViewById(R.id.availabilitySpinner);
        noiseSpinner = findViewById(R.id.noiseSpinner);
        applyFilterButton = findViewById(R.id.applyFilterButton);
        cafeListView = findViewById(R.id.cafeListView);

        // Charger la base de données
        CafeRepository repository = new CafeRepository(this);
        fullCafeList = repository.getAllCafes();


        // Adapter
        adapter = new CafeAdapter(this, fullCafeList);
        cafeListView.setAdapter(adapter);

        // Configurer les spinners
        ArrayAdapter<CharSequence> availabilityAdapter = ArrayAdapter.createFromResource(
                this, R.array.disponibilite_array, android.R.layout.simple_spinner_item);
        availabilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        availabilitySpinner.setAdapter(availabilityAdapter);

        ArrayAdapter<CharSequence> noiseAdapter = ArrayAdapter.createFromResource(
                this, R.array.bruit_array, android.R.layout.simple_spinner_item);
        noiseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        noiseSpinner.setAdapter(noiseAdapter);

        equipmentButton = findViewById(R.id.equipmentButton);

        equipmentButton.setOnClickListener(v -> {
            String[] equipmentOptions = getResources().getStringArray(R.array.equipements_array);
            boolean[] checkedItems = new boolean[equipmentOptions.length];

            // Pré-cocher les équipements déjà sélectionnés
            for (int i = 0; i < equipmentOptions.length; i++) {
                checkedItems[i] = selectedEquipments.contains(equipmentOptions[i]);
            }

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Sélectionnez les équipements")
                    .setMultiChoiceItems(equipmentOptions, checkedItems, (dialog, which, isChecked) -> {
                        if (isChecked) {
                            selectedEquipments.add(equipmentOptions[which]);
                        } else {
                            selectedEquipments.remove(equipmentOptions[which]);
                        }
                    })
                    .setPositiveButton("OK", null)
                    .show();
        });



        // Appliquer le filtrage
        applyFilterButton.setOnClickListener(v -> applyAllFilters());

        cafeListView.setOnItemClickListener((parent, view, position, id) -> {
            // Récupérer le café à la position cliquée
            Cafe clickedCafe = (Cafe) parent.getItemAtPosition(position);

            // Implémenter ce que vous voulez faire lors d'un clic, par exemple ouvrir une activité avec les détails du café
            Intent intent = new Intent(AllListActivity.this, CafeDetailActivity.class);
            intent.putExtra("cafeId", clickedCafe.getId());
            startActivityForResult(intent, 1);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            reloadCafeList();
        }
    }

    private void reloadCafeList() {
        CafeRepository repository = new CafeRepository(this);
        ArrayList<Cafe> updatedList = repository.getAllCafes();

        fullCafeList.clear();
        fullCafeList.addAll(updatedList);
        adapter.setData(fullCafeList);
        adapter.notifyDataSetChanged();
    }

    void applyAllFilters() {
        String query = searchBar.getText().toString().trim().toLowerCase();
        String availability = availabilitySpinner.getSelectedItem().toString();
        String noise = noiseSpinner.getSelectedItem().toString();

        adapter.applyFilters(query, availability, selectedEquipments, noise);
    }


    @Override
    protected void onResume() {
        super.onResume();
        reloadCafeList();

    }

}