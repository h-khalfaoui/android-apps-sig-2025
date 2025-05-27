package com.example.alert.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.alert.R;
import com.example.alert.database.SQLiteHelper;

import java.io.ByteArrayOutputStream;

public class ClaimActivity extends AppCompatActivity {

    private EditText descriptionEditText;
    private TextView locationTextView;
    private Button submitClaimButton, importImageButton;
    private ImageView imageView;
    private byte[] imageBytes;
    private String currentLocation = "Non définie";
    private static final String CHANNEL_ID = "claim_notifications";

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> notificationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim);

        descriptionEditText = findViewById(R.id.descriptionEditText);
        locationTextView = findViewById(R.id.locationTextView);
        submitClaimButton = findViewById(R.id.submitClaimButton);
        importImageButton = findViewById(R.id.importImageButton);
        imageView = findViewById(R.id.imageView);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("location")) {
            currentLocation = intent.getStringExtra("location");
            locationTextView.setText("📍 Localisation : " + currentLocation);
        }

        createNotificationChannel();

        initLaunchers();


        requestNotificationPermission();

        importImageButton.setOnClickListener(v -> showImageSourceDialog());

        submitClaimButton.setOnClickListener(v -> submitClaim());
    }

    private void initLaunchers() {
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Bitmap imageBitmap = (Bitmap) result.getData().getExtras().get("data");
                setImage(imageBitmap);
            }
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri selectedImageUri = result.getData().getData();
                try {
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                    setImage(imageBitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Erreur lors de l'importation de l'image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (!isGranted) {
                        Toast.makeText(this, "Les notifications sont désactivées.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void setImage(Bitmap imageBitmap) {
        imageView.setImageBitmap(imageBitmap);
        imageView.setVisibility(ImageView.VISIBLE);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        imageBytes = baos.toByteArray();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Réclamations",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications pour les nouvelles réclamations");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void showImageSourceDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Choisir une source d'image")
                .setMessage("Voulez-vous utiliser la caméra ou le stockage?")
                .setPositiveButton("Caméra", (dialog, which) -> openCamera())
                .setNegativeButton("Galerie", (dialog, which) -> openGallery())
                .setCancelable(true)
                .show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(takePictureIntent);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    private void openGallery() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhotoIntent.setType("image/*");
        galleryLauncher.launch(pickPhotoIntent);
    }

    private void submitClaim() {
        String description = descriptionEditText.getText().toString().trim();

        if (description.isEmpty() || imageBytes == null || currentLocation.equals("Non définie")) {
            Toast.makeText(this, "Veuillez remplir tous les champs.", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteHelper dbHelper = new SQLiteHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "INSERT INTO claims (location, description, image, status) VALUES (?, ?, ?, 'en attente');";
        db.execSQL(sql, new Object[]{currentLocation, description, imageBytes});
        db.close();

        sendClaimNotification(description);

        Toast.makeText(this, "Réclamation envoyée avec succès.", Toast.LENGTH_SHORT).show();
        finish();
    }

    @SuppressLint("MissingPermission")
    private void sendClaimNotification(String description) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_claim)
                .setContentTitle("Nouvelle Réclamation")
                .setContentText("📝 " + description)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify((int) (Math.random() * 10000), builder.build());
    }
}
