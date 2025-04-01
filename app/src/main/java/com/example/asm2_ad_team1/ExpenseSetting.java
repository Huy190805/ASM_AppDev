package com.example.asm2_ad_team1;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class ExpenseSetting extends AppCompatActivity {

    private LinearLayout expenseContainer;
    private Button btnAddExpense, btnEditExpense, btnDeleteExpense, btnAddRecurringExpense;
    private TextView btnBack;
    private String currentUsername;
    private DatabaseReference mDatabase;
    private Map<String, String> expenseIdMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_setting);

        currentUsername = getIntent().getStringExtra("username");
        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        expenseContainer = findViewById(R.id.expenseContainer);
        btnAddExpense = findViewById(R.id.btn_add_expense);
        btnEditExpense = findViewById(R.id.btn_edit_expense);
        btnDeleteExpense = findViewById(R.id.btn_delete_expense);
        btnBack = findViewById(R.id.btn_back);
        btnAddRecurringExpense = findViewById(R.id.btn_addrecurring_expense);

        btnAddExpense.setOnClickListener(v -> showAddExpenseDialog());
        btnEditExpense.setOnClickListener(v -> showEditExpenseDialog());
        btnDeleteExpense.setOnClickListener(v -> showDeleteExpenseDialog());
        btnAddRecurringExpense.setOnClickListener(view -> showAddRecurringExpenseDialog());

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ExpenseSetting.this, MainActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
            finish();
        });

        loadExpenses();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "expense_alerts", "Expense Alerts", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifies when a category exceeds or nears budget");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

    }

    private void showAddExpenseDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_expense, null);
        EditText edtDesc = view.findViewById(R.id.input_expense_description);
        EditText edtDate = view.findViewById(R.id.input_expense_date);
        EditText edtAmount = view.findViewById(R.id.input_expense_amount);
        Spinner spinner = view.findViewById(R.id.spinner_expense_category);

        // Show DatePicker when clicking the date field
        edtDate.setOnClickListener(v -> showDatePicker(edtDate));

        // Load categories into spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.expense_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Create the dialog
        new AlertDialog.Builder(this)
                .setTitle("Add Expense")
                .setView(view)
                .setPositiveButton("Add", (dialog, which) -> {
                    String desc = edtDesc.getText().toString().trim();
                    String date = edtDate.getText().toString().trim();
                    String category = spinner.getSelectedItem().toString();
                    String amtStr = edtAmount.getText().toString().trim();

                    if (desc.isEmpty() || date.isEmpty() || amtStr.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        int amount = Integer.parseInt(amtStr);
                        String id = UUID.randomUUID().toString();
                        DatabaseReference ref = mDatabase.child(currentUsername).child("expenses").child(id);

                        Map<String, Object> expense = new HashMap<>();
                        expense.put("description", desc);
                        expense.put("date", date);
                        expense.put("category", category.toLowerCase());
                        expense.put("amount", amount);

                        ref.setValue(expense).addOnCompleteListener(t -> {
                            if (t.isSuccessful()) {
                                Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show();
                                loadExpenses();
                            } else {
                                Toast.makeText(this, "Error: " + t.getException(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditExpenseDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_expense, null);
        Spinner spinner = dialogView.findViewById(R.id.spinner_existing_expenses);
        EditText edtDesc = dialogView.findViewById(R.id.edit_expense_description);
        EditText edtDate = dialogView.findViewById(R.id.edit_expense_date);
        EditText edtAmount = dialogView.findViewById(R.id.edit_expense_amount);
        Spinner catSpinner = dialogView.findViewById(R.id.edit_expense_category);

        edtDate.setOnClickListener(v -> showDatePicker(edtDate));

        ArrayAdapter<String> idAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>(expenseIdMap.keySet()));
        spinner.setAdapter(idAdapter);

        ArrayAdapter<CharSequence> catAdapter = ArrayAdapter.createFromResource(this,
                R.array.expense_categories, android.R.layout.simple_spinner_item);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        catSpinner.setAdapter(catAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Edit Expense")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String selected = (String) spinner.getSelectedItem();
                    String expenseId = expenseIdMap.get(selected);
                    if (expenseId == null) return;

                    String desc = edtDesc.getText().toString().trim();
                    String date = edtDate.getText().toString().trim();
                    String amountStr = edtAmount.getText().toString().trim();
                    String category = catSpinner.getSelectedItem().toString().toLowerCase();

                    if (desc.isEmpty() || date.isEmpty() || amountStr.isEmpty()) return;

                    int amount = Integer.parseInt(amountStr);

                    Map<String, Object> updated = new HashMap<>();
                    updated.put("description", desc);
                    updated.put("date", date);
                    updated.put("amount", amount);
                    updated.put("category", category);

                    mDatabase.child(currentUsername).child("expenses").child(expenseId).updateChildren(updated)
                            .addOnCompleteListener(t -> {
                                Toast.makeText(this, "Expense updated", Toast.LENGTH_SHORT).show();
                                expenseContainer.removeAllViews();
                                loadExpenses();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteExpenseDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_expense, null);
        Spinner spinner = view.findViewById(R.id.spinner_delete_expense);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new ArrayList<>(expenseIdMap.keySet()));
        spinner.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setTitle("Delete Expense")
                .setView(view)
                .setPositiveButton("Delete", (dialog, which) -> {
                    String selected = (String) spinner.getSelectedItem();
                    String expenseId = expenseIdMap.get(selected);

                    if (expenseId == null) {
                        Toast.makeText(this, "Invalid selection", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DatabaseReference expenseRef = mDatabase.child(currentUsername).child("expenses").child(expenseId);
                    expenseRef.get().addOnSuccessListener(snapshot -> {
                        // Check if this expense has a recurringId tag
                        String recurringId = snapshot.child("recurringId").getValue(String.class);

                        // Remove the expense
                        expenseRef.removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Expense deleted", Toast.LENGTH_SHORT).show();
                                loadExpenses();
                            } else {
                                Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                            }
                        });

                        // If it was tied to a recurring expense, also delete the recurring rule
                        if (recurringId != null && !recurringId.isEmpty()) {
                            mDatabase.child(currentUsername).child("recurring_expenses").child(recurringId).removeValue()
                                    .addOnSuccessListener(unused -> Toast.makeText(this, "Recurring rule removed", Toast.LENGTH_SHORT).show());
                        }
                    });
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

    private void loadExpenses() {
        expenseContainer.removeAllViews();
        expenseIdMap.clear();

        DatabaseReference expRef = mDatabase.child(currentUsername).child("expenses");
        expRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                expenseIdMap.clear();
                for (DataSnapshot exp : snapshot.getChildren()) {
                    String expenseId = exp.getKey();
                    String desc = exp.child("description").getValue(String.class);
                    String date = exp.child("date").getValue(String.class);
                    Integer amount = exp.child("amount").getValue(Integer.class);
                    String category = exp.child("category").getValue(String.class);

                    if (expenseId != null && desc != null && date != null && amount != null && category != null) {
                        expenseIdMap.put(desc + " (" + date + ")", expenseId);
                        addExpenseCard(expenseId, desc, date, amount, category);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ExpenseSetting.this, "Failed to load expenses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addExpenseCard(String id, String desc, String date, int amount, String category) {
        CardView card = new CardView(this);
        card.setCardElevation(4);
        card.setRadius(12);
        card.setUseCompatPadding(true);

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(20, 20, 20, 20);

        TextView tvDesc = new TextView(this);
        tvDesc.setText("Description: " + desc);
        TextView tvDate = new TextView(this);
        tvDate.setText("Date: " + date);
        TextView tvAmount = new TextView(this);
        tvAmount.setText("Amount: " + amount + " VND");
        TextView tvCat = new TextView(this);
        tvCat.setText("Category: " + capitalizeFirstLetter(category));

        inner.addView(tvDesc);
        inner.addView(tvDate);
        inner.addView(tvAmount);
        inner.addView(tvCat);
        card.addView(inner);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(20, 20, 20, 20);

        expenseContainer.addView(card, 0, params);
    }

    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    private void showAddRecurringExpenseDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_recurring_expense, null);

        EditText edtDesc = view.findViewById(R.id.input_recurring_description);
        EditText edtAmt = view.findViewById(R.id.input_recurring_amount);
        EditText edtStart = view.findViewById(R.id.input_start_date);
        EditText edtEnd = view.findViewById(R.id.input_end_date);
        Spinner spinnerCategory = view.findViewById(R.id.spinner_recurring_category);
        Spinner spinnerFrequency = view.findViewById(R.id.spinner_frequency);

        // Category Spinner
        ArrayAdapter<CharSequence> catAdapter = ArrayAdapter.createFromResource(this,
                R.array.expense_categories, android.R.layout.simple_spinner_item);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        // Frequency Spinner
        ArrayAdapter<String> freqAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                Arrays.asList("monthly")); // Can add weekly, etc.
        freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(freqAdapter);

        // DatePickers
        edtStart.setOnClickListener(v -> showDatePicker(edtStart));
        edtEnd.setOnClickListener(v -> showDatePicker(edtEnd));

        new AlertDialog.Builder(this)
                .setTitle("Add Recurring Expense")
                .setView(view)
                .setPositiveButton("Add", (dialog, which) -> {
                    String desc = edtDesc.getText().toString().trim();
                    String amtStr = edtAmt.getText().toString().trim();
                    String startDate = edtStart.getText().toString().trim();
                    String endDate = edtEnd.getText().toString().trim();
                    String category = spinnerCategory.getSelectedItem().toString().toLowerCase();
                    String frequency = spinnerFrequency.getSelectedItem().toString();

                    if (desc.isEmpty() || amtStr.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        int amount = Integer.parseInt(amtStr);
                        String id = UUID.randomUUID().toString();

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users")
                                .child(currentUsername).child("recurring_expenses").child(id);

                        Map<String, Object> data = new HashMap<>();
                        data.put("description", desc);
                        data.put("amount", amount);
                        data.put("category", category);
                        data.put("startDate", startDate);
                        data.put("endDate", endDate);
                        data.put("frequency", frequency);

                        ref.setValue(data).addOnCompleteListener(t -> {
                            if (t.isSuccessful()) {
                                Toast.makeText(this, "Recurring expense saved!", Toast.LENGTH_SHORT).show();
                                RecurringExpenseManager.applyRecurringExpenses(currentUsername);

                                new Handler().postDelayed(this::loadExpenses, 1000);
                            } else {
                                Toast.makeText(this, "Error: " + t.getException(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                    }
                })

                .setNegativeButton("Cancel", null)

                .show();


    }





}
