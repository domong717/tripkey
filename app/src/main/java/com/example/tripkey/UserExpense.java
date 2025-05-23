package com.example.tripkey;

public class UserExpense {
    private String userId;
    private int totalAmount;
    private String profileImageUrl;
    private String travelId;

    public UserExpense(String userId, int totalAmount, String profileImageUrl, String travelId) {
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.profileImageUrl = profileImageUrl;
        this.travelId = travelId;
    }

    public String getUserId() { return userId; }
    public int getTotalAmount() { return totalAmount; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public String getTravelId() { return travelId; }
}
