package com.example.quietspaceeee.data.view;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quietspaceeee.R;

import com.example.quietspaceeee.data.db.CafeRepository;

public class AddCafeActivity extends AppCompatActivity {

    private EditText editTextNom, editTextAdresse;
    private Spinner spinnerDisponibilite, spinnerEquipements, spinnerBruit;
    private Button buttonAjouter;

    private CafeRepository cafeRepository;

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_add_cafe);
//
//        editTextNom = findViewById(R.id.editTextNom);
//        editTextAdresse = findViewById(R.id.editTextAdresse);
//        spinnerDisponibilite = findViewById(R.id.spinnerDisponibilite);
//        spinnerEquipements = findViewById(R.id.spinnerEquipements);
//        spinnerBruit = findViewById(R.id.spinnerBruit);
//        buttonAjouter = findViewById(R.id.buttonAjouter);
//
//        cafeRepository = new CafeRepository(this);
//
//        setupSpinners();
//
//        buttonAjouter.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ajouterCafe();
//            }
//        });
//    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> adapterDisponibilite = ArrayAdapter.createFromResource(this,
                R.array.disponibilite_array, android.R.layout.simple_spinner_item);
        adapterDisponibilite.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDisponibilite.setAdapter(adapterDisponibilite);

        ArrayAdapter<CharSequence> adapterEquipements = ArrayAdapter.createFromResource(this,
                R.array.equipements_array, android.R.layout.simple_spinner_item);
        adapterEquipements.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEquipements.setAdapter(adapterEquipements);

        ArrayAdapter<CharSequence> adapterBruit = ArrayAdapter.createFromResource(this,
                R.array.bruit_array, android.R.layout.simple_spinner_item);
        adapterBruit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBruit.setAdapter(adapterBruit);
    }

    private void ajouterCafe() {
        String nom = editTextNom.getText().toString().trim();
        String adresse = editTextAdresse.getText().toString().trim();
        String disponibilite = spinnerDisponibilite.getSelectedItem().toString();
        String equipement = spinnerEquipements.getSelectedItem().toString();
        String bruit = spinnerBruit.getSelectedItem().toString();

        if (nom.isEmpty() || adresse.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir le nom et l'adresse", Toast.LENGTH_SHORT).show();
            return;
        }

//        Toast.makeText(this, "Café ajouté avec succès", Toast.LENGTH_SHORT).show();
        finish(); // Retour à la liste des cafés
    }
}