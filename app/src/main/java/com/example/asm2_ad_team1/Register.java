package com.example.asm2_ad_team1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Register extends AppCompatActivity {

    EditText res_username, res_email, res_password;
    TextView routerLogin;
    Button res_btn;

    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Bind views
        res_username = findViewById(R.id.res_username);
        res_email = findViewById(R.id.res_email);
        res_password = findViewById(R.id.res_password);
        res_btn = findViewById(R.id.res_btn);
        routerLogin = findViewById(R.id.routeLogin);

        // Firebase setup
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");

        // Register button
        res_btn.setOnClickListener(view -> {
            String email = res_email.getText().toString().trim();
            String password = res_password.getText().toString().trim();
            String username = res_username.getText().toString().trim().toLowerCase(); // enforce lowercase

            // Simple validation
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(Register.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Optional: enforce format
            if (!username.matches("^[a-z0-9_]+$")) {
                res_username.setError("Username must be lowercase letters/numbers only");
                return;
            }

            // Check if user already exists
            reference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        res_username.setError("Username already exists");
                    } else {
                        Helper helperClass = new Helper(username ,email, password );
                        reference.child(username).setValue(helperClass)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Register.this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                                        Log.d("Register", "User saved: " + username);

                                        Intent intent = new Intent(Register.this, Login.class);
                                        intent.putExtra("username", username);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(Register.this, "Registration failed: " + task.getException(), Toast.LENGTH_LONG).show();
                                        Log.e("Register", "Save failed: ", task.getException());
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Register.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Register", "onCancelled: " + error.getMessage());
                }
            });
        });

        // Go to login
        routerLogin.setOnClickListener(view -> {
            Intent intent = new Intent(Register.this, Login.class);
            startActivity(intent);
            finish();
        });
    }
}
