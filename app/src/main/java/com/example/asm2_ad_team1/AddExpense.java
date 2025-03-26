package com.example.asm2_ad_team1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class AddExpense extends AppCompatActivity {
    private EditText amount, note, date;
    private Spinner group;
    private Button addExpense;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        amount = findViewById(R.id.amount);
        note = findViewById(R.id.note);
        date = findViewById(R.id.date);
        group = findViewById(R.id.group);
        addExpense = findViewById(R.id.btnAddExpense);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        addExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (amount.getText().toString().isEmpty() ||
                        note.getText().toString().isEmpty() ||
                        date.getText().toString().isEmpty()) {

                    Toast.makeText(AddExpense.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                String expenseId = UUID.randomUUID().toString();
                Expense newExpense = new Expense(
                        expenseId,
                        Integer.parseInt(amount.getText().toString()),
                        group.getSelectedItem().toString(),
                        note.getText().toString(),
                        date.getText().toString()
                );

                // Thêm dữ liệu vào Firestore
                db.collection("User").document(user.getUid())
                        .collection("Expense").document(expenseId)
                        .set(newExpense)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(AddExpense.this, "Expense added successfully!", Toast.LENGTH_SHORT).show();

                            // Trả kết quả về ExpenseActivity để cập nhật danh sách
                            Intent resultIntent = new Intent();
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(AddExpense.this, "Failed to add expense: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            }
        });
    }
}
