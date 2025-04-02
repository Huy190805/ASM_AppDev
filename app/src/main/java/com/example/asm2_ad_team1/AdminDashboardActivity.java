package com.example.asm2_ad_team1;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;


public class AdminDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private String currentUsername;

    private Button btnManageUsers, btnViewReports, btnLogout;
    private TextView tvWelcome;

    private Button btnAddUser;
    private Button btnEditUser;
    private Button btnDeleteUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        currentUsername = getIntent().getStringExtra("username");

        Toolbar toolbar = findViewById(R.id.toolbar_admin);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout_admin);
        tvWelcome = findViewById(R.id.tv_admin_welcome);
        tvWelcome.setText("Welcome, Admin: " + currentUsername);


        btnViewReports = findViewById(R.id.btn_view_reports);
        btnLogout = findViewById(R.id.btn_admin_logout);

        btnAddUser = findViewById(R.id.btn_add_user);
        btnAddUser.setOnClickListener(v -> showAddUserDialog());

        btnEditUser = findViewById(R.id.btn_edit_user);
        btnEditUser.setOnClickListener(v -> showEditUserDialog());

        btnDeleteUser = findViewById(R.id.btn_delete_user);
        btnDeleteUser.setOnClickListener(v -> showDeleteUserDialog());



        btnManageUsers = findViewById(R.id.btn_manage_users);
        btnViewReports = findViewById(R.id.btn_view_reports);
        btnLogout = findViewById(R.id.btn_admin_logout);

        btnManageUsers.setOnClickListener(v ->
                Toast.makeText(this, "Manage Users (not implemented)", Toast.LENGTH_SHORT).show());


        btnViewReports.setOnClickListener(v ->
                Toast.makeText(this, "View Reports (not implemented)", Toast.LENGTH_SHORT).show());

        btnLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Admin logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AdminDashboardActivity.this, Login.class));
            finish();
        });
    }


    private void showAddUserDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_user, null);
        EditText inputUsername = view.findViewById(R.id.input_new_username);
        EditText inputEmail = view.findViewById(R.id.input_new_email);
        EditText inputPassword = view.findViewById(R.id.input_new_password);

        new AlertDialog.Builder(this)
                .setTitle("Add New User")
                .setView(view)
                .setPositiveButton("Add", (dialog, which) -> {
                    String username = inputUsername.getText().toString().trim().toLowerCase();
                    String email = inputEmail.getText().toString().trim();
                    String password = inputPassword.getText().toString().trim();

                    if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String hashedPassword = PasswordUtils.hashPassword(password);

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
                    ref.child(username).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
                        } else {
                            Map<String, Object> newUser = new HashMap<>();
                            newUser.put("username", username);
                            newUser.put("email", email);
                            newUser.put("password", hashedPassword);
                            newUser.put("role", "user");

                            ref.child(username).setValue(newUser).addOnCompleteListener(t -> {
                                if (t.isSuccessful()) {
                                    Toast.makeText(this, "User added!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Failed to add user", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditUserDialog() {
        if (isFinishing() || isDestroyed()) return; // 🛡️ Prevent leaked window

        View view = getLayoutInflater().inflate(R.layout.dialog_edit_user, null);
        EditText inputUsername = view.findViewById(R.id.edit_username);
        EditText inputEmail = view.findViewById(R.id.edit_email);
        EditText inputRole = view.findViewById(R.id.edit_role);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit User Info")
                .setView(view)
                .setPositiveButton("Update", (d, which) -> {
                    String username = inputUsername.getText().toString().trim().toLowerCase();
                    String email = inputEmail.getText().toString().trim();
                    String role = inputRole.getText().toString().trim().toLowerCase();

                    if (username.isEmpty()) {
                        Toast.makeText(this, "Username is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(username);

                    userRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            Map<String, Object> updates = new HashMap<>();
                            if (!email.isEmpty()) updates.put("email", email);
                            if (role.equals("admin") || role.equals("user")) updates.put("role", role);

                            userRef.updateChildren(updates).addOnCompleteListener(updateTask -> {
                                if (updateTask.isSuccessful()) {
                                    Toast.makeText(this, "User updated", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    });

                })
                .setNegativeButton("Cancel", null)
                .create();

        if (!isFinishing() && !isDestroyed()) {
            dialog.show(); // ✅ Safe to show
        }
    }

    private void showDeleteUserDialog() {
        if (isFinishing() || isDestroyed()) return;

        View view = getLayoutInflater().inflate(R.layout.dialog_delete_user, null);
        EditText inputUsername = view.findViewById(R.id.delete_username);

        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setView(view)
                .setPositiveButton("Delete", (dialog, which) -> {
                    String username = inputUsername.getText().toString().trim().toLowerCase();
                    if (username.isEmpty()) {
                        Toast.makeText(this, "Username is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // ✅ Prevent deleting self
                    if (username.equalsIgnoreCase(currentUsername)) {
                        Toast.makeText(this, "❌ You cannot delete your own admin account!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(username);
                    userRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            userRef.removeValue().addOnCompleteListener(deleteTask -> {
                                if (deleteTask.isSuccessful()) {
                                    Toast.makeText(this, "✅ User deleted", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "❌ Failed to delete user", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(this, "❌ User not found", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }



}
