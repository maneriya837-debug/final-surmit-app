package com.example.finalsurmitapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsActivity extends AppCompatActivity {

    CardView logoutCard, privacyPolicyCard, changePasswordCard, AboutUs;
    Switch darkModeSwitch, notificationSwitch;
    TextView profileName;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        // 🔹 Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // 🔹 SharedPreferences for toggles
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // 🔹 Views
        profileName = findViewById(R.id.profileName);
        darkModeSwitch = findViewById(R.id.darkModeSwitch);
        notificationSwitch = findViewById(R.id.notificationSwitch);
        logoutCard = findViewById(R.id.logoutCard);
        privacyPolicyCard = findViewById(R.id.privacyPolicyCard);
        changePasswordCard = findViewById(R.id.changePasswordCard);
        AboutUs = findViewById(R.id.AboutUs);
        // 🔹 Set user name
        if(currentUser != null){
            profileName.setText(currentUser.getDisplayName() != null ?
                    currentUser.getDisplayName() : "User");
        }

        // 🔹 Dark mode toggle
        darkModeSwitch.setChecked(sharedPreferences.getBoolean("dark_mode", false));
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("dark_mode", isChecked);
            editor.apply();
            Toast.makeText(this, "Dark Mode " + (isChecked ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
        });

        // 🔹 Notification toggle
        notificationSwitch.setChecked(sharedPreferences.getBoolean("notifications", true));
        notificationSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            editor.putBoolean("notifications", isChecked);
            editor.apply();
            Toast.makeText(this, "Notifications " + (isChecked ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
        });

        // 🔹 Privacy Policy click
        privacyPolicyCard.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, PrivacyPolicyActivity.class);
            startActivity(intent);
        });

        // 🔹 Change Password click
        changePasswordCard.setOnClickListener(v -> {
            if(currentUser != null && currentUser.getEmail() != null){
                mAuth.sendPasswordResetEmail(currentUser.getEmail())
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                Toast.makeText(SettingsActivity.this, "Reset link sent to email", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(SettingsActivity.this, "Failed to send reset link", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // 🔹 Logout click
        logoutCard.setOnClickListener(v -> showLogoutDialog());
    }

    // 🔹 Logout temporary/permanent dialog
    private void showLogoutDialog() {
        String[] options = {"Temporary Logout (Keep Data)", "Permanent Logout (Delete Data)"};

        new AlertDialog.Builder(this)
                .setTitle("Choose Logout Type")
                .setItems(options, (dialog, which) -> {
                    if(which == 0){
                        // 🔹 Temporary logout
                        mAuth.signOut();
                        Toast.makeText(this, "Logged out temporarily", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                        finish();
                    } else {
                        // 🔹 Permanent logout
                        if(currentUser != null){
                            String uid = currentUser.getUid();
                            // 🔹 Delete user data from Firebase Realtime Database (example path)
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(uid).removeValue()
                                    .addOnSuccessListener(unused -> {
                                        currentUser.delete().addOnCompleteListener(task -> {
                                            if(task.isSuccessful()){
                                                Toast.makeText(this, "Account permanently deleted", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(SettingsActivity.this, RegistrationActivity.class));
                                                finish();
                                            } else {
                                                Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to remove user data", Toast.LENGTH_SHORT).show());
                        }
                    }
                }).show();
    }
}