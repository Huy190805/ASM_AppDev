package com.example.asm2_ad_team1;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;



public class RecurringExpenseManager {
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;



    public RecurringExpenseManager() {
        databaseReference = FirebaseDatabase.getInstance().getReference("RecurringExpenses");
        auth = FirebaseAuth.getInstance();
    }

    public void addRecurringExpense(String category, double amount, String startDate, String endDate, String frequency) {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            String expenseId = databaseReference.push().getKey();

            RecurringExpense expense = new RecurringExpense(expenseId, userId, category, amount, startDate, endDate, frequency);
            databaseReference.child(userId).child(expenseId).setValue(expense)
                    .addOnSuccessListener(aVoid -> System.out.println("Recurring expense added successfully."))
                    .addOnFailureListener(e -> System.out.println("Failed to add expense: " + e.getMessage()));
        } else {
            System.out.println("Error: User not logged in.");
        }
    }
}
