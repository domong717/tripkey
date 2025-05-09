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

// ... 패키지, import 동일

public class RegisterMoneyActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_register_money);

        recyclerView = findViewById(R.id.recyclerView);
        textTotal = findViewById(R.id.text_total);
        ImageButton buttonAdd = findViewById(R.id.button_add);
        Button buttonSettleAll = findViewById(R.id.button_settle_all);

        adapter = new DateGroupAdapter(this, dateGroups);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Firestore
        db = FirebaseFirestore.getInstance();

        // userId 가져오기
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);

        // travelId 가져오기
        travelId = getIntent().getStringExtra("travelId");

        loadExpensesFromFirestore();

        // "추가하기" 버튼 클릭 시 날짜 선택 다이얼로그 띄우기
        buttonAdd.setOnClickListener(v -> showDateSelectionDialog());

        // 전체 정산 화면 이동
        buttonSettleAll.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterMoneyActivity.this, CalculateActivity.class);
            intent.putExtra("travelId", travelId); // travelId 전달
            startActivity(intent);
        });

    }

    private void loadExpensesFromFirestore() {
        if (userId == null || travelId == null) return;

        // 🔹 총 사용금액 불러오기
        db.collection("users")
                .document(userId)
                .collection("travel")
                .document(travelId)
                .get()
                .addOnSuccessListener(travelDoc -> {
                    if (travelDoc.exists()) {
                        Long total = travelDoc.getLong("total");
                        totalMoney = (total != null) ? total.intValue() : 0;
                        textTotal.setText("총 사용금액 : " + totalMoney + "원");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "총합 불러오기 실패", Toast.LENGTH_SHORT).show();
                });

        // 🔹 날짜별 지출 불러오기
        db.collection("users")
                .document(userId)
                .collection("travel")
                .document(travelId)
                .collection("expenses")
                .get()
                .addOnSuccessListener(dateDocs -> {
                    dateGroups.clear(); // 기존 리스트 초기화

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
                                    for (var doc : itemDocs) {
                                        String desc = doc.getString("description");
                                        Long amt = doc.getLong("amount");
                                        if (desc != null && amt != null) {
                                            expenses.add(new Expense(desc, amt.intValue()));
                                        }
                                    }

                                    if (!expenses.isEmpty()) {
                                        dateGroups.add(new DateGroup(date, expenses));
                                        adapter.notifyDataSetChanged(); // 각 날짜별 추가 후 갱신
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "지출 항목 불러오기 실패", Toast.LENGTH_SHORT).show();
                });
    }


    private void showDateSelectionDialog() {
        db.collection("users")
                .document(userId)
                .collection("travel")
                .document(travelId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String startDateStr = document.getString("startDate");
                        String endDateStr = document.getString("endDate");

                        if (startDateStr != null && endDateStr != null) {
                            List<String> dateList = getDateRangeList(startDateStr, endDateStr);

                            new AlertDialog.Builder(this)
                                    .setTitle("지출 날짜 선택")
                                    .setItems(dateList.toArray(new String[0]), (dialog, which) -> {
                                        String selectedDate = dateList.get(which);
                                        showAddExpenseDialog(selectedDate);  // 바로 추가 다이얼로그 띄움
                                    })
                                    .show();
                        } else {
                            Toast.makeText(this, "날짜 정보가 없습니다", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "날짜를 불러오지 못했습니다", Toast.LENGTH_SHORT).show();
                });
    }

    private List<String> getDateRangeList(String start, String end) {
        List<String> dates = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            Date startDate = sdf.parse(start);
            Date endDate = sdf.parse(end);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);

            while (!calendar.getTime().after(endDate)) {
                dates.add(sdf.format(calendar.getTime()));
                calendar.add(Calendar.DATE, 1);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dates;
    }

    private void showAddExpenseDialog(String selectedDate) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_expense, null);
        EditText editAmount = dialogView.findViewById(R.id.edit_amount);
        EditText editDescription = dialogView.findViewById(R.id.edit_desc);

        new AlertDialog.Builder(this)
                .setTitle("[" + selectedDate + "] 지출 추가")
                .setView(dialogView)
                .setPositiveButton("추가", (dialog, which) -> {
                    String amountStr = editAmount.getText().toString().trim();
                    String description = editDescription.getText().toString().trim();

                    if (amountStr.isEmpty() || description.isEmpty()) {
                        Toast.makeText(this, "모든 항목을 입력하세요", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int amount = Integer.parseInt(amountStr);
                    Expense expense = new Expense(description, amount);
                    addExpenseToDateGroup(selectedDate, expense);
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void addExpenseToDateGroup(String date, Expense newExpense) {
        boolean dateFound = false;

        for (DateGroup group : dateGroups) {
            if (group.getDate().equals(date)) {
                group.getExpenses().add(newExpense);
                dateFound = true;
                break;
            }
        }

        if (!dateFound) {
            List<Expense> newList = new ArrayList<>();
            newList.add(newExpense);
            dateGroups.add(new DateGroup(date, newList));
        }

        totalMoney += newExpense.getAmount();
        textTotal.setText("총 사용금액 : " + totalMoney + "원");
        adapter.notifyDataSetChanged();

        saveExpenseToAllTeamMembers(date, newExpense);
    }

    private void saveExpenseToAllTeamMembers(String date, Expense expense) {
        db.collection("users")
                .document(userId)
                .collection("travel")
                .document(travelId)
                .get()
                .addOnSuccessListener(travelDoc -> {
                    if (travelDoc.exists()) {
                        String teamId = travelDoc.getString("teamId");

                        if (teamId != null) {
                            db.collection("users")
                                    .document(userId)
                                    .collection("teams")
                                    .document(teamId)
                                    .get()
                                    .addOnSuccessListener(teamDoc -> {
                                        if (teamDoc.exists()) {
                                            List<String> members = (List<String>) teamDoc.get("members");

                                            if (members != null) {
                                                for (String memberId : members) {
                                                    // 날짜 문서 생성
                                                    db.collection("users")
                                                            .document(memberId)
                                                            .collection("travel")
                                                            .document(travelId)
                                                            .collection("expenses")
                                                            .document(date)
                                                            .set(Collections.singletonMap("exists", true), SetOptions.merge());

                                                    // 지출 항목 추가
                                                    db.collection("users")
                                                            .document(memberId)
                                                            .collection("travel")
                                                            .document(travelId)
                                                            .collection("expenses")
                                                            .document(date)
                                                            .collection("items")
                                                            .add(expense.toMap(memberId));
                                                }
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


}

