package com.example.tripkey;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChecklistActivity extends AppCompatActivity {

    private List<ChecklistItem> checklistItems; // 체크리스트 데이터
    private ChecklistAdapter adapter;          // RecyclerView 어댑터
    private EditText newItemEditText; // 새 항목 입력 필드
    private SharedPreferences sharedPreferences; // SharedPreferences


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist); // XML 레이아웃 연결

        // SharedPreferences 초기화
        sharedPreferences = getSharedPreferences("ChecklistPrefs", MODE_PRIVATE);
        // 데이터 초기화 및 로드
        checklistItems = new ArrayList<>();
        loadChecklistItems(); // SharedPreferences에서 데이터 로드



        // RecyclerView 설정
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChecklistAdapter(checklistItems);
        recyclerView.setAdapter(adapter);

        // 추가 버튼 클릭 이벤트
        newItemEditText = findViewById(R.id.newItemEditText);
        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newItemText = newItemEditText.getText().toString().trim();
                if (!newItemText.isEmpty()) {
                    // 새 항목 생성 및 추가
                    ChecklistItem newItem = new ChecklistItem(newItemText, false);
                    checklistItems.add(newItem);

                    // RecyclerView 갱신
                    adapter.notifyDataSetChanged();

                    // SharedPreferences에 저장
                    saveChecklistItem(newItem);

                    // 입력 필드 초기화
                    newItemEditText.setText("");
                } else {
                    // 입력 필드가 비어 있을 경우 Toast 메시지 표시
                    Toast.makeText(ChecklistActivity.this, "항목 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 초기화 버튼 클릭 이벤트
        ImageButton resetButton = findViewById(R.id.resetButton);
        resetButton.setOnClickListener(v -> resetChecklist());

        // 뒤로가기 버튼 클릭 이벤트
        ImageButton backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> finish());
    }

    /**
     * 체크리스트를 초기화(모든 체크박스 해제)합니다.
     */
    private void resetChecklist() {
        for (ChecklistItem item : checklistItems) {
            item.setChecked(false); // 모든 항목의 체크 상태를 해제
            saveChecklistItem(item); // SharedPreferences에 업데이트
        }
        adapter.notifyDataSetChanged(); // RecyclerView 갱신
    }


    /**
     * 체크리스트 항목을 SharedPreferences에 저장합니다.
     */
    public void saveChecklistItem(ChecklistItem item) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(item.getText(), item.isChecked());
        editor.apply();
    }
    /**
     * SharedPreferences에서 체크리스트 항목을 로드합니다.
     */
    private void loadChecklistItems() {
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getValue() instanceof Boolean) {
                String key = entry.getKey();
                boolean value = (boolean) entry.getValue();
                checklistItems.add(new ChecklistItem(key, value));
            }
        }
    }

}
