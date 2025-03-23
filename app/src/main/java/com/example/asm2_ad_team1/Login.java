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

public class Login extends AppCompatActivity {

    EditText login_username, login_password;
    TextView routerRes;
    Button login_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        login_username = findViewById(R.id.login_username);
        login_password = findViewById(R.id.login_password);
        routerRes = findViewById(R.id.routeRes);
        login_btn = findViewById(R.id.login_btn);

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateUsername() | !validatePassword()) {
                    // Don't proceed if input is invalid
                } else {
                    checkUser();
                }
            }
        });

        routerRes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
            }
        });
    }

    public boolean validateUsername() {
        String value = login_username.getText().toString().trim();
        if (value.isEmpty()) {
            login_username.setError("Username cannot be empty");
            return false;
        } else {
            login_username.setError(null);
            return true;
        }
    }

    public boolean validatePassword() {
        String value = login_password.getText().toString().trim();
        if (value.isEmpty()) {
            login_password.setError("Password cannot be empty");
            return false;
        } else {
            login_password.setError(null);
            return true;
        }
    }

    public void checkUser() {
        String username = login_username.getText().toString().trim().toLowerCase(); // force lowercase
        String userpassword = login_password.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

        reference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String passwordFromDB = snapshot.child("password").getValue(String.class);

                    if (passwordFromDB != null && passwordFromDB.equals(userpassword)) {
                        // Login successful
                        Toast.makeText(Login.this, "Login successful!", Toast.LENGTH_SHORT).show();

                        // Proceed to MainActivity
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        login_password.setError("Invalid credentials");
                        login_password.requestFocus();
                    }
                } else {
                    login_username.setError("User does not exist");
                    login_username.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("LoginError", "Database error: " + error.getMessage());
                Toast.makeText(Login.this, "Database error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
