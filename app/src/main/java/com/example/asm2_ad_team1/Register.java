package com.example.asm2_ad_team1;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
        setContentView(R.layout.activity_register);

        res_username = findViewById(R.id.res_username);
        res_email = findViewById(R.id.res_email);
        res_password = findViewById(R.id.res_password);
        res_btn = findViewById(R.id.res_btn);
        routerLogin = findViewById(R.id.routeLogin);

        res_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = res_username.getText().toString().trim().toLowerCase(); // ✅ Now declared
                String email = res_email.getText().toString().trim();
                String rawPassword = res_password.getText().toString().trim();

                if (username.isEmpty() || email.isEmpty() || rawPassword.isEmpty()) {
                    Toast.makeText(Register.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Hash password before saving
                String hashedPassword = PasswordUtils.hashPassword(rawPassword);

                // Save to Firebase
                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");
                Helper helperClass = new Helper(email, hashedPassword, username);
                reference.child(username).setValue(helperClass);

                // Save to SQLite
                UserSQLiteHelper dbHelper = new UserSQLiteHelper(Register.this);
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(UserSQLiteHelper.COL_USERNAME, username);
                values.put(UserSQLiteHelper.COL_EMAIL, email);
                values.put(UserSQLiteHelper.COL_PASSWORD, hashedPassword);

                long rowId = db.insert(UserSQLiteHelper.TABLE_USERS, null, values);
                if (rowId != -1) {
                    Toast.makeText(Register.this, "Saved locally (SQLite)", Toast.LENGTH_SHORT).show();
                }

                Toast.makeText(Register.this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Register.this, Login.class));
                finish();
            }
        });

        routerLogin.setOnClickListener(view -> {
            startActivity(new Intent(Register.this, Login.class));
            finish();
        });
    }
}
