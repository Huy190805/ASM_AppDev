package com.example.asm2_ad_team1;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ExpenseActivity extends AppCompatActivity {
    private EditText edtCategory, edtAmount, edtDate;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        edtCategory = findViewById(R.id.edtCategory);
        edtAmount = findViewById(R.id.edtAmount);
        edtDate = findViewById(R.id.edtDate);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String category = edtCategory.getText().toString();
                double amount = Double.parseDouble(edtAmount.getText().toString());
                String date = edtDate.getText().toString();

                Expense expense = new Expense(category, amount, date);
                ExpenseDatabase.getInstance(ExpenseActivity.this).expenseDao().insert(expense);

                Toast.makeText(ExpenseActivity.this, "Đã lưu chi tiêu!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
