package com.example.asm2_ad_team1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ExpenseActivity extends AppCompatActivity {
    private static final int ADD_EXPENSE_REQUEST = 1;
    private static final int EDIT_EXPENSE_REQUEST = 2;

    private ListView expenseListView;
    private TextView totalExpense;
    private Button btnAddExpense;
    private FirebaseFirestore db;
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
        db = FirebaseFirestore.getInstance();
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

        CollectionReference expenseRef = db.collection("User").document(user.getUid()).collection("Expense");
        expenseRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(ExpenseActivity.this, "Failed to load expenses: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (value == null) return;

                expenseList.clear();
                expenseNames.clear();
                double total = 0;

                for (var doc : value.getDocuments()) {
                    Expense expense = doc.toObject(Expense.class);
                    if (expense != null) {
                        expenseList.add(expense);
                        expenseNames.add(expense.getNote() + " - $" + expense.getAmount());
                        total += expense.getAmount();
                    }
                }

                // Cập nhật giao diện trên luồng chính
                double finalTotal = total;
                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    totalExpense.setText("Total Expense: $" + finalTotal);
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // Kiểm tra requestCode để đảm bảo danh sách được cập nhật đúng cách
            if (requestCode == ADD_EXPENSE_REQUEST || requestCode == EDIT_EXPENSE_REQUEST) {
                loadExpenses(); // Làm mới danh sách khi quay lại từ màn hình thêm/sửa
            }
        }
    }
}
