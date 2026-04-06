package com.example.finalsurmitapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();

        new Handler().postDelayed(() -> {

            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            boolean isFirstTime = prefs.getBoolean("isFirstTime", true);

            FirebaseUser user = mAuth.getCurrentUser();

            if (user != null) {
                // ✅ Already logged in
                startActivity(new Intent(SplashActivity.this, DashboardActivity.class));

            } else {
                if (isFirstTime) {
                    // 🆕 First time → Registration Page
                    startActivity(new Intent(SplashActivity.this, RegistrationActivity.class));

                } else {
                    // 🔁 Old user but logged out → Login Page
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                }
            }

            finish();

        }, 3000);
    }
}