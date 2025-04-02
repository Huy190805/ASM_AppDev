package com.example.asm2_ad_team1;

public class RecurringExpense {
    private String id;
    private String userId;
    private String category;
    private double amount;
    private String startDate;
    private String endDate;
    private String frequency;

    public RecurringExpense() {
        // Constructor mặc định để Firebase có thể khởi tạo đối tượng
    }

    public RecurringExpense(String id, String userId, String category, double amount, String startDate, String endDate, String frequency) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.frequency = frequency;
    }

    // Getter và Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
}
