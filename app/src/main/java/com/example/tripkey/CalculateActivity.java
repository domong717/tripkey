package com.example.tripkey;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
    private TextView textTravelDate;
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

        textTravelDate = findViewById(R.id.travel_date);
        textTotalMoney = findViewById(R.id.total_money);

        recyclerView = findViewById(R.id.receiptRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EachMoneyAdapter(this, userExpenseList);
        recyclerView.setAdapter(adapter);

        loadData();
    }

    private void loadData() {
        if (userId == null || travelId == null) return;

        // teamId 얻기
        db.collection("users")
                .document(userId)
                .collection("travel")
                .document(travelId)
                .get()
                .addOnSuccessListener(travelDoc -> {
                    if (travelDoc.exists()) {
                        String teamId = travelDoc.getString("teamId");

                        // total 표시
                        Long totalMoney = travelDoc.getLong("total");
                        if (totalMoney != null) {
                            textTotalMoney.setText(totalMoney + "");
                        }

                        // 날짜 범위 표시
                        String startDate = travelDoc.getString("startDate");
                        String endDate = travelDoc.getString("endDate");
                        if (startDate != null && endDate != null) {
                            textTravelDate.setText(startDate + " ~ " + endDate);
                        }

                        if (teamId != null) {
                            db.collection("users")
                                    .document(userId)
                                    .collection("teams")
                                    .document(teamId)
                                    .get()
                                    .addOnSuccessListener(teamDoc -> {
                                        if (teamDoc.exists()) {
                                            List<String> members = (List<String>) teamDoc.get("members");

                                            if (members != null && !members.isEmpty()) {
                                                fetchTeamExpenses(members);
                                            }
                                        }
                                    });
                        }
                    }

                });
    }

    private void fetchTeamExpenses(List<String> memberIds) {
        final int memberCount = memberIds.size();
        final Map<String, Integer> userTotalMap = new HashMap<>();
        userExpenseList.clear();

        final int[] loadedMemberCount = {0};

        for (String memberId : memberIds) {
            db.collection("users")
                    .document(memberId)
                    .collection("travel")
                    .document(travelId)
                    .collection("expenses")
                    .get()
                    .addOnSuccessListener(dateDocs -> {
                        if (dateDocs.isEmpty()) {
                            loadedMemberCount[0]++;
                            if (loadedMemberCount[0] == memberCount) updateUI(userTotalMap, memberCount);
                            return;
                        }

                        final int[] loadedDateCount = {0};
                        final int totalDates = dateDocs.size();
                        final int[] userTotal = {0};

                        for (var dateDoc : dateDocs.getDocuments()) {
                            String date = dateDoc.getId();

                            db.collection("users")
                                    .document(memberId)
                                    .collection("travel")
                                    .document(travelId)
                                    .collection("expenses")
                                    .document(date)
                                    .collection("items")
                                    .get()
                                    .addOnSuccessListener(itemDocs -> {
                                        for (var doc : itemDocs.getDocuments()) {
                                            Long amount = doc.getLong("amount");
                                            String postedUserId = doc.getString("userId");

                                            // items 내부의 userId가 현재 memberId와 같을 때만 누적
                                            if (amount != null && postedUserId != null && postedUserId.equals(memberId)) {
                                                userTotal[0] += amount.intValue();
                                            }
                                        }

                                        loadedDateCount[0]++;
                                        if (loadedDateCount[0] == totalDates) {
                                            userTotalMap.put(memberId, userTotal[0]);
                                            Log.d(userTotalMap.toString(), "userTotalMap: " + userTotalMap);

                                            loadedMemberCount[0]++;
                                            if (loadedMemberCount[0] == memberCount) {
                                                updateUI(userTotalMap, memberCount);
                                            }
                                        }
                                    });
                        }
                    });
        }
    }

    private void updateUI(Map<String, Integer> userTotalMap, int memberCount) {
        // userId 리스트를 추출하고, profile image 포함해서 불러오기
        List<String> userIds = new ArrayList<>(userTotalMap.keySet());
        fetchUserProfiles(userIds, userTotalMap, memberCount);
    }


    private void fetchUserProfiles(List<String> userIds, Map<String, Integer> userTotalMap, int memberCount) {
        userExpenseList.clear();
        final int[] loadedCount = {0};

        for (String uid : userIds) {
            db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(doc -> {
                        String profileUrl = doc.getString("profileImage");
                        int dividedAmount = userTotalMap.get(uid) / memberCount;

                        userExpenseList.add(new UserExpense(uid, dividedAmount, profileUrl));
                        loadedCount[0]++;

                        if (loadedCount[0] == userIds.size()) {
                            adapter.notifyDataSetChanged();
                        }
                    });
        }
    }

}