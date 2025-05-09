package com.example.tripkey;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String userId, travelId;

    private TextView textTotalMoney;
    private RecyclerView recyclerView;
    private EachMoneyAdapter adapter;
    private List<UserExpense> userExpenseList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate_money);

        db = FirebaseFirestore.getInstance();

        // userId 가져오기
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);
        travelId = getIntent().getStringExtra("travelId");

        textTotalMoney = findViewById(R.id.total_money);
        recyclerView = findViewById(R.id.receiptRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EachMoneyAdapter(userExpenseList);
        recyclerView.setAdapter(adapter);

        loadData();
    }

    private void loadData() {
        if (userId == null || travelId == null) return;

        Map<String, Integer> userSums = new HashMap<>();
        final int[] totalSum = {0};

        db.collection("users")
                .document(userId)
                .collection("travel")
                .document(travelId)
                .collection("expenses")
                .get()
                .addOnSuccessListener(dateSnapshots -> {
                    for (var dateDoc : dateSnapshots.getDocuments()) {
                        String date = dateDoc.getId();
                        db.collection("users")
                                .document(userId)
                                .collection("travel")
                                .document(travelId)
                                .collection("expenses")
                                .document(date)
                                .collection("items")
                                .get()
                                .addOnSuccessListener(itemSnapshots -> {
                                    for (var doc : itemSnapshots.getDocuments()) {
                                        Long amt = doc.getLong("amount");
                                        String uId = doc.getString("userId");

                                        if (amt != null && uId != null) {
                                            int current = userSums.getOrDefault(uId, 0);
                                            userSums.put(uId, current + amt.intValue());
                                            totalSum[0] += amt;
                                        }
                                    }

                                    // 다 돌고 난 뒤, 리스트로 바꾸고 갱신
                                    userExpenseList.clear();
                                    for (Map.Entry<String, Integer> entry : userSums.entrySet()) {
                                        userExpenseList.add(new UserExpense(entry.getKey(), entry.getValue()));
                                    }
                                    adapter.notifyDataSetChanged();
                                    textTotalMoney.setText(String.valueOf(totalSum[0]));
                                });
                    }
                });
    }
}

