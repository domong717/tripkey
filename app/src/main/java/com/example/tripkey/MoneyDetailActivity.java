package com.example.tripkey;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MoneyDetailActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DateGroupAdapter adapter;
    private List<DateGroup> dateGroups = new ArrayList<>();
    private TextView textTotal;
    private int totalMoney = 0;
    private FirebaseFirestore db;
    private String userId;
    private String travelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_money_detail);

        // Firestore
        db = FirebaseFirestore.getInstance();

        ImageButton backButton = findViewById(R.id.button_back);
        recyclerView = findViewById(R.id.recyclerView);
        textTotal = findViewById(R.id.text_total);

        adapter = new DateGroupAdapter(this, dateGroups);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // userId 가져오기
        userId = getIntent().getStringExtra("userId");
        // travelId 가져오기
        travelId = getIntent().getStringExtra("travelId");

        loadExpensesFromFirestore();

        // 뒤로 가기 버튼
        backButton.setOnClickListener(v -> finish());

    }

    private void loadExpensesFromFirestore() {
        if (userId == null || travelId == null) return;

        totalMoney = 0; // 총합 초기화
        dateGroups.clear();

        db.collection("users")
                .document(userId)
                .collection("travel")
                .document(travelId)
                .collection("expenses")
                .get()
                .addOnSuccessListener(dateDocs -> {

                    for (var dateDoc : dateDocs) {
                        String date = dateDoc.getId();

                        db.collection("users")
                                .document(userId)
                                .collection("travel")
                                .document(travelId)
                                .collection("expenses")
                                .document(date)
                                .collection("items")
                                .get()
                                .addOnSuccessListener(itemDocs -> {
                                    List<Expense> expenses = new ArrayList<>();
                                    int dailyTotal = 0;

                                    for (var doc : itemDocs) {
                                        String desc = doc.getString("description");
                                        Long amt = doc.getLong("amount");
                                        String writerId = doc.getString("userId");

                                        if (desc != null && amt != null && userId.equals(writerId)) {
                                            int amount = amt.intValue();
                                            expenses.add(new Expense(desc, amount, writerId));
                                            totalMoney += amount; // ✅ 총합에 추가
                                        }
                                    }

                                    if (!expenses.isEmpty()) {
                                        dateGroups.add(new DateGroup(date, expenses));
                                        adapter.notifyDataSetChanged();
                                        textTotal.setText("총 지불금액 : " + totalMoney + "원"); // ✅ 실시간 갱신
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "지출 항목 불러오기 실패", Toast.LENGTH_SHORT).show();
                });
    }

}

