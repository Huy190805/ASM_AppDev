package com.example.asm2_ad_team1;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private String currentUsername;

    // Drawer menu buttons
    private Button btnExpenseSetting, btnBudgetSetting, btnProfile, btnSettings, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // ✅ Must match layout file

        // Get username from login
        currentUsername = getIntent().getStringExtra("username");
        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(this, "No user logged in. Redirecting to login.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
            return;
        }

        // Set up toolbar and drawer toggle
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Drawer button bindings
        btnExpenseSetting = findViewById(R.id.nav_btn_expense_setting);
        btnBudgetSetting = findViewById(R.id.nav_btn_budget_setting);
        btnProfile = findViewById(R.id.nav_btn_profile);
        btnSettings = findViewById(R.id.nav_btn_settings);
        btnLogout = findViewById(R.id.nav_btn_logout);

        // Expense Setting → AddExpenseActivity
        btnExpenseSetting.setOnClickListener(v -> {
                Intent intent = new Intent(this, ExpenseSetting.class);
                intent.putExtra("username", currentUsername);
                startActivity(intent);
            });

        // Budget Setting → BudgetSetting activity
        btnBudgetSetting.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, BudgetSetting.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });

        // Profile
        btnProfile.setOnClickListener(view ->
                Toast.makeText(MainActivity.this, "Profile clicked (not implemented)", Toast.LENGTH_SHORT).show());

        // Settings
        btnSettings.setOnClickListener(view ->
                Toast.makeText(MainActivity.this, "Settings clicked (not implemented)", Toast.LENGTH_SHORT).show());

        // Logout
        btnLogout.setOnClickListener(view -> {
            Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
        });

        Button btnReport = findViewById(R.id.btn_generate_report);
        btnReport.setOnClickListener(v -> generateMonthlyComparison());

        loadExpenseOverview();
        RecurringExpenseManager.applyRecurringExpenses(currentUsername);
    }
    private void loadExpenseOverview() {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users").child(currentUsername);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalSpent = 0;
                int monthlyBudget = 0;
                Map<String, Integer> categorySpent = new HashMap<>();
                Map<String, Integer> categoryBudget = new HashMap<>();

                // Get monthly budget
                if (snapshot.child("MonthlyBudget").exists()) {
                    Integer amt = snapshot.child("MonthlyBudget").child("amount").getValue(Integer.class);
                    if (amt != null) monthlyBudget = amt;
                }

                // Sum spent by category
                if (snapshot.child("expenses").exists()) {
                    for (DataSnapshot exp : snapshot.child("expenses").getChildren()) {
                        Integer amt = exp.child("amount").getValue(Integer.class);
                        String cat = exp.child("category").getValue(String.class);
                        if (amt != null && cat != null) {
                            totalSpent += amt;
                            cat = cat.toLowerCase();
                            categorySpent.put(cat, categorySpent.getOrDefault(cat, 0) + amt);
                        }
                    }
                }

                // Get category budget limits
                if (snapshot.child("categories").exists()) {
                    for (DataSnapshot catSnap : snapshot.child("categories").getChildren()) {
                        String cat = catSnap.getKey();
                        Integer amt = catSnap.child("amount").getValue(Integer.class);
                        if (cat != null && amt != null) {
                            categoryBudget.put(cat.toLowerCase(), amt);
                        }
                    }
                }

                // Build overview string
                TextView tvSpent = findViewById(R.id.tv_total_spent);
                TextView tvRemain = findViewById(R.id.tv_remaining_budget);
                TextView tvBreakdown = findViewById(R.id.tv_category_percentages);

                float percentUsed = monthlyBudget > 0 ? (totalSpent * 100f / monthlyBudget) : 0;
                tvSpent.setText(String.format(Locale.getDefault(),
                        "You have used %.1f%% of your monthly budget", percentUsed));

                tvRemain.setText(" "); // optional blank or additional summary

                StringBuilder breakdown = new StringBuilder();
                for (String cat : categorySpent.keySet()) {
                    int spent = categorySpent.get(cat);
                    int catLimit = categoryBudget.getOrDefault(cat, 0);
                    float catPercent = catLimit > 0 ? (spent * 100f / catLimit) : 0;
                    breakdown.append("• ").append(capitalize(cat)).append(": ")
                            .append(String.format(Locale.getDefault(), "%.1f", catPercent)).append("%\n")
                            .append("  You have used ").append(spent).append(" VND of ")
                            .append(catLimit).append(" VND in ").append(cat).append(" budget\n\n");
                }

                tvBreakdown.setText(breakdown.toString().trim());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load overview", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateMonthlyComparison() {
        String thisMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        String lastMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(cal.getTime());

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users").child(currentUsername).child("expenses");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int thisTotal = 0, lastTotal = 0;
                Map<String, Integer> thisCat = new HashMap<>();
                Map<String, Integer> lastCat = new HashMap<>();

                for (DataSnapshot expense : snapshot.getChildren()) {
                    String date = expense.child("date").getValue(String.class);
                    String cat = expense.child("category").getValue(String.class);
                    Integer amount = expense.child("amount").getValue(Integer.class);

                    if (date != null && amount != null && cat != null) {
                        cat = capitalize(cat);
                        if (date.startsWith(thisMonth)) {
                            thisTotal += amount;
                            thisCat.put(cat, thisCat.getOrDefault(cat, 0) + amount);
                        } else if (date.startsWith(lastMonth)) {
                            lastTotal += amount;
                            lastCat.put(cat, lastCat.getOrDefault(cat, 0) + amount);
                        }
                    }
                }

                StringBuilder msg = new StringBuilder();
                msg.append("📅 Report:\n")
                        .append("This month (").append(thisMonth).append("): ").append(thisTotal).append(" VND\n")
                        .append("Last month (").append(lastMonth).append("): ").append(lastTotal).append(" VND\n\n");

                msg.append("📊 This Month by Category:\n");
                for (String cat : thisCat.keySet()) {
                    msg.append("• ").append(cat).append(": ").append(thisCat.get(cat)).append(" VND\n");
                }

                msg.append("\n📊 Last Month by Category:\n");
                for (String cat : lastCat.keySet()) {
                    msg.append("• ").append(cat).append(": ").append(lastCat.get(cat)).append(" VND\n");
                }

                msg.append("\n");
                float diff = thisTotal - lastTotal;
                if (diff > 0) {
                    msg.append("⬆ You spent ").append((int) diff).append(" VND more than last month.");
                } else if (diff < 0) {
                    msg.append("⬇ You spent ").append(Math.abs((int) diff)).append(" VND less than last month.");
                } else {
                    msg.append("You spent exactly the same as last month.");
                }

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Monthly Comparison")
                        .setMessage(msg.toString())
                        .setPositiveButton("OK", null)
                        .show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load report", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }


}

