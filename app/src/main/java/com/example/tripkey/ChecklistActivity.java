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
import java.util.List;
import java.util.Map;

public class ChecklistActivity extends AppCompatActivity {

    private List<ChecklistItem> checklistItems; // 체크리스트 데이터
    private ChecklistAdapter adapter;          // RecyclerView 어댑터
    private EditText newItemEditText;          // 새 항목 입력 필드

    private FirebaseFirestore db;              // Firestore 인스턴스
    private CollectionReference checklistRef;  // Firestore 컬렉션 참조

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);

        // Firestore 초기화
        db = FirebaseFirestore.getInstance();

        // SharedPreferences에서 사용자 ID 가져오기
        String userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("userId", "");
        checklistRef = db.collection("users").document(userId).collection("checklist");

        // UI 초기화
        newItemEditText = findViewById(R.id.newItemEditText);
        Button addButton = findViewById(R.id.addButton);
        ImageButton resetButton = findViewById(R.id.resetButton);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        checklistItems = new ArrayList<>();
        adapter = new ChecklistAdapter(checklistItems, checklistRef);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

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
}
