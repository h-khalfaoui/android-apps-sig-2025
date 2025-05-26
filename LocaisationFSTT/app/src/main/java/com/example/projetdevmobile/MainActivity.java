package com.example.projetdevmobile;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.Manifest;
import android.widget.Button;
import android.widget.Toast;


import com.mapbox.bindgen.Expected;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.QueriedFeature;
import com.mapbox.maps.QueryRenderedFeaturesCallback;
import com.mapbox.maps.RenderedQueryGeometry;
import com.mapbox.maps.RenderedQueryOptions;
import com.mapbox.maps.ScreenCoordinate;
import com.mapbox.maps.Style;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mapbox.maps.plugin.gestures.GesturesPlugin;

import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private MapView mapView;

    FirebaseAuth auth;

    static int tried = 0;


    //    Login Button
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1); // 1 is requestCode
        }

        mapView = findViewById(R.id.mapView);

        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
        });

        // Set initial camera position
        CameraOptions cameraOptions = new CameraOptions.Builder()
                .center(Point.fromLngLat(-5.894187, 35.736253))
                .pitch(0.0)
                .zoom(17.0)
                .bearing(0.0)
                .build();

        mapView.getMapboxMap().setCamera(cameraOptions);



        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
            Toast.makeText(this, "Map Style Loaded", Toast.LENGTH_SHORT).show();

            // Kotlin call from Java
            GeoJsonHelper.addGeoJsonLayer(mapView, style, this, "Final_FSTT_Geojson.geojson");
        });




        /// ////////////////////////////////////////////////////////////////////////
        // check if user already signed in
        auth = FirebaseAuth.getInstance();


        FirebaseUser utilisateurActuel = auth.getCurrentUser();
        if (utilisateurActuel != null || tried!=0) {
        }
        else{
            tried=1;
            Intent intent = new Intent(MainActivity.this, ActiviteConnexion.class);
            startActivity(intent);
        }

//        ////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied, show a message
                Toast.makeText(this, "Location permission rejected", Toast.LENGTH_SHORT).show();
            }
        }
    }
}