package com.example.asm2_ad_team1;

import android.util.Log;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;

public class SharedExpenseManager {

    public static void applySharedExpenses(String username) {
        Log.d("SharedExpense", "applySharedExpenses() called for " + username);

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference sharedRef = rootRef.child("shared_expenses");
        DatabaseReference userRef = rootRef.child("users").child(username);

        sharedRef.get().addOnSuccessListener(sharedSnapshot -> {
            if (!sharedSnapshot.exists()) {
                Log.d("SharedExpense", "❌ No shared_expenses found.");
                return;
            }

            for (DataSnapshot sharedExp : sharedSnapshot.getChildren()) {
                String sharedId = sharedExp.getKey();
                String desc = sharedExp.child("description").getValue(String.class);
                Integer amt = sharedExp.child("amount").getValue(Integer.class);
                String cat = sharedExp.child("category").getValue(String.class);
                String date = sharedExp.child("date").getValue(String.class);

                if (sharedId == null || desc == null || amt == null || cat == null || date == null) {
                    Log.w("SharedExpense", "❌ Invalid data in shared_expense: " + sharedId);
                    continue;
                }

                Log.d("SharedExpense", "Checking sharedId: " + sharedId);

                // Check if user deleted it
                userRef.child("deleted_shared_expenses").child(sharedId).get().addOnSuccessListener(deleted -> {
                    if (deleted.exists()) {
                        Log.d("SharedExpense", "🚫 User has deleted sharedId: " + sharedId);
                        return;
                    }

                    // Check if already applied
                    userRef.child("expenses").orderByChild("sharedId").equalTo(sharedId).get()
                            .addOnSuccessListener(existing -> {
                                if (existing.exists()) {
                                    Log.d("SharedExpense", "✅ User already has sharedId: " + sharedId);
                                    return;
                                }

                                // Add it
                                Log.d("SharedExpense", "➡️ Applying shared expense " + sharedId + " to user " + username);

                                String expId = userRef.child("expenses").push().getKey();
                                Map<String, Object> data = new HashMap<>();
                                data.put("description", desc + " (Shared)");
                                data.put("amount", amt);
                                data.put("category", cat);
                                data.put("date", date);
                                data.put("sharedId", sharedId);

                                userRef.child("expenses").child(expId).setValue(data)
                                        .addOnSuccessListener(unused ->
                                                Log.d("SharedExpense", "✅ Shared expense added: " + sharedId))
                                        .addOnFailureListener(e ->
                                                Log.e("SharedExpense", "❌ Failed to add shared expense", e));
                            })
                            .addOnFailureListener(e -> Log.e("SharedExpense", "❌ Failed to check existing expenses", e));
                }).addOnFailureListener(e -> Log.e("SharedExpense", "❌ Failed to check deleted_shared_expenses", e));
            }
        }).addOnFailureListener(e -> {
            Log.e("SharedExpense", "❌ Failed to get shared_expenses", e);
        });
    }
}


