package com.example.asm2_ad_team1;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class BudgetSetting extends AppCompatActivity {

    private TextView tvMonthlyMoney, dateMonthlyFrom, dateMonthlyTo,btn_back;
    private Button btnUpdateMonthlyBudget, btnAddCategory, btnEditCategory, btnDeleteCategory;
    private LinearLayout categoryCardContainer;
    private DatabaseReference mDatabase;
    private String currentUsername;
    private EditText edtName, edtAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_setting);

        currentUsername = getIntent().getStringExtra("username");
        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(this, "User not found. Closing screen.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        tvMonthlyMoney = findViewById(R.id.tv_monthly_money);
        dateMonthlyFrom = findViewById(R.id.date_monthly_from);
        dateMonthlyTo = findViewById(R.id.date_monthly_to);
        btnUpdateMonthlyBudget = findViewById(R.id.button);
        btnAddCategory = findViewById(R.id.button2);
        btnEditCategory = findViewById(R.id.button3);
        btnDeleteCategory = findViewById(R.id.button4);
        categoryCardContainer = findViewById(R.id.layoutCategories);
        btn_back = findViewById(R.id.btn_back);

        loadMonthlyBudget();
        loadCategories();

        btnUpdateMonthlyBudget.setOnClickListener(v -> showBudgetUpdateDialog());

        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog(categoryCardContainer));

        btnEditCategory.setOnClickListener(v -> {
            showEditCategoryDialog();
        });

        btnDeleteCategory.setOnClickListener(v -> {
            showDeleteCategoryDialog();
        });

        btn_back.setOnClickListener(view -> {
            Intent intent = new Intent(BudgetSetting.this, MainActivity.class);
            intent.putExtra("username", currentUsername); // 👈 pass username again
            startActivity(intent);
            finish();
        });
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

    private void loadCategories() {
        DatabaseReference catRef = mDatabase.child(currentUsername).child("categories");
        catRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot category : snapshot.getChildren()) {
                    String name = category.getKey();
                    Integer amount = category.child("amount").getValue(Integer.class);
                    if (name != null && amount != null) {
                        addCategoryCard(categoryCardContainer, name, amount);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BudgetSetting.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
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

        new AlertDialog.Builder(this)
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

    private void showAddCategoryDialog(LinearLayout categoryCardContainer) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        edtName = dialogView.findViewById(R.id.input_category_name);
        edtAmount = dialogView.findViewById(R.id.input_category_amount);

        new AlertDialog.Builder(this)
                .setTitle("Add Category")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = edtName.getText().toString().trim();
                    String amountStr = edtAmount.getText().toString().trim();

                    if (name.isEmpty() || amountStr.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int amount = Integer.parseInt(amountStr);

                    DatabaseReference ref = mDatabase.child(currentUsername).child("categories").child(name.toLowerCase());
                    ref.child("amount").setValue(amount);

                    addCategoryCard(categoryCardContainer, name, amount);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addCategoryCard(LinearLayout layout, String name, int amount) {
        CardView card = new CardView(this);
        card.setCardElevation(4);
        card.setRadius(12);
        card.setUseCompatPadding(true);

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(20, 20, 20, 20);

        TextView title = new TextView(this);
        title.setText(name);
        title.setTextSize(20);

        TextView money = new TextView(this);
        money.setText("Amount: " + amount + " VND");

        inner.addView(title);
        inner.addView(money);
        card.addView(inner);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(20, 20, 20, 20);

        layout.addView(card, 0, params);
    }

    private void showEditCategoryDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        EditText inputName = dialogView.findViewById(R.id.input_category_name);
        EditText inputAmount = dialogView.findViewById(R.id.input_category_amount);

        inputName.setHint("Category name to edit");
        inputAmount.setHint("New amount");

        new AlertDialog.Builder(this)
                .setTitle("Edit Category")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String name = inputName.getText().toString().trim().toLowerCase();
                    String amountStr = inputAmount.getText().toString().trim();

                    if (name.isEmpty() || amountStr.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int newAmount = Integer.parseInt(amountStr);

                    DatabaseReference ref = mDatabase.child(currentUsername).child("categories").child(name);
                    ref.child("amount").setValue(newAmount).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Category updated!", Toast.LENGTH_SHORT).show();
                            categoryCardContainer.removeAllViews();
                            loadCategories(); // refresh all category cards
                        } else {
                            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void showDeleteCategoryDialog() {
        EditText input = new EditText(this);
        input.setHint("Category name to delete");

        new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Enter the category name you want to delete:")
                .setView(input)
                .setPositiveButton("Delete", (dialog, which) -> {
                    String categoryName = input.getText().toString().trim().toLowerCase();

                    if (categoryName.isEmpty()) {
                        Toast.makeText(this, "Category name required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DatabaseReference ref = mDatabase.child(currentUsername).child("categories").child(categoryName);
                    ref.removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Category deleted", Toast.LENGTH_SHORT).show();
                            categoryCardContainer.removeAllViews();
                            loadCategories(); // Refresh list
                        } else {
                            Toast.makeText(this, "Failed to delete category", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


}