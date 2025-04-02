package com.example.asm2_ad_team1;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.*;

public class RecurringExpenseManager extends AppCompatActivity {

    public static void applyRecurringExpenses(String username) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(username);

        userRef.child("recurring_expenses").get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) return;

            String currentMonth = new SimpleDateFormat("yyyy-MM").format(new Date());

            for (DataSnapshot recur : snapshot.getChildren()) {
                String recurringId = recur.getKey();
                String desc = recur.child("description").getValue(String.class);
                String cat = recur.child("category").getValue(String.class);
                Integer amt = recur.child("amount").getValue(Integer.class);
                String start = recur.child("startDate").getValue(String.class);
                String end = recur.child("endDate").getValue(String.class);
                String frequency = recur.child("frequency").getValue(String.class); // e.g., "monthly"

                if (desc == null || cat == null || amt == null || start == null || end == null || recurringId == null)
                    continue;

                // Skip if current month is outside range
                if (!isCurrentMonthInRange(currentMonth, start, end)) continue;

                // Skip if this month was already applied
                if (recur.child("appliedMonths").child(currentMonth).exists()) continue;

                // Create expense ID to check if already exists
                String expenseId = currentMonth + "_" + recurringId;

                userRef.child("expenses").child(expenseId).get().addOnSuccessListener(existing -> {
                    if (!existing.exists()) {
                        Map<String, Object> newExpense = new HashMap<>();
                        newExpense.put("description", desc + " (Recurring)");
                        newExpense.put("amount", amt);
                        newExpense.put("category", cat.toLowerCase());
                        newExpense.put("date", currentMonth + "-01");
                        newExpense.put("recurringId", recurringId);

                        // Save new expense
                        userRef.child("expenses").child(expenseId).setValue(newExpense);

                        // Mark as applied
                        userRef.child("recurring_expenses")
                                .child(recurringId)
                                .child("appliedMonths")
                                .child(currentMonth)
                                .setValue(true);

                        Log.d("RecurringExpense", "Added recurring: " + desc + " for " + currentMonth);
                    }
                });
            }
        }).addOnFailureListener(e -> Log.e("RecurringExpense", "Failed to read recurring data", e));
    }

    private static boolean isCurrentMonthInRange(String currentMonth, String startDate, String endDate) {
        return currentMonth.compareTo(startDate.substring(0, 7)) >= 0 &&
                currentMonth.compareTo(endDate.substring(0, 7)) <= 0;
    }

    public static void addRecurringExpense(String username, String category, double amount,
                                           String startDate, String endDate, String frequency) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(username)
                .child("recurring_expenses")
                .push();

        Map<String, Object> data = new HashMap<>();
        data.put("description", category); // Optional: store separately
        data.put("category", category.toLowerCase());
        data.put("amount", (int) amount);
        data.put("startDate", startDate);
        data.put("endDate", endDate);
        data.put("frequency", frequency);

        ref.setValue(data);
    }

}