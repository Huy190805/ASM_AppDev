package com.example.asm2_ad_team1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private String currentUsername;

    // Drawer menu buttons
    private Button btnExpenseSetting, btnBudgetSetting, btnProfile, btnSettings, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // ✅ Must match layout file

        // Get username from login
        currentUsername = getIntent().getStringExtra("username");
        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(this, "No user logged in. Redirecting to login.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
            return;
        }

        // Set up toolbar and drawer toggle
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Drawer button bindings
        btnExpenseSetting = findViewById(R.id.nav_btn_expense_setting);
        btnBudgetSetting = findViewById(R.id.nav_btn_budget_setting);
        btnProfile = findViewById(R.id.nav_btn_profile);
        btnSettings = findViewById(R.id.nav_btn_settings);
        btnLogout = findViewById(R.id.nav_btn_logout);

        // Expense Setting → AddExpenseActivity
        btnExpenseSetting.setOnClickListener(v -> {
                Intent intent = new Intent(this, ExpenseSetting.class);
                intent.putExtra("username", currentUsername);
                startActivity(intent);
            });

        // Budget Setting → BudgetSetting activity
        btnBudgetSetting.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, BudgetSetting.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });

        // Profile
        btnProfile.setOnClickListener(view ->
                Toast.makeText(MainActivity.this, "Profile clicked (not implemented)", Toast.LENGTH_SHORT).show());

        // Settings
        btnSettings.setOnClickListener(view ->
                Toast.makeText(MainActivity.this, "Settings clicked (not implemented)", Toast.LENGTH_SHORT).show());

        // Logout
        btnLogout.setOnClickListener(view -> {
            Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
        });
    }
}
