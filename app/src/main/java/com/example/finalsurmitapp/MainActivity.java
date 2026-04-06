package com.example.finalsurmitapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView txtForgot;
    TextView txtSignup;

    FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtForgot = findViewById(R.id.txtForgot);

        mAuth = FirebaseAuth.getInstance();

        // 🔥 LOGIN BUTTON
        btnLogin.setOnClickListener(v -> loginUser());

        // 🔥 FORGOT PASSWORD (👉 इथे add kar)
        txtForgot.setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim();

            if(TextUtils.isEmpty(email)){
                Toast.makeText(this,"Enter email first",Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Toast.makeText(this,"Reset link sent to email",Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(this,"Error: "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });
        });
        txtSignup = findViewById(R.id.txtSignup);

        txtSignup.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
        });
    }

    private void loginUser(){

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){

            Toast.makeText(this,"Enter Email & Password",Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(task -> {

                    if(task.isSuccessful()){

                        startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                        finish();

                    }else{
                        Toast.makeText(this,"Wrong Email or Password",Toast.LENGTH_SHORT).show();
                    }

                });


    }
}