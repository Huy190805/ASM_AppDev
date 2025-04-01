package com.example.asm2_ad_team1;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
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
    private RecurringExpenseManager recurringExpenseManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recurring_expense);

        editTextCategory = findViewById(R.id.editTextCategory);
        editTextAmount = findViewById(R.id.editTextAmount);
        textViewStartDate = findViewById(R.id.textViewStartDate);
        textViewEndDate = findViewById(R.id.textViewEndDate);
        spinnerFrequency = findViewById(R.id.spinnerFrequency);
        buttonAddExpense = findViewById(R.id.buttonAddExpense);

        recurringExpenseManager = new RecurringExpenseManager();

        // Xử lý chọn ngày cho Start Date
        textViewStartDate.setOnClickListener(v -> showDatePickerDialog(textViewStartDate));

        // Xử lý chọn ngày cho End Date
        textViewEndDate.setOnClickListener(v -> showDatePickerDialog(textViewEndDate));

        // Xử lý thêm dữ liệu khi nhấn nút
        buttonAddExpense.setOnClickListener(v -> addRecurringExpense());
    }

    private void showDatePickerDialog(TextView textView) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String selectedDate = selectedYear + "-" + String.format("%02d", (selectedMonth + 1)) + "-" + String.format("%02d", selectedDay);
            textView.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }

    private void addRecurringExpense() {
        try {
            String category = editTextCategory.getText().toString().trim();
            String amountStr = editTextAmount.getText().toString().trim();
            String startDate = textViewStartDate.getText().toString().trim();
            String endDate = textViewEndDate.getText().toString().trim();
            String frequency = spinnerFrequency.getSelectedItem().toString();

            // Kiểm tra dữ liệu nhập vào
            if (category.isEmpty() || amountStr.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);

            recurringExpenseManager.addRecurringExpense(category, amount, startDate, endDate, frequency);
            Toast.makeText(this, "Expense added successfully", Toast.LENGTH_SHORT).show();
            finish(); // Đóng Activity sau khi thêm thành công
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

