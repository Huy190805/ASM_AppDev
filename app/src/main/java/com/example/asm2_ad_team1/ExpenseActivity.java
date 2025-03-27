package com.example.asm2_ad_team1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ExpenseActivity extends AppCompatActivity {
    private static final int ADD_EXPENSE_REQUEST = 1;
    private static final int EDIT_EXPENSE_REQUEST = 2;

    private ListView expenseListView;
    private TextView totalExpense;
    private Button btnAddExpense;
    private DatabaseReference dbRef;
    private FirebaseUser user;
    private List<Expense> expenseList;
    private ArrayAdapter<String> adapter;
    private List<String> expenseNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        // Khởi tạo UI
        expenseListView = findViewById(R.id.expenseListView);
        totalExpense = findViewById(R.id.totalExpense);
        btnAddExpense = findViewById(R.id.btnAddExpense);
        dbRef = FirebaseDatabase.getInstance().getReference("User");
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Khởi tạo danh sách
        expenseList = new ArrayList<>();
        expenseNames = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, expenseNames);
        expenseListView.setAdapter(adapter);

        // Tải dữ liệu lần đầu
        loadExpenses();

        // Chuyển sang màn hình thêm Expense
        btnAddExpense.setOnClickListener(v -> {
            Intent intent = new Intent(ExpenseActivity.this, AddExpense.class);
            startActivityForResult(intent, ADD_EXPENSE_REQUEST);
        });

        // Chỉnh sửa Expense
        expenseListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(ExpenseActivity.this, EditExpense.class);
            intent.putExtra("expense", expenseList.get(position));
            startActivityForResult(intent, EDIT_EXPENSE_REQUEST);
        });
    }

    private void loadExpenses() {
        if (user == null) return;

        dbRef.child(user.getUid()).child("Expense").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                expenseList.clear();
                expenseNames.clear();
                double total = 0;

                for (DataSnapshot doc : snapshot.getChildren()) {
                    Expense expense = doc.getValue(Expense.class);
                    if (expense != null) {
                        expenseList.add(expense);
                        expenseNames.add(expense.getNote() + " - $" + expense.getAmount());
                        total += expense.getAmount();
                    }
                }

                // Cập nhật giao diện
                double finalTotal = total;
                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    totalExpense.setText("Total Expense: $" + finalTotal);
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ExpenseActivity.this, "Failed to load expenses: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_EXPENSE_REQUEST && resultCode == RESULT_OK) {
            // Cập nhật danh sách ngay khi quay lại màn hình chính
            loadExpenses();
        }
    }
}
