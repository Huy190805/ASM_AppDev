package com.example.asm2_ad_team1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

public class AdminDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private String currentUsername;

    private Button btnManageUsers, btnViewReports, btnLogout;
    private TextView tvWelcome;

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
}
