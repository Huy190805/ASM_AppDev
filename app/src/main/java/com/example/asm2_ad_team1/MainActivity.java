package com.example.asm2_ad_team1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnAddExpense, btnViewStatistics, btnBudgetSetting, btnLogout;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Ensure this layout matches your file

        // Get username passed from Login activity
        currentUsername = getIntent().getStringExtra("username");

        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(this, "No user logged in. Returning to login.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
            return;
        }

        // Bind buttons
        btnAddExpense = findViewById(R.id.btnAddExpense);
        btnViewStatistics = findViewById(R.id.btnViewStatistics);
        btnBudgetSetting = findViewById(R.id.budgetSetting);
        btnLogout = findViewById(R.id.btnLogout); // Make sure this exists in your layout

        // Navigate to Budget Setting
        btnBudgetSetting.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, BudgetSetting.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });

        // Add Expense (Not implemented)
        btnAddExpense.setOnClickListener(view ->
                Toast.makeText(MainActivity.this, "Add Expense clicked (not implemented yet)", Toast.LENGTH_SHORT).show());

        // View Statistics (Not implemented)
        btnViewStatistics.setOnClickListener(view ->
                Toast.makeText(MainActivity.this, "View Statistics clicked (not implemented yet)", Toast.LENGTH_SHORT).show());

        // Optional: Logout button
        btnLogout.setOnClickListener(view -> {
            Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(intent);
            finish();
        });
    }
}
