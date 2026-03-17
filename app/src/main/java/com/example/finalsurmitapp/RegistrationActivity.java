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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity {

    EditText etFullName, etEmail, etMobile, etPassword, etConfirmPassword;
    Button btnVerify;
    TextView tvLogin;

    FirebaseAuth mAuth;
    DatabaseReference databaseReference;

    @SuppressLint("MissingInflatedId")
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

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        btnVerify.setOnClickListener(v -> registerUser());

        tvLogin.setOnClickListener(v -> {

            Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
            startActivity(intent);

        });
    }

    private void registerUser() {

        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        if(TextUtils.isEmpty(name) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(mobile) || TextUtils.isEmpty(password)){

            Toast.makeText(this,"Please fill all fields",Toast.LENGTH_SHORT).show();
            return;
        }

        if(!password.equals(confirm)){

            Toast.makeText(this,"Password does not match",Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email,password)

                .addOnCompleteListener(task -> {

                    if(task.isSuccessful()){

                        String userId = mAuth.getCurrentUser().getUid();

                        UserModel user = new UserModel(name,email,mobile);

                        databaseReference.child(userId).setValue(user);

                        Toast.makeText(this,"Registration Successful",Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(this,DashboardActivity.class));
                        finish();

                    }else{

                        Toast.makeText(this,task.getException().getMessage(),Toast.LENGTH_LONG).show();

                    }

                });
    }
}