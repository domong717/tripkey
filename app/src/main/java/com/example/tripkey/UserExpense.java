package com.example.tripkey;

public class UserExpense {
    private String userId;
    private int totalAmount;
    private String profileImageUrl;

    public UserExpense(String userId, int totalAmount, String profileImageUrl) {
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.profileImageUrl = profileImageUrl;
    }

    public String getUserId() { return userId; }
    public int getTotalAmount() { return totalAmount; }
    public String getProfileImageUrl() { return profileImageUrl; }
}
