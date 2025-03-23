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
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

        res_username = findViewById(R.id.res_username);   // ID from your register layout
        res_email = findViewById(R.id.res_email);         // FIXED: email input ID
        res_password = findViewById(R.id.res_password);   // FIXED: password input ID
        res_btn = findViewById(R.id.res_btn);
        routerLogin = findViewById(R.id.routeLogin);

        res_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = res_username.getText().toString().trim();
                String email = res_email.getText().toString().trim();
                String password = res_password.getText().toString().trim();

                if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(Register.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");

                Helper helperClass = new Helper(email, password, username );
                reference.child(username).setValue(helperClass);

                Toast.makeText(Register.this, "You have registered successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);
                Log.d("Register", "User saved to Firebase: " + username);

                finish();
            }
        });

        routerLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
