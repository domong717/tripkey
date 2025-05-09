package com.example.tripkey;

import java.util.HashMap;
import java.util.Map;

public class Expense {
    private String description;
    private int amount;

    public Expense(String description, int amount) {
        this.description = description;
        this.amount = amount;
    }

    public String getDescription() { return description; }
    public int getAmount() { return amount; }

    public Map<String, Object> toMap(String userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("description", description);
        map.put("amount", amount);
        map.put("userId", userId);
        map.put("timestamp", System.currentTimeMillis());  // 정렬용
        return map;
    }
}
