package com.example.asm2_ad_team1;


import java.io.Serializable;

public class Expense implements Serializable {
    private String id;
    private String note;
    private double amount;

    public Expense() {}

    public Expense(String id, String note, double amount) {
        this.id = id;
        this.note = note;
        this.amount = amount;
    }

    public Expense(String expenseId, int i, String string, String string1, String string2) {
    }

    public String getId() { return id; }
    public String getNote() { return note; }
    public double getAmount() { return amount; }
}
