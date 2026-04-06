package com.example.finalsurmitapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

public class EmergencyAlertActivity extends AppCompatActivity {

    TextView txtWeather, txtWarning, txtTemp, txtHumidity, txtWind, txtLocation;
    Button btnRoute;

    double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_alert);

        // 🔥 connect views
        txtWeather = findViewById(R.id.txtWeather);
        txtWarning = findViewById(R.id.txtWarning);
        txtTemp = findViewById(R.id.txtTemp);
        txtHumidity = findViewById(R.id.txtHumidity);
        txtWind = findViewById(R.id.txtWind);
        txtLocation = findViewById(R.id.txtLocation);
        btnRoute = findViewById(R.id.btnRoute);

        // 🔥 get location from dashboard
        lat = getIntent().getDoubleExtra("lat", 0);
        lng = getIntent().getDoubleExtra("lng", 0);

        // 🔥 REAL WEATHER API CALL
        fetchWeather(lat, lng);

        // 🔥 Firebase LIVE alerts (auto update)
        loadAlertsFromFirebase();

        // 🔥 button click → open evacuation page
        btnRoute.setOnClickListener(v -> {
            Intent intent = new Intent(EmergencyAlertActivity.this, EvacuationActivity.class);
            intent.putExtra("lat", lat);
            intent.putExtra("lng", lng);
            startActivity(intent);
        });
    }

    // 🔥 LIVE FIREBASE (auto अपडेट)
    private void loadAlertsFromFirebase() {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("EmergencyAlerts");

        ref.addValueEventListener(new ValueEventListener() { // 🔥 live update
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                for (DataSnapshot snap : snapshot.getChildren()) {

                    String warning = snap.child("warning").getValue(String.class);

                    if (warning != null) {
                        txtWarning.setText(warning);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    // 🔥 REAL WEATHER API
    private void fetchWeather(double lat, double lng) {

        new Thread(() -> {
            try {
                String urlStr = "https://api.openweathermap.org/data/2.5/weather"
                        + "?lat=" + lat
                        + "&lon=" + lng
                        + "&appid=7ca53d8589deb1aaaf6d5024b19e0bc3"
                        + "&units=metric";

                java.net.URL url = new java.net.URL(urlStr);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.connect();

                java.io.InputStream is = conn.getInputStream();
                java.util.Scanner scanner = new java.util.Scanner(is).useDelimiter("\\A");
                String result = scanner.hasNext() ? scanner.next() : "";

                org.json.JSONObject json = new org.json.JSONObject(result);

                String temp = json.getJSONObject("main").getString("temp");
                String humidity = json.getJSONObject("main").getString("humidity");
                String wind = json.getJSONObject("wind").getString("speed");
                String weather = json.getJSONArray("weather")
                        .getJSONObject(0).getString("main");
                String city = json.getString("name");

                runOnUiThread(() -> {
                    txtWeather.setText("🌧 " + weather);
                    txtTemp.setText("Temperature: " + temp + "°C");
                    txtHumidity.setText("Humidity: " + humidity + "%");
                    txtWind.setText("Wind: " + wind + " km/h");
                    txtLocation.setText("📍 Location: " + city);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}