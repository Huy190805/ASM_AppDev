package com.example.asm2_ad_team1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Button btnAddExpense, btnViewStatistics;
    private Button budgetSetting;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAddExpense = findViewById(R.id.btnAddExpense);
        btnViewStatistics = findViewById(R.id.btnViewStatistics);

        budgetSetting = findViewById(R.id.budgetSetting);



//        btnAddExpense.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(MainActivity.this, ExpenseActivity.class));
//            }
//        });
//
//        btnViewStatistics.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(MainActivity.this, StatisticsActivity.class));
//            }
//        });



        // Get username passed from Login activity
        currentUsername = getIntent().getStringExtra("username");

        budgetSetting = findViewById(R.id.budgetSetting);

        budgetSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BudgetSetting.class);
                intent.putExtra("username", currentUsername);
                startActivity(intent);
            }
        });
    }
}
