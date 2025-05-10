package com.example.tripkey;

public class UserExpense {
    private String userId;
    private int perPersonShare;

    public UserExpense(String userId, int perPersonShare) {
        this.userId = userId;
        this.perPersonShare = perPersonShare;
    }

    public String getUserId() {
        return userId;
    }

    public int getTotalAmount() {
        return perPersonShare;
    }
}
