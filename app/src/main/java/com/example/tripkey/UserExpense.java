package com.example.tripkey;

public class UserExpense {
    private String userId;
    private int totalAmount;

    public UserExpense(String userId, int totalAmount) {
        this.userId = userId;
        this.totalAmount = totalAmount;
    }

    public String getUserId() {
        return userId;
    }

    public int getTotalAmount() {
        return totalAmount;
    }
}
