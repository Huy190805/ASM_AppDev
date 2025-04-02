package com.example.asm2_ad_team1;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class AddRecurringExpenseActivity extends AppCompatActivity {

    private EditText editTextCategory, editTextAmount;
    private TextView textViewStartDate, textViewEndDate;
    private Spinner spinnerFrequency;
    private Button buttonAddExpense;

    private String currentUsername;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recurring_expense);

        // Get the username passed from previous activity
        currentUsername = getIntent().getStringExtra("username");
        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(this, "No user found. Closing screen.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI
        editTextCategory = findViewById(R.id.editTextCategory);
        editTextAmount = findViewById(R.id.editTextAmount);
        textViewStartDate = findViewById(R.id.textViewStartDate);
        textViewEndDate = findViewById(R.id.textViewEndDate);
        spinnerFrequency = findViewById(R.id.spinnerFrequency);
        buttonAddExpense = findViewById(R.id.buttonAddExpense);

        textViewStartDate.setOnClickListener(v -> showDatePickerDialog(textViewStartDate));
        textViewEndDate.setOnClickListener(v -> showDatePickerDialog(textViewEndDate));

        buttonAddExpense.setOnClickListener(v -> addRecurringExpense());
    }

    private void showDatePickerDialog(TextView target) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
            String date = y + "-" + String.format("%02d", m + 1) + "-" + String.format("%02d", d);
            target.setText(date);
        }, year, month, day);

        dialog.show();
    }

    private void addRecurringExpense() {
        try {
            String category = editTextCategory.getText().toString().trim();
            String amountStr = editTextAmount.getText().toString().trim();
            String startDate = textViewStartDate.getText().toString().trim();
            String endDate = textViewEndDate.getText().toString().trim();
            String frequency = spinnerFrequency.getSelectedItem().toString();

            if (category.isEmpty() || amountStr.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);

            RecurringExpenseManager.addRecurringExpense(
                    currentUsername, category, amount, startDate, endDate, frequency
            );

            Toast.makeText(this, "Recurring expense added successfully", Toast.LENGTH_SHORT).show();
            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
