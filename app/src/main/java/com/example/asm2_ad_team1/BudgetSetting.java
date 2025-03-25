package com.example.asm2_ad_team1;

import android.app.DatePickerDialog;
import android.os.Bundle;
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
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class BudgetSetting extends AppCompatActivity {

    private TextView tvMonthlyMoney, dateMonthlyFrom, dateMonthlyTo;
    private Button btnUpdateMonthlyBudget;

    private DatabaseReference mDatabase;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_setting); // Ensure this layout exists and has correct IDs

        // Get username from Intent
        currentUsername = getIntent().getStringExtra("username");
        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(this, "User not found. Closing screen.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Firebase reference
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        // Bind views
        tvMonthlyMoney = findViewById(R.id.tv_monthly_money);
        dateMonthlyFrom = findViewById(R.id.date_monthly_from);
        dateMonthlyTo = findViewById(R.id.date_monthly_to);
        btnUpdateMonthlyBudget = findViewById(R.id.button);

        // Load budget info
        loadMonthlyBudget();

        // Button: show dialog
        btnUpdateMonthlyBudget.setOnClickListener(v -> showBudgetUpdateDialog());
    }

    private void loadMonthlyBudget() {
        DatabaseReference budgetRef = mDatabase.child(currentUsername).child("MonthlyBudget");
        budgetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer amount = snapshot.child("amount").getValue(Integer.class);
                    String fromDate = snapshot.child("fromDate").getValue(String.class);
                    String toDate = snapshot.child("toDate").getValue(String.class);

                    if (amount != null) tvMonthlyMoney.setText(amount + " VND");
                    if (fromDate != null) dateMonthlyFrom.setText(fromDate);
                    if (toDate != null) dateMonthlyTo.setText(toDate);
                } else {
                    Toast.makeText(BudgetSetting.this, "No budget data found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BudgetSetting.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showBudgetUpdateDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_update_budget, null);

        EditText inputAmount = dialogView.findViewById(R.id.input_amount);
        EditText inputFrom = dialogView.findViewById(R.id.input_from);
        EditText inputTo = dialogView.findViewById(R.id.input_to);

        inputFrom.setOnClickListener(v -> showDatePicker(inputFrom));
        inputTo.setOnClickListener(v -> showDatePicker(inputTo));

        new android.app.AlertDialog.Builder(this)
                .setTitle("Update Monthly Budget")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String amountStr = inputAmount.getText().toString().trim();
                    String fromDate = inputFrom.getText().toString().trim();
                    String toDate = inputTo.getText().toString().trim();

                    if (amountStr.isEmpty() || fromDate.isEmpty() || toDate.isEmpty()) {
                        Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int amount = Integer.parseInt(amountStr);
                    updateBudgetInFirebase(amount, fromDate, toDate);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDatePicker(EditText target) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
            String date = y + "-" + String.format("%02d", m + 1) + "-" + String.format("%02d", d);
            target.setText(date);
        }, year, month, day);

        dialog.show();
    }

    private void updateBudgetInFirebase(int amount, String fromDate, String toDate) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("amount", amount);
        updates.put("fromDate", fromDate);
        updates.put("toDate", toDate);

        DatabaseReference budgetRef = mDatabase.child(currentUsername).child("MonthlyBudget");

        budgetRef.updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        tvMonthlyMoney.setText(amount + " VND");
                        dateMonthlyFrom.setText(fromDate);
                        dateMonthlyTo.setText(toDate);
                        Toast.makeText(this, "Monthly budget updated!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Update failed: " + task.getException(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
