package com.example.finalsurmitapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class DashboardActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient locationClient;
    private static final int LOCATION_PERMISSION_REQUEST = 101;

    Button btnEvacuate;
    LinearLayout btnEmergency, btnShelterLocations;
    FrameLayout btnSOS;
    ImageView settingsIcon;

    double currentLat = 0.0, currentLng = 0.0;

    DatabaseReference sosRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 🔹 Buttons
        btnEvacuate = findViewById(R.id.btnEvacuate);
        btnEmergency = findViewById(R.id.btnEmergency);
        btnShelterLocations = findViewById(R.id.btnShelterLocations);
        btnSOS = findViewById(R.id.btnSOS);
        settingsIcon = findViewById(R.id.settingsIcon);
        settingsIcon = findViewById(R.id.settingsIcon);

// Settings click listener
        settingsIcon.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(DashboardActivity.this, SettingsActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(DashboardActivity.this, "Failed to open Settings", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });

        // 🔹 Firebase reference
        sosRef = FirebaseDatabase.getInstance().getReference("SOS_Requests");

        // 🔹 Evacuate
        btnEvacuate.setOnClickListener(v -> {
            Toast.makeText(this, "Evacuation Started!", Toast.LENGTH_SHORT).show();
            saveEvacuationAlert();

            Intent intent = new Intent(DashboardActivity.this, EvacuationActivity.class);
            intent.putExtra("lat", currentLat);
            intent.putExtra("lng", currentLng);
            startActivity(intent);
        });

        // 🔹 Emergency Alerts
        btnEmergency.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Alerts...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(DashboardActivity.this, EmergencyAlertActivity.class);
            intent.putExtra("lat", currentLat);
            intent.putExtra("lng", currentLng);
            startActivity(intent);
        });

        // 🔹 Shelter Locations
        btnShelterLocations.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ShelterLocationsActivity.class);
            intent.putExtra("lat", currentLat);
            intent.putExtra("lng", currentLng);
            startActivity(intent);
        });

        // 🔴 SOS Button
        btnSOS.setOnClickListener(v -> {
            sendSOS(); // Firebase SOS

            // Open Emergency Contacts Screen safely
            try {
                Intent intent = new Intent(DashboardActivity.this, EmergencyContactsActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(DashboardActivity.this, "Failed to open Emergency Contacts", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });

        // 🔹 Location init
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        // 🔹 Map init
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        checkPermission();
    }

    // 🔐 Permission check
    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            getLocation();
        }
    }

    // 📍 LIVE LOCATION TRACKING
    @SuppressLint("MissingPermission")
    private void getLocation() {

        LocationRequest locationRequest = new LocationRequest.Builder(3000).build();

        locationClient.requestLocationUpdates(
                locationRequest,
                new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult result) {
                        if (result == null) return;

                        for (android.location.Location location : result.getLocations()) {

                            currentLat = location.getLatitude();
                            currentLng = location.getLongitude();

                            LatLng user = new LatLng(currentLat, currentLng);

                            mMap.clear();

                            mMap.addMarker(new MarkerOptions()
                                    .position(user)
                                    .title("You are here"));

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(user, 15));
                            mMap.setMyLocationEnabled(true);

                            addMarkers(user);
                        }
                    }
                },
                getMainLooper()
        );
    }

    // 📍 Demo markers
    private void addMarkers(LatLng user) {

        LatLng flood = new LatLng(user.latitude + 0.01, user.longitude + 0.01);
        mMap.addMarker(new MarkerOptions()
                .position(flood)
                .title("Flood Area")
                .snippet("Avoid this area")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        LatLng shelter = new LatLng(user.latitude - 0.01, user.longitude - 0.01);
        mMap.addMarker(new MarkerOptions()
                .position(shelter)
                .title("Safe Shelter")
                .snippet("Go here for safety")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    }

    // 🔴 SOS FUNCTION
    private void sendSOS() {

        if (currentLat == 0.0 || currentLng == 0.0) {
            Toast.makeText(this, "Getting location... Try again!", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = sosRef.push().getKey();

        HashMap<String, Object> map = new HashMap<>();
        map.put("latitude", currentLat);
        map.put("longitude", currentLng);
        map.put("time", System.currentTimeMillis());
        map.put("status", "ACTIVE");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) map.put("userId", user.getUid());

        if (id != null) {
            sosRef.child(id).setValue(map)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(DashboardActivity.this, "🚨 SOS Sent Successfully!", Toast.LENGTH_LONG).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(DashboardActivity.this, "Failed to send SOS", Toast.LENGTH_SHORT).show()
                    );
        } else {
            Toast.makeText(this, "Failed to generate SOS request", Toast.LENGTH_SHORT).show();
        }
    }

    // 🔥 Firebase Save (Evacuation)
    private void saveEvacuationAlert() {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("EvacuationAlerts");

        String id = ref.push().getKey();

        HashMap<String, Object> map = new HashMap<>();
        map.put("status", "Evacuated");
        map.put("time", System.currentTimeMillis());
        map.put("latitude", currentLat);
        map.put("longitude", currentLng);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) map.put("userId", user.getUid());

        if (id != null) {
            ref.child(id).setValue(map)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(this, "Saved to Firebase", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show()
                    );
        }
    }

    // 🔁 Permission result
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                getLocation();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}