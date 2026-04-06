package com.example.finalsurmitapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class EmergencyContactsActivity extends AppCompatActivity {

    ImageButton callPolice, callAmbulance, callFire;
    Button btnSOS, btnAddContact;
    LinearLayout contactsContainer;

    ArrayList<String> emergencyContacts = new ArrayList<>();
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contact);

        callPolice = findViewById(R.id.callPolice);
        callAmbulance = findViewById(R.id.callAmbulance);
        callFire = findViewById(R.id.callFire);
        btnSOS = findViewById(R.id.btnSOS);
        btnAddContact = findViewById(R.id.btnAddContact);
        contactsContainer = findViewById(R.id.contactsContainer);

        // 🔹 SharedPreferences for storing contacts
        prefs = getSharedPreferences("emergency_contacts", MODE_PRIVATE);
        loadContacts();
        displayContacts();

        // 🔹 CALL BUTTONS
        callPolice.setOnClickListener(v -> makeCall("100"));
        callAmbulance.setOnClickListener(v -> makeCall("108"));
        callFire.setOnClickListener(v -> makeCall("101"));

        // 🔹 SOS BUTTON
        btnSOS.setOnClickListener(v -> sendSOS());

        // 🔹 ADD NEW CONTACT
        btnAddContact.setOnClickListener(v -> showAddContactDialog());
    }

    // 📞 CALL FUNCTION
    private void makeCall(String number) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, 1);
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        startActivity(intent);
    }

    // 🚨 SOS FUNCTION
    private void sendSOS() {
        if (emergencyContacts.isEmpty()) {
            Toast.makeText(this, "No contacts available!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, 2);
            return;
        }

        String message = "🚨 EMERGENCY! I need help. Please reach me immediately.";

        for (String number : emergencyContacts) {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(number, null, message, null, null);
        }

        Toast.makeText(this, "SOS sent to all contacts!", Toast.LENGTH_SHORT).show();
    }

    // 🔹 ADD CONTACT DIALOG
    private void showAddContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Emergency Contact");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Enter phone number");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String number = input.getText().toString().trim();
            if (!number.isEmpty()) {
                emergencyContacts.add(number);
                saveContacts();
                displayContacts();
                Toast.makeText(this, "Contact Added!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Enter a valid number", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // 🔹 DISPLAY CONTACTS
    private void displayContacts() {
        contactsContainer.removeAllViews();
        for (String number : emergencyContacts) {
            TextView tv = new TextView(this);
            tv.setText(number);
            tv.setTextSize(18);
            tv.setPadding(10, 10, 10, 10);
            tv.setOnClickListener(v -> makeCall(number)); // Tap to call
            contactsContainer.addView(tv);
        }
    }

    // 🔹 SAVE CONTACTS
    private void saveContacts() {
        Set<String> set = new HashSet<>(emergencyContacts);
        prefs.edit().putStringSet("contacts", set).apply();
    }

    // 🔹 LOAD CONTACTS
    private void loadContacts() {
        Set<String> set = prefs.getStringSet("contacts", null);
        if (set != null) emergencyContacts.addAll(set);
    }

    // 🔁 PERMISSION RESULT
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Call Permission Granted", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSOS();
            }
        }
    }
}