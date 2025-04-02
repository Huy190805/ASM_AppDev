package com.example.asm2_ad_team1;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Login extends AppCompatActivity {

    EditText login_username, login_password;
    TextView routerRes;
    Button login_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login_username = findViewById(R.id.login_username);
        login_password = findViewById(R.id.login_password);
        routerRes = findViewById(R.id.routeRes);
        login_btn = findViewById(R.id.login_btn);

        login_btn.setOnClickListener(view -> {
            if (!validateUsername() | !validatePassword()) return;
            checkUser(); // Try Firebase first
        });

        routerRes.setOnClickListener(view -> {
            startActivity(new Intent(Login.this, Register.class));
        });
        printLocalUsers();


    }

    private void printLocalUsers() {
        UserSQLiteHelper dbHelper = new UserSQLiteHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(UserSQLiteHelper.TABLE_USERS, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String user = cursor.getString(cursor.getColumnIndexOrThrow(UserSQLiteHelper.COL_USERNAME));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(UserSQLiteHelper.COL_EMAIL));
            Log.d("SQLiteUser", "User: " + user + ", Email: " + email);
        }
        cursor.close();
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
        String username = login_username.getText().toString().trim().toLowerCase();
        String userpassword = login_password.getText().toString().trim();
        String hashedInputPassword = PasswordUtils.hashPassword(userpassword);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

        reference.child(username).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String passwordFromDB = snapshot.child("password").getValue(String.class);
                    if (passwordFromDB != null && passwordFromDB.equals(hashedInputPassword)) {
                        // ✅ Role-based redirect
                        String role = snapshot.child("role").getValue(String.class);
                        if (role != null && role.equalsIgnoreCase("admin")) {
                            Toast.makeText(Login.this, "Welcome admin!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Login.this, AdminDashboardActivity.class);
                            intent.putExtra("username", username);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(Login.this, "Login successful (Firebase)", Toast.LENGTH_SHORT).show();
                            proceedToMain(username);
                        }
                    } else {
                        login_password.setError("Invalid credentials");
                        login_password.requestFocus();
                    }
                } else {
                    // Fallback to local SQLite check
                    checkUserFromSQLite(username, hashedInputPassword);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("LoginError", "Database error: " + error.getMessage());
                Toast.makeText(Login.this, "Firebase error. Trying local login...", Toast.LENGTH_SHORT).show();
                checkUserFromSQLite(username, hashedInputPassword);
            }
        });
    }

    public void checkUserFromSQLite(String username, String hashedPassword) {
        UserSQLiteHelper dbHelper = new UserSQLiteHelper(Login.this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                UserSQLiteHelper.TABLE_USERS,
                null,
                UserSQLiteHelper.COL_USERNAME + "=? AND " + UserSQLiteHelper.COL_PASSWORD + "=?",
                new String[]{username, hashedPassword},
                null, null, null);

        if (cursor.moveToFirst()) {
            Toast.makeText(this, "Login successful (SQLite)", Toast.LENGTH_SHORT).show();
            proceedToMain(username);
        } else {
            login_username.setError("User not found");
            login_password.setError("Invalid login");
            login_username.requestFocus();
        }

        cursor.close();
    }

    private void proceedToMain(String username) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(username);
        ref.child("role").get().addOnCompleteListener(task -> {
            String role = "user"; // default
            if (task.isSuccessful() && task.getResult().getValue() != null) {
                role = task.getResult().getValue(String.class);
            }

            Intent intent;
            if ("admin".equalsIgnoreCase(role)) {
                intent = new Intent(Login.this, AdminDashboardActivity.class);
            } else {
                intent = new Intent(Login.this, MainActivity.class);
            }

            intent.putExtra("username", username);
            startActivity(intent);
            finish();
        });
    }
}
