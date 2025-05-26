package com.example.geotourisme.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.geotourisme.R;
import com.example.geotourisme.ViewModel.SiteViewModel;
import com.example.geotourisme.adapter.SiteListAdapter;
import com.example.geotourisme.databinding.FragmentHomeBinding;
import com.example.geotourisme.model.Site;
import com.google.android.gms.maps.model.Circle;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.slider.Slider;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.Polygon;
import android.graphics.Color;

import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private SiteViewModel vm;
    private SiteListAdapter adapter;

    // Cartographie
    private MapView mapView;
    private MyLocationNewOverlay myLocationOverlay;
    private FloatingActionButton locationButton;
    private EditText searchInput;
    private View overlay;
    private ImageButton layerToggleButton;
    private boolean isSatellite = false;
    private FloatingActionButton bufferButton;
    private FragmentHomeBinding binding;
    private ConstraintLayout bufferPanel;
    private Slider radiusSlider;
    private TextView radiusValue;
    private Polygon bufferCircle;
    private GeoPoint bufferCenter;
    private MapView bufferMapView;

    private SiteViewModel siteViewModel;
    private Bitmap currentLocationBitmap;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
       // return inflater.inflate(R.layout.fragment_home, container, false);
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 2. Récupérer le bouton
        FloatingActionButton fabViewSites = view.findViewById(R.id.fab_view_sites);

        // 3. Lire les préférences
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String email = prefs.getString("user_email", "");
        String name = prefs.getString("user_name", "");
        String password = prefs.getString("user_password", "");

        // 4. Vérification + test
        if (email.equals("admin@gmail.com") && password.equals("admin123")) {
            fabViewSites.setVisibility(View.VISIBLE);
        } else {
            fabViewSites.setVisibility(View.GONE);
        }


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View v, Bundle savedInstanceState) {
        // 1. Références UI
        mapView = v.findViewById(R.id.mapView);

        locationButton = v.findViewById(R.id.locationButton);
        searchInput = v.findViewById(R.id.searchInput);
        layerToggleButton = v.findViewById(R.id.layersButton);
        bufferButton = v.findViewById(R.id.bufferButton);
        bufferPanel = v.findViewById(R.id.bufferPanel);
        radiusSlider       = v.findViewById(R.id.radiusSlider);
        radiusValue      = v.findViewById(R.id.radiusValue);
        overlay = v.findViewById(R.id.overlay);
        MaterialRadioButton rbSuggested = v.findViewById(R.id.radioSuggested);
        MaterialRadioButton rbCustom    = v.findViewById(R.id.radioCustom);
        Button applyButton        = v.findViewById(R.id.applyButton);
        RadioGroup radioGroup            = v.findViewById(R.id.radioGroup);
        bufferMapView = v.findViewById(R.id.bufferMapView);
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        bufferMapView.setTileSource(TileSourceFactory.MAPNIK);
        bufferMapView.setBuiltInZoomControls(false);
        bufferMapView.setMultiTouchControls(true);
        MaterialButton entertainmentButton = getView().findViewById(R.id.entertainmentButton);
        MaterialButton museumButton = getView().findViewById(R.id.museumButton);
        MaterialButton restaurantButton = getView().findViewById(R.id.restaurantButton);
        MaterialButton hotelButton = getView().findViewById(R.id.hotelButton);
        MaterialButton natureButton = getView().findViewById(R.id.natureButton);

        List<MaterialButton> filterButtons = Arrays.asList(
                entertainmentButton, museumButton, restaurantButton, hotelButton, natureButton
        );
        for (MaterialButton button : filterButtons) {
            button.setOnClickListener(view -> {
                String type = (String) view.getTag();
                boolean wasSelected = view.isSelected();

                if (wasSelected) {
                    view.setSelected(false);
                    siteViewModel.setTypeFilter(null);
                } else {
                    for (MaterialButton btn : filterButtons) btn.setSelected(false);
                    view.setSelected(true);
                    siteViewModel.setTypeFilter(type);
                }
            });
        }

        siteViewModel = new ViewModelProvider(requireActivity()).get(SiteViewModel.class);
        observeSites();
        // 2. Permissions localisation
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{ Manifest.permission.ACCESS_FINE_LOCATION },
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }


        // 3. Configuration OSMDroid
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        OnlineTileSourceBase esriSatellite = new OnlineTileSourceBase(
                "ESRI_Satellite",
                1, 20, 256, ".jpg",
                new String[]{"https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/"}
        ) {
            @Override
            public String getTileURLString(long pMapTileIndex) {
                int zoom = MapTileIndex.getZoom(pMapTileIndex);
                int x = MapTileIndex.getX(pMapTileIndex);
                int y = MapTileIndex.getY(pMapTileIndex);
                return getBaseUrl() + zoom + "/" + y + "/" + x;
            }
        };
        mapView.setTileSource(esriSatellite);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        currentLocationBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.nearby);

        // 4. Centre initial (Tanger)
        GeoPoint tanger = new GeoPoint(35.7595, -5.834);
        mapView.getController().setZoom(12.0);
        mapView.getController().setCenter(tanger);

        // 5. Overlay de localisation
        myLocationOverlay = new MyLocationNewOverlay(
                new GpsMyLocationProvider(requireContext()), mapView);
        mapView.getOverlays().add(myLocationOverlay);
        bufferMapView.getOverlays().add(myLocationOverlay);
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            myLocationOverlay.enableMyLocation();
        }
        myLocationOverlay.runOnFirstFix(() ->
                requireActivity().runOnUiThread(() -> {
                    GeoPoint p = myLocationOverlay.getMyLocation();
                    if (p != null) {
                        mapView.getController().animateTo(p);
                        mapView.getController().setZoom(15.0);
                    }
                })
        );
        layerToggleButton.setOnClickListener(view -> {
            if (isSatellite) {
                mapView.setTileSource(TileSourceFactory.MAPNIK);
                Toast.makeText(getContext(), "Vue normale", Toast.LENGTH_SHORT).show();
            } else {

                mapView.setTileSource(esriSatellite);
                Toast.makeText(getContext(), "Vue satellite", Toast.LENGTH_SHORT).show();
            }
            isSatellite = !isSatellite;
        });
        // Toggle visibility logic
        bufferButton.setOnClickListener(view -> {
            if (bufferPanel.getVisibility() == View.GONE) {
                if (ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), "Permission required", Toast.LENGTH_SHORT).show();
                    return;
                }
                GeoPoint current = myLocationOverlay.getMyLocation();
                if (current == null) {
                    Toast.makeText(getContext(), "Location not available", Toast.LENGTH_SHORT).show();
                    return;
                }
                bufferCenter = current;

                // Configure bufferMapView
                bufferMapView.getController().setCenter(bufferCenter);
                bufferMapView.getController().setZoom(mapView.getZoomLevelDouble());
                bufferPanel.setVisibility(View.VISIBLE);
                updateBufferCircle((int) radiusSlider.getValue());
            } else {
                bufferPanel.setVisibility(View.GONE);
            }
        });
        // Update slider value text
        radiusSlider.addOnChangeListener((slider, value, fromUser) -> {
            radiusValue.setText(String.format("%d KM", Math.round(value)));
            if (fromUser) {
                updateBufferCircle((int) value);
                bufferMapView.getController().setZoom(12.0-value/30);
            }

        });


        // Enable/disable slider based on radio selection
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            boolean custom = (checkedId == R.id.radioCustom);
            radiusSlider.setEnabled(custom);
            // if suggested, reset to default value
            if (!custom) {
                radiusSlider.setValue(20);
                updateBufferCircle(20);
            }
        });

        // Handle overlay click
        overlay.setOnClickListener(v1 -> {
            bufferPanel.setVisibility(View.GONE);
            overlay.setVisibility(View.GONE);
            // Clear buffer filters properly
            siteViewModel.setBufferParams(0, 0, 0);
            if (bufferCircle != null) {
                bufferMapView.getOverlays().remove(bufferCircle);
                bufferCircle = null;
                bufferMapView.invalidate();
            }
            mapView.invalidate();  // Add this to refresh main map
        });

        applyButton.setOnClickListener(btn -> {
            bufferPanel.setVisibility(View.GONE);
            overlay.setVisibility(View.GONE);
            if (bufferCircle != null) {
                // Get the current radius value from the slider
                int radius = (int) radiusSlider.getValue();  // Add this line

                siteViewModel.setBufferParams(
                        bufferCenter.getLatitude(),
                        bufferCenter.getLongitude(),
                        radius  // Use the variable here
                );

                // Cleanup remains the same
                bufferMapView.getOverlays().remove(bufferCircle);
                bufferCircle = null;
                bufferMapView.invalidate();
            }
        });

        // Prevent clicks inside bufferPanel from closing it
        bufferPanel.setOnClickListener(panel -> {
            // consume click
        });
        // 6. Bouton “ma position”
        locationButton.setOnClickListener(u -> centerOnUserLocation());

        // 7. Recherche (simple toast)
        searchInput.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Toast.makeText(getContext(),
                        "Recherche : " + textView.getText(), Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    private void centerOnUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(),
                    "Autorisation requise pour la localisation", Toast.LENGTH_SHORT).show();
            return;
        }
        GeoPoint user = myLocationOverlay.getMyLocation();
        if (user != null) {
            mapView.getController().animateTo(user);
            mapView.getController().setZoom(15.0);
        } else {
            Toast.makeText(getContext(),
                    "Position non disponible pour le moment", Toast.LENGTH_SHORT).show();
        }
    }

    @Override public void onResume() {
        super.onResume();
        mapView.onResume();
    }
    private void updateBufferCircle(int radiusKm) {
        if (bufferCenter == null) return;

        int radiusMeters = radiusKm * 1000;

        // Clear existing buffer circle
        bufferMapView.getOverlays().removeIf(overlay -> overlay instanceof Polygon);

        bufferCircle = new Polygon();
        bufferCircle.setPoints(Polygon.pointsAsCircle(bufferCenter, radiusMeters));
        bufferCircle.setStrokeColor(Color.RED);
        bufferCircle.setStrokeWidth(2f);
        bufferCircle.setFillColor(Color.argb(50, 255, 0, 0));
        bufferMapView.getOverlays().add(bufferCircle);
        bufferMapView.invalidate();
    }
    private void observeSites() {
        siteViewModel.getFilteredSites().observe(getViewLifecycleOwner(), sites -> {
            mapView.getOverlays().removeIf(overlay -> overlay instanceof Marker);

            for (Site site : sites) {
                if (site.getLatitude() != 0.0 && site.getLongitude() != 0.0) {
                    GeoPoint point = new GeoPoint(site.getLatitude(), site.getLongitude());
                    Marker marker = new Marker(mapView);
                    marker.setPosition(point);
                    marker.setTitle(site.getNom_site());

                    if (site.getImageUrl() != null) {
                        loadMarkerImage(marker, site.getImageUrl(),site.getVisites());
                    } else {
                        marker.setIcon(new BitmapDrawable(getResources(),
                                creerImageCirculaire(currentLocationBitmap,site.getVisites())));
                    }
                    mapView.getOverlays().add(marker);
                }
            }
            mapView.invalidate();
        });
    }


    private void loadMarkerImage(Marker marker, String imageUrl ,int visits) {
        Glide.with(this)
                .asBitmap()
                .load(Uri.parse(imageUrl))
                .centerCrop()
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                        Bitmap rounded = creerImageCirculaire(resource,visits);
                        marker.setIcon(new BitmapDrawable(getResources(), rounded));
                        mapView.invalidate();
                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                    }
                });
    }

    private Bitmap creerImageCirculaire(Bitmap bitmap ,int visits) {
        // Same implementation you had in MainActivity
        int baseSize = 70;
        double factor   = 20;
        int desiredSize = baseSize
                + visits*5;

        // 2) Optionally cap it so it never exceeds, say, 300px:
        int maxSize = 300;
        desiredSize = Math.min(desiredSize, maxSize);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, desiredSize, desiredSize, true);
        Bitmap output = Bitmap.createBitmap(desiredSize, desiredSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final RectF rect = new RectF(0, 0, desiredSize, desiredSize);
        final float radius = desiredSize / 2f;
        Path path = new Path();
        path.addCircle(radius, radius, radius, Path.Direction.CCW);

        canvas.clipPath(path);
        canvas.drawBitmap(scaledBitmap, null, rect, paint);

        return output;
    }


    @Override public void onPause() {
        super.onPause();
        mapView.onPause();
        bufferMapView.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int reqCode, @NonNull String[] perms, @NonNull int[] res) {
        super.onRequestPermissionsResult(reqCode, perms, res);
        if (reqCode == LOCATION_PERMISSION_REQUEST_CODE
                && res.length>0 && res[0]==PackageManager.PERMISSION_GRANTED) {
            myLocationOverlay.enableMyLocation();
        }
    }


}
