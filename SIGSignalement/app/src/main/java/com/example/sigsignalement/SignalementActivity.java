package com.example.sigsignalement;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.example.sigsignalement.model.AppDatabase;
import com.example.sigsignalement.model.Signalement;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SignalementActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private static final int REQUEST_MAP = 3;
    private static final int PERMISSION_REQUEST_CAMERA = 100;
    private static final int PERMISSION_REQUEST_NOTIFICATION = 101;

    private byte[] imageData = null;
    private Bitmap photoBitmap;
    private double latitude = 0.0, longitude = 0.0;

    EditText editTextType;
    Button buttonTakePhoto, buttonOpenMap, buttonSave, buttonChooseFromGallery;
    ImageView imageViewPhoto;
    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signalement);

        editTextType = findViewById(R.id.editTextType);
        buttonTakePhoto = findViewById(R.id.buttonTakePhoto);
        buttonOpenMap = findViewById(R.id.buttonOpenMap);
        buttonSave = findViewById(R.id.buttonSave);
        imageViewPhoto = findViewById(R.id.imageViewPhoto);
        buttonChooseFromGallery = findViewById(R.id.buttonChooseFromGallery);

        db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "signalements_db")
                .allowMainThreadQueries()
                .build();

        buttonTakePhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            } else {
                openCamera();
            }
        });

        buttonChooseFromGallery.setOnClickListener(v -> {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto, REQUEST_PICK_IMAGE);
        });

        buttonOpenMap.setOnClickListener(v -> {
            Intent intent = new Intent(SignalementActivity.this, MapsActivity.class);
            startActivityForResult(intent, REQUEST_MAP);
        });

        buttonSave.setOnClickListener(v -> {
            String type = editTextType.getText().toString().trim();

            if (type.isEmpty() || photoBitmap == null || imageData == null || latitude == 0.0 || longitude == 0.0) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
            String email = getSharedPreferences("USER_PREFS", MODE_PRIVATE).getString("email", "anonyme");

            Signalement signalement = new Signalement(type, latitude, longitude, currentDate, imageData, email);
            db.signalementDao().insert(signalement);
            showNotification();

            Toast.makeText(this, "Signalement enregistré !", Toast.LENGTH_LONG).show();

            editTextType.setText("");
            imageViewPhoto.setImageBitmap(null);
            photoBitmap = null;
            imageData = null;
            latitude = longitude = 0.0;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_NOTIFICATION);
            }
        }
    }

    private void showNotification() {
        String CHANNEL_ID = "signalement_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "SignalementChannel";
            String description = "Canal pour les notifications de signalement";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Signalement confirmé")
                .setContentText("Merci ! Votre signalement a été enregistré avec succès.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Appareil photo non disponible", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Permission caméra refusée", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSION_REQUEST_NOTIFICATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission de notification accordée", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission de notification refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                photoBitmap = (Bitmap) data.getExtras().get("data");
            } else if (requestCode == REQUEST_PICK_IMAGE && data != null) {
                Uri imageUri = data.getData();
                try {
                    photoBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == REQUEST_MAP && data != null) {
                latitude = data.getDoubleExtra("latitude", 0.0);
                longitude = data.getDoubleExtra("longitude", 0.0);
                Toast.makeText(this, "Position sélectionnée", Toast.LENGTH_SHORT).show();
            }

            if (photoBitmap != null) {
                imageViewPhoto.setImageBitmap(photoBitmap);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                photoBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                imageData = stream.toByteArray();
            }
        }
    }
}
