package com.example.quietspaceeee.data.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quietspaceeee.R;
import com.example.quietspaceeee.data.db.CafeRepository;
import com.example.quietspaceeee.data.model.Cafe;

import java.io.IOException;
import java.io.InputStream;

public class CafeEditActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editName, editCity, editDescription, editLocation;
    private CheckBox checkboxWifi, checkboxPrises, checkboxSnacks, checkboxBoisson;
    private Spinner spinnerNoise, spinnerDisponibilite, spinnerType;
    private Button buttonSelectImage, buttonUpdateCafe;
    private ImageView selectedImageView;
    private Uri selectedImageUri = null;

    private int cafeId = -1;
    private CafeRepository repository;
    private Cafe currentCafe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cafe_edit);

        // Initialiser les vues
        editName = findViewById(R.id.editCafeName);
        editCity = findViewById(R.id.editCafeCity);
        editDescription = findViewById(R.id.editDescription);
        editLocation = findViewById(R.id.editCafeLocation);

        checkboxWifi = findViewById(R.id.checkbox_wifi);
        checkboxPrises = findViewById(R.id.checkbox_prises);
        checkboxSnacks = findViewById(R.id.checkbox_snacks);
        checkboxBoisson = findViewById(R.id.checkbox_boisson);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerNoise = findViewById(R.id.spinnerCafeNoise);
        spinnerDisponibilite = findViewById(R.id.spinnerCafeDisponibilite);

        buttonSelectImage = findViewById(R.id.buttonSelectImage);
        buttonUpdateCafe = findViewById(R.id.buttonUpdateCafe);
        selectedImageView = findViewById(R.id.selectedImageView);

        repository = new CafeRepository(this);

        // Adapter pour Spinner
        ArrayAdapter<CharSequence> noiseAdapter = ArrayAdapter.createFromResource(this,
                R.array.bruit_array, android.R.layout.simple_spinner_item);
        noiseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNoise.setAdapter(noiseAdapter);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.type_array, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        ArrayAdapter<CharSequence> dispoAdapter = ArrayAdapter.createFromResource(this,
                R.array.disponibilite_array, android.R.layout.simple_spinner_item);
        dispoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDisponibilite.setAdapter(dispoAdapter);

        // Récupérer l'ID du café
        cafeId = getIntent().getIntExtra("cafeId", -1);
        if (cafeId != -1) {
            loadCafeData();
        }

        buttonSelectImage.setOnClickListener(v -> openImageChooser());
        buttonUpdateCafe.setOnClickListener(v -> updateCafe());
    }

    private void loadCafeData() {
        currentCafe = repository.getCafeById(cafeId);
        if (currentCafe == null) {
            Toast.makeText(this, "Café introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editName.setText(currentCafe.getName());
        editCity.setText(currentCafe.getCity());
        editDescription.setText(currentCafe.getDescription());
        editLocation.setText(currentCafe.getLocation());

        checkboxWifi.setChecked(currentCafe.getEquipments().contains("Wi-Fi"));
        checkboxPrises.setChecked(currentCafe.getEquipments().contains("Prises"));
        checkboxSnacks.setChecked(currentCafe.getEquipments().contains("Snacks"));
        checkboxBoisson.setChecked(currentCafe.getEquipments().contains("Boisson"));

        setSpinnerSelection(spinnerNoise, currentCafe.getNoiseLevel());
        setSpinnerSelection(spinnerType, currentCafe.getType());
        setSpinnerSelection(spinnerDisponibilite, currentCafe.getAvailability());

        if (currentCafe.getImageUrl() != null) {
            selectedImageUri = Uri.parse(currentCafe.getImageUrl());
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            getContentResolver().takePersistableUriPermission(
                    selectedImageUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateCafe() {
        String name = editName.getText().toString().trim();
        String city = editCity.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String location = editLocation.getText().toString().trim();
        String noise = spinnerNoise.getSelectedItem().toString();
        String type = spinnerType.getSelectedItem().toString();
        String availability = spinnerDisponibilite.getSelectedItem().toString();

        if (name.isEmpty() || city.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir au minimum le nom et la ville", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder equipments = new StringBuilder();
        if (checkboxWifi.isChecked()) equipments.append("Wi-Fi ");
        if (checkboxPrises.isChecked()) equipments.append("Prises ");
        if (checkboxSnacks.isChecked()) equipments.append("Snacks ");
        if (checkboxBoisson.isChecked()) equipments.append("Boisson ");

        currentCafe.setName(name);
        currentCafe.setCity(city);
        currentCafe.setDescription(description);
        currentCafe.setLocation(location);
        currentCafe.setNoiseLevel(noise);
        currentCafe.setType(type);
        currentCafe.setAvailability(availability);
        currentCafe.setEquipments(equipments.toString());

        if (selectedImageUri != null) {
            currentCafe.setImageUrl(selectedImageUri.toString());
        }

        boolean updated = repository.updateCafe(currentCafe);
        if (updated) {
            Toast.makeText(this, "Café mis à jour avec succès", Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Échec de la mise à jour", Toast.LENGTH_SHORT).show();
        }

        setResult(RESULT_OK);
        finish();
    }
}