package com.example.tripkey;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChecklistActivity extends AppCompatActivity {

    private List<ChecklistItem> checklistItems; // 체크리스트 데이터
    private ChecklistAdapter adapter;          // RecyclerView 어댑터
    private EditText newItemEditText;          // 새 항목 입력 필드

    private FirebaseFirestore db;              // Firestore 인스턴스
    private CollectionReference checklistRef;  // Firestore 컬렉션 참조
    private RecyclerView recommendedRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);

        // Firestore 초기화
        db = FirebaseFirestore.getInstance();

        // SharedPreferences에서 사용자 ID 가져오기
        String userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("userId", "");
        String travelId = getIntent().getStringExtra("travelId");
        checklistRef = db.collection("users")
                .document(userId)
                .collection("travel")
                .document(travelId)
                .collection("checklist");

        // UI 초기화
        newItemEditText = findViewById(R.id.newItemEditText);
        Button addButton = findViewById(R.id.addButton);
        Button resetButton = findViewById(R.id.resetButton);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        checklistItems = new ArrayList<>();
        adapter = new ChecklistAdapter(checklistItems, checklistRef);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 추천 준비물 RecyclerView 설정
        recommendedRecyclerView = findViewById(R.id.recommendedRecyclerView);
        loadRecommendedSupplies(); // 메서드 호출 추가

        // Firestore에서 데이터 로드
        loadChecklistItems();

        // 항목 추가 버튼 클릭 이벤트
        addButton.setOnClickListener(v -> {
            String newItemText = newItemEditText.getText().toString().trim();
            if (!newItemText.isEmpty()) {
                addChecklistItem(newItemText);
                newItemEditText.setText("");
            } else {
                Toast.makeText(ChecklistActivity.this, "항목 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });

        // 초기화 버튼 클릭 이벤트
        resetButton.setOnClickListener(v -> resetChecklist());

        // 뒤로가기 버튼 클릭 이벤트
        ImageButton backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> finish());
    }

    /**
     * Firestore에서 체크리스트 항목을 로드합니다.
     */
    private void loadChecklistItems() {
        checklistRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            checklistItems.clear();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                String id = document.getId();
                String text = document.getString("text");
                boolean isChecked = document.getBoolean("isChecked");
                checklistItems.add(new ChecklistItem(id, text, isChecked));
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> Toast.makeText(this, "데이터 로드 실패", Toast.LENGTH_SHORT).show());
    }

    /**
     * Firestore에 체크리스트 항목을 추가합니다.
     */
    private void addChecklistItem(String text) {
        Map<String, Object> item = new HashMap<>();
        item.put("text", text);
        item.put("isChecked", false);

        checklistRef.add(item).addOnSuccessListener(documentReference -> {
            String id = documentReference.getId();
            checklistItems.add(new ChecklistItem(id, text, false));
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "항목 추가 성공", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> Toast.makeText(this, "항목 추가 실패", Toast.LENGTH_SHORT).show());
    }

    /**
     * Firestore에서 모든 체크박스를 해제합니다.
     */
    private void resetChecklist() {
        for (ChecklistItem item : checklistItems) {
            item.setChecked(false);
            checklistRef.document(item.getId()).update("isChecked", false);
        }
        adapter.notifyDataSetChanged();
    }

    // ChecklistActivity 클래스 내부
    private void loadRecommendedSupplies() {
        String userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("userId", "");
        String travelId = getIntent().getStringExtra("travelId");

        // GPT 계획 참조
        CollectionReference gptPlanRef = db.collection("users")
                .document(userId)
                .collection("travel")
                .document(travelId)
                .collection("gpt_plan");

        gptPlanRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            Set<String> uniqueSupplies = new HashSet<>(); // 중복 제거용 Set

            for (QueryDocumentSnapshot dateDoc : queryDocumentSnapshots) {
                // 각 날짜의 places 컬렉션 가져오기
                CollectionReference placesRef = dateDoc.getReference().collection("places");
                placesRef.get().addOnSuccessListener(placeSnapshots -> {
                    for (QueryDocumentSnapshot placeDoc : placeSnapshots) {
                        // supply 배열 필드 추출
                        // 수정된 부분: 타입 체크 후 처리
                        Object supplyObj = placeDoc.get("supply");
                        List<String> supplies = new ArrayList<>();

                        if (supplyObj instanceof List) {
                            supplies.addAll((List<String>) supplyObj);
                        } else if (supplyObj instanceof String) {
                            supplies.add((String) supplyObj);
                        }

                        if (!supplies.isEmpty()) {
                            uniqueSupplies.addAll(supplies);
                        }
                    }
                    // UI에 추천 준비물 표시
                    showRecommendedSupplies(new ArrayList<>(uniqueSupplies));
                });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "추천 준비물 로드 실패", Toast.LENGTH_SHORT).show();
        });
    }

    private void showRecommendedSupplies(List<String> supplies) {
        SupplyAdapter adapter = new SupplyAdapter(supplies, supply -> {
            // "+" 버튼 클릭 시 체크리스트에 추가
            addChecklistItem(supply);
            Toast.makeText(this, supply + " 추가됨", Toast.LENGTH_SHORT).show();
        });
        recommendedRecyclerView.setAdapter(adapter);
    }
}
