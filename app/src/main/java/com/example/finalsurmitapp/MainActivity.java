package com.example.finalsurmitapp;

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

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtForgot = findViewById(R.id.txtForgot);

        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(v -> loginUser());

        txtForgot.setOnClickListener(v -> {

            Intent intent = new Intent(MainActivity.this, OtpActivity.class);
            startActivity(intent);

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

                        Toast.makeText(this,"Login Successful",Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                        finish();

                    }else{

                        Toast.makeText(this,"Login Failed",Toast.LENGTH_SHORT).show();

                    }

                });
    }
}