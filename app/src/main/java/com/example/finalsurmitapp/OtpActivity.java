package com.example.finalsurmitapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {

    EditText otp1, otp2, otp3, otp4, otp5;
    Button btnVerify;
    TextView txtResend;

    String verificationId;
    PhoneAuthProvider.ForceResendingToken resendToken;

    FirebaseAuth mAuth;

    String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otp5 = findViewById(R.id.otp5);

        btnVerify = findViewById(R.id.btnVerify);
        txtResend = findViewById(R.id.txtResend);

        mAuth = FirebaseAuth.getInstance();

        // Get phone number from previous screen
        phoneNumber = getIntent().getStringExtra("phone");

        if(phoneNumber == null){
            Toast.makeText(this,"Phone number missing",Toast.LENGTH_SHORT).show();
            finish();
        }

        // SEND OTP FIRST TIME
        sendOTP(phoneNumber);

        // VERIFY BUTTON
        btnVerify.setOnClickListener(v -> verifyOTP());

        // RESEND OTP
        txtResend.setOnClickListener(v -> resendOTP());

        // TIMER START
        startTimer();
    }

    // ================= SEND OTP =================
    private void sendOTP(String phone){

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + phone,   // India code
                60,
                TimeUnit.SECONDS,
                this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        // Auto verification
                        signInWithCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {

                    }

                    public void onVerificationFailed(Exception e) {
                        Toast.makeText(OtpActivity.this,
                                "OTP Failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(String verId,
                                           PhoneAuthProvider.ForceResendingToken token) {

                        verificationId = verId;
                        resendToken = token;

                        Toast.makeText(OtpActivity.this,
                                "OTP Sent Successfully",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    // ================= RESEND OTP =================
    private void resendOTP(){

        if(phoneNumber == null) return;

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        signInWithCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {

                    }

                    public void onVerificationFailed(Exception e) {
                        Toast.makeText(OtpActivity.this,
                                "Resend Failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(String verId,
                                           PhoneAuthProvider.ForceResendingToken token) {

                        verificationId = verId;
                        resendToken = token;

                        Toast.makeText(OtpActivity.this,
                                "OTP Resent",
                                Toast.LENGTH_SHORT).show();

                        startTimer(); // restart timer
                    }
                }
        );
    }

    // ================= VERIFY OTP =================
    private void verifyOTP(){

        String code = otp1.getText().toString().trim() +
                otp2.getText().toString().trim() +
                otp3.getText().toString().trim() +
                otp4.getText().toString().trim() +
                otp5.getText().toString().trim();

        if(code.length() != 5){
            Toast.makeText(this,"Enter valid OTP",Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(verificationId)){
            Toast.makeText(this,"Verification ID missing",Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(verificationId, code);

        signInWithCredential(credential);
    }

    // ================= SIGN IN =================
    private void signInWithCredential(PhoneAuthCredential credential){

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {

                    if(task.isSuccessful()){

                        Toast.makeText(this,"OTP Verified",Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(OtpActivity.this, DashboardActivity.class));
                        finish();

                    }else{

                        Toast.makeText(this,"Invalid OTP",Toast.LENGTH_SHORT).show();
                    }

                });
    }

    // ================= TIMER =================
    private void startTimer(){

        txtResend.setEnabled(false);

        new CountDownTimer(15000,1000){

            public void onTick(long millisUntilFinished){
                txtResend.setText("Resend in " + millisUntilFinished/1000 + "s");
            }

            public void onFinish(){
                txtResend.setText("Resend OTP");
                txtResend.setEnabled(true);
            }

        }.start();
    }

    // ================= BACK BUTTON =================
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}