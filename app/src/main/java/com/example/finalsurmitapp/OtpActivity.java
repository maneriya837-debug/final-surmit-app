package com.example.finalsurmitapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OtpActivity extends AppCompatActivity {

    EditText otp1,otp2,otp3,otp4,otp5;
    Button btnVerify;

    String verificationId;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        otp1=findViewById(R.id.otp1);
        otp2=findViewById(R.id.otp2);
        otp3=findViewById(R.id.otp3);
        otp4=findViewById(R.id.otp4);
        otp5=findViewById(R.id.otp5);

        btnVerify=findViewById(R.id.btnVerify);

        mAuth=FirebaseAuth.getInstance();

        verificationId=getIntent().getStringExtra("verificationId");

        btnVerify.setOnClickListener(v -> verifyOTP());
    }

    private void verifyOTP(){

        String code = otp1.getText().toString() +
                otp2.getText().toString() +
                otp3.getText().toString() +
                otp4.getText().toString() +
                otp5.getText().toString();

        if(code.length()!=4){

            Toast.makeText(this,"Enter valid OTP",Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(verificationId,code);

        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential){

        mAuth.signInWithCredential(credential)

                .addOnCompleteListener(task -> {

                    if(task.isSuccessful()){

                        Toast.makeText(this,"OTP Verified",Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(OtpActivity.this,DashboardActivity.class));

                        finish();

                    }else{

                        Toast.makeText(this,"OTP Verification Failed",Toast.LENGTH_SHORT).show();

                    }

                });
    }
}