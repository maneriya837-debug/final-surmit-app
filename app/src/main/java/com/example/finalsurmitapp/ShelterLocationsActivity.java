package com.example.finalsurmitapp;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ShelterLocationsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private RecyclerView rvShelters;
    private ShelterAdapter shelterAdapter;
    private List<Shelter> shelterList;
    private CardView cardNearestShelter;
    private TextView tvNearestName, tvNearestStatus, tvNearestDistance;
    private Button btnGetDirections;

    private LatLng userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shelter_locations);

        // 📍 Get user location
        double userLat = getIntent().getDoubleExtra("lat", 19.0760);
        double userLng = getIntent().getDoubleExtra("lng", 72.8777);
        userLocation = new LatLng(userLat, userLng);

        // 🗺️ Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        // 📋 RecyclerView
        rvShelters = findViewById(R.id.rvShelters);
        rvShelters.setLayoutManager(new LinearLayoutManager(this));
        shelterList = new ArrayList<>();
        shelterAdapter = new ShelterAdapter(shelterList);
        rvShelters.setAdapter(shelterAdapter);

        // 📍 UI
        cardNearestShelter = findViewById(R.id.cardNearestShelter);
        tvNearestName = findViewById(R.id.tvNearestShelterName);
        tvNearestStatus = findViewById(R.id.tvNearestShelterStatus);
        tvNearestDistance = findViewById(R.id.tvNearestShelterDistance);
        btnGetDirections = findViewById(R.id.btnGetDirections);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadSheltersFromFirebase();
    }

    private void loadSheltersFromFirebase() {

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("shelters");

        dbRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                shelterList.clear();
                if (mMap != null) mMap.clear();

                double nearestDistance = Double.MAX_VALUE;
                Shelter nearestShelter = null;

                for (DataSnapshot data : snapshot.getChildren()) {

                    Shelter shelter = data.getValue(Shelter.class);

                    if (shelter != null) {

                        shelterList.add(shelter);

                        LatLng loc = new LatLng(shelter.getLat(), shelter.getLng());

                        // 🎨 Marker color
                        float color = shelter.getStatus().equalsIgnoreCase("OPEN") ?
                                BitmapDescriptorFactory.HUE_GREEN :
                                BitmapDescriptorFactory.HUE_RED;

                        mMap.addMarker(new MarkerOptions()
                                .position(loc)
                                .title(shelter.getName())
                                .snippet("Status: " + shelter.getStatus())
                                .icon(BitmapDescriptorFactory.defaultMarker(color)));

                        // 📏 Distance
                        float[] results = new float[1];
                        Location.distanceBetween(
                                userLocation.latitude,
                                userLocation.longitude,
                                shelter.getLat(),
                                shelter.getLng(),
                                results
                        );

                        if (results[0] < nearestDistance) {
                            nearestDistance = results[0];
                            nearestShelter = shelter;
                        }
                    }
                }

                shelterAdapter.notifyDataSetChanged();

                // 🔥 Show nearest shelter
                if (nearestShelter != null) {

                    tvNearestName.setText(nearestShelter.getName());
                    tvNearestStatus.setText("Status: " + nearestShelter.getStatus());
                    tvNearestDistance.setText(String.format("%.2f km away", nearestDistance / 1000));

                    Shelter finalNearestShelter = nearestShelter;

                    // 👉 Button click navigation
                    btnGetDirections.setOnClickListener(v -> {
                        Uri uri = Uri.parse("google.navigation:q="
                                + finalNearestShelter.getLat() + ","
                                + finalNearestShelter.getLng());

                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.setPackage("com.google.android.apps.maps");
                        startActivity(intent);
                    });

                    // 🚀 AUTO NAVIGATION (NEW FEATURE)
                    Uri uri = Uri.parse("google.navigation:q="
                            + nearestShelter.getLat() + ","
                            + nearestShelter.getLng());

                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setPackage("com.google.android.apps.maps");
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ShelterLocationsActivity.this,
                        "Failed to load shelters", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12f));

        // 📍 User marker
        mMap.addMarker(new MarkerOptions()
                .position(userLocation)
                .title("You are here"));
    }
}