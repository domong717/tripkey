package com.example.tripkey;

import java.util.HashMap;
import java.util.Map;

public class Expense {
    private String description;
    private int amount;
    private String userId;
    private String travelId;
    private String date;

    public Expense(String description, int amount, String userId, String travelId, String date) {
        this.description = description;
        this.amount = amount;
        this.userId = userId;
        this.travelId = travelId;
        this.date = date;
    }

    public String getDescription() { return description; }
    public int getAmount() { return amount; }
    public String getUserId() { return userId; }
    public String getTravelId() { return travelId; }
    public String getDate() { return date; }

    public Map<String, Object> toMap(String userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("description", description);
        map.put("amount", amount);
        map.put("userId", userId);
        map.put("timestamp", System.currentTimeMillis());  // 정렬용
        return map;
    }
}
