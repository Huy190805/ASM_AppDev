package com.example.asm2_ad_team1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnAddExpense, btnViewStatistics, btnBudgetSetting;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Make sure your layout file is named activity_main.xml

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

        // Budget Setting button action
        btnBudgetSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BudgetSetting.class);
                intent.putExtra("username", currentUsername);
                startActivity(intent);
            }
        });

        // Optional: other buttons can be wired similarly
        btnAddExpense.setOnClickListener(view ->
                Toast.makeText(MainActivity.this, "Add Expense clicked (not implemented yet)", Toast.LENGTH_SHORT).show());

        btnViewStatistics.setOnClickListener(view ->
                Toast.makeText(MainActivity.this, "View Statistics clicked (not implemented yet)", Toast.LENGTH_SHORT).show());
    }
}
