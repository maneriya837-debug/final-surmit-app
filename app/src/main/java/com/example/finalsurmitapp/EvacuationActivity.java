package com.example.finalsurmitapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class EvacuationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    double userLat, userLng;

    Button btnNavigate, btnCall, btnShare, btnSOS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evacuation);

        // 🔥 GET LOCATION FROM DASHBOARD
        userLat = getIntent().getDoubleExtra("lat", 0);
        userLng = getIntent().getDoubleExtra("lng", 0);

        btnNavigate = findViewById(R.id.btnNavigate);
        btnCall = findViewById(R.id.btnCall);
        btnShare = findViewById(R.id.btnShare);
        btnSOS = findViewById(R.id.btnSOS);

        // 🔥 MAP LOAD
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.evacMap);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // 🔥 WEATHER CHECK → FLOOD ALERT
        checkFloodRisk();

        // ================= NAVIGATION =================
        btnNavigate.setOnClickListener(v -> {

            double shelterLat = userLat - 0.01;
            double shelterLng = userLng - 0.01;

            Uri uri = Uri.parse("google.navigation:q=" + shelterLat + "," + shelterLng);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.google.android.apps.maps");
            startActivity(intent);
        });

        // ================= CALL =================
        btnCall.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:112"));
            startActivity(callIntent);
        });

        // ================= SHARE =================
        btnShare.setOnClickListener(v -> {

            String message = "🚨 Emergency! My Location: https://maps.google.com/?q="
                    + userLat + "," + userLng;

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        // ================= SOS =================
        btnSOS.setOnClickListener(v -> {
            sendSOS();
        });
    }

    // ================= MAP =================
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng user = new LatLng(userLat, userLng);

        mMap.addMarker(new MarkerOptions()
                .position(user)
                .title("You are here"));

        LatLng shelter = new LatLng(userLat - 0.01, userLng - 0.01);

        mMap.addMarker(new MarkerOptions()
                .position(shelter)
                .title("Safe Shelter")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(user, 14));

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);
        }
    }

    // ================= 🔥 FLOOD PREDICTION =================
    private void checkFloodRisk() {

        new Thread(() -> {
            try {

                String urlStr = "https://api.openweathermap.org/data/2.5/weather"
                        + "?lat=" + userLat
                        + "&lon=" + userLng
                        + "&appid=7ca53d8589deb1aaaf6d5024b19e0bc3"
                        + "&units=metric";

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();

                Scanner scanner = new Scanner(conn.getInputStream()).useDelimiter("\\A");
                String result = scanner.hasNext() ? scanner.next() : "";

                JSONObject json = new JSONObject(result);

                double rainfall = 0;

                if (json.has("rain")) {
                    rainfall = json.getJSONObject("rain").optDouble("1h", 0);
                }

                double finalRainfall = rainfall;

                runOnUiThread(() -> {

                    if (finalRainfall > 10) {
                        Toast.makeText(this, "⚠ High Flood Risk!", Toast.LENGTH_LONG).show();
                    } else if (finalRainfall > 5) {
                        Toast.makeText(this, "⚠ Moderate Flood Risk", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "✅ Safe Area", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ================= 🔥 SOS FIREBASE =================
    private void sendSOS() {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("SOS_Alerts");

        String id = ref.push().getKey();

        SOSModel model = new SOSModel(userLat, userLng, System.currentTimeMillis());

        ref.child(id).setValue(model)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "🚨 SOS Sent!", Toast.LENGTH_SHORT).show();

                    // 🔥 FUTURE: Push Notification trigger
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show());
    }
}