package com.example.finalsurmitapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegistrationActivity extends AppCompatActivity {

    EditText etFullName, etEmail, etMobile, etPassword, etConfirmPassword;
    Button btnVerify;
    TextView tvLogin;
    ImageView btnBack;
    EditText etPhone;


    FirebaseAuth mAuth;
    DatabaseReference databaseReference;

    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_main);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnVerify = findViewById(R.id.btnVerify);
        tvLogin = findViewById(R.id.tvLogin);
        btnBack = findViewById(R.id.btnBack);
        etPhone = findViewById(R.id.etPhone);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // 🔙 Back button
        btnBack.setOnClickListener(v -> finish());

        // 🔐 Register button
        btnVerify.setOnClickListener(v -> registerUser());

        // 🔁 Login click
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
            finish();
        });
    }

    private void registerUser() {

        String name = etFullName.getText().toString().trim();
        String email = etMobile.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etFullName.setError("Enter Name");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etMobile.setError("Enter Email");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Enter Phone");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Enter Password");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Password not match");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Min 6 characters");
            return;
        }

        // 🔥 Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        // 🔥 Get User ID
                        String userId = mAuth.getCurrentUser().getUid();

                        // 🔥 Save data in Firebase Realtime DB
                        DatabaseReference ref = FirebaseDatabase.getInstance()
                                .getReference("Users");

                        HashMap<String, String> userMap = new HashMap<>();
                        userMap.put("name", name);
                        userMap.put("email", email);
                        userMap.put("phone", phone);

                        ref.child(userId).setValue(userMap);

                        Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();

                        // ✅ Dashboard Open
                        startActivity(new Intent(RegistrationActivity.this, DashboardActivity.class));
                        finish();

                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}