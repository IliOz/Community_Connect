package com.example.myapplication.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;

public class MapPickerActivity extends AppCompatActivity {

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- THE FIX IS HERE ---
        // This is the correct, non-deprecated way to initialize osmdroid configuration.
        // It uses a named SharedPreferences file to avoid conflicts.
        Configuration.getInstance().load(getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE));

        // This sets the user agent to your app's package name, which is required by osmdroid.
        Configuration.getInstance().setUserAgentValue(getPackageName());


        setContentView(R.layout.activity_map_picker);

        mapView = findViewById(R.id.map_view);
        mapView.setMultiTouchControls(true);

        MapController mapController = (MapController) mapView.getController();
        mapController.setZoom(15.0);

        // Default to a known location (e.g., Dimona) if no other location is passed
        GeoPoint startPoint = new GeoPoint(31.0652, 35.0336);
        mapController.setCenter(startPoint);

        findViewById(R.id.select_location_button).setOnClickListener(v -> {
            // Get the GeoPoint at the center of the map
            GeoPoint selectedPoint = (GeoPoint) mapView.getMapCenter();

            // Create an intent to return the result
            Intent resultIntent = new Intent();
            resultIntent.putExtra("latitude", selectedPoint.getLatitude());
            resultIntent.putExtra("longitude", selectedPoint.getLongitude());
            setResult(RESULT_OK, resultIntent);
            finish(); // Close this activity
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
}
