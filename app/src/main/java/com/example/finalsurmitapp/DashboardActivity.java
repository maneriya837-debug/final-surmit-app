package com.example.finalsurmitapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DashboardActivity extends AppCompatActivity {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;

    private double curLat = 0;
    private double curLng = 0;

    private static final String API_KEY = "AIzaSyBTzcHFv78SYN7x6U9UWv22nnfAV5uvHPk";

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {

                if(granted){
                    loadMapAndLocation();
                }else{
                    Toast.makeText(this,"Location Permission Required",Toast.LENGTH_SHORT).show();
                }

            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);

        if(mapFragment != null){

            mapFragment.getMapAsync(googleMap -> {

                mMap = googleMap;

                checkPermission();

            });

        }
    }

    private void checkPermission(){

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){

            loadMapAndLocation();

        }else{

            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);

        }

    }

    @SuppressLint("MissingPermission")
    private void loadMapAndLocation(){

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {

            if(location == null){
                Toast.makeText(this,"Location unavailable",Toast.LENGTH_SHORT).show();
                return;
            }

            curLat = location.getLatitude();
            curLng = location.getLongitude();

            LatLng me = new LatLng(curLat,curLng);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(me,14f));

            mMap.addMarker(new MarkerOptions()
                    .position(me)
                    .title("You are here"));

            fetchNearbyPlaces(curLat,curLng,"hospital");

        });

    }

    private void fetchNearbyPlaces(double lat,double lng,String type){

        new Thread(() -> {

            try{

                String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
                        + "?location=" + lat + "," + lng
                        + "&radius=3000"
                        + "&type=" + type
                        + "&key=" + API_KEY;

                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder().url(url).build();

                Response response = client.newCall(request).execute();

                if(!response.isSuccessful() || response.body() == null) return;

                String body = response.body().string();

                JSONObject root = new JSONObject(body);

                JSONArray results = root.getJSONArray("results");

                for(int i=0;i<results.length();i++){

                    JSONObject p = results.getJSONObject(i);

                    String name = p.optString("name");

                    JSONObject loc = p.getJSONObject("geometry")
                            .getJSONObject("location");

                    double plat = loc.getDouble("lat");
                    double plng = loc.getDouble("lng");

                    LatLng place = new LatLng(plat,plng);

                    runOnUiThread(() -> {

                        mMap.addMarker(new MarkerOptions()
                                .position(place)
                                .title(name));

                    });

                }

            }catch(Exception e){

                runOnUiThread(() ->
                        Toast.makeText(this,"API Error",Toast.LENGTH_SHORT).show());

            }

        }).start();

    }

}